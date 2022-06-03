# baseline-profiles

For recording baseline profiles for Android, see:

https://developer.android.com/topic/performance/baselineprofiles

Initial results on this sample Java app on a Pixel 4a device:

```log
ExampleStartupBenchmark_startupNoCompilation
timeToInitialDisplayMs   min 251.7,   median 270.0,   max 320.1
ExampleStartupBenchmark_startupBaselineProfile
timeToInitialDisplayMs   min 235.0,   median 239.2,   max 257.9
```

To try this sample exampl, you need at least `Android Studio Dolphin | 2021.3.1 Beta 1.`

## .NET MAUI Apps

These are the steps to try this in a .NET MAUI app.

1. `dotnet new maui`, and run the app on an Android Emulator of your
   choice.

2. Run `adb root`.

3. Run `BaselineProfileGenerator` in Android Studio Dolphin+:

```java
@ExperimentalBaselineProfilesApi
@RunWith(AndroidJUnit4.class)
public class BaselineProfileGenerator {
    @Rule
    public BaselineProfileRule baselineProfileRule = new BaselineProfileRule();
    
    @Test
    public void startup() {
        baselineProfileRule.collectBaselineProfile("com.microsoft.hellomaui", new Function1<MacrobenchmarkScope, Unit>() {
            @Override
            public Unit invoke(MacrobenchmarkScope macrobenchmarkScope) {
                macrobenchmarkScope.pressHome();
                macrobenchmarkScope.startActivityAndWait();
                return null;
            }
        });
    }
}
```

4. This test will print out a log message that tells you to do:

```bash
adb pull "/sdcard/Android/media/com.microsoft.hellomaui/additional_test_output/BaselineProfileGenerator_startup-baseline-prof-2022-06-03-19-46-28.txt" .
```

5. Rename the file to `baseline-prof.txt`, and put it in your project.
   Create an `Assets\dexopt` directory as well.

6. Build `profgen-cli`, see:

https://github.com/jonathanpeppers/xamarin-android/tree/baseline-profiles

7. Run `profgen-cli` to get a binary version of `baseline-prof.txt`:

```bash
java -jar .\bin\Debug\lib\xamarin.android\xbuild\Xamarin\Android\profgen-cli.jar bin C:\src\foo\baseline-prof.txt -o C:\src\hellomaui\Platforms\Android\Assets\dexopt\baseline.prof -om C:\src\hellomaui\Platforms\Android\Assets\dexopt\baseline.profm -a C:\src\hellomaui\bin\Release\net6.0-android\android-arm64\com.microsoft.hellomaui-Signed.apk
```

8. Test the profile works. You'll need to add changes to the app to
   use the MacroBenchmark library:

Download `profileinstaller-1.2.0-beta03.aar` and drop it in your project:

https://mvnrepository.com/artifact/androidx.profileinstaller/profileinstaller/1.2.0-beta03

Edit your `.csproj`:
```xml
<ItemGroup>
  <PackageReference Include="Xamarin.AndroidX.Startup.StartupRuntime" Version="1.1.1.1" />
  <AndroidLibrary Include="*.aar" Bind="false" />
</ItemGroup>
```

Edit your `AndroidManifest.xml`:
```xml
<application ...>
    <profileable android:shell="true"/>
</application>
```

9. Run `ExampleStartupBenchmark` in Android Studio Dolphin+:

```java
@RunWith(AndroidJUnit4.class)
public class ExampleStartupBenchmark {
    @Rule
    public MacrobenchmarkRule mBenchmarkRule = new MacrobenchmarkRule();
    
    @Test
    public void startupNoCompilation() {
        startup(new CompilationMode.None());
    }
    
    @Test
    public void startupBaselineProfile() {
        startup(new CompilationMode.Partial(BaselineProfileMode.Require));
    }
    
    private void startup(CompilationMode compilationMode) {
        mBenchmarkRule.measureRepeated(
                "com.microsoft.hellomaui",
                Collections.singletonList(new StartupTimingMetric()),
                compilationMode,
                StartupMode.COLD,
                10,
                scope -> {
                    scope.pressHome();
                    scope.startActivityAndWait();
                    return null;
                });
    }
}
```

10. Even after all of this, I'm unsure how much this helps startup in
    a .NET MAUI application:

```log
ExampleStartupBenchmark_startupNoCompilation
timeToInitialDisplayMs   min 774.7,   median 779.4,   max 796.9
Traces: Iteration 0 1 2 3 4 5 6 7 8 9
ExampleStartupBenchmark_startupBaselineProfile
timeToInitialDisplayMs   min 767.4,   median 791.6,   max 828.9
Traces: Iteration 0 1 2 3 4 5 6 7 8 9
```

package com.microsoft.benchmark;

import androidx.benchmark.macro.ExperimentalBaselineProfilesApi;
import androidx.benchmark.macro.MacrobenchmarkScope;
import androidx.benchmark.macro.junit4.BaselineProfileRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@ExperimentalBaselineProfilesApi
@RunWith(AndroidJUnit4.class)
public class BaselineProfileGenerator {
	@Rule
	public BaselineProfileRule baselineProfileRule = new BaselineProfileRule();

	@Test
	public void startup() {
		baselineProfileRule.collectBaselineProfile("com.microsoft.baselineprofiles", new Function1<MacrobenchmarkScope, Unit>() {
			@Override
			public Unit invoke(MacrobenchmarkScope macrobenchmarkScope) {
				macrobenchmarkScope.pressHome();
				macrobenchmarkScope.startActivityAndWait();
				return null;
			}
		});
	}
}

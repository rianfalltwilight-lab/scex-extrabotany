package io.github.lounode.extrabotany.common.telemetry.charts;

import com.google.gson.JsonObject;

import java.util.concurrent.Callable;

public class SingleLineChart extends Chart {

	private final Callable<Integer> callable;

	public SingleLineChart(String chartId, Callable<Integer> callable) {
		super(chartId);
		this.callable = callable;
	}

	@Override
	protected JsonObject getChartData() throws Exception {
		JsonObject data = new JsonObject();
		int value = callable.call();
		if (value == 0) {
			return null;
		}

		data.addProperty("value", value);
		return data;
	}
}

package io.github.lounode.extrabotany.common.telemetry.charts;

import com.google.gson.JsonObject;

import java.util.concurrent.Callable;

public class SimplePie extends Chart {

	private final Callable<String> callable;

	public SimplePie(String chartId, Callable<String> callable) {
		super(chartId);
		this.callable = callable;
	}

	@Override
	protected JsonObject getChartData() throws Exception {
		String value = callable.call();
		if (value == null || value.isEmpty()) {
			return null;
		}
		JsonObject data = new JsonObject();
		data.addProperty("value", value);

		return data;
	}
}

package io.github.lounode.extrabotany.common.telemetry.charts;

import com.google.gson.JsonObject;

import java.util.Map;
import java.util.concurrent.Callable;

public class DrilldownPie extends Chart {

	private final Callable<Map<String, Map<String, Integer>>> callable;

	public DrilldownPie(String chartId, Callable<Map<String, Map<String, Integer>>> callable) {
		super(chartId);
		this.callable = callable;
	}

	@Override
	protected JsonObject getChartData() throws Exception {
		Map<String, Map<String, Integer>> map = callable.call();
		if (map == null || map.isEmpty()) {
			return null;
		}

		JsonObject data = new JsonObject();
		JsonObject values = new JsonObject();

		boolean reallyAllSkipped = true;

		for (Map.Entry<String, Map<String, Integer>> entryValues : map.entrySet()) {
			JsonObject value = new JsonObject();
			boolean allSkipped = true;
			for (Map.Entry<String, Integer> valueEntry : map.get(entryValues.getKey()).entrySet()) {
				value.addProperty(valueEntry.getKey(), valueEntry.getValue());
				allSkipped = false;
			}
			if (!allSkipped) {
				reallyAllSkipped = false;
				values.add(entryValues.getKey(), value);
			}
		}

		if (reallyAllSkipped) {
			return null;
		}

		data.add("values", values);
		return data;
	}
}

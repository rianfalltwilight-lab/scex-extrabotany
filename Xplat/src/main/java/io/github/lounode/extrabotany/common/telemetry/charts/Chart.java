package io.github.lounode.extrabotany.common.telemetry.charts;

import com.google.gson.JsonObject;

public abstract class Chart {
	private final String chartId;

	protected Chart(String chartId) {
		this.chartId = chartId;
	}

	public JsonObject getRequestJsonObject() {
		JsonObject chart = new JsonObject();
		chart.addProperty("chartId", this.chartId);

		try {
			JsonObject data = getChartData();
			if (data == null) {
				return null;
			}
			chart.add("data", data);

		} catch (Throwable t) {
			return null;
		}

		return chart;
	}

	protected abstract JsonObject getChartData() throws Exception;
}

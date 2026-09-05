package io.github.lounode.extrabotany.common.telemetry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.server.MinecraftServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.lounode.extrabotany.common.telemetry.charts.Chart;

import javax.net.ssl.HttpsURLConnection;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPOutputStream;

public class Metrics {
	private static final Logger LOGGER = LoggerFactory.getLogger("Metrics");
	public static final String METRICS_VERSION = "3.1.1-SNAPSHOT";
	private static final String REPORT_URL = "https://bStats.org/api/v2/data/%s";

	private final String platform;
	private final String serverUuid;
	private final int serviceId;
	private final JsonObject platformData;
	private final JsonObject appendServiceDataConsumer;

	private final Set<Chart> customCharts = new HashSet<>();
	private final MinecraftServer server;

	public Metrics(
			String platform,
			String serverUuid,
			int serviceId,
			JsonObject appendPlatformDataConsumer,
			JsonObject appendServiceDataConsumer, MinecraftServer server) {
		this.platform = platform;
		this.serverUuid = serverUuid;
		this.serviceId = serviceId;
		this.platformData = appendPlatformDataConsumer;
		this.appendServiceDataConsumer = appendServiceDataConsumer;
		this.server = server;

		startSubmitting();
	}

	public void addCustomChart(Chart chart) {
		this.customCharts.add(chart);
	}

	private void startSubmitting() {
		final Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				submitData();
			}
		}, 1000 * 60 * 5, 1000 * 60 * 30);
	}

	private void submitData() {
		final JsonObject baseJsonBuilder = platformData.deepCopy();
		final JsonObject serviceJsonBuilder = appendServiceDataConsumer.deepCopy();

		JsonArray chartData = new JsonArray();
		for (var chart : customCharts) {
			JsonObject chartJson = chart.getRequestJsonObject();
			if (chartJson == null) {
				continue;
			}
			chartData.add(chart.getRequestJsonObject());
		}

		serviceJsonBuilder.addProperty("id", serviceId);
		serviceJsonBuilder.add("customCharts", chartData);

		baseJsonBuilder.add("service", serviceJsonBuilder);
		baseJsonBuilder.addProperty("serverUUID", serverUuid);
		baseJsonBuilder.addProperty("metricsVersion", METRICS_VERSION);
		baseJsonBuilder.addProperty("playerAmount", server.getPlayerCount());

		JsonObject data = baseJsonBuilder;

		try {
			sendData(data);
		} catch (Exception e) {
			if (debug()) {
				LOGGER.error("Could not submit bStats metrics data", e);
			}
		}
	}

	private void sendData(JsonObject data) throws Exception {
		if (debug()) {
			LOGGER.info("Sent bStats metrics data: {}", data.toString());
		}

		String url = String.format(REPORT_URL, platform);
		HttpsURLConnection connection = (HttpsURLConnection) URI.create(url).toURL().openConnection();

		byte[] compressedData = compress(data.toString());

		connection.setRequestMethod("POST");
		connection.addRequestProperty("Accept", "application/json");
		connection.addRequestProperty("Connection", "close");
		connection.addRequestProperty("Content-Encoding", "gzip");
		connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("User-Agent", "Metrics-Service/1");

		connection.setDoOutput(true);
		try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
			outputStream.write(compressedData);
		}

		StringBuilder builder = new StringBuilder();
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				builder.append(line);
			}
		}

		if (debug()) {
			LOGGER.info("Sent data to bStats and received response: {}", builder);
		}
	}

	private static byte[] compress(final String str) throws IOException {
		if (str == null) {
			return null;
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
			gzip.write(str.getBytes(StandardCharsets.UTF_8));
		}
		return outputStream.toByteArray();
	}

	private static boolean debug() {
		return System.getProperty("metrics_debug") != null;
	}
}

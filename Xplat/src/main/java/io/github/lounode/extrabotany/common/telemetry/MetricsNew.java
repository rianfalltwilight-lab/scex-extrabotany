package io.github.lounode.extrabotany.common.telemetry;

import com.google.gson.JsonObject;

import net.minecraft.server.MinecraftServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;

public class MetricsNew {
	protected static final Logger LOGGER = LoggerFactory.getLogger("Metrics");

	protected static final String LOCAL_URL = "http://localhost:8080/api/data";
	protected static final String UPLOAD_URL = "https://lounode.top/api/data";

	protected final TelemetryPropertyMap deviceSessionProperties;
	protected final UUID serverUUID;
	private MinecraftServer server;
	private static final ExecutorService METRICS_EXECUTOR = Executors.newCachedThreadPool(r -> {
		Thread t = new Thread(r, "Metrics-Sender");
		t.setDaemon(true);
		return t;
	});

	private final Timer timer;

	protected static final long INITIAL_COOLDOWN = 1000 * 60 * 5;
	protected static final long DEFAULT_COOLDOWN = 1000 * 60 * 30;
	protected boolean enable;

	public MetricsNew(TelemetryPropertyMap deviceSessionProperties, UUID serverUUID, MinecraftServer server) {
		this.deviceSessionProperties = deviceSessionProperties;
		this.serverUUID = serverUUID;
		this.server = server;

		this.timer = new Timer(true);
	}

	public void start() {
		if (enable) {
			return;
		}
		enable = true;

		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				submitData();
			}
		}, getInitialCooldown(), getDefaultCooldown());
	}

	public void shutdown() {
		enable = false;
		timer.cancel();
	}

	private void submitData() {
		if (!enable) {
			return;
		}
		var data = TelemetryPropertyMap.builder().putAll(deviceSessionProperties);
		appendServerData(data, getServer());

		METRICS_EXECUTOR.execute(() -> {
			try {
				this.request(data.build());
			} catch (Exception e) {
				if (debug()) {
					LOGGER.error("Could not submit metrics data", e);
				}
			}
		});
	}

	private void request(TelemetryPropertyMap map) throws Exception {
		JsonObject json = map.toJson();
		if (debug()) {
			LOGGER.info("Send api with data {}", json);
		}

		String url = getUploadUrl();

		HttpURLConnection connection;

		if (debug()) {
			connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
		} else {
			connection = (HttpsURLConnection) URI.create(url).toURL().openConnection();
		}

		byte[] compressedData = compress(json.toString());

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
			LOGGER.info("Sent data to api and received response: {}", builder);
		}

	}

	private void appendServerData(TelemetryPropertyMap.Builder builder, MinecraftServer server) {
		builder.put(TelemetryProperty.ONLINE_MODE, server.usesAuthentication());
		builder.put(TelemetryProperty.PLAYER_AMOUNT, server.getPlayerCount());
		builder.put(TelemetryProperty.SERVER_TYPE, server.isDedicatedServer() ? TelemetryProperty.ServerType.OTHER : TelemetryProperty.ServerType.LOCAL);
		builder.put(TelemetryProperty.SERVER_UUID, serverUUID);
	}

	protected long getInitialCooldown() {
		return debug() ? (1000 * 10) : INITIAL_COOLDOWN;
	}

	protected long getDefaultCooldown() {
		return DEFAULT_COOLDOWN;
	}

	protected MinecraftServer getServer() {
		return server;
	}

	protected String getUploadUrl() {
		return debug() ? LOCAL_URL : UPLOAD_URL;
	}

	public static UUID getServerUUID(MinecraftServer server, UUID uniqueID) {
		long seed = server.getWorldData().worldGenOptions().seed();

		String combinedInput = seed + "|" + uniqueID;

		return UUID.nameUUIDFromBytes(combinedInput.getBytes(StandardCharsets.UTF_8));
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

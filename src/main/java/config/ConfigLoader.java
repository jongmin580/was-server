package config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;


public class ConfigLoader {
	private static int port;
	private static final Map<String, Map<String, String>> hostConfig = new HashMap<>();
	private static final String BASE_DIRECTORY = Paths.get(System.getProperty("user.dir"), "").toString();

	public static void loadConfig() throws IOException {
		InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream("config.json");

		if (inputStream == null) {
			throw new IOException("config.json File not Found");
		}

		try (Reader reader = new InputStreamReader(inputStream, "UTF-8");
			JsonReader jsonReader = new JsonReader(reader)) {

			JsonObject rootNode = JsonParser.parseReader(jsonReader).getAsJsonObject();
			port = rootNode.get("port").getAsInt();

			JsonObject hostsNode = rootNode.getAsJsonObject("hosts");
			if (hostsNode != null) {
				for (Map.Entry<String, JsonElement> entry : hostsNode.entrySet()) {
					String host = entry.getKey();
					JsonObject settings = entry.getValue().getAsJsonObject();
					Map<String, String> configMap = new HashMap<>();
					
					settings.entrySet().forEach(e -> configMap.put(e.getKey(), e.getValue().getAsString()));
					
					hostConfig.put(host, configMap);
				}
			}
		}
	}

	public static int getPort() {
		return port;
	}
	
	public static Map<String, String> getHostConfig(String host) {
		return hostConfig.getOrDefault(host, hostConfig.get("default"));
	}
	
	public static String getHttpRoot(String host) {
		String httpRoot = getHostConfig(host).get("HTTP_ROOT");

		File rootDir = new File(httpRoot);
		if (!rootDir.isAbsolute()) {
			rootDir = new File(BASE_DIRECTORY, httpRoot);
		}

		return rootDir.getAbsolutePath();
	}
	
	public static String getErrorPage(String host, String errorCode) {
		Map<String, String> config = hostConfig.getOrDefault(host, hostConfig.get("default"));
		return config.get(errorCode);
	}
}

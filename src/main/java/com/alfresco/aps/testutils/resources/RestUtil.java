package com.alfresco.aps.testutils.resources;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

import org.activiti.engine.impl.util.json.JSONObject;
import org.apache.commons.io.IOUtils;

public class RestUtil {

	public static JSONObject getAppDeployments(String restEndpoint, String user, String password) {
		return new JSONObject(call(restEndpoint, user, password));
	}

	public static void getAppResource(String restEndpoint, String user, String password, String path) {
		call(restEndpoint, user, password, path);
	}

	private static String call(String restEndpoint, String user, String password) {

		StringBuilder result = new StringBuilder();
		try {

			URL url = new URL(restEndpoint);
			URLConnection urlConnection = url.openConnection();

			String authString = user + ":" + password;
			String authStringEnc = new String(Base64.getUrlEncoder().encode(authString.getBytes()));
			urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);

			BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line);
			}
			reader.close();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return result.toString();
	}

	private static void call(String restEndpoint, String user, String password, String path) {

		try {

			URL url = new URL(restEndpoint);
			URLConnection urlConnection = url.openConnection();

			String authString = user + ":" + password;
			String authStringEnc = new String(Base64.getUrlEncoder().encode(authString.getBytes()));
			urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);

			FileOutputStream targetStream = new FileOutputStream(path, false);
			IOUtils.copy(urlConnection.getInputStream(),targetStream);
			urlConnection.getInputStream().close();
			targetStream.close();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}

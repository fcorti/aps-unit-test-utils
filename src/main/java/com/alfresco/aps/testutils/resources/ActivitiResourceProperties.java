package com.alfresco.aps.testutils.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ActivitiResourceProperties extends Properties {

	private static final long serialVersionUID = 1L;

	private static final String FILE_NAME = "activiti-resources.properties";

	public static final String ACTIVITI_BASE_URL = "activiti.baseUrl";
	public static final String ACTIVITI_REST_ENDPOINT_APP_DEFINITIONS = "activiti.endpoint.appDefinitions";
	public static final String ACTIVITI_REST_ENDPOINT_APP_EXPORT_PREFIX = "activiti.endpoint.appExport.prefix";
	public static final String ACTIVITI_REST_ENDPOINT_APP_EXPORT_POSTFIX = "activiti.endpoint.appExport.postfix";
	public static final String ACTIVITI_USER = "activiti.username";
	public static final String ACTIVITI_PASSWORD = "activiti.password";

	public final void load() {

		InputStream input = getClass().getClassLoader().getResourceAsStream(ActivitiResourceProperties.FILE_NAME);

		if (input != null) {

			try {

				load(input);

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}

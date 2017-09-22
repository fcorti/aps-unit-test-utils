package com.alfresco.aps.testutils.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.activiti.engine.impl.util.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class ActivitiResources {

	static Logger log = Logger.getLogger(ActivitiResources.class.getName());

	private static final String DATA = "data";
	private static final String NAME = "name";
	private static final String ID = "id";

	public static <T> void get(String appName, String appResource, Boolean replaceResource, Boolean resourceMustExistDeployed) throws Exception {

		log.info("Reading properties.");

		ActivitiResourceProperties activitiResourceProperties = new ActivitiResourceProperties();
		activitiResourceProperties.load();

		log.info("Retrieving all the app deployments.");

		JSONObject appDeployments = null;
		try {
			appDeployments = RestUtil.getAppDeployments(
					activitiResourceProperties.getProperty(ActivitiResourceProperties.ACTIVITI_BASE_URL) + activitiResourceProperties.getProperty(ActivitiResourceProperties.ACTIVITI_REST_ENDPOINT_APP_DEFINITIONS),
					activitiResourceProperties.getProperty(ActivitiResourceProperties.ACTIVITI_USER),
					activitiResourceProperties.getProperty(ActivitiResourceProperties.ACTIVITI_PASSWORD));
		}
		catch (Exception e) {
			if (resourceMustExistDeployed) {
				throw e;
			}
		}

		if (appDeployments != null) {

			log.info("Searching for the app deployment.");

			JSONObject appDeployment = null;
			for (int i=0; i < appDeployments.getJSONArray(ActivitiResources.DATA).length(); ++i) {
				if (appName.equals(appDeployments.getJSONArray(ActivitiResources.DATA).getJSONObject(i).getString(ActivitiResources.NAME))) {
					appDeployment = appDeployments.getJSONArray(ActivitiResources.DATA).getJSONObject(i);
				}
			}
			if (appDeployment == null) {
				if (resourceMustExistDeployed) {
					throw new RuntimeException("Cannot find the app deployment with name '" + appName + "'.");
				}
			}
			else {

				String appResourcePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "app";
				String appResourceZip = appResourcePath + File.separator + appResource + ".zip";

				log.info("Cleaning the app directory before download."); 
				
				File rootDirectory = (new File(appResourcePath));
				System.out.println(rootDirectory.isDirectory());
				FileUtils.cleanDirectory(rootDirectory); 
				
				log.info("Getting the app resources locally.");

				if ((new File(appResourceZip)).exists() && !replaceResource) {
					throw new RuntimeException("App '" + appResource + "' already exists and requested to preserve it.");
				}

				RestUtil.getAppResource(
					activitiResourceProperties.getProperty(ActivitiResourceProperties.ACTIVITI_BASE_URL) 
						+ activitiResourceProperties.getProperty(ActivitiResourceProperties.ACTIVITI_REST_ENDPOINT_APP_EXPORT_PREFIX)
						+ appDeployment.getString(ActivitiResources.ID)
						+ activitiResourceProperties.getProperty(ActivitiResourceProperties.ACTIVITI_REST_ENDPOINT_APP_EXPORT_POSTFIX),
					activitiResourceProperties.getProperty(ActivitiResourceProperties.ACTIVITI_USER),
					activitiResourceProperties.getProperty(ActivitiResourceProperties.ACTIVITI_PASSWORD),
					appResourceZip);

				log.info("Unzipping the app locally.");

				zipExtract(appName, appResourceZip, appResourcePath);
				(new File(appResourceZip)).delete();

			}
		}
	}

	public static void get(String processKey, String processResource, Boolean resourceMustExistDeployed) throws Exception {

		get(processKey, processResource, resourceMustExistDeployed, true);

	}

	public static void get(String processKey, String processResource) throws Exception {

		get(processKey, processResource, false, true);

	}

	public static void getIfAvailable(String processKey, String processResource, Boolean replaceResource) throws Exception {

		get(processKey, processResource, replaceResource, false);

	}

	public static void getIfAvailable(String processKey, String processResource) throws Exception {

		getIfAvailable(processKey, processResource, false);

	}

	public static void force(String processKey, String processResource, Boolean resourceMustExistDeployed) throws Exception {

		get(processKey, processResource, true, resourceMustExistDeployed);

	}

	public static void force(String processKey, String processResource) throws Exception {

		force(processKey, processResource, true);

	}

	public static void forceIfAvailable(String processKey, String processResource) throws Exception {

		get(processKey, processResource, true, false);

	}

	private static void zipExtract(String zipName, String zipPath, String targetFloder) {

		try {
			
			ZipFile zipFile = new ZipFile(zipPath);
			Enumeration<?> enu = zipFile.entries();
			while (enu.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) enu.nextElement();

				String name = zipEntry.getName();
				long size = zipEntry.getSize();
				long compressedSize = zipEntry.getCompressedSize();
				System.out.printf("name: %-20s | size: %6d | compressed size: %6d\n", name, size, compressedSize);

				File file = new File(targetFloder + File.separator + name);
				if (name.endsWith("/")) {
					file.mkdirs();
					continue;
				}

				File parent = file.getParentFile();
				if (parent != null) {
					parent.mkdirs();
				}

				InputStream is = zipFile.getInputStream(zipEntry);
				FileOutputStream fos = new FileOutputStream(file);
				byte[] bytes = new byte[1024];
				int length;
				while ((length = is.read(bytes)) >= 0) {
					fos.write(bytes, 0, length);
				}
				is.close();
				fos.close();

			}
			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

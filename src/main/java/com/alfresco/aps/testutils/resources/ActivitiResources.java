package com.alfresco.aps.testutils.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.activiti.engine.impl.util.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import static com.alfresco.aps.testutils.TestUtilsConstants.*;

public class ActivitiResources {

	static Logger log = Logger.getLogger(ActivitiResources.class.getName());

	private static final String DATA = "data";
	private static final String NAME = "name";
	private static final String ID = "id";

	public static <T> void get(String appName) throws Exception {

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
			
				throw e;
			
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
					throw new RuntimeException("Cannot find the app deployment with name '" + appName + "'.");
			}
			else {

				String appResourcePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "app";
				String appResourceZip = appResourcePath + File.separator + "app.zip";
				
				log.info("Getting the app resources locally.");

				if ((new File(appResourceZip)).exists()) {
					throw new RuntimeException("App zip already exists.");
				}
				
				log.info("Cleaning the app directory before download."); 
				
				File rootDirectory = (new File(appResourcePath));
				FileUtils.cleanDirectory(rootDirectory); 

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
				if ((new File(DMN_RESOURCE_PATH)).exists()){
					
					Iterator<File> it = FileUtils.iterateFiles(new File(DMN_RESOURCE_PATH), null, false);
					while (it.hasNext()) {
						String dmnModel = ((File) it.next()).getPath();
						String extension = FilenameUtils.getExtension(dmnModel);
						if (extension.equals("json")) {
							DMNConverter.convertJsonModelToDMNXml(dmnModel, StringUtils.substringBefore(dmnModel, ".json")+".dmn");
						}
					}

				}

			}
		}
	}
	
	public static void forceGet(String appName) throws Exception {

		get(appName);

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

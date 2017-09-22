package com.alfresco.aps.testutils.resources;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.activiti.engine.impl.util.json.JSONObject;
import org.apache.log4j.Logger;

public class ActivitiResources {

	static Logger log = Logger.getLogger(ActivitiResources.class.getName());

	private static final String DATA = "data";
	private static final String NAME = "name";
	private static final String ID = "id";

	public static <T> void get(String appname, String appResource, Boolean replaceResource, Boolean resourceMustExistDeployed) throws Exception {

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
				if (appname.equals(appDeployments.getJSONArray(ActivitiResources.DATA).getJSONObject(i).getString(ActivitiResources.NAME))) {
					appDeployment = appDeployments.getJSONArray(ActivitiResources.DATA).getJSONObject(i);
				}
			}
			if (appDeployment == null) {
				if (resourceMustExistDeployed) {
					throw new RuntimeException("Cannot find the app deployment with name '" + appname + "'.");
				}
			}
			else {

				String appResourcePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "app";
				String appResourceZip = appResourcePath + File.separator + appResource + ".zip";

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

				//zipExtract(appResource, zipPath, targetFloder);

				/*
				ZipInputStream zipIn = new ZipInputStream(new FileInputStream(appResourceZip));
		        ZipEntry entry = zipIn.getNextEntry();
		        // iterates over entries in the zip file
		        while (entry != null) {
		            String filePath = appResourcePath + File.separator + entry.getName();
		            if (!entry.isDirectory()) {
		                // if the entry is a file, extracts it
		                extractFile(zipIn, filePath);
		            } else {
		                // if the entry is a directory, make the directory
		                File dir = new File(filePath);
		                dir.mkdir();
		            }
		            zipIn.closeEntry();
		            entry = zipIn.getNextEntry();
		        }
		        zipIn.close(); */

		        /*
				String processBpmn = RestUtil.getProcessBpmn(
					processResourceOnActiviti.getString(ActivitiResources.CONTENT_URL),
					activitiResourceProperties.getProperty(ActivitiResourceProperties.ACTIVITI_USER),
					activitiResourceProperties.getProperty(ActivitiResourceProperties.ACTIVITI_PASSWORD));

				log.info("Writing the BPMN into the file.");

				String processResourcePath = System.getProperty("user.dir") + "/src/test/resources/" + processResource;


				try {
					FileWriter fWriter = new FileWriter(processResourcePath);
					BufferedWriter bufWriter = new BufferedWriter(fWriter);
					bufWriter.write(processBpmn);
					bufWriter.close();
					fWriter.close();
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
				*/
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
		
	}
}

package com.alfresco.aps.test.process;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.ActivitiRule;
import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;

import com.alfresco.aps.testutils.resources.ActivitiResources;

public class Example1Test {

	static Logger log = Logger.getLogger(ActivitiResources.class.getName());

	@Rule
	public ActivitiRule activitiRule = new ActivitiRule();

	@Test
	public void test() throws Exception {

		// Process info.
		String appName = "Test App";
		String processDefinitionKey = "TestProcess";
		String appResourcePath = "app";

		/**
		 * The only difference from a standard JUnit is here!
		 */
		// Get process from Activiti.
		ActivitiResources.force(appName, appResourcePath);

		// Deploying the process.
		activitiRule.getRepositoryService().createDeployment().name(appName).addClasspathResource(appResourcePath).deploy();

		log.info("Process deployed.");

		// Defining the mandatory variables to start the process.
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("name", "Francesco");

		log.info("Starting a new process instance.");

		// Starting the a new process instance.
		ProcessInstance processInstance = activitiRule.getRuntimeService().startProcessInstanceByKey(processDefinitionKey, variables);
		assertNotNull(processInstance);

		log.info("Process instance started with id=" + processInstance.getId() + ".");

	}

}
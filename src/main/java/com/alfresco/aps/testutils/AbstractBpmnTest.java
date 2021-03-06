package com.alfresco.aps.testutils;

import static com.alfresco.aps.testutils.TestUtilsConstants.BPMN_RESOURCE_PATH;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.mail.NoSuchProviderException;
import javax.mail.Provider;
import javax.mail.Session;
import javax.mail.Provider.Type;
import javax.naming.NamingException;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import com.alfresco.aps.mockdelegates.AlfrescoPublishDelegate;
import com.alfresco.aps.mockdelegates.ExecuteDecisionBean;
import com.alfresco.aps.mockdelegates.RestCallMockClass;
import com.alfresco.aps.testutils.resources.ActivitiResources;

public abstract class AbstractBpmnTest {

	@Autowired
	protected ApplicationContext appContext;

	@Autowired
	protected Environment env;

	@Autowired
	protected ActivitiRule activitiRule;

	@Autowired
	protected UnitTestHelpers unitTestHelpers;

	@Autowired
	protected HistoryService historyService;

	@Autowired
	protected RuntimeService runtimeService;

	@Autowired
	protected RepositoryService repositoryService;

	@Autowired
	protected TaskService taskService;

	@Autowired
	protected ManagementService managementService;

	@Autowired
	protected SpringProcessEngineConfiguration processEngineConfiguration;

	protected static List<EmailType> actualEmails = new ArrayList<EmailType>();

	@Autowired
	protected RestCallMockClass activiti_restCallDelegate;

	@Autowired
	protected ExecuteDecisionBean activiti_executeDecisionDelegate;

	@Autowired
	protected AlfrescoPublishDelegate activiti_publishAlfrescoDelegate;

	protected static Set<String> activityIdSet = new TreeSet<String>();
	protected static Set<String> flowElementIdSet = new TreeSet<String>();
	protected static String appName;
	protected static String processDefinitionKey;
	protected static String processDefinitionId;

	/*
	 * Setting up the mock email transport which can be used for testing email
	 * steps in BPMN
	 */
	@BeforeClass
	public static void setUp() {
		Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.provider.class", MockEmailTransport.class.getName());
		props.put("mail.smtp.class", MockEmailTransport.class.getName());
		props.put("mail.smtp.provider.vendor", "test");
		props.put("mail.smtp.provider.version", "0.0.0");

		Provider provider = new Provider(Type.TRANSPORT, "smtp", MockEmailTransport.class.getName(), "test", "1.0");
		Session mailSession = Session.getDefaultInstance(props);
		SimpleNamingContextBuilder builder = null;
		try {
			mailSession.setProvider(provider);
			builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
			builder.bind("java:comp/env/Session", mailSession);
		} catch (NamingException e) {
			// logger.error(e);
		} catch (NoSuchProviderException e) {
			// logger.error(e);
		}
	}

	/* Including it in the Abstract Class to avoid writing this in all the Tests.
	 * Pre-test logic flow - 
	 * 1) 	Download from APS if system property -Daps.app.download=true
	 * 2) 	Find all the bpmn20.xml's in {@value
	 * 		BPMN_RESOURCE_PATH} and deploy to process engine 
	 * 3)	Find all the elements in the process that is being tested. This set will 
	 * 		be compared with another set that contains the process elements that are 
	 * 		covered in each tests (this get updated after each tests).
	 */
	@Before
	public void before() throws Exception {

		if (System.getProperty("aps.app.download") != null && System.getProperty("aps.app.download").equals("true")) {
			ActivitiResources.forceGet(appName);
		}

		Iterator<File> it = FileUtils.iterateFiles(new File(BPMN_RESOURCE_PATH), null, false);
		while (it.hasNext()) {
			String bpmnXml = ((File) it.next()).getPath();
			String extension = FilenameUtils.getExtension(bpmnXml);
			if (extension.equals("xml")) {
				repositoryService.createDeployment().addInputStream(bpmnXml, new FileInputStream(bpmnXml)).deploy();
			}
		}
		processDefinitionId = repositoryService.createProcessDefinitionQuery()
				.processDefinitionKey(processDefinitionKey).singleResult().getId();
		List<Process> processList = repositoryService.getBpmnModel(processDefinitionId).getProcesses();
		for (Process proc : processList) {
			for (FlowElement flowElement : proc.getFlowElements()) {
				if (!(flowElement instanceof SequenceFlow)) {
					flowElementIdSet.add(flowElement.getId());
				}
			}
		}
	}

	/*
	 * Post-test logic flow - 
	 * 1) 	Update activityIdSet (Set containing all the elements tested)
	 * 2) 	Delete all deployments
	 */
	@After
	public void after() {
		for (HistoricActivityInstance act : historyService.createHistoricActivityInstanceQuery().list()) {
			activityIdSet.add(act.getActivityId());
		}
		List<Deployment> deploymentList = activitiRule.getRepositoryService().createDeploymentQuery().list();
		for (Deployment deployment : deploymentList) {
			activitiRule.getRepositoryService().deleteDeployment(deployment.getId(), true);
		}
	}

	/*
	 * Tear down logic - Compare the flowElementIdSet with activityIdSet and
	 * alert the developer if some parts are not tested
	 */
	@AfterClass
	public static void afterClass() {
		if (!flowElementIdSet.equals(activityIdSet)) {
			System.out.println(
					"***********PROCESS TEST COVERAGE WARNING: Not all paths are being tested, please review the test cases!***********");
			System.out.println("Steps In Model: " + flowElementIdSet);
			System.out.println("Steps Tested: " + activityIdSet);
		}
	}

}

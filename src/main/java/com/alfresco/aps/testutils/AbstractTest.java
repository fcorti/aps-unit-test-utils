package com.alfresco.aps.testutils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.NoSuchProviderException;
import javax.mail.Provider;
import javax.mail.Session;
import javax.mail.Provider.Type;
import javax.naming.NamingException;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import com.alfresco.aps.mockdelegates.RestCallMockClass;

public abstract class AbstractTest {

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

}

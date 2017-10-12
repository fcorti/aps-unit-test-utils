package com.alfresco.aps.testutils;

import static com.alfresco.aps.testutils.TestUtilsConstants.DMN_RESOURCE_PATH;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.activiti.dmn.engine.DmnEngine;
import com.activiti.dmn.engine.DmnEngineConfiguration;
import com.activiti.dmn.engine.DmnRepositoryService;
import com.activiti.dmn.engine.DmnRuleService;
import com.activiti.dmn.engine.domain.entity.DmnDeployment;
import com.activiti.dmn.engine.test.ActivitiDmnRule;
import com.alfresco.aps.testutils.resources.ActivitiResources;

public abstract class AbstractDmnTest {

	@Autowired
	protected ApplicationContext appContext;
	
	@Autowired
	protected Environment env;
	
	@Autowired
	protected DmnRepositoryService repositoryService;
	
	@Autowired
	protected DmnRuleService ruleService;
    
    @Autowired
    protected DmnEngine dmnEngine;
    
    @Autowired
    protected DmnEngineConfiguration dmnEngineConfiguration;
    
    @Autowired
    protected ActivitiDmnRule activitiDmnRule;
	
	protected static ArrayList<Long> deploymentList = new ArrayList<Long>();
	protected static String appName;
	protected static String decisonTableKey;
	
	@Before
	public void before() throws Exception {
		
		if(System.getProperty("aps.app.download")!=null && System.getProperty("aps.app.download").equals("true")) {
			ActivitiResources.forceGet(appName);
		}
		
		//Deploy the dmn files
		Iterator<File> it = FileUtils.iterateFiles(new File(DMN_RESOURCE_PATH), null, false);
		while (it.hasNext()) {
			String bpmnXml = ((File) it.next()).getPath();

			String extension = FilenameUtils.getExtension(bpmnXml);
			if (extension.equals("dmn")) {
				DmnDeployment dmnDeplyment = repositoryService.createDeployment()
						.addInputStream(bpmnXml, new FileInputStream(bpmnXml)).deploy();
				deploymentList.add(dmnDeplyment.getId());
			}
		}
	}

	@After
	public void after() {
		for (Long deploymentId : deploymentList) {
			repositoryService.deleteDeployment(deploymentId);
		}
		deploymentList.clear();
	}

}

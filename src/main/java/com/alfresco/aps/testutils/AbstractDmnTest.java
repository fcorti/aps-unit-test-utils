package com.alfresco.aps.testutils;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.activiti.dmn.engine.DmnEngine;
import com.activiti.dmn.engine.DmnEngineConfiguration;
import com.activiti.dmn.engine.DmnRepositoryService;
import com.activiti.dmn.engine.DmnRuleService;
import com.activiti.dmn.engine.test.ActivitiDmnRule;

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

}

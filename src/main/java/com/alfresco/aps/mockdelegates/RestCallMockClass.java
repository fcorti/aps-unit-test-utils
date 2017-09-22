package com.alfresco.aps.mockdelegates;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import org.activiti.engine.delegate.Expression;

public class RestCallMockClass implements JavaDelegate{
	
	Expression restUrl;
	Expression httpMethod;
	Expression baseEndpoint;
	Expression baseEndpointName;
	Expression requestMappingJSONTemplate;
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		// TODO Auto-generated method stub
	}

}

package com.alfresco.aps.mockdelegates;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

public class AlfrescoPublishDelegate implements JavaDelegate{
	
	Expression contentSource;
	Expression account;
	Expression site;
	Expression publishAsType;
	Expression path;
	
	@Override
	public void execute(DelegateExecution execution) {
		
	}

}

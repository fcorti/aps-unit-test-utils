package com.alfresco.aps.mockdelegates;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

public class ExecuteDecisionBean implements JavaDelegate{
	
	Expression decisionTableReferenceKey;
	
	@Override
	public void execute(DelegateExecution execution) {
		
	}

}

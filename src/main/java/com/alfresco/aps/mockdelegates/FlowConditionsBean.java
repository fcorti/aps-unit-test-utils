package com.alfresco.aps.mockdelegates;

import org.activiti.engine.delegate.DelegateExecution;

public class FlowConditionsBean {
	
	public boolean exists(DelegateExecution execution, String variableName) {
		return execution.getVariable(variableName)!=null;
	}

}

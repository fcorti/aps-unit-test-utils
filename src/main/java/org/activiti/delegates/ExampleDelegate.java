package org.activiti.delegates;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.log4j.Logger;

public class ExampleDelegate implements JavaDelegate {

	static Logger log = Logger.getLogger(ExampleDelegate.class.getName());

	public void execute(DelegateExecution execution) {

		log.info("Hello " + execution.getVariable("name") + "!");

	}

}
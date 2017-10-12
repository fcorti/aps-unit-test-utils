package com.alfresco.aps.testutils;

import java.util.Objects;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.ActivitiRule;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

public class ProcessInstanceAssert extends AbstractAssert<ProcessInstanceAssert, ProcessInstance> {

	ActivitiRule activitiRule = SpringBeanLookupUtil.getActivitiRule();
	
	public ProcessInstanceAssert(ProcessInstance actual) {
		super(actual, ProcessInstanceAssert.class);
	}

	public static ProcessInstanceAssert assertThat(ProcessInstance actual) {
		return new ProcessInstanceAssert(actual);
	}

	/**
	 * Make sure that {@link ProcessInstance} has a name set and is equal to the
	 * given name.
	 * 
	 * @return this {@link ProcessInstanceAssert}
	 */
	public ProcessInstanceAssert hasName(String name) {

		isNotNull();

		Assertions.assertThat(actual.getName()).as("is not null").isNotNull();

		if (!Objects.equals(actual.getName(), name)) {
			failWithMessage("Expected process instance name to be <%s> but was <%s>", name, actual.getName());
		}

		return this;
	}

	/**
	 * Make sure that {@link ProcessInstance} is complete.
	 * 
	 * @return this {@link ProcessInstanceAssert}
	 */
	public ProcessInstanceAssert isComplete() {
		Assertions.assertThat(activitiRule.getRuntimeService().createProcessInstanceQuery()
				.processInstanceId(actual.getProcessInstanceId()).list().size() == 0).isTrue();
		return this;
	}

}

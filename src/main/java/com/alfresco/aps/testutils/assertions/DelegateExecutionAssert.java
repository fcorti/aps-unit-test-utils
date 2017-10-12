package com.alfresco.aps.testutils.assertions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.ActivitiRule;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import com.alfresco.aps.testutils.SpringBeanLookupUtil;

public class DelegateExecutionAssert extends AbstractAssert<DelegateExecutionAssert, DelegateExecution> {

	ActivitiRule activitiRule = SpringBeanLookupUtil.getActivitiRule();

	public DelegateExecutionAssert(DelegateExecution actual) {
		super(actual, DelegateExecutionAssert.class);
	}

	public static DelegateExecutionAssert assertThat(DelegateExecution actual) {
		return new DelegateExecutionAssert(actual);
	}

	/**
	 * Make sure that {@link ProcessInstance} has a name set and is equal to the
	 * given name.
	 * 
	 * @return this {@link DelegateExecutionAssert}
	 */
	public DelegateExecutionAssert assertFieldExtensions(int expectedNumber, HashMap<String, String> expectedFields) {
		FlowElement flowElement = activitiRule.getRepositoryService().getBpmnModel(actual.getProcessDefinitionId())
				.getFlowElement(actual.getCurrentActivityId());

		Assertions.assertThat(flowElement).isNotNull();

		Assertions.assertThat(flowElement instanceof ServiceTask).isTrue();
		ServiceTask serviceTask = (ServiceTask) flowElement;
		List<FieldExtension> fieldExtensions = serviceTask.getFieldExtensions();
		Assertions.assertThat(fieldExtensions.size()).as("Check field extension count").isEqualTo(expectedNumber);
		if (expectedNumber > 0) {
			for (Map.Entry<String, String> field : expectedFields.entrySet()) {
				boolean fieldFound = false;
				for (FieldExtension fieldExtension : fieldExtensions) {
					if (fieldExtension.getFieldName().equals(field.getKey())) {
						fieldFound = true;
						Assertions.assertThat(fieldExtension.getStringValue()).as("Check field extension ")
								.isEqualTo(expectedFields.get(fieldExtension.getFieldName()));
					}
				}
				Assertions.assertThat(fieldFound).isTrue();
			}

		}
		return this;
	}
}

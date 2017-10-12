package com.alfresco.aps.testutils.assertions;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ReceiveTask;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.ActivitiRule;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.alfresco.aps.testutils.UnitTestHelpers;
import com.alfresco.aps.testutils.SpringBeanLookupUtil;

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

	/**
	 * Make sure that {@link ProcessInstance} created the given variables.
	 * 
	 * @return this {@link ProcessInstanceAssert}
	 */
	public ProcessInstanceAssert hasHistoricVariables(Map<String, Object> expectedVariables) {
		for (Map.Entry<String, Object> entry : expectedVariables.entrySet()) {
			Assertions.assertThat(activitiRule.getHistoryService().createHistoricVariableInstanceQuery()
					.processInstanceId(actual.getProcessInstanceId()).variableName(entry.getKey()).singleResult()
					.getValue().equals(entry.getValue())).isTrue();
		}
		return this;
	}

	public ProcessInstanceAssert signalWaitCountIs(int expectedNumber, String signal) {
		signalWaitCountIs(null, expectedNumber, signal);
		return this;
	}

	public ProcessInstanceAssert signalWaitCountIs(String activityId, int expectedNumber, String signal) {

		ExecutionQuery executionQuery = activitiRule.getRuntimeService().createExecutionQuery()
				.signalEventSubscriptionName(signal);

		if (activityId != null) {
			executionQuery.activityId(activityId);
		}

		Assertions.assertThat(executionQuery.list().size()).as("Check execution list size").isEqualTo(expectedNumber);
		return this;
	}

	public ProcessInstanceAssert executeSignalWaitAtivities(String signal, Map<String, Object> vars) {
		executeSignalWaitAtivities(null, signal, vars);
		return this;
	}

	public ProcessInstanceAssert executeSignalWaitAtivities(String activityId, String signal,
			Map<String, Object> vars) {

		ExecutionQuery executionQuery = activitiRule.getRuntimeService().createExecutionQuery()
				.signalEventSubscriptionName(signal);

		if (activityId != null) {
			executionQuery.activityId(activityId);
		}

		for (Execution execution : executionQuery.list()) {
			activitiRule.getRuntimeService().signalEventReceived(signal, execution.getId(), vars);
		}
		return this;
	}

	public ProcessInstanceAssert messageWaitCountIs(int expectedNumber, String message) {
		return messageWaitCountIs(null, expectedNumber, message);
	}

	public ProcessInstanceAssert messageWaitCountIs(String activityId, int expectedNumber, String message) {

		ExecutionQuery executionQuery = activitiRule.getRuntimeService().createExecutionQuery()
				.messageEventSubscriptionName(message);

		if (activityId != null) {
			executionQuery.activityId(activityId);
		}

		Assertions.assertThat(executionQuery.list().size()).as("Check execution list size").isEqualTo(expectedNumber);
		return this;
	}

	public ProcessInstanceAssert executeMessageWaitAtivities(String message, Map<String, Object> vars) {
		return executeMessageWaitAtivities(null, message, vars);
	}

	public ProcessInstanceAssert executeMessageWaitAtivities(String activityId, String message,
			Map<String, Object> vars) {

		ExecutionQuery executionQuery = activitiRule.getRuntimeService().createExecutionQuery()
				.messageEventSubscriptionName(message);

		if (activityId != null) {
			executionQuery.activityId(activityId);
		}

		for (Execution execution : executionQuery.list()) {
			activitiRule.getRuntimeService().messageEventReceived(message, execution.getId(), vars);
		}
		return this;
	}

	public ProcessInstanceAssert receiveTaskCountIs(int expectedNumber) {

		ExecutionQuery executionQuery = activitiRule.getRuntimeService().createExecutionQuery();

		int i = 0;
		for (Execution execution : executionQuery.list()) {
			FlowElement flowElement = activitiRule.getRepositoryService().getBpmnModel(actual.getProcessDefinitionId())
					.getFlowElement(execution.getActivityId());
			if (flowElement instanceof ReceiveTask) {
				i++;
			}
		}
		Assertions.assertThat(i).as("Check receive task count").isEqualTo(expectedNumber);
		return this;
	}

	public ProcessInstanceAssert executeReceiveTasks() {
		return executeReceiveTasks(null);
	}

	public ProcessInstanceAssert executeReceiveTasks(Map<String, Object> vars) {
		ExecutionQuery executionQuery = activitiRule.getRuntimeService().createExecutionQuery();

		for (Execution execution : executionQuery.list()) {
			FlowElement flowElement = activitiRule.getRepositoryService().getBpmnModel(actual.getProcessDefinitionId())
					.getFlowElement(execution.getActivityId());
			if (flowElement instanceof ReceiveTask) {
				activitiRule.getRuntimeService().signal(execution.getId(), vars);
			}
		}
		return this;
	}

	// Job is a candidate for something like JobAssert. to be done later
	public ProcessInstanceAssert timerJobCountIs(int expectedJobListSize, Integer expectedTimeFromNow,
			String expectedTimeUnit) {
		JobQuery jobQuery = activitiRule.getManagementService().createJobQuery().timers();
		if (expectedTimeFromNow != null && expectedTimeUnit != null) {
			// only supports precision up to minute level
			Date now = new Date();
			DateTime expectedDate = UnitTestHelpers.calculateExpectedDate(expectedTimeFromNow, expectedTimeUnit, now);
			jobQuery.duedateHigherThan(expectedDate.minusMinutes(1).toDate())
					.duedateLowerThan(expectedDate.plusMinutes(1).toDate());
		}
		List<Job> timerJobList = jobQuery.list();
		Assertions.assertThat(timerJobList.size()).as("Check timer size").isEqualTo(expectedJobListSize);
		return this;
	}

	public ProcessInstanceAssert timerJobCountIs(int expectedJobListSize) {
		return timerJobCountIs(expectedJobListSize, null, null);
	}

	// Job is a candidate for something like JobAssert. to be done later
	public ProcessInstanceAssert timerJobsWithDueDateFromNow(Integer expectedTimeFromNow, String expectedTimeUnit) {
		// only supports precision up to minute level
		Date now = new Date();
		DateTime expectedDate = UnitTestHelpers.calculateExpectedDate(expectedTimeFromNow, expectedTimeUnit, now);
		List<Job> timerJobList = activitiRule.getManagementService().createJobQuery().timers()
				.duedateHigherThan(expectedDate.minusMinutes(1).toDate())
				.duedateLowerThan(expectedDate.plusMinutes(1).toDate()).list();

		// Ignoring seconds as it is hard to get the precision!
		DateTimeFormatter formatter = ISODateTimeFormat.dateHourMinute();
		for (Job job : timerJobList) {
			String actualDateString = formatter.print(new DateTime(job.getDuedate()));
			String exepctedDateString = formatter.print(expectedDate);
			Assertions.assertThat(actualDateString).as("Check due date").isEqualTo(exepctedDateString);
		}
		return this;
	}

	// Job is a candidate for something like JobAssert. to be done later
	public ProcessInstanceAssert executeTimerJobs(Integer expectedTimeFromNow, String expectedTimeUnit) {
		JobQuery jobQuery = activitiRule.getManagementService().createJobQuery().timers();
		if (expectedTimeFromNow != null && expectedTimeUnit != null) {
			// only supports precision up to minute level
			Date now = new Date();
			DateTime expectedDate = UnitTestHelpers.calculateExpectedDate(expectedTimeFromNow, expectedTimeUnit, now);
			jobQuery.duedateHigherThan(expectedDate.minusMinutes(1).toDate())
					.duedateLowerThan(expectedDate.plusMinutes(1).toDate());
		}
		List<Job> timerJobList = jobQuery.list();
		for (Job job : timerJobList) {
			activitiRule.getManagementService().executeJob(job.getId());
		}
		return this;
	}

	public ProcessInstanceAssert executeTimerJobs() {
		return executeTimerJobs(null, null);
	}
}

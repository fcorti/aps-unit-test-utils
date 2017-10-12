package com.alfresco.aps.testutils;

import static org.assertj.core.api.Assertions.*;
import static com.alfresco.aps.testutils.TestUtilsConstants.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ReceiveTask;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;

public class UnitTestHelpers {

	@Autowired
	private ActivitiRule activitiRule;

	public void assertTimerJob(int expectedJobListSize, Integer expectedTimeFromNow, String expectedTimeUnit,
			Boolean executeJob) {
		// only supports precision up to minute level
		Date now = new Date();
		DateTime expectedDate = calculateExpectedDate(expectedTimeFromNow, expectedTimeUnit, now);
		List<Job> timerJobList = activitiRule.getManagementService().createJobQuery().timers()
				.duedateLowerThan(expectedDate.plusMinutes(1).toDate()).list();
		assertThat(timerJobList.size()).as("Check timer size").isEqualTo(expectedJobListSize);

		// Ignoring seconds as it is hard to get the precision!
		DateTimeFormatter formatter = ISODateTimeFormat.dateHourMinute();
		for (Job job : timerJobList) {
			String actualDateString = formatter.print(new DateTime(job.getDuedate()));
			String exepctedDateString = formatter.print(expectedDate);
			assertThat(actualDateString).as("Check due date").isEqualTo(exepctedDateString);
			if (executeJob) {
				activitiRule.getManagementService().executeJob(job.getId());
			}
		}
	}

	public static DateTime calculateExpectedDate(Integer expectedNumber, String expectedUnit, Date createTime) {
		DateTime exepctedDate;
		switch (expectedUnit) {
		case TIME_UNIT_DAY:
			exepctedDate = new DateTime(createTime).plusDays(expectedNumber);
			break;
		case TIME_UNIT_HOUR:
			exepctedDate = new DateTime(createTime).plusHours(expectedNumber);
			break;
		case TIME_UNIT_MINUTE:
			exepctedDate = new DateTime(createTime).plusMinutes(expectedNumber);
			break;
		case TIME_UNIT_SECOND:
			exepctedDate = new DateTime(createTime).plusSeconds(expectedNumber);
			break;
		default:
			exepctedDate = null;
			break;
		}
		return exepctedDate;
	}

	public String getTaskOutcomeVariable(Task task) {
		return "form" + task.getFormKey() + "outcome";
	}

	public void assertHistoricVariableValues(String processInstanceId, Map<String, Object> expectedVariables) {
		for (Map.Entry<String, Object> entry : expectedVariables.entrySet()) {
			assertThat(activitiRule.getHistoryService().createHistoricVariableInstanceQuery()
					.processInstanceId(processInstanceId).variableName(entry.getKey()).singleResult().getValue()
					.equals(entry.getValue())).isTrue();
		}

	}

	public void assertSignalWait(int expectedNumber, String activityId, String signal, Boolean execute,
			Map<String, Object> vars) {

		ExecutionQuery executionQuery = activitiRule.getRuntimeService().createExecutionQuery()
				.signalEventSubscriptionName(signal);

		if (activityId != null) {
			executionQuery.activityId(activityId);
		}


		assertThat(executionQuery.list().size()).as("Check execution list size").isEqualTo(expectedNumber);

		if (execute) {
			for (Execution execution : executionQuery.list()) {
				activitiRule.getRuntimeService().signalEventReceived(signal, execution.getId(), vars);
			}
		}
	}

	public void assertMessageWait(int expectedNumber, String activityId, String message, Boolean execute,
			Map<String, Object> vars) {

		ExecutionQuery executionQuery = activitiRule.getRuntimeService().createExecutionQuery()
				.messageEventSubscriptionName(message);

		if (activityId != null) {
			executionQuery.activityId(activityId);
		}

		assertThat(executionQuery.list().size()).as("Check execution list size").isEqualTo(expectedNumber);

		if (execute) {
			for (Execution execution : executionQuery.list()) {
				activitiRule.getRuntimeService().messageEventReceived(message, execution.getId(), vars);
			}
		}
	}

	public void assertReceiveTask(int expectedNumber, Boolean execute, Map<String, Object> vars,
			String processDefinitionId) {

		ExecutionQuery executionQuery = activitiRule.getRuntimeService().createExecutionQuery();

		if (execute) {
			int i = 0;
			for (Execution execution : executionQuery.list()) {
				FlowElement flowElement = activitiRule.getRepositoryService().getBpmnModel(processDefinitionId)
						.getFlowElement(execution.getActivityId());
				if (flowElement instanceof ReceiveTask) {
					i++;
					activitiRule.getRuntimeService().signal(execution.getId(), vars);
				}

			}
			assertThat(i).as("Check receive task count").isEqualTo(expectedNumber);
		}
	}

	public void assertFieldExtensions(int expectedNumber, DelegateExecution execution,
			HashMap<String, String> expectedFields) {
		FlowElement flowElement = activitiRule.getRepositoryService().getBpmnModel(execution.getProcessDefinitionId())
				.getFlowElement(execution.getCurrentActivityId());

		assertThat(flowElement).isNotNull();
		
		assertThat(flowElement instanceof ServiceTask).isTrue();
		ServiceTask serviceTask = (ServiceTask) flowElement;
		List<FieldExtension> fieldExtensions = serviceTask.getFieldExtensions();
		assertThat(fieldExtensions.size()).as("Check field extension count").isEqualTo(expectedNumber);
		if (expectedNumber > 0) {
			for (Map.Entry<String, String> field : expectedFields.entrySet()) {
				boolean fieldFound = false;
				for (FieldExtension fieldExtension : fieldExtensions) {
					if(fieldExtension.getFieldName().equals(field.getKey())){
						fieldFound = true;
						assertThat(fieldExtension.getStringValue()).as("Check field extension ").isEqualTo(expectedFields.get(fieldExtension.getFieldName()));
					}
				}
				assertThat(fieldFound).isTrue();
			}
			
		}
	}

	public void assertEmails(int expectedNumber, int indexToAssert, String body, String subject, String from,
			String[] toList, String[] ccList, String[] bccList) {


		assertThat(AbstractBpmnTest.actualEmails.size()).as("Check email count ").isEqualTo(expectedNumber);
		if (expectedNumber > 0) {

			EmailType emailToAssert = AbstractBpmnTest.actualEmails.get(indexToAssert);
			
			assertThat(emailToAssert.getSubject()).as("Check email subject ").isEqualTo(subject);
			assertThat(emailToAssert.getBody().replaceAll("\\s+", "")).as("Check email body ").isEqualTo(body.replaceAll("\\s+", ""));
			
			if (from != null) {
				assertThat(emailToAssert.getFrom()).as("Check email from address ").isEqualTo(from);
			}

			if (toList != null) {
				assertThat(emailToAssert.getTo().size()).as("Check \"to\" address count ").isEqualTo(toList.length);
				for (String to : toList) {
					assertThat(emailToAssert.getTo().contains(to)).isTrue();
				}
			}
			if (ccList != null) {
				assertThat(emailToAssert.getCc().size()).as("Check email count ").isEqualTo(ccList.length);
				for (String cc : ccList) {
					assertThat(emailToAssert.getCc().contains(cc)).isTrue();
				}
			}
			if (bccList != null) {
				assertThat(emailToAssert.getBcc().size()).as("Check email count ").isEqualTo(bccList.length);
				for (String bcc : bccList) {
					assertThat(emailToAssert.getBcc().contains(bcc)).isTrue();
				}
			}

		}

	}

	public boolean waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
		Timer timer = new Timer();
		InteruptTask task = new InteruptTask(Thread.currentThread());
		timer.schedule(task, maxMillisToWait);
		boolean areJobsAvailable = true;
		try {
			while (areJobsAvailable && !task.isTimeLimitExceeded()) {
				Thread.sleep(intervalMillis);
				areJobsAvailable = areJobsAvailable();
			}
		} catch (InterruptedException e) {
		} finally {
			timer.cancel();
		}
		return areJobsAvailable;
	}

	public boolean areJobsAvailable() {
		return !activitiRule.getManagementService().createJobQuery().executable().list().isEmpty();
	}

	public static class InteruptTask extends TimerTask {
		protected boolean timeLimitExceeded = false;
		protected Thread thread;

		public InteruptTask(Thread thread) {
			this.thread = thread;
		}

		public boolean isTimeLimitExceeded() {
			return timeLimitExceeded;
		}

		public void run() {
			timeLimitExceeded = true;
			thread.interrupt();
		}
	}

}

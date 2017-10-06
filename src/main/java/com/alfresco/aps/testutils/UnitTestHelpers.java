package com.alfresco.aps.testutils;

import static org.junit.Assert.*;
import static com.alfresco.aps.testutils.TestUtilsConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ReceiveTask;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.task.IdentityLink;
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
		assertEquals(expectedJobListSize, timerJobList.size());

		// Ignoring seconds as it is hard to get the precision!
		DateTimeFormatter formatter = ISODateTimeFormat.dateHourMinute();
		for (Job job : timerJobList) {
			String actualDateString = formatter.print(new DateTime(job.getDuedate()));
			String exepctedDateString = formatter.print(expectedDate);
			assertEquals("Due Date is correct", exepctedDateString, actualDateString);
			if (executeJob) {
				activitiRule.getManagementService().executeJob(job.getId());
			}
		}
	}

	public void assertTaskDueDate(Integer expectedNumberFromCreateTime, String expectedUnit, Task task) {
		Date dueDate = task.getDueDate();
		Date createTime = task.getCreateTime();
		assertNotNull(dueDate);

		// Ignoring seconds as it is hard to get the precision!
		DateTimeFormatter formatter = ISODateTimeFormat.dateHourMinute();
		String exepctedDate = formatter
				.print(calculateExpectedDate(expectedNumberFromCreateTime, expectedUnit, createTime));
		String actualDate = formatter.print(new DateTime(dueDate));
		assertEquals("Due Date is correct", exepctedDate, actualDate);
	}

	private DateTime calculateExpectedDate(Integer expectedNumber, String expectedUnit, Date createTime) {
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

	public void assertNullProcessInstance(String processInstanceId) {
		assertTrue(activitiRule.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId)
				.list().size() == 0);
	}

	public void assertHistoricVariableValues(String processInstanceId, Map<String, Object> expectedVariables) {
		for (Map.Entry<String, Object> entry : expectedVariables.entrySet()) {
			assertTrue(activitiRule.getHistoryService().createHistoricVariableInstanceQuery()
					.processInstanceId(processInstanceId).variableName(entry.getKey()).singleResult().getValue()
					.equals(entry.getValue()));
		}

	}

	public void assertSignalWait(int expectedNumber, String activityId, String signal, Boolean execute,
			Map<String, Object> vars) {

		ExecutionQuery executionQuery = activitiRule.getRuntimeService().createExecutionQuery()
				.signalEventSubscriptionName(signal);

		if (activityId != null) {
			executionQuery.activityId(activityId);
		}

		assertEquals(expectedNumber, executionQuery.list().size());

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

		assertEquals(expectedNumber, executionQuery.list().size());

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
			assertEquals(expectedNumber, i);
		}
	}

	public void assertFieldExtensions(int expectedNumber, DelegateExecution execution,
			HashMap<String, String> expectedFields) {
		FlowElement flowElement = activitiRule.getRepositoryService().getBpmnModel(execution.getProcessDefinitionId())
				.getFlowElement(execution.getCurrentActivityId());
		assertNotNull(flowElement);
		assertTrue(flowElement instanceof ServiceTask);
		ServiceTask serviceTask = (ServiceTask) flowElement;
		List<FieldExtension> fieldExtensions = serviceTask.getFieldExtensions();
		assertEquals(expectedNumber, fieldExtensions.size());
		if (expectedNumber > 0) {
			for (Map.Entry<String, String> field : expectedFields.entrySet()) {
				boolean fieldFound = false;
				for (FieldExtension fieldExtension : fieldExtensions) {
					if(fieldExtension.getFieldName().equals(field.getKey())){
						fieldFound = true;
						assertEquals(expectedFields.get(fieldExtension.getFieldName()), fieldExtension.getStringValue());
					}
				}
				assertTrue(fieldFound);
			}
			
		}
	}

	public void assertUserAssignment(String expectedAssignee, Task task, Boolean assignmentLookupRequired,
			Boolean isUserLookupBasedOnExternalId) {
		if (assignmentLookupRequired != null && assignmentLookupRequired) {
			Map<String, List<ExtensionElement>> extensionElements = activitiRule.getRepositoryService()
					.getBpmnModel(task.getProcessDefinitionId()).getFlowElement(task.getTaskDefinitionKey())
					.getExtensionElements();
			for (Map.Entry<String, List<ExtensionElement>> entry : extensionElements.entrySet()) {
				if (isUserLookupBasedOnExternalId && entry.getKey().equals("assignee-info-externalid")) {
					assertEquals(expectedAssignee, entry.getValue().get(0).getElementText());
				} else if (!isUserLookupBasedOnExternalId && entry.getKey().equals("assignee-info-email")) {
					assertEquals(expectedAssignee, entry.getValue().get(0).getElementText());
				}
			}
		} else {
			assertEquals(expectedAssignee, task.getAssignee());
		}
	}

	public void assertCandidateAssignment(String[] expectedGroups, String[] expectedUsers, Task task,
			Boolean assignmentLookupRequired, Boolean isUserLookupBasedOnExternalId) {

		List<String> groupIdArray = new ArrayList<String>();
		List<String> userIdArray = new ArrayList<String>();
		List<String> expectedGroupsArray = new ArrayList<String>();
		List<String> expectedUsersArray = new ArrayList<String>();

		if (expectedGroups != null) {
			expectedGroupsArray = Arrays.asList(expectedGroups);
		}
		if (expectedUsers != null) {
			expectedUsersArray = Arrays.asList(expectedUsers);
		}

		if (assignmentLookupRequired != null && assignmentLookupRequired) {
			Map<String, List<ExtensionElement>> extensionElements = activitiRule.getRepositoryService()
					.getBpmnModel(task.getProcessDefinitionId()).getFlowElement(task.getTaskDefinitionKey())
					.getExtensionElements();
			for (Map.Entry<String, List<ExtensionElement>> entry : extensionElements.entrySet()) {
				if (entry.getKey().startsWith("group-info-name-")
						&& expectedGroupsArray.contains(entry.getValue().get(0).getElementText())) {
					groupIdArray.add(entry.getKey().substring(entry.getKey().lastIndexOf("-") + 1));
				} else if (isUserLookupBasedOnExternalId && entry.getKey().startsWith("user-info-externalid-")
						&& expectedUsersArray.contains(entry.getValue().get(0).getElementText())) {
					userIdArray.add(entry.getKey().substring(entry.getKey().lastIndexOf("-") + 1));
				} else if (!isUserLookupBasedOnExternalId && entry.getKey().startsWith("user-info-email-")
						&& expectedUsersArray.contains(entry.getValue().get(0).getElementText())) {
					userIdArray.add(entry.getKey().substring(entry.getKey().lastIndexOf("-") + 1));
				}
			}
		} else {
			if (expectedGroups != null) {
				groupIdArray = Arrays.asList(expectedGroups);
			}
			if (expectedUsers != null) {
				userIdArray = Arrays.asList(expectedUsers);
			}
		}

		TaskService taskService = activitiRule.getTaskService();
		int candidateGroupSize = 0;
		int candidateUserSize = 0;

		for (IdentityLink idLink : taskService.getIdentityLinksForTask(task.getId())) {
			if (idLink.getGroupId() != null) {
				String groupId = idLink.getGroupId();
				// Assert candidate group
				assertTrue(groupIdArray.contains(groupId));
				candidateGroupSize++;
			}
			if (idLink.getUserId() != null && !idLink.getType().equals("assignee")) {
				String userId = idLink.getUserId();
				// Assert candidate user
				assertTrue(userIdArray.contains(userId));
				candidateUserSize++;
			}
		}

		// Assert candidate group count
		if (expectedGroups != null) {
			assertEquals(expectedGroups.length, candidateGroupSize);
		}
		// Assert candidate user count
		if (expectedUsers != null) {
			assertEquals(expectedUsers.length, candidateUserSize);
		}
	}

	public void assertEmails(int expectedNumber, int indexToAssert, String body, String subject, String from,
			String[] toList, String[] ccList, String[] bccList) {

		assertEquals(expectedNumber, AbstractBpmnTest.actualEmails.size());
		if (expectedNumber > 0) {

			EmailType emailToAssert = AbstractBpmnTest.actualEmails.get(indexToAssert);

			assertEquals(subject, emailToAssert.getSubject());

			assertEquals(body.replaceAll("\\s+", ""), emailToAssert.getBody().replaceAll("\\s+", ""));

			if (from != null) {
				assertEquals(from, emailToAssert.getFrom());
			}

			if (toList != null) {
				assertEquals(toList.length, emailToAssert.getTo().size());
				for (String to : toList) {
					assertTrue(emailToAssert.getTo().contains(to));
				}
			}
			if (ccList != null) {
				assertEquals(ccList.length, emailToAssert.getCc().size());
				for (String cc : ccList) {
					assertTrue(emailToAssert.getCc().contains(cc));
				}
			}
			if (bccList != null) {
				assertEquals(bccList.length, emailToAssert.getBcc().size());
				for (String bcc : bccList) {
					assertTrue(emailToAssert.getBcc().contains(bcc));
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

package com.alfresco.aps.testutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;

public class UnitTestHelpers {

	@Autowired
	private ActivitiRule activitiRule;

	public void assertTimerJobDateLowerThan(Integer expectedNumberOfDays, Boolean executeJob) {
		// currently only supports day checks and one timer
		List<Job> timerJobList = activitiRule.getManagementService().createJobQuery().timers()
				.duedateLowerThan((new DateTime()).plusDays(expectedNumberOfDays).plusMinutes(1).toDate()).list();
		assertEquals(1, timerJobList.size());
		assertTrue("Due Date is correct", Days.daysBetween(new DateTime().plusDays(expectedNumberOfDays),
				new DateTime(timerJobList.get(0).getDuedate())).getDays() == 0);
		if (executeJob) {
			activitiRule.getManagementService().executeJob(timerJobList.get(0).getId());
		}
	}

	public void assertTimerJobsTimeInSecondsLowerThan(Integer expectedTimeInSeconds, Boolean executeJob) {
		// currently only supports day checks and one timer
		List<Job> timerJobList = activitiRule.getManagementService().createJobQuery().timers()
				.duedateLowerThan((new DateTime()).plusSeconds(expectedTimeInSeconds).toDate()).list();
		assertEquals(1, timerJobList.size());
		assertTrue("Due Date is correct", Days.daysBetween(new DateTime().plusSeconds(expectedTimeInSeconds),
				new DateTime(timerJobList.get(0).getDuedate())).getDays() == 0);
		if (executeJob) {
			activitiRule.getManagementService().executeJob(timerJobList.get(0).getId());
		}
	}

	public void assertTaskDueDate(Integer expectedNumberOfDays, Date dueDate) {
		assertTrue("Due Date is correct",
				Days.daysBetween(new DateTime().plusDays(expectedNumberOfDays), new DateTime(dueDate)).getDays() == 0);
	}

	public void assertNullProcessInstance(String processInstanceId) {
		assertTrue(activitiRule.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId)
				.list().size() == 0);
	}

	public void assertSignalWait(int expectedNumber, String activityId, String signal, Boolean execute, Map<String, Object> vars) {
		
		ExecutionQuery executionQuery = activitiRule.getRuntimeService().createExecutionQuery().signalEventSubscriptionName(signal);
		
		if(activityId!=null){
			executionQuery.activityId(activityId);
		}
		
		assertEquals(expectedNumber, executionQuery.list().size());
		
		if (execute) {
			for( Execution execution : executionQuery.list()){
				activitiRule.getRuntimeService().signalEventReceived (signal, execution.getId(), vars);
			}
		}
	}
	
	public void assertMessageWait(int expectedNumber, String activityId, String message, Boolean execute, Map<String, Object> vars) {
		
		ExecutionQuery executionQuery = activitiRule.getRuntimeService().createExecutionQuery().messageEventSubscriptionName(message);
		
		if(activityId!=null){
			executionQuery.activityId(activityId);
		}
		
		assertEquals(expectedNumber, executionQuery.list().size());
		
		if (execute) {
			for( Execution execution : executionQuery.list()){
				activitiRule.getRuntimeService().messageEventReceived(message, execution.getId(), vars);
			}
		}
	}
	
	public void assertReceiveTask(int expectedNumber, String activityId, Boolean execute, Map<String, Object> vars) {
		
		ExecutionQuery executionQuery = activitiRule.getRuntimeService().createExecutionQuery();
		
		if(activityId!=null){
			executionQuery.activityId(activityId);
		}
		
		assertEquals(expectedNumber, executionQuery.list().size());
		if (execute) {
			for( Execution execution : executionQuery.list()){
				activitiRule.getRuntimeService().signal(execution.getId(), vars);
			}
		}
	}
	
	public void assertUserAssignment(String expectedAssignee, Task task, Boolean assignmentLookupRequired, Boolean isUserLookupBasedOnExternalId) {
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
	
	public void assertEmails(int expectedNumber, int indexToAssert, String body, String subject, String[] toList, String[] ccList, String[] bccList){
		
		assertEquals(expectedNumber, AbstractTest.actualEmails.size());
		if(expectedNumber>0){
			
			EmailType emailToAssert = AbstractTest.actualEmails.get(indexToAssert);
			
			assertEquals(subject, emailToAssert.getSubject());
			
			assertEquals(body.replaceAll("\\s+", ""), emailToAssert.getBody().replaceAll("\\s+", ""));
			
			if(toList!=null){
				assertEquals(toList.length, emailToAssert.getTo().size());
				for (String to: toList){
					assertTrue(emailToAssert.getTo().contains(to));
				}
			}
			if(ccList!=null){
				assertEquals(ccList.length, emailToAssert.getCc().size());
				for (String cc: ccList){
					assertTrue(emailToAssert.getCc().contains(cc));
				}
			}
			if(bccList!=null){
				assertEquals(bccList.length, emailToAssert.getBcc().size());
				for (String bcc: bccList){
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

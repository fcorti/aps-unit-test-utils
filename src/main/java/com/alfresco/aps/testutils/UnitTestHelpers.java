package com.alfresco.aps.testutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("unitTestHelpers")
public class UnitTestHelpers {

	@Autowired
	private ActivitiRule activitiRule;

	public void assertTimerJobs(Integer expectedNumberOfDays, Boolean executeJob) {
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

	public void assertTimerJobsTimeInSeconds(Integer expectedTimeInSeconds, Boolean executeJob) {
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

	public void assertSignalWait(String message, Boolean executeSignal, Map<String, Object> vars) {
		assertEquals(1, activitiRule.getRuntimeService().createExecutionQuery().signalEventSubscriptionName(message)
				.list().size());
		if (executeSignal) {
			activitiRule.getRuntimeService().signalEventReceived(message, vars);
		}
	}

	public void assertGroupAssignment(String expectedGroupId, Task task) {
		// no assignee - should be a group
		assertNull(task.getAssignee());
		TaskService taskService = activitiRule.getTaskService();
		// Assert only one candidate
		assertEquals(1, taskService.getIdentityLinksForTask(task.getId()).size());
		// Assert no candidate users
		assertNull(taskService.getIdentityLinksForTask(task.getId()).get(0).getUserId());
		// Assert 1 candidate group
		assertNotNull(taskService.getIdentityLinksForTask(task.getId()).get(0).getGroupId());
		assertEquals(expectedGroupId, taskService.getIdentityLinksForTask(task.getId()).get(0).getGroupId());
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

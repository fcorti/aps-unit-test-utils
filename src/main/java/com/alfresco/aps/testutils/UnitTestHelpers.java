package com.alfresco.aps.testutils;

import static org.assertj.core.api.Assertions.*;
import static com.alfresco.aps.testutils.TestUtilsConstants.*;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

public class UnitTestHelpers {

	@Autowired
	private ActivitiRule activitiRule;

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

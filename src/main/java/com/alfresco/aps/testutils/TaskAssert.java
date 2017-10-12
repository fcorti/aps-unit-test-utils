package com.alfresco.aps.testutils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class TaskAssert extends AbstractAssert<TaskAssert, Task> {

	ActivitiRule activitiRule = SpringBeanLookupUtil.getActivitiRule();

	public TaskAssert(Task actual) {
		super(actual, TaskAssert.class);
	}

	public static TaskAssert assertThat(Task actual) {
		return new TaskAssert(actual);
	}

	/**
	 * Make sure that {@link Task} has a name set and is equal to the given
	 * name.
	 * 
	 * @return this {@link TaskAssert}
	 */
	public TaskAssert hasName(String name) {

		isNotNull();

		Assertions.assertThat(actual.getName()).as("is not null").isNotNull();

		if (!Objects.equals(actual.getName(), name)) {
			failWithMessage("Expected task name to be <%s> but was <%s>", name, actual.getName());
		}

		return this;
	}

	/**
	 * Make sure that key of {@link Task} is equal to the given name.
	 * 
	 * @return this {@link TaskAssert}
	 */
	public TaskAssert hasTaskDefinitionKey(String key) {

		isNotNull();

		Assertions.assertThat(actual.getTaskDefinitionKey()).as("is not null").isNotNull();

		if (!Objects.equals(actual.getTaskDefinitionKey(), key)) {
			failWithMessage("Expected task key to be <%s> but was <%s>", key, actual.getTaskDefinitionKey());
		}

		return this;
	}

	/**
	 * Make sure that the due date of the {@link Task} is equal to the given
	 * date (only up to minute level precision).
	 * 
	 * @return this {@link TaskAssert}
	 */
	public TaskAssert hasDueDate(Integer expectedNumberFromCreateTime, String expectedUnit) {

		Date dueDate = actual.getDueDate();
		Date createTime = actual.getCreateTime();
		Assertions.assertThat(dueDate).isNotNull();

		// Ignoring seconds as it is hard to get the precision!
		DateTimeFormatter formatter = ISODateTimeFormat.dateHourMinute();
		String exepctedDate = formatter
				.print(UnitTestHelpers.calculateExpectedDate(expectedNumberFromCreateTime, expectedUnit, createTime));
		String actualDate = formatter.print(new DateTime(dueDate));
		Assertions.assertThat(actualDate).as("Check due date").isEqualTo(exepctedDate);

		return this;
	}

	public TaskAssert hasAssignee(String expectedAssignee, Boolean assignmentLookupRequired,
			Boolean isUserLookupBasedOnExternalId) {
		if (assignmentLookupRequired != null && assignmentLookupRequired) {
			Map<String, List<ExtensionElement>> extensionElements = activitiRule.getRepositoryService()
					.getBpmnModel(actual.getProcessDefinitionId()).getFlowElement(actual.getTaskDefinitionKey())
					.getExtensionElements();
			for (Map.Entry<String, List<ExtensionElement>> entry : extensionElements.entrySet()) {
				if (isUserLookupBasedOnExternalId && entry.getKey().equals("assignee-info-externalid")) {
					Assertions.assertThat(entry.getValue().get(0).getElementText()).as("Check assignee id ")
							.isEqualTo(expectedAssignee);
				} else if (!isUserLookupBasedOnExternalId && entry.getKey().equals("assignee-info-email")) {
					Assertions.assertThat(entry.getValue().get(0).getElementText()).as("Check assignee email ")
							.isEqualTo(expectedAssignee);
				}
			}
		} else {
			Assertions.assertThat(actual.getAssignee()).as("Check assignee ").isEqualTo(expectedAssignee);
		}
		return this;
	}

	public TaskAssert hasCandidateUsers(String[] expectedUsers, Boolean assignmentLookupRequired,
			Boolean isUserLookupBasedOnExternalId) {

		List<String> userIdArray = new ArrayList<String>();
		List<String> expectedUsersArray = new ArrayList<String>();

		expectedUsersArray = Arrays.asList(expectedUsers);

		if (assignmentLookupRequired != null && assignmentLookupRequired) {
			Map<String, List<ExtensionElement>> extensionElements = activitiRule.getRepositoryService()
					.getBpmnModel(actual.getProcessDefinitionId()).getFlowElement(actual.getTaskDefinitionKey())
					.getExtensionElements();
			for (Map.Entry<String, List<ExtensionElement>> entry : extensionElements.entrySet()) {
				if (isUserLookupBasedOnExternalId && entry.getKey().startsWith("user-info-externalid-")
						&& expectedUsersArray.contains(entry.getValue().get(0).getElementText())) {
					userIdArray.add(entry.getKey().substring(entry.getKey().lastIndexOf("-") + 1));
				} else if (!isUserLookupBasedOnExternalId && entry.getKey().startsWith("user-info-email-")
						&& expectedUsersArray.contains(entry.getValue().get(0).getElementText())) {
					userIdArray.add(entry.getKey().substring(entry.getKey().lastIndexOf("-") + 1));
				}
			}
		} else {
			if (expectedUsers != null) {
				userIdArray = Arrays.asList(expectedUsers);
			}
		}

		TaskService taskService = activitiRule.getTaskService();
		int candidateUserSize = 0;

		for (IdentityLink idLink : taskService.getIdentityLinksForTask(actual.getId())) {
			if (idLink.getUserId() != null && !idLink.getType().equals("assignee")) {
				String userId = idLink.getUserId();
				// Assert candidate user
				Assertions.assertThat(userIdArray.contains(userId)).isTrue();
				candidateUserSize++;
			}
		}

		// Assert candidate user count
		if (expectedUsers != null) {
			Assertions.assertThat(candidateUserSize).as("Check user size ").isEqualTo(expectedUsers.length);
		}
		return this;
	}

	public TaskAssert hasCandidateGroups(String[] expectedGroups, Boolean assignmentLookupRequired,
			Boolean isUserLookupBasedOnExternalId) {

		List<String> groupIdArray = new ArrayList<String>();
		List<String> expectedGroupsArray = new ArrayList<String>();

		if (expectedGroups != null) {
			expectedGroupsArray = Arrays.asList(expectedGroups);
		}

		if (assignmentLookupRequired != null && assignmentLookupRequired) {
			Map<String, List<ExtensionElement>> extensionElements = activitiRule.getRepositoryService()
					.getBpmnModel(actual.getProcessDefinitionId()).getFlowElement(actual.getTaskDefinitionKey())
					.getExtensionElements();
			for (Map.Entry<String, List<ExtensionElement>> entry : extensionElements.entrySet()) {
				if (entry.getKey().startsWith("group-info-name-")
						&& expectedGroupsArray.contains(entry.getValue().get(0).getElementText())) {
					groupIdArray.add(entry.getKey().substring(entry.getKey().lastIndexOf("-") + 1));
				}
			}
		} else {
			if (expectedGroups != null) {
				groupIdArray = Arrays.asList(expectedGroups);
			}
		}

		TaskService taskService = activitiRule.getTaskService();
		int candidateGroupSize = 0;

		for (IdentityLink idLink : taskService.getIdentityLinksForTask(actual.getId())) {
			if (idLink.getGroupId() != null) {
				String groupId = idLink.getGroupId();
				// Assert candidate group
				Assertions.assertThat(groupIdArray.contains(groupId)).isTrue();
				candidateGroupSize++;
			}
		}

		// Assert candidate group count
		if (expectedGroups != null) {
			Assertions.assertThat(candidateGroupSize).as("Check candidate group size ")
					.isEqualTo(expectedGroups.length);
		}
		return this;
	}

	public void complete() {
		activitiRule.getTaskService().complete(actual.getId());
	}

	public void complete(Map<String, Object> variables) {
		activitiRule.getTaskService().complete(actual.getId(), variables);
	}

}

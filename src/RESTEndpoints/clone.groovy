// CLONE

import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.transform.BaseScript
import static com.atlassian.jira.component.ComponentAccessor.*

import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response
	
@BaseScript CustomEndpointDelegate delegate

clone(httpMethod: "GET") { MultivaluedMap queryParams ->

	String issueKey = queryParams.getFirst("issueKey") as String
	String assigneeId = queryParams.getFirst("assigneeId") as String
	String coworkerIds = queryParams.getFirst("coworkerIds") as String
	String summary = queryParams.getFirst("summary") as String
	def issue = issueManager.getIssueObject(issueKey)

	
	def clonedIssue = issueFactory.cloneIssue(issue)
	if(summary) clonedIssue.summary = summary
	if(assigneeId) clonedIssue.assigneeId = assigneeId
	
	if(coworkerIds) {
		clonedIssue.setCustomFieldValue(
			customFieldManager.getCustomFieldObjectByName("Co-Workers"),
			coworkerIds.split('[,\\s]+').collect { userManager.getUserByName(it) }
		)
	}
	

	customFieldManager.getCustomFieldObjects( issue.projectObject.id, issue.issueTypeObject.id).each {
		def value = it.getValue(issue)
		if(value) clonedIssue.setCustomFieldValue(it, value)
	}
	def createdIssue = issueManager.createIssueObject(jiraAuthenticationContext.user, clonedIssue)
	
	def builder = new groovy.json.JsonBuilder()
	builder { key createdIssue.key }
	return Response.status(201).entity(builder.toString()).build()
}

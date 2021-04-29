// setProdDateActual

import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.transform.BaseScript
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

import com.atlassian.jira.workflow.*
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.label.LabelManager
import com.atlassian.sal.api.ApplicationProperties
import com.onresolve.scriptrunner.runner.ScriptRunnerImpl
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.transform.BaseScript
import static com.atlassian.jira.component.ComponentAccessor.*
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import static com.atlassian.jira.issue.history.ChangeLogUtils.*
import com.atlassian.jira.issue.history.*
import com.atlassian.jira.bc.issue.* 
import com.atlassian.jira.issue.*
import com.atlassian.jira.*
import java.text.SimpleDateFormat    

@BaseScript CustomEndpointDelegate delegate

setProdDateActual(httpMethod: "GET") { MultivaluedMap queryParams ->

    String issueKey = queryParams.getFirst("issueKey") as String
    if(!issueKey) return Response.status(400).entity("issueKey must be given").build()
    String date = queryParams.getFirst("date") as String
    if(!date) date = new Date().format('dd.MM.yyyy HH:mm')
    def issue = issueManager.getIssueObject(issueKey)

    def user = userManager.getUser("l000760") // jiraAuthenticationContext.loggedInUser

    JiraWorkflow workflow = workflowManager.getWorkflow(issue);
    int stepId = workflow.getLinkedStep(issue.statusObject.genericValue).id
    def stepDescriptor = workflow.descriptor.getStep(stepId)
    def action = stepDescriptor.actions.find {it.name == "Update Read Only Fields"}
    log.warn(action.dump())

 	def prodDateActual = customFieldManager.getCustomFieldObjects(issue).find{it.fieldName == "PROD Date Actual"}
	IssueService issueService = getComponent(IssueService.class)
    IssueInputParameters issueInputParameters = new IssueInputParametersImpl([:])
    issueInputParameters.addCustomFieldValue(prodDateActual.idAsLong, date) 
    IssueService.TransitionValidationResult validationResult = issueService.validateTransition(user, issue.id, action.id , issueInputParameters)
    def errorCollection = validationResult.errorCollection

    if (! errorCollection.hasAnyErrors()) {
        issueService.transition(user, validationResult)
        def builder = new groovy.json.JsonBuilder()
		builder { key issue.key } 
    	return Response.status(200).entity(builder.toString()).build()
	}
    else {
      log.error(errorCollection.dump())
       return Response.status(500).entity(errorCollection.dump()).build()
    }   
}

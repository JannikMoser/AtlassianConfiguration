// REJECT


import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.label.LabelManager
import com.atlassian.sal.api.ApplicationProperties
import com.onresolve.scriptrunner.runner.ScriptRunnerImpl
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.transform.BaseScript
import static com.atlassian.jira.component.ComponentAccessor.*
import javax.ws.rs.core.MultivaluedMap
// import com.atlassian.jira.issue.util.IssueChangeHolder
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import javax.ws.rs.core.Response
import com.atlassian.jira.issue.index.IssueIndexingService
import static com.atlassian.jira.issue.history.ChangeLogUtils.*
import com.atlassian.jira.issue.history.*
import com.atlassian.jira.bc.issue.* 
import com.atlassian.jira.issue.*
import com.atlassian.jira.*
import java.text.SimpleDateFormat
    
@BaseScript CustomEndpointDelegate delegate

def applicationProperties = ScriptRunnerImpl.getOsgiService(ApplicationProperties)

reject(httpMethod: "GET") { MultivaluedMap queryParams ->

    def issueId = queryParams.getFirst("issueId") as Long
    def env = queryParams.getFirst("env") as String
    def issue = issueManager.getIssueObject(issueId)
    def customFields = customFieldManager.getCustomFieldObjects(issue)
    customFields.find({it.fieldName == "$env rejected by"}).createValue(issue, jiraAuthenticationContext.loggedInUser)
    customFields.find({it.fieldName == "$env rejected on"}).createValue(issue, new java.sql.Timestamp(System.currentTimeMillis()))

    // Set status
	// reject: Test in progress (871) / Int (811)  =>  Development in Progress (851)
	def transitionsIDs = ['TEST': 851, 'INT':851]  
    
	IssueService issueService = getComponent(IssueService.class)
    IssueInputParameters issueInputParameters = new IssueInputParametersImpl([:])
    IssueService.TransitionValidationResult validationResult = issueService
         .validateTransition(jiraAuthenticationContext.loggedInUser, issue.id, transitionsIDs[env] as Integer, issueInputParameters)
    def errorCollection = validationResult.errorCollection

    if (! errorCollection.hasAnyErrors()) {
        issueService.transition(jiraAuthenticationContext.loggedInUser, validationResult)
    }
    else {
      // log
    }
    
    /*
    ChangeItemBean changeItemBean = new ChangeItemBean(ChangeItemBean.CUSTOM_FIELD ,"$env rejected","","rejected");
    //[2017.08.03: sa]
    // createChangeGroup(jiraAuthenticationContext.loggedInUser, issue.getGenericValue(), issue.getGenericValue(), [changeItemBean], true);
    createChangeGroup(jiraAuthenticationContext.loggedInUser, issue, issue, [changeItemBean], true);
    */
    DefaultIssueChangeHolder issueChangeHolder = new DefaultIssueChangeHolder();
	
	// Add 'Status'
	ChangeItemBean changeItemBean = new ChangeItemBean(ChangeItemBean.CUSTOM_FIELD ,"Status", issue.status.name + "[" + issue.status.id + "]", issue.status.name + "[" + issue.status.id + "]" );	
	issueChangeHolder.addChangeItem(changeItemBean);
	
	// Add 'Rejected By'
    changeItemBean = new ChangeItemBean(ChangeItemBean.CUSTOM_FIELD ,"$env rejected by", "", jiraAuthenticationContext.loggedInUser?.displayName + "[" + jiraAuthenticationContext.loggedInUser?.key + "]" );	
	issueChangeHolder.addChangeItem(changeItemBean);

	// Add 'Rejected On'
	changeItemBean = new ChangeItemBean(ChangeItemBean.CUSTOM_FIELD ,"$env rejected-on", "", (new SimpleDateFormat("dd.MM.yyyy hh:mm")).format( new java.sql.Timestamp(System.currentTimeMillis())));	
	issueChangeHolder.addChangeItem(changeItemBean);

	// Group changed items	
	createChangeGroup(jiraAuthenticationContext.loggedInUser, issue, issue, issueChangeHolder.getChangeItems().each{it}, true);
    
    // Re-Index changed items to issue
    ComponentAccessor.getComponent(IssueIndexingService.class).reIndex(issue) 
    
    // Send response
    Response.temporaryRedirect(URI.create("${applicationProperties.baseUrl}/browse/${issue.key}")).build()
}

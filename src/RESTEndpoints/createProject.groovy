import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.json.JsonBuilder
import groovy.transform.BaseScript

import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

import com.atlassian.jira.component.ComponentAccessor
import com.onresolve.scriptrunner.canned.jira.admin.CopyProject
import org.apache.log4j.Logger

import com.atlassian.jira.project.Project
import com.atlassian.jira.project.ProjectManager
import com.atlassian.jira.security.roles.*
import com.atlassian.jira.bc.projectroles.ProjectRoleService
import static com.atlassian.sal.api.component.ComponentLocator.getComponent
import com.atlassian.jira.user.util.*
import com.atlassian.jira.util.SimpleErrorCollection
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpRequest;
import org.apache.http.protocol.HttpContext;
import com.atlassian.jira.user.ApplicationUser;
import com.onresolve.scriptrunner.runner.customisers.PluginModuleCompilationCustomiser
import com.atlassian.greenhopper.model.rapid.RapidView
import com.atlassian.greenhopper.service.rapid.view.RapidViewService
import com.atlassian.greenhopper.service.PageRequests
import com.atlassian.greenhopper.service.rapid.view.RapidViewQuery
import com.atlassian.greenhopper.model.rapid.BoardAdmin
import com.atlassian.greenhopper.service.rapid.view.BoardAdminService
import com.atlassian.jira.sharing.*
import com.atlassian.jira.sharing.rights.ShareRights
import com.atlassian.jira.sharing.SharedEntity.SharePermissions
import com.atlassian.jira.bc.filter.*
import com.atlassian.jira.issue.search.*


private Response status(int code, String message) {
	Response.status(code).header("Content-Type", "text/plain").entity("$code : $message" as String).build()
}

private Response ok(JsonBuilder jsonb) {
	Response.ok(jsonb.toString()).header("Content-Type", "application/json").build()
}


@BaseScript CustomEndpointDelegate delegate
createProject(httpMethod: "GET", groups: ["jira-users"]) { MultivaluedMap queryParams, String body ->
		   String name = queryParams.getFirst('name')
		String key = queryParams.getFirst('key')
		if(!(name && key)) {
			return status(400, "name and key parameters are mandatory")
		}
		
		ApplicationUser requestor = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
		def copyProject = new CopyProject()

		def inputs = [
			(CopyProject.FIELD_SOURCE_PROJECT) : 'TEMPS',
			(CopyProject.FIELD_TARGET_PROJECT) : key,
			(CopyProject.FIELD_TARGET_PROJECT_NAME) : name,
			(CopyProject.FIELD_COPY_VERSIONS) : "true",
			(CopyProject.FIELD_COPY_COMPONENTS) : "true",
			(CopyProject.FIELD_COPY_ISSUES) : "true",
			//(CopyProject.FIELD_COPY_GREENHOPPER) : true,
			(CopyProject.FIELD_COPY_DASH_AND_FILTERS) : "false",
			(CopyProject.FIELD_CLONE_BOARD_NAME) : "Simple Template",
			(CopyProject.FIELD_TARGET_BOARD_NAME) : name,
			// (CopyProject.FIELD_ORDER_BY) : "Rank",
		]

		def errorCollection = copyProject.doValidate(inputs, false)
		if(errorCollection.hasAnyErrors()) {
			String message = "Couldn't create project: $errorCollection"
			log.warn(message)
			return status(500, message)
		}
		else {
			ApplicationUser admin = getAdmin()
			ComponentAccessor.getJiraAuthenticationContext().setLoggedInUser(admin)
			copyProject.doScript(inputs)
			Long boardId = addAdmin(requestor, admin, name, key)
			return ok(new JsonBuilder([boardId : boardId]))
		}


	
	
}

private ApplicationUser getAdmin(){
	return getComponent(UserManager.class).getUserByName('admin')
	def util = ComponentAccessor.getUserUtil()
	def adminsGroup = util.getGroupObject("jira-administrators")
	assert adminsGroup // must have jira-administrators group defined
	def admins = util.getAllUsersInGroups([adminsGroup])
	assert admins // must have at least one admin
	util.getUserByName(admins.first().name)
}

RapidView addBoardAdmin(String boardName, ApplicationUser user,ApplicationUser admin) {
	RapidViewService rvs = PluginModuleCompilationCustomiser.getGreenHopperBean(RapidViewService)
	BoardAdminService bas = PluginModuleCompilationCustomiser.getGreenHopperBean(BoardAdminService)
	RapidView board = rvs.getRapidViews(user, PageRequests.all(), RapidViewQuery.builder().partialName(boardName).build()).value.values.find{it.name == boardName}
		//rvs.findRapidViewsByName(admin, boardName).value.find{it.name == boardName}
	List<BoardAdmin> admins = bas.getBoardAdmins(board).collect {
		BoardAdmin.builder().key(it.key).type(it.type).build()
	}
	if(!admins.find{it.key==user.key}) {
		admins += new BoardAdmin.RapidViewBoardAdminBuilder().key(user.key).type(com.atlassian.greenhopper.model.rapid.BoardAdmin.Type.USER).build()
		bas.updateBoardAdmins(board, admin, admins)
	}
	return board
}

def allowEditingFilter(Long filterID, ApplicationUser admin) {
	def searchRequestManager = ComponentAccessor.getComponent(SearchRequestManager)
	SearchRequest filter = searchRequestManager.getSearchRequestById(admin, filterID);
	SharePermissions withEdit = new SharePermissions(filter.permissions.collect{
		new SharePermissionImpl(it.type, it.param1 ,it.param2, ShareRights.VIEW_EDIT )
	} as Set )

	filter.setPermissions(withEdit)
	searchRequestManager.update(admin,filter)
}

Long addAdmin(ApplicationUser requestor,ApplicationUser admin, String name, String key){
  
	ProjectManager proMan =  getComponent(ProjectManager.class)
	Project proj = proMan.getProjectObjByKey(key)

	proMan.updateProject(proj, proj.name, proj.description, requestor.key, proj.url, proj.assigneeType)
	
	ProjectRoleManager  roleMan =  getComponent(ProjectRoleManager.class)
	ProjectRole padmins = roleMan.getProjectRole("Administrators")
	ProjectRoleService projectRoleService = getComponent(ProjectRoleService)

	SimpleErrorCollection errorCollection = new SimpleErrorCollection()
	projectRoleService.addActorsToProjectRole([requestor.name],padmins,proj,com.atlassian.jira.security.roles.ProjectRoleActor.USER_ROLE_ACTOR_TYPE,errorCollection)
	if(errorCollection.hasAnyErrors()) {
			log.warn("$errorCollection")
	}
	RapidView board = addBoardAdmin(name, requestor, admin)
	allowEditingFilter(board.savedFilterId, admin)
	return board.id
}

import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.json.JsonBuilder
import groovy.transform.BaseScript

import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response
import javax.servlet.http.HttpServletRequest

@BaseScript CustomEndpointDelegate delegate

private Map paths(HttpServletRequest request) {
	File scriptsFolder = new File('/opt/atlassian/jira-data/scripts')
	def path = request.requestURI.split('/serverScript/',2)
	path = path.length > 1 &&  path[1] ? new File(scriptsFolder, path[1]) : false
	[scriptsFolder : scriptsFolder, script : path ]
}

private Response status(int code, String message) {
	Response.status(code).header("Content-Type", "text/plain").entity("$code : $message" as String).build()
}

private Response ok(String text) {
	Response.ok(text).header("Content-Type", "text/plain").build()
}

private Response list(File folder) {
	Response.ok(new JsonBuilder(folder.listFiles().collect{
			File f -> [name: f.name, lastModified : new Date(f.lastModified()).format("dd.MM.yyyy HH:mm")]
		}).toString()).build()
}

serverScript(httpMethod: "GET", groups: ["jira-administrators"]) { MultivaluedMap queryParams, String body, HttpServletRequest request ->
    Map paths =  paths(request) 
    if(paths.script) {
        if(paths.script.exists())
			return  paths.script.file ? ok(paths.script.text) : list(paths.script)
		else 
			return  status(404, 'not found')
    } else {
        return list(paths.scriptsFolder)
    }
}

serverScript(httpMethod: "DELETE", groups: ["jira-administrators"]) { MultivaluedMap queryParams, String body, HttpServletRequest request ->
	Map paths =  paths(request) 
	if(paths.script) {
		return  paths.script.exists() ? ok(''+paths.script.delete()): status(404, 'not found')
	} else {
		return status(403, 'forbidden')
	}
}

serverScript(httpMethod: "PUT", groups: ["jira-administrators"]) { MultivaluedMap queryParams, String body, HttpServletRequest request ->
	Map paths =  paths(request)
	if(paths.script) {
		if(!paths.script.name.endsWith('.groovy')) return status(400, 'only groovy script are permitted')
		paths.script.parentFile.mkdirs()
		paths.script.text = body
		return Response.ok().build()		
	} else {
		return status(403, 'forbidden')
	}
}

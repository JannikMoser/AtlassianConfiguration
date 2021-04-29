import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.json.JsonBuilder
import groovy.transform.BaseScript

import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response
import javax.servlet.http.HttpServletRequest

@BaseScript CustomEndpointDelegate delegate

localhostOnly(httpMethod: "GET") { MultivaluedMap queryParams, String body, HttpServletRequest request ->
    if(request.remoteHost != '127.0.0.1') return Response.status(403).build()
    return Response.ok(new JsonBuilder([iam: InetAddress.localHost.hostAddress]).toString()).build();
}

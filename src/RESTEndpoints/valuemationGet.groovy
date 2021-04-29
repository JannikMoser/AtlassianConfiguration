import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.json.JsonBuilder
import groovy.transform.BaseScript

import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

@BaseScript CustomEndpointDelegate delegate

valuemationGet(httpMethod: "GET") { MultivaluedMap queryParams, String body ->
	//log.setLevel(org.apache.log4j.Level.DEBUG)
	def pageRequest = queryParams['last'] ? 'next'                 : 'first'
	def last        = queryParams['last'] ? queryParams['last'][0] : ''
	def query = queryParams['q'][0].trim() ? queryParams['q'][0] : ""
	def parts = [
					"Category" :	"""
                    					{
                                            "field": "parentCategory.category",
                                            "operator": "==",
                                            "value": "Standard Change"
                                        },{
                                            "logicalOperator": "and"
                                        },{
                                            "field": "${queryParams['vFieldName'][0]}",
                                            "operator": "like",
                                            "toUpper":"true",
                                            "value": "%${query}%"
                                        }
    						  		""",
					"Services" :		"""
                    					{
                                            "field": "serviceType.serviceClass.serviceclass",
                                            "operator": "in",
                                            "value": [
                                                "Business Service",
                                                "Technical Service",
                                                "External Service"
                                            ]
                                        },
                                        {
                                            "logicalOperator": "and"
                                        },
                                        {
                                            "field": "status",
                                            "operator": "in",
                                            "value": [
                                                "Approved",
                                                "In Review",
                                                "Released"
                                            ]
                                        },
                                        {
                                            "logicalOperator": "and"
                                        },
                                        {
                                            "field": "isServiceTemplate",
                                            "operator": "==",
                                            "value": "false"
                                        },
                                        {
                                            "logicalOperator": "and"
                                        },
                                        {
                                            "field": "${queryParams['vFieldName'][0]}",
                                            "operator": "like",
                                            "toUpper":"true",
                                            "value": "%${query}%"
                                        }
    						  		""",
					"System" :		"""
                    					{
                                            "field": "${queryParams['vFieldName'][0]}",
                                            "operator": "like",
                                            "value": "%${query}%"
                                        }  
    						  		""",
					"SBU" :			"""           
                                        {
                                            "field": "validto",
                                            "operator": ">",
                                            "value": "${new Date().format('yyyy-MM-dd')}"
                                        },
                                        {
                                            "logicalOperator": "and"
                                        },
                                        {
                                            "field": "${queryParams['vFieldName'][0]}",
                                            "operator": "like",
                                            "toUpper":"true",
                                            "value": "%${query}%"
                                        }
    						  		"""
				]

	def json = """
                {
                    "accessToken": "B@lo!seREST",
                    "username": "L004189",
                    "password": "D02F186F71C435D744D546FC79BAF795A916C372371CC3E8E9CF2043B290358D",
                    "encrypted": "Y",
                    "service": "GetObjects",
                    "params": {
                        "types": [
                            {
                                "type": "${queryParams['vType'][0]}",
                                "fields": [
                                    {
                                        "name": "${queryParams['vFieldName'][0]}"
                                    }
                                ],
                                "seekRead":{
                                    "pageRequest":"${pageRequest}",
                                    "last": "${last}",
                                    "pageSize":"10"
                                },
                                "condition": {
                                    "buildCondition": "manual",
                                    "parts": [
                                        ${parts[queryParams['vType'][0]]}
                                    ]
                                }
                            }
                        ]
                    }
                }
"""
	log.debug(json)
	// def url = "https://valuemation.baloisenet.com/vmweb/services/workflowExecutionRESTService/runsubworkflowservice/runsubworkflow"
	//	def url = "https://int-valuemation.baloisenet.com/vmweb/services/workflowExecutionRESTService/runsubworkflowservice/runsubworkflow"
	
	def baseurl = com.atlassian.jira.component.ComponentAccessor.getApplicationProperties().getString("jira.baseurl")
	String valuemationServer = ' '
	if ( baseurl == 'https://jira.baloisenet.com/atlassian' ) {valuemationServer =  'https://valuemation.baloisenet.com/vmweb'}  
	else { valuemationServer =  'https://int-valuemation.baloisenet.com/vmweb'}
	def url = "$valuemationServer/services/workflowExecutionRESTService/runsubworkflowservice/runsubworkflow"

	
	HttpURLConnection con = new URL(url).openConnection() as HttpURLConnection
	con.setRequestProperty("Content-Type", "application/json")
	con.doOutput = true
	con.outputStream << json
	
	return Response
		.status(con.responseCode)
		.entity(con.inputStream.text)
		.header("Access-Control-Allow-Origin", "*")
		.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
		.header("Access-Control-Allow-Credentials", "true")
		.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
		.header("Access-Control-Max-Age", "1209600")
		.build();
}

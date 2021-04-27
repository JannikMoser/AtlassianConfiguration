import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.json.JsonBuilder
import groovy.transform.BaseScript

import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.search.SearchProvider
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.web.bean.PagerFilter
import static com.atlassian.jira.component.ComponentAccessor.*
import com.atlassian.jira.issue.index.IssueIndexingService

@BaseScript CustomEndpointDelegate delegate

reIndex(httpMethod: "GET", groups: ["jira-administrators"]) { MultivaluedMap queryParams, String body ->
    def start = System.currentTimeMillis()
    def jqlQueryParser = ComponentAccessor.getComponent(JqlQueryParser.class)
    def searchProvider = ComponentAccessor.getComponent(SearchProvider.class)
    def issueManager = ComponentAccessor.getIssueManager()
    def user = ComponentAccessor.getJiraAuthenticationContext().getUser()
     
    
    def query = jqlQueryParser.parseQuery(queryParams['jql'])
    def results = searchProvider.search(query, user, PagerFilter.getUnlimitedFilter())
    log.info(results.total +" issues to index")
    def ids = []
    IssueIndexingService issueIndexService = ComponentAccessor.getComponent(IssueIndexingService.class);
    results.issues.each {
        def issue = issueManager.getIssueObject(it.id)
        issueIndexService.reIndex(issue)  
        ids << issue.key
        
    }
    log.info("indexing "+results.total+" issues took "+(System.currentTimeMillis()-start)+" msec")
    return Response.ok(new JsonBuilder([jql: queryParams['jql']
                                        //,q : query
                                        ,total : results.total, ids : ids, msec : (System.currentTimeMillis()- start)
                                       ]).toString()).build();
}

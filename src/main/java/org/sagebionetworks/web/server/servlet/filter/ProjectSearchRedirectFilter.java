package org.sagebionetworks.web.server.servlet.filter;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sagebionetworks.repo.model.entity.query.Condition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldName;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.entity.query.EntityQueryUtils;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.entity.query.Operator;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.server.servlet.UserDataProvider;

/**
 * This filter will attempt to find a matching project, and redirect to that project page
 *
 */
public class ProjectSearchRedirectFilter implements Filter {
	public static final String PROJECT = "/project/"; 
	
	private SynapseClientImpl synapseClient;
	private TokenProvider tokenProvider = new TokenProvider() {
		@Override
		public String getSessionToken() {
			return UserDataProvider.getThreadLocalUserToken(perThreadRequest.get());
		}
	};
	protected static final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();
	
	@Override
	public void destroy() {
		// nothing to do
	}
	
	@Override
	public void doFilter(ServletRequest rqst, ServletResponse rspn,
			FilterChain chain) throws IOException, ServletException {
		try {
			HttpServletRequest httpRqst = (HttpServletRequest)rqst;
			perThreadRequest.set(httpRqst);
			
			URL requestURL = new URL(httpRqst.getRequestURL().toString());
			//use path as the search string, but replace all '_' with spaces
			String searchString = requestURL.getPath().substring(PROJECT.length()).replace('_', ' ');
			
			//do the search
			EntityQuery query = getEntityQuery(searchString);
			EntityQueryResults results = synapseClient.executeEntityQuery(query);
			String newPath = "/";
			if (results.getTotalEntityCount() == 1) {
				EntityQueryResult result = results.getEntities().get(0);
				newPath = "/#!Synapse:" + result.getId();
			}
			URL redirectURL = new URL(requestURL.getProtocol(), requestURL.getHost(), requestURL.getPort(), newPath);
			HttpServletResponse httpRsp = (HttpServletResponse)rspn;
			httpRsp.sendRedirect(httpRsp.encodeRedirectURL(redirectURL.toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public EntityQuery getEntityQuery(String searchString) {
		EntityQuery query = new EntityQuery();
		Condition condition = EntityQueryUtils.buildCondition(
				EntityFieldName.name, Operator.EQUALS, searchString);
		query.setConditions(Arrays.asList(condition));
		query.setFilterByType(EntityType.project);
		query.setLimit(1L);
		query.setOffset(0L);
		return query;
	}
	
	@Override
	public void init(FilterConfig config) throws ServletException {
		synapseClient = new SynapseClientImpl();
		synapseClient.setServiceUrlProvider(new ServiceUrlProvider());
		synapseClient.setTokenProvider(tokenProvider);
	}
}

package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.LogEntry;
import org.sagebionetworks.repo.model.entity.query.Condition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldName;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.entity.query.EntityQueryUtils;
import org.sagebionetworks.repo.model.entity.query.Operator;
import org.sagebionetworks.util.SerializationUtils;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.inject.Inject;

/**
 * Handles file handler uploads.
 *
 * @author jay
 *
 */
public class ProjectAliasServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected static final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();
	
	/**
	 * Injected with Gin
	 */
	@SuppressWarnings("unused")
	private ServiceUrlProvider urlProvider;
	private SynapseProvider synapseProvider = new SynapseProviderImpl();
	private TokenProvider tokenProvider = new TokenProvider() {
		@Override
		public String getSessionToken() {
			return UserDataProvider.getThreadLocalUserToken(ProjectAliasServlet.perThreadRequest.get());
		}
	};

	/**
	 * Unit test can override this.
	 *
	 * @param fileHandleProvider
	 */
	public void setSynapseProvider(SynapseProvider synapseProvider) {
		this.synapseProvider = synapseProvider;
	}

	/**
	 * Essentially the constructor. Setup synapse client.
	 *
	 * @param provider
	 */
	@Inject
	public void setServiceUrlProvider(ServiceUrlProvider provider) {
		this.urlProvider = provider;
	}

	/**
	 * Unit test uses this to provide a mock token provider
	 *
	 * @param tokenProvider
	 */
	public void setTokenProvider(TokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		ProjectAliasServlet.perThreadRequest.set(arg0);
		super.service(arg0, arg1);
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		super.service(arg0, arg1);
	}

	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		//instruct not to cache
		response.setHeader(WebConstants.CACHE_CONTROL_KEY, WebConstants.CACHE_CONTROL_VALUE_NO_CACHE); // Set standard HTTP/1.1 no-cache headers.
		response.setHeader(WebConstants.PRAGMA_KEY, WebConstants.NO_CACHE_VALUE); // Set standard HTTP/1.0 no-cache header.
		response.setDateHeader(WebConstants.EXPIRES_KEY, 0L); // Proxy
		String token = null;
		try {
			token = getSessionToken(request);
		} catch(Throwable e) {
			//unable to get session token, so it's an anonymous request
		}
		HttpServletRequest httpRqst = (HttpServletRequest)request;
		URL requestURL = new URL(httpRqst.getRequestURL().toString());
		try {
			SynapseClient client = createNewClient(token);
			perThreadRequest.set(httpRqst);
			String alias = requestURL.getPath().substring(1);

			EntityQuery query = getEntityQuery(alias);
			EntityQueryResults results = client.entityQuery(query);
			String newPath = "/";
			if (results.getTotalEntityCount() == 1) {
				EntityQueryResult result = results.getEntities().get(0);
				newPath = "/#!Synapse:" + result.getId();
			} else {
				String error = "The requested URL " + requestURL.getPath() + " was not found on this server.";
				if (token == null) {
					error += " \nPlease log in and try again.";
				}
				throw new SynapseNotFoundException(error);
			}
			URL redirectURL = new URL(requestURL.getProtocol(), requestURL.getHost(), requestURL.getPort(), newPath);
			response.sendRedirect(response.encodeRedirectURL(redirectURL.toString()));
		} catch (SynapseException e) {
			//redirect to error place with an entry
			LogEntry entry = new LogEntry();
			entry.setLabel("Sorry");
			entry.setMessage(e.getMessage());
//			entry.setStacktrace(ExceptionUtils.getStackTrace(e));
			String entryString = SerializationUtils.serializeAndHexEncode(entry);
			response.sendRedirect(new URL(requestURL.getProtocol(), requestURL.getHost(), requestURL.getPort(), "/#!Error:"+entryString).toString());
		}
	}
		
	/**
	 * Get the session token
	 * @param request
	 * @return
	 */
	public String getSessionToken(final HttpServletRequest request){
		return tokenProvider.getSessionToken();
	}

	/**
	 * Create a new Synapse client.
	 *
	 * @return
	 */
	private SynapseClient createNewClient(String sessionToken) {
		SynapseClient client = synapseProvider.createNewClient();
		client.setAuthEndpoint(urlProvider.getPrivateAuthBaseUrl());
		client.setRepositoryEndpoint(urlProvider.getRepositoryServiceUrl());
		client.setFileEndpoint(StackConfiguration.getFileServiceEndpoint());
		if (sessionToken != null)
			client.setSessionToken(sessionToken);
		return client;
	}

	public String getBaseUrl(HttpServletRequest request) {
		StringBuffer url = request.getRequestURL();
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		String base = url.substring(0, url.length() - uri.length() + ctx.length()) + "/";
		return base;
	}
	
	/**
	 * Uses the Project Alias feature
	 * @param alias
	 * @return
	 */
	public EntityQuery getEntityQuery(String alias) {
		EntityQuery query = new EntityQuery();
		Condition condition = EntityQueryUtils.buildCondition(
				EntityFieldName.alias, Operator.EQUALS, alias);
		query.setConditions(Arrays.asList(condition));
		query.setFilterByType(EntityType.project);
		query.setLimit(1L);
		query.setOffset(0L);
		return query;
	}

}
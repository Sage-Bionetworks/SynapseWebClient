package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.StackEndpoints;
import org.sagebionetworks.web.shared.WebConstants;

/**
 * Handles given an alias, will redirect to the profile or team page assocated with that alias
 *
 * @author jay
 *
 */
public class AliasRedirectorServlet extends HttpServlet {

	public static final String TEAM_PLACE = "/#!Team:";

	public static final String PROFILE_PLACE = "/#!Profile:";

	private static final long serialVersionUID = 1L;

	protected static final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();

	/**
	 * Injected with Gin
	 */
	private SynapseProvider synapseProvider = new SynapseProviderImpl();

	/**
	 * Unit test can override this.
	 *
	 * @param fileHandleProvider
	 */
	public void setSynapseProvider(SynapseProvider synapseProvider) {
		this.synapseProvider = synapseProvider;
	}

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		AliasRedirectorServlet.perThreadRequest.set(arg0);
		super.service(arg0, arg1);
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {
		super.service(arg0, arg1);
	}


	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// instruct not to cache
		response.setHeader(WebConstants.CACHE_CONTROL_KEY, WebConstants.CACHE_CONTROL_VALUE_NO_CACHE); // Set standard HTTP/1.1 no-cache headers.
		response.setHeader(WebConstants.PRAGMA_KEY, WebConstants.NO_CACHE_VALUE); // Set standard HTTP/1.0 no-cache header.
		response.setDateHeader(WebConstants.EXPIRES_KEY, 0L); // Proxy
		HttpServletRequest httpRqst = (HttpServletRequest) request;
		URL requestURL = new URL(httpRqst.getRequestURL().toString());
		try {
			String alias = httpRqst.getParameter(WebConstants.ALIAS_PARAM_KEY);
			SynapseClient client = createNewClient();
			perThreadRequest.set(httpRqst);

			// use new service call to resolve
			List<UserGroupHeader> ughList = client.getUserGroupHeadersByAliases(Collections.singletonList(alias));
			if (!ughList.isEmpty()) {
				UserGroupHeader ugh = ughList.get(0);
				String place = ugh.getIsIndividual() ? PROFILE_PLACE : TEAM_PLACE;
				StringBuilder newPathBuilder = new StringBuilder();
				newPathBuilder.append(place);
				newPathBuilder.append(ugh.getOwnerId());
				String newPath = newPathBuilder.toString();
				URL redirectURL = new URL(requestURL.getProtocol(), requestURL.getHost(), requestURL.getPort(), newPath);
				String encodedRedirectURL = response.encodeRedirectURL(redirectURL.toString());
				response.sendRedirect(encodedRedirectURL);
			}
		} catch (Exception e) {
			// redirect to error place
			response.sendRedirect(FileHandleAssociationServlet.getBaseUrl(request) + FileHandleAssociationServlet.ERROR_PLACE + URLEncoder.encode(e.getMessage()));
		}
	}

	private SynapseClient createNewClient() {
		SynapseClient client = synapseProvider.createNewClient();
		client.setAuthEndpoint(StackEndpoints.getAuthenticationServicePublicEndpoint());
		client.setRepositoryEndpoint(StackEndpoints.getRepositoryServiceEndpoint());
		client.setFileEndpoint(StackEndpoints.getFileServiceEndpoint());
		return client;
	}

}

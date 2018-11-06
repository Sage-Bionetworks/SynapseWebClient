package org.sagebionetworks.web.server.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.web.client.StackEndpoints;
import static org.sagebionetworks.web.client.cookie.CookieKeys.*;
import org.sagebionetworks.web.shared.WebConstants;

/**
 * Servlet for setting the session token HttpOnly cookie.
 */
public class InitSessionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected static final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();
	
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
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		InitSessionServlet.perThreadRequest.set(arg0);
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
		// return the Set-Cookie response with the session token
		try {
			String sessionToken = request.getParameter(WebConstants.SESSION_TOKEN_KEY);
			if (sessionToken == null) {
				sessionToken = "";
			}
			if (sessionToken.isEmpty()) {
				// validate session token is valid
				createNewClient(sessionToken).getUserSessionData();
			}
			boolean isSecure = Boolean.parseBoolean(request.getParameter(WebConstants.IS_SECURE_COOKIE_KEY));
			String secureString = isSecure ? " Secure;" : ""; 
			response.setHeader("Set-Cookie", USER_LOGIN_TOKEN + "="+sessionToken+"; Domain=; HttpOnly; " + secureString);
		} catch (SynapseException e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getOutputStream().write("Invalid session token".getBytes("UTF-8"));
			response.getOutputStream().flush();
		}
	}
	
	/**
	 * Create a new Synapse client.
	 *
	 * @return
	 */
	private SynapseClient createNewClient(String sessionToken) {
		SynapseClient client = synapseProvider.createNewClient();
		client.setAuthEndpoint(StackEndpoints.getAuthenticationServicePublicEndpoint());
		client.setRepositoryEndpoint(StackEndpoints.getRepositoryServiceEndpoint());
		client.setFileEndpoint(StackEndpoints.getFileServiceEndpoint());
		if (sessionToken != null)
			client.setSessionToken(sessionToken);
		return client;
	}

	@Override
	public void doPost(final HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {}
}

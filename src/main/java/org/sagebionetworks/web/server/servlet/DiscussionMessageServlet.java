package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.web.client.StackEndpoints;
import org.sagebionetworks.web.shared.WebConstants;

/**
 * Handle downloading discussion messages.
 */
public class DiscussionMessageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected static final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();

	private SynapseProvider synapseProvider = new SynapseProviderImpl();
	private TokenProvider tokenProvider = new TokenProvider() {
		@Override
		public String getSessionToken() {
			return UserDataProvider.getThreadLocalUserToken(DiscussionMessageServlet.perThreadRequest.get());
		}
	};

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		DiscussionMessageServlet.perThreadRequest.set(arg0);
		super.service(arg0, arg1);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String token = tokenProvider.getSessionToken();
		SynapseClient client = createNewClient(token);

		String messageKey = request.getParameter(WebConstants.MESSAGE_KEY_PARAM);
		String type = request.getParameter(WebConstants.TYPE_PARAM);

		try {
			URL resolvedUrl = null;
			if (type.equals(WebConstants.THREAD_TYPE)) {
				resolvedUrl = client.getThreadUrl(messageKey);
			} else if (type.equals(WebConstants.REPLY_TYPE)) {
				resolvedUrl = client.getReplyUrl(messageKey);
			} else {
				throw new IllegalArgumentException("Do not support type " + type);
			}
			response.sendRedirect(resolvedUrl.toString());
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
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
}

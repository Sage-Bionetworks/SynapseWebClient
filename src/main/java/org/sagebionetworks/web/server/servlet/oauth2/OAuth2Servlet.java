package org.sagebionetworks.web.server.servlet.oauth2;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseServerException;
import org.sagebionetworks.client.exceptions.SynapseServiceUnavailable;
import org.sagebionetworks.repo.model.oauth.OAuthProvider;
import org.sagebionetworks.repo.model.oauth.OAuthUrlRequest;
import org.sagebionetworks.repo.model.oauth.OAuthUrlResponse;
import org.sagebionetworks.web.client.StackEndpoints;
import org.sagebionetworks.web.server.servlet.FileHandleAssociationServlet;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.SynapseProviderImpl;
import org.sagebionetworks.web.shared.WebConstants;

public abstract class OAuth2Servlet extends HttpServlet {
	private SynapseProvider synapseProvider = new SynapseProviderImpl();

	/**
	 * Injected
	 * 
	 * @param synapseProvider
	 */
	public void setSynapseProvider(SynapseProvider synapseProvider) {
		this.synapseProvider = synapseProvider;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public abstract void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;

	/**
	 * Create a redirect URL.
	 * 
	 * @param request
	 * @param provider
	 * @return
	 */
	public String createRedirectUrl(HttpServletRequest request, OAuthProvider provider) {
		return request.getRequestURL().toString() + "?" + WebConstants.OAUTH2_PROVIDER + "=" + provider.name();
	}

	/**
	 * Step one, send the user to the OAuth provider for authentication.
	 * 
	 * @param req
	 * @param resp
	 * @param provider
	 * @throws IOException
	 */
	public void redirectToProvider(HttpServletRequest req, HttpServletResponse resp, OAuthProvider provider, String redirectUrl, String state) throws IOException {
		HttpServletRequest httpRqst = (HttpServletRequest) req;
		URL requestURL = new URL(httpRqst.getRequestURL().toString());

		try {
			SynapseClient client = createSynapseClient();
			OAuthUrlRequest request = new OAuthUrlRequest();
			request.setProvider(provider);
			request.setRedirectUrl(redirectUrl);
			if (state != null && !state.isEmpty()) {
				state = URLDecoder.decode(state);
				request.setState(state);
			}
			OAuthUrlResponse respone = client.getOAuth2AuthenticationUrl(request);
			resp.sendRedirect(respone.getAuthorizationUrl());
		} catch (SynapseServerException e) {
			if (e instanceof SynapseServiceUnavailable) {
				resp.sendRedirect(new URL(requestURL.getProtocol(), requestURL.getHost(), requestURL.getPort(), "/#!Down:0").toString());
			} else {
				sendRedirectToError(req, e, resp);
			}
		} catch (SynapseException e) {
			// 400 error
			sendRedirectToError(req, e, resp);
		}
	}

	private void sendRedirectToError(HttpServletRequest request, Exception e, HttpServletResponse resp) throws MalformedURLException, IOException {
		resp.sendRedirect(FileHandleAssociationServlet.getBaseUrl(request) + FileHandleAssociationServlet.ERROR_PLACE + URLEncoder.encode(e.getMessage()));
	}

	/**
	 * Creates a Synapse client that can only make anonymous calls
	 */
	protected SynapseClient createSynapseClient() {
		return createSynapseClient(null);
	}

	/**
	 * Creates a Synapse client
	 */
	protected SynapseClient createSynapseClient(String sessionToken) {
		SynapseClient synapseClient = synapseProvider.createNewClient();
		if (sessionToken != null) {
			synapseClient.setSessionToken(sessionToken);
		}
		synapseClient.setAuthEndpoint(StackEndpoints.getAuthenticationServicePublicEndpoint());
		return synapseClient;
	}
}

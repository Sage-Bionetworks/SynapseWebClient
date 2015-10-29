package org.sagebionetworks.web.server.servlet.oauth2;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseServerException;
import org.sagebionetworks.repo.model.oauth.OAuthProvider;
import org.sagebionetworks.repo.model.oauth.OAuthUrlRequest;
import org.sagebionetworks.repo.model.oauth.OAuthUrlResponse;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.SynapseProviderImpl;
import org.sagebionetworks.web.shared.WebConstants;
import org.springframework.http.HttpStatus;

import com.google.inject.Inject;

public abstract class OAuth2Servlet extends HttpServlet {
	private ServiceUrlProvider urlProvider;
	private SynapseProvider synapseProvider = new SynapseProviderImpl();

	/**
	 * Injected with Gin
	 */
	@Inject
	public void setUrlProvider(ServiceUrlProvider urlProvider) {
		this.urlProvider = urlProvider;
	}

	/**
	 * Injected
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
	public abstract void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException;
	
	/**
	 * Create a redirect URL.
	 * @param request
	 * @param provider
	 * @return
	 */
	public String createRedirectUrl(HttpServletRequest request, OAuthProvider provider){
		return request.getRequestURL().toString()+"?"+WebConstants.OAUTH2_PROVIDER+"="+provider.name();
	}

	/**
	 * Step one, send the user to the OAuth provider for authentication.
	 * @param req
	 * @param resp
	 * @param provider
	 * @throws IOException
	 */
	public void redirectToProvider(HttpServletRequest req,
			HttpServletResponse resp, OAuthProvider provider, String redirectUrl)
			throws IOException {
		try {
			SynapseClient client = createSynapseClient();
			OAuthUrlRequest request = new OAuthUrlRequest();
			request.setProvider(provider);
			request.setRedirectUrl(redirectUrl);
			OAuthUrlResponse respone = client.getOAuth2AuthenticationUrl(request);
			resp.sendRedirect(respone.getAuthorizationUrl());
		} catch (SynapseServerException e) {
			resp.setStatus(e.getStatusCode());
			resp.getWriter().println("{\"reason\":\"" + e.getMessage() + "\"}");
		}catch (SynapseException e) {
			// 400 error
			resp.setStatus(HttpStatus.BAD_REQUEST.value());
			resp.getWriter().println("{\"reason\":\"" + e.getMessage() + "\"}");
		}
	}

	/**
	 * Creates a Synapse client that can only make anonymous calls
	 */
	protected SynapseClient createSynapseClient() {
		SynapseClient synapseClient = synapseProvider.createNewClient();
		synapseClient.setAuthEndpoint(urlProvider.getPrivateAuthBaseUrl());
		return synapseClient;
	}
}

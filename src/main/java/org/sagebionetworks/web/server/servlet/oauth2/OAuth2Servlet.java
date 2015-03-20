package org.sagebionetworks.web.server.servlet.oauth2;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseForbiddenException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.oauth.OAuthProvider;
import org.sagebionetworks.repo.model.oauth.OAuthUrlRequest;
import org.sagebionetworks.repo.model.oauth.OAuthUrlResponse;
import org.sagebionetworks.repo.model.oauth.OAuthValidationRequest;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.SynapseProviderImpl;
import org.sagebionetworks.web.shared.WebConstants;
import org.springframework.http.HttpStatus;

import com.google.inject.Inject;

public class OAuth2Servlet extends HttpServlet {
	
	/**
	 * Injected with Gin
	 */
	@Inject
	private ServiceUrlProvider urlProvider;
	private SynapseProvider synapseProvider = new SynapseProviderImpl();

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String provideString = req.getParameter(WebConstants.OAUTH2_PROVIDER);
		OAuthProvider provider = OAuthProvider.valueOf(provideString);
		// This code will be provided after the user authenticates with a provider.
		String athenticationCode = req.getParameter(WebConstants.OAUTH2_CODE);
		String redirectUrl = createRedirectUrl(req, provider);
		// If we do not have a code 
		if(athenticationCode == null){
			redirectToProvider(req, resp, provider, redirectUrl);
		}else{
			validateUser(resp, provider, athenticationCode, redirectUrl);
		}
		super.doGet(req, resp);
	}
	
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
		} catch (SynapseException e) {
			// 400 error
			resp.setStatus(HttpStatus.BAD_REQUEST.value());
			resp.getWriter().println("{\"reason\":\"" + e.getMessage() + "\"}");
		}
	}

	/**
	 * Step two, use the resulting authentication code to sign-in with Synapse.
	 * @param resp
	 * @param provider
	 * @param athenticationCode
	 * @throws IOException
	 */
	public void validateUser(HttpServletResponse resp, OAuthProvider provider,
			String athenticationCode, String redirectUrl) throws IOException {
		try {
			SynapseClient client = createSynapseClient();
			OAuthValidationRequest request = new OAuthValidationRequest();
			request.setAuthenticationCode(athenticationCode);
			request.setProvider(provider);
			request.setRedirectUrl(redirectUrl);
			Session token = client.validateOAuthAuthenticationCode(request);
			resp.sendRedirect("/#!LoginPlace:"+token.getSessionToken());
		} catch (SynapseNotFoundException e) {
			// Send the user to register
			resp.sendRedirect("/#!RegisterAccount:"+e.getMessage());
		}catch (SynapseForbiddenException e) {
			// 400 error
			resp.setStatus(HttpStatus.FORBIDDEN.value());
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
	private SynapseClient createSynapseClient() {
		SynapseClient synapseClient = synapseProvider.createNewClient();
		synapseClient.setAuthEndpoint(urlProvider.getPrivateAuthBaseUrl());
		return synapseClient;
	}
}

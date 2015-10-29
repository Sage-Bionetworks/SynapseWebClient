package org.sagebionetworks.web.server.servlet.oauth2;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseForbiddenException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.client.exceptions.SynapseServerException;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.oauth.OAuthProvider;
import org.sagebionetworks.repo.model.oauth.OAuthValidationRequest;
import org.sagebionetworks.web.shared.WebConstants;
import org.springframework.http.HttpStatus;

public class OAuth2SessionServlet extends OAuth2Servlet {
	

	private static final String REGISTER_ACCOUNT = "/#!RegisterAccount:";
	private static final String LOGIN_PLACE = "/#!LoginPlace:";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
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
			resp.sendRedirect(LOGIN_PLACE+token.getSessionToken());
		} catch (SynapseNotFoundException e) {
			// Send the user to register
			resp.sendRedirect(REGISTER_ACCOUNT+e.getMessage());
		}catch (SynapseForbiddenException e) {
			resp.setStatus(HttpStatus.FORBIDDEN.value());
			resp.getWriter().println("{\"reason\":\"" + e.getMessage() + "\"}");
		}catch (SynapseServerException e) {
			resp.setStatus(e.getStatusCode());
			resp.getWriter().println("{\"reason\":\"" + e.getMessage() + "\"}");
		}catch (SynapseException e) {
			// 400 error
			resp.setStatus(HttpStatus.BAD_REQUEST.value());
			resp.getWriter().println("{\"reason\":\"" + e.getMessage() + "\"}");
		}
	}
}

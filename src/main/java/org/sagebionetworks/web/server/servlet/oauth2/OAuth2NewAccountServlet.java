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
import org.sagebionetworks.client.exceptions.UnknownSynapseServerException;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.oauth.OAuthProvider;
import org.sagebionetworks.repo.model.oauth.OAuthValidationRequest;
import org.sagebionetworks.web.shared.WebConstants;
import org.springframework.http.HttpStatus;

public class OAuth2NewAccountServlet extends OAuth2Servlet {
	private static final String LOGIN_PLACE = "/#!LoginPlace:";
	private static final String OAUTH2_NEW_ACCOUNT_ERROR = "/#!OAuth2NewAccount:error=";
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String provideString = req.getParameter(WebConstants.OAUTH2_PROVIDER);
		OAuthProvider provider = OAuthProvider.valueOf(provideString);
		// This code will be provided after the user authenticates with a provider.
		String athenticationCode = req.getParameter(WebConstants.OAUTH2_CODE);
		String username = req.getParameter(WebConstants.OPEN_ID_NEW_ACCOUNT_USERNAME);
		String redirectUrl = createRedirectUrl(req, provider);
		// If we do not have a code 
		if(athenticationCode == null){
			redirectToProvider(req, resp, provider, redirectUrl);
		}else{
			createAccountViaOauth(resp, username, provider, athenticationCode, redirectUrl);
		}
	}

	/**
	 * Step two, use the resulting authentication code and username to create a new Synapse user account.
	 * @param resp
	 * @param provider
	 * @param athenticationCode
	 * @throws IOException
	 */
	public void createAccountViaOauth(HttpServletResponse resp, String username, OAuthProvider provider,
			String athenticationCode, String redirectUrl) throws IOException {
		try {
			//TODO: use new service to validate code and set username
			SynapseClient client = createSynapseClient();
			OAuthAccountCreationRequest request = new OAuthAccountCreationRequest();
			request.setAuthenticationCode(athenticationCode);
			request.setProvider(provider);
			request.setRedirectUrl(redirectUrl);
			request.setUsername(username);
			Session token = client.createAccountViaOauth(request);
			resp.sendRedirect(LOGIN_PLACE+token.getSessionToken());
		} catch (Exception e) {
			resp.sendRedirect(OAUTH2_NEW_ACCOUNT_ERROR + e.getMessage());
		};
			
	}
}

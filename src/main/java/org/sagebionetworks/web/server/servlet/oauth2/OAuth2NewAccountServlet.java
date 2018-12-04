package org.sagebionetworks.web.server.servlet.oauth2;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.oauth.OAuthAccountCreationRequest;
import org.sagebionetworks.repo.model.oauth.OAuthProvider;
import org.sagebionetworks.web.shared.WebConstants;

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
			//use new service to validate code and create a new account using the given username (and info from Google)
			SynapseClient client = createSynapseClient();
			OAuthAccountCreationRequest request = new OAuthAccountCreationRequest();
			request.setAuthenticationCode(athenticationCode);
			request.setProvider(provider);
			request.setRedirectUrl(redirectUrl);
			request.setUserName(username);
			Session token = client.createAccountViaOAuth2(request);
			resp.sendRedirect(LOGIN_PLACE+token.getSessionToken());
		} catch (Exception e) {
			resp.sendRedirect(OAUTH2_NEW_ACCOUNT_ERROR + URLEncoder.encode(e.getMessage()));
		};
			
	}
}

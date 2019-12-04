package org.sagebionetworks.web.server.servlet.oauth2;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.oauth.OAuthAccountCreationRequest;
import org.sagebionetworks.repo.model.oauth.OAuthProvider;
import org.sagebionetworks.repo.model.oauth.OAuthValidationRequest;
import org.sagebionetworks.web.server.servlet.FileHandleAssociationServlet;
import org.sagebionetworks.web.shared.WebConstants;

public class OAuth2SessionServlet extends OAuth2Servlet {
	public static final String REGISTER_ACCOUNT = "/#!RegisterAccount:0";
	public static final String LOGIN_PLACE = "/#!LoginPlace:";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String provideString = req.getParameter(WebConstants.OAUTH2_PROVIDER);
		OAuthProvider provider = OAuthProvider.valueOf(provideString);
		// This code will be provided after the user authenticates with a provider.
		String athenticationCode = req.getParameter(WebConstants.OAUTH2_CODE);
		// we're using the OAuth2 state parameter to send the username through the OAuth round-trip
		// (creating a new account).
		String state = req.getParameter(WebConstants.OAUTH2_STATE);
		String redirectUrl = createRedirectUrl(req, provider);
		// If we do not have a code
		if (athenticationCode == null) {
			redirectToProvider(req, resp, provider, redirectUrl, state);
		} else if (state != null && !state.isEmpty()) {
			// create the new account
			createAccountViaOauth(req, resp, URLDecoder.decode(state), provider, athenticationCode, redirectUrl);
		} else {
			validateUser(req, resp, provider, athenticationCode, redirectUrl);
		}
	}

	/**
	 * Step two, use the resulting authentication code to sign-in with Synapse.
	 * 
	 * @param resp
	 * @param provider
	 * @param athenticationCode
	 * @throws IOException
	 */
	public void validateUser(HttpServletRequest req, HttpServletResponse resp, OAuthProvider provider, String athenticationCode, String redirectUrl) throws IOException {
		try {
			SynapseClient client = createSynapseClient();
			OAuthValidationRequest request = new OAuthValidationRequest();
			request.setAuthenticationCode(athenticationCode);
			request.setProvider(provider);
			request.setRedirectUrl(redirectUrl);
			Session token = client.validateOAuthAuthenticationCode(request);
			resp.sendRedirect(LOGIN_PLACE + token.getSessionToken());
		} catch (SynapseNotFoundException e) {
			// used to send the user to register
			resp.sendRedirect(REGISTER_ACCOUNT);
		} catch (SynapseException e) {
			resp.sendRedirect(FileHandleAssociationServlet.getBaseUrl(req) + FileHandleAssociationServlet.ERROR_PLACE + URLEncoder.encode(e.getMessage()));
		}
	}

	/**
	 * Step 2b, use the resulting authentication code and username to create a new Synapse user account.
	 * 
	 * @param resp
	 * @param provider
	 * @param athenticationCode
	 * @throws IOException
	 */
	public void createAccountViaOauth(HttpServletRequest req, HttpServletResponse resp, String username, OAuthProvider provider, String athenticationCode, String redirectUrl) throws IOException {
		try {
			// use new service to validate code and create a new account using the given username (and info from
			// Google)
			SynapseClient client = createSynapseClient();
			OAuthAccountCreationRequest request = new OAuthAccountCreationRequest();
			request.setAuthenticationCode(athenticationCode);
			request.setProvider(provider);
			request.setRedirectUrl(redirectUrl);
			request.setUserName(username);
			Session token = client.createAccountViaOAuth2(request);
			resp.sendRedirect(LOGIN_PLACE + token.getSessionToken());
		} catch (Exception e) {
			resp.sendRedirect(FileHandleAssociationServlet.getBaseUrl(req) + FileHandleAssociationServlet.ERROR_PLACE + URLEncoder.encode(e.getMessage()));
		} ;

	}
}

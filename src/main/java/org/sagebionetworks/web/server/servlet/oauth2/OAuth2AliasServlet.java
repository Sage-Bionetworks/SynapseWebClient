package org.sagebionetworks.web.server.servlet.oauth2;

import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.repo.model.oauth.OAuthProvider;
import org.sagebionetworks.repo.model.oauth.OAuthValidationRequest;
import org.sagebionetworks.repo.model.principal.PrincipalAlias;
import org.sagebionetworks.web.server.servlet.FileHandleAssociationServlet;
import org.sagebionetworks.web.server.servlet.UserDataProvider;
import org.sagebionetworks.web.shared.WebConstants;

public class OAuth2AliasServlet extends OAuth2Servlet {

	private static final String PROFILE_BIND_SUCCESS_PLACE = "/#!Profile:oauth_bound";
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String provideString = req.getParameter(WebConstants.OAUTH2_PROVIDER);
		OAuthProvider provider = OAuthProvider.valueOf(provideString);
		// This code will be provided after the user authenticates with a provider.
		String athenticationCode = req.getParameter(WebConstants.OAUTH2_CODE);
		if (athenticationCode == null) {
			// If we do not have a code
			String redirectUrl = createRedirectUrl(req, provider);
			redirectToProvider(req, resp, provider, redirectUrl, null);
		} else {
			bindOAuthProviderToUser(req, resp, provider, athenticationCode);
		}
	}


	/**
	 * Tie the OAuth provider id to the user.
	 * 
	 * @param req
	 * @param resp
	 * @param provider
	 * @param authorizationCode
	 * @throws IOException
	 */
	public void bindOAuthProviderToUser(HttpServletRequest req, HttpServletResponse resp, OAuthProvider provider, String authorizationCode) throws IOException {
		try {
			String sessionToken = UserDataProvider.getThreadLocalUserToken(req);
			SynapseClient client = createSynapseClient(sessionToken);
			OAuthValidationRequest request = new OAuthValidationRequest();
			request.setProvider(provider);
			request.setAuthenticationCode(authorizationCode);
			PrincipalAlias response = client.bindOAuthProvidersUserId(request);
			resp.sendRedirect(PROFILE_BIND_SUCCESS_PLACE);
		} catch (Exception e) {
			resp.sendRedirect(FileHandleAssociationServlet.getBaseUrl(req) + FileHandleAssociationServlet.ERROR_PLACE + URLEncoder.encode(e.getMessage()));

		}
	}
}

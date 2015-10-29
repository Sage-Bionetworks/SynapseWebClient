package org.sagebionetworks.web.server.servlet.oauth2;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseServerException;
import org.sagebionetworks.repo.model.LogEntry;
import org.sagebionetworks.repo.model.oauth.OAuthProvider;
import org.sagebionetworks.repo.model.oauth.OAuthValidationRequest;
import org.sagebionetworks.repo.model.principal.PrincipalAlias;
import org.sagebionetworks.util.SerializationUtils;
import org.sagebionetworks.web.shared.WebConstants;
import org.springframework.http.HttpStatus;

public class OAuth2AliasServlet extends OAuth2SessionServlet {
	
	private static final String PROFILE_MESSAGE_PLACE = "/#!Profile:message/";
	private static final String ERROR_PLACE = "/#!Error:";
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String provideString = req.getParameter(WebConstants.OAUTH2_PROVIDER);
		OAuthProvider provider = OAuthProvider.valueOf(provideString);
		// This code will be provided after the user authenticates with a provider.
		String athenticationCode = req.getParameter(WebConstants.OAUTH2_CODE);
 		if(athenticationCode == null){
			// If we do not have a code
			String redirectUrl = createRedirectUrl(req, provider);
			redirectToProvider(req, resp, provider, redirectUrl);
		}else{
			bindOAuthProviderToUser(req, resp, provider, athenticationCode);
		}
	}

	
	/**
	 * Tie the OAuth provider id to the user.
	 * @param req
	 * @param resp
	 * @param provider
	 * @param authorizationCode
	 * @throws IOException
	 */
	public void bindOAuthProviderToUser(
			HttpServletRequest req,
			HttpServletResponse resp, 
			OAuthProvider provider, 
			String authorizationCode)
			throws IOException {
		try {
			SynapseClient client = createSynapseClient();
			OAuthValidationRequest request = new OAuthValidationRequest();
			request.setProvider(provider);
			request.setAuthenticationCode(authorizationCode);
			PrincipalAlias response = client.bindOAuthProvidersUserId(request);
			resp.sendRedirect(PROFILE_MESSAGE_PLACE + URLEncoder.encode(provider.name() + " has been successfully linked to your Synapse account.", "UTF-8"));
		} catch (Exception e) {
			LogEntry entry = new LogEntry();
			entry.setLabel("Unable to link with " + provider);
			entry.setMessage(e.getMessage());
//			entry.setStacktrace(ExceptionUtils.getStackTrace(e));
			String entryString = SerializationUtils.serializeAndHexEncode(entry);
			resp.sendRedirect(ERROR_PLACE + entryString);
			
		}
	}
}

package org.sagebionetworks.web.server.servlet.openid;

import static org.sagebionetworks.web.shared.WebConstants.OPEN_ID_PROVIDER_GOOGLE_VALUE;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.OpenIDException;
import org.sagebionetworks.authutil.OpenIDConsumerUtils;
import org.sagebionetworks.authutil.OpenIDInfo;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseUnauthorizedException;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.web.shared.WebConstants;

public class OpenIDUtils {
	public static final String OPEN_ID_PROVIDER_GOOGLE_ENDPOINT = "https://www.google.com/accounts/o8/id";
	public static final String OPENID_CALLBACK_URI = "/Portal/openidcallback";
	
	/**
	 * This maps allowed provider names to their OpenID endpoints
	 * At this time only Google is supported
	 */
	private static String getOpenIdProviderURLforName(String name) {
		if (name.equals(OPEN_ID_PROVIDER_GOOGLE_VALUE)) return OPEN_ID_PROVIDER_GOOGLE_ENDPOINT;
		throw new IllegalArgumentException(name);
	}
	
	/**
	 * @param redirectEndpoint  this is the end point to which the OpenID provider should redirect
	 * to complete the first part of the OpenID handshake
	 */
	public static void openID(String openIdProviderName,
			Boolean acceptsTermsOfUse, String redirectMode, String returnToURL,
			HttpServletRequest request, HttpServletResponse response,
			String redirectEndpoint) throws IOException, ServletException,
			OpenIDException, URISyntaxException {
		
		String openIdProvider = getOpenIdProviderURLforName(openIdProviderName);
		
		// Build up a return URL 
		String openIDCallbackURL = redirectEndpoint + OPENID_CALLBACK_URI;
		openIDCallbackURL = OpenIDConsumerUtils.addRequestParameter(returnToURL, OpenIDInfo.ACCEPTS_TERMS_OF_USE_PARAM_NAME + "=" + acceptsTermsOfUse);
		if (redirectMode != null) {
			openIDCallbackURL = OpenIDConsumerUtils.addRequestParameter(returnToURL, OpenIDInfo.REDIRECT_MODE_PARAM_NAME + "=" + redirectMode);
		}
		
		// Note: this must be the last parameter to be added
		openIDCallbackURL = OpenIDConsumerUtils.addRequestParameter(returnToURL, OpenIDInfo.RETURN_TO_URL_PARAM_NAME + "=" + openIDCallbackURL);
		
		String redirectURL = OpenIDConsumerUtils.authRequest(openIdProvider, openIDCallbackURL);
		response.sendRedirect(redirectURL);
	}
	
	public static String createRedirectURL(String returnToURL,
			String sessionToken, boolean crowdAcceptsTermsOfUse,
			boolean isGWTMode) throws URISyntaxException {
		String redirectUrl = null;
		if (isGWTMode) {
			redirectUrl = returnToURL+":";
			if (crowdAcceptsTermsOfUse) {
				redirectUrl += sessionToken;
			} else {
				redirectUrl += WebConstants.ACCEPTS_TERMS_OF_USE_REQUIRED_TOKEN;
			}
		} else {
			if (crowdAcceptsTermsOfUse) {
				redirectUrl = OpenIDConsumerUtils.addRequestParameter(returnToURL, "status=OK&sessionToken="+sessionToken);
			} else {
				redirectUrl = OpenIDConsumerUtils.addRequestParameter(returnToURL, "status=" + WebConstants.ACCEPTS_TERMS_OF_USE_REQUIRED_TOKEN);
			}
		}
		return redirectUrl;
	}

	public static String createErrorRedirectURL(String returnToURL,
			boolean isGWTMode) throws URISyntaxException {
		String redirectUrl = null;
		if (isGWTMode) {
			redirectUrl = returnToURL + ":" + WebConstants.OPEN_ID_ERROR_TOKEN;
		} else {
			redirectUrl = OpenIDConsumerUtils.addRequestParameter(returnToURL, "status=" + WebConstants.OPEN_ID_ERROR_TOKEN);
		}
		return redirectUrl;
	}

	/**
	 * Completes the Open ID handshake 
	 * 
	 * @param request From the Open ID provider
	 * @param synapse Should be able to make anonymous requests (at least)
	 */
	public static void openIDCallback(HttpServletRequest request,
			HttpServletResponse response, SynapseClient synapse)
			throws IOException, URISyntaxException,
			SynapseUnauthorizedException {
		Boolean isGWTMode = null;
		
		String returnToURL = request.getParameter(OpenIDInfo.RETURN_TO_URL_PARAM_NAME);
		String redirectMode = request.getParameter(OpenIDInfo.REDIRECT_MODE_PARAM_NAME);
		String acceptsTermsOfUse = request.getParameter(OpenIDInfo.ACCEPTS_TERMS_OF_USE_PARAM_NAME);
		
		if (returnToURL == null) {
			throw new RuntimeException("Missing required return-to URL.");
		}

		isGWTMode = redirectMode != null && WebConstants.OPEN_ID_MODE_GWT.equals(redirectMode);
		
		try {
			// Send all the Open ID info to the repository services
			Session session = synapse.passThroughOpenIDParameters(request.getQueryString());

			// Redirect the user appropriately
			String redirectUrl = createRedirectURL(returnToURL,
					session.getSessionToken(), new Boolean(acceptsTermsOfUse), isGWTMode);
			String location = response.encodeRedirectURL(redirectUrl);
			response.sendRedirect(location);
			
		} catch (Exception e) {
			// We want to send the error as a 'redirect' but cannot do so unless
			// we have the returnToURL and know whether we are in 'GWT mode'
			if (isGWTMode == null || returnToURL == null) {
				if (e instanceof SynapseUnauthorizedException) {
					throw (SynapseUnauthorizedException) e;
				}
				throw new RuntimeException(e);
			} else {
				String redirectUrl = createErrorRedirectURL(returnToURL, isGWTMode);
				String location = response.encodeRedirectURL(redirectUrl);
				response.sendRedirect(location);
			}
		}
	}

}

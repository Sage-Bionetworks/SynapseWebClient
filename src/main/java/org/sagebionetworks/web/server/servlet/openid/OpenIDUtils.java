package org.sagebionetworks.web.server.servlet.openid;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.OpenIDException;
import org.sagebionetworks.authutil.OpenIDConsumerUtils;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseForbiddenException;
import org.sagebionetworks.client.exceptions.SynapseUnauthorizedException;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.web.shared.WebConstants;

public class OpenIDUtils {
	public static final String OPENID_CALLBACK_URI = "/Portal/openidcallback";
	
	public static final String ACCEPTS_TERMS_OF_USE_COOKIE_NAME = "sagebionetworks.acceptsTermsOfUse";
	public static final String RETURN_TO_URL_COOKIE_NAME = "sagebionetworks.returnToUrl";
	public static final String REDIRECT_MODE_COOKIE_NAME = "sagebionetworks.redirectMode";
	public static final int COOKIE_MAX_AGE_SECONDS = 60;
	
	/**
	 * @param redirectEndpoint  this is the end point to which the OpenID provider should redirect
	 * to complete the first part of the OpenID handshake
	 */
	public static void openID(String openIdProviderName,
			Boolean acceptsTermsOfUse, String redirectMode, String returnToURL,
			HttpServletRequest request, HttpServletResponse response,
			String redirectEndpoint) throws IOException, ServletException,
			OpenIDException, URISyntaxException {

		// Stash info that the portal needs in cookies
		Cookie cookie = new Cookie(RETURN_TO_URL_COOKIE_NAME, returnToURL);
		cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
		response.addCookie(cookie);
		
		cookie = new Cookie(ACCEPTS_TERMS_OF_USE_COOKIE_NAME, ""+acceptsTermsOfUse);
		cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
		response.addCookie(cookie);
		
		if (redirectMode!=null) {
			cookie = new Cookie(REDIRECT_MODE_COOKIE_NAME, redirectMode);
			cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
			response.addCookie(cookie);
		}
		
		// Get the redirect
		String openIDCallbackURL = redirectEndpoint + OPENID_CALLBACK_URI;
		String redirectURL = OpenIDConsumerUtils.authRequest(openIdProviderName, openIDCallbackURL);
		
		// Send the user off to the next part of the handshake
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
		
		String returnToURL = null;
		Boolean acceptsTermsOfUse = null;
		String redirectMode = null;
		Cookie[] cookies = request.getCookies();
		for (Cookie c : cookies) {
			if (RETURN_TO_URL_COOKIE_NAME.equals(c.getName())) {
				returnToURL = c.getValue();
			} else if (ACCEPTS_TERMS_OF_USE_COOKIE_NAME.equals(c.getName())) {
				acceptsTermsOfUse = Boolean.parseBoolean(c.getValue());
			} else if (REDIRECT_MODE_COOKIE_NAME.equals(c.getName())) {
				redirectMode = c.getValue();
			}
		}
		
		if (returnToURL == null) {
			throw new RuntimeException("Missing required return-to URL.");
		}

		isGWTMode = redirectMode != null && WebConstants.OPEN_ID_MODE_GWT.equals(redirectMode);
		
		try {
			// Send all the Open ID info to the repository services
			System.out.println(URLDecoder.decode(request.getQueryString(), "UTF-8"));
			Session session = synapse.passThroughOpenIDParameters(
					URLDecoder.decode(request.getQueryString(), "UTF-8"), 
					acceptsTermsOfUse, true);
			
			// Check to see if the user has accepted the terms of use
			synapse.setSessionToken(session.getSessionToken());
			try {
				synapse.getAuthUserInfo();
				
				// The user has accepted the terms, otherwise the previous operation would have failed
				acceptsTermsOfUse = true;
			} catch (SynapseForbiddenException e) {
				// The user has not accepted the terms
				// If something else broke, the error would be a SynapseUnauthorizedException
				acceptsTermsOfUse = false;
			}

			// Redirect the user appropriately
			String redirectUrl = createRedirectURL(returnToURL,
					session.getSessionToken(), acceptsTermsOfUse, isGWTMode);
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

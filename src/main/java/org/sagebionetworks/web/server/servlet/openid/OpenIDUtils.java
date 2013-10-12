package org.sagebionetworks.web.server.servlet.openid;

import static org.sagebionetworks.web.shared.WebConstants.OPEN_ID_PROVIDER_GOOGLE_VALUE;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.NotImplementedException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;
import org.sagebionetworks.authutil.BasicOpenIDConsumer;
import org.sagebionetworks.authutil.OpenIDInfo;
import org.sagebionetworks.client.HttpClientProvider;
import org.sagebionetworks.client.HttpClientProviderImpl;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseUnauthorizedException;
import org.sagebionetworks.utils.DefaultHttpClientSingleton;
import org.sagebionetworks.web.shared.WebConstants;

public class OpenIDUtils {
	public static final String OPEN_ID_PROVIDER_GOOGLE_ENDPOINT = "https://www.google.com/accounts/o8/id";
	public static final String OPENID_CALLBACK_URI = "/Portal/openidcallback";
	
	//
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
	public static void openID(
			String openIdProviderName,
			Boolean acceptsTermsOfUse,
			String redirectMode,
			String returnToURL,
              HttpServletRequest request,
              HttpServletResponse response,
              String redirectEndpoint) throws IOException, ServletException {

		HttpServlet servlet = null;
		
		String openIdProvider = getOpenIdProviderURLforName(openIdProviderName);
		
		String openIDCallbackURL = redirectEndpoint+OPENID_CALLBACK_URI;

		Cookie cookie = new Cookie(OpenIDInfo.RETURN_TO_URL_COOKIE_NAME, returnToURL);
		cookie.setMaxAge(OpenIDInfo.COOKIE_MAX_AGE_SECONDS);
		response.addCookie(cookie);
		
		cookie = new Cookie(OpenIDInfo.ACCEPTS_TERMS_OF_USE_COOKIE_NAME, ""+acceptsTermsOfUse);
		cookie.setMaxAge(OpenIDInfo.COOKIE_MAX_AGE_SECONDS);
		response.addCookie(cookie);
		
		if (redirectMode!=null) {
			cookie = new Cookie(OpenIDInfo.REDIRECT_MODE_COOKIE_NAME, redirectMode);
			cookie.setMaxAge(OpenIDInfo.COOKIE_MAX_AGE_SECONDS);
			response.addCookie(cookie);
		}
		
		BasicOpenIDConsumer.authRequest(openIdProvider, openIDCallbackURL, servlet, request, response);
	}
	
	public static String createRedirectURL(
			String returnToURL, 
			String sessionToken,
			boolean crowdAcceptsTermsOfUse,
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
				redirectUrl = addRequestParameter(returnToURL, "status=OK&sessionToken="+sessionToken);
			} else {
				redirectUrl = addRequestParameter(returnToURL, "status="+WebConstants.ACCEPTS_TERMS_OF_USE_REQUIRED_TOKEN);
			}
		}
		return redirectUrl;
	}

	public static String createErrorRedirectURL(
			String returnToURL, 
			boolean isGWTMode) throws URISyntaxException {
		String redirectUrl = null;
		if (isGWTMode) {
			redirectUrl = returnToURL+":"+WebConstants.OPEN_ID_ERROR_TOKEN;
		} else {
			redirectUrl = addRequestParameter(returnToURL, "status="+WebConstants.OPEN_ID_ERROR_TOKEN);
		}
		return redirectUrl;
	}

	public static void openIDCallback(
			HttpServletRequest request,
			HttpServletResponse response, 
			SynapseClient synapse) throws IOException, URISyntaxException, SynapseUnauthorizedException {
		Boolean isGWTMode = null;
		String returnToURL = null;
		Cookie acceptsTermsOfUseCookie = null;
		Cookie discoveryInfoCookie = null;
		String redirectMode = null;
		
		Cookie[] cookies = request.getCookies();
		for (Cookie c : cookies) {
			if (OpenIDInfo.RETURN_TO_URL_COOKIE_NAME.equals(c.getName())) {
				returnToURL = c.getValue();
			} else if (OpenIDInfo.ACCEPTS_TERMS_OF_USE_COOKIE_NAME.equals(c.getName())) {
				acceptsTermsOfUseCookie = c;
			} else if (OpenIDInfo.REDIRECT_MODE_COOKIE_NAME.equals(c.getName())) {
				redirectMode = c.getValue();
			} else if (BasicOpenIDConsumer.DISCOVERY_INFO_COOKIE_NAME.equals(c.getName())) {
				discoveryInfoCookie = c;
			}
		}
		if (returnToURL == null) {
			throw new RuntimeException("Missing required return-to URL.");
		}

		isGWTMode = redirectMode != null && WebConstants.OPEN_ID_MODE_GWT.equals(redirectMode);
		try {
			String uri = "/openIdCallback";
			throw new NotImplementedException();
			JSONObject session = synapse.createAuthEntity(uri,
					new JSONObject());

			String redirectUrl = createRedirectURL(returnToURL,
					session.getString("sessionToken"), new Boolean(
							acceptsTermsOfUseCookie.getValue()), isGWTMode);
			String location = response.encodeRedirectURL(redirectUrl);
			response.sendRedirect(location);
		} catch (Exception e) {
			// we want to send the error as a 'redirect' but cannot do so unless
			// we have the
			// returnToURL and know whether we are in 'GWT mode'
			if (isGWTMode == null || returnToURL == null) {
				if (e instanceof SynapseUnauthorizedException) {
					throw (SynapseUnauthorizedException) e;
				}
				throw new RuntimeException(e);
			} else {
				String redirectUrl = createErrorRedirectURL(returnToURL,
						isGWTMode);
				String location = response.encodeRedirectURL(redirectUrl);
				response.sendRedirect(location);
			}
		}
	}
	
	/**
	 * Add a new query parameter to an existing url
	 * @param urlString
	 * @param queryParameter
	 * @return
	 */
	public static String addRequestParameter(String urlString, String queryParameter) throws URISyntaxException {
		URI uri = new URI(urlString);
		String query = uri.getQuery();
		if (query==null || query.length()==0) {
			query = queryParameter;
		} else {
			query += "&"+queryParameter;
		}
		URI uriMod = new URI(
				uri.getScheme(), 
				uri.getUserInfo(), 
				uri.getHost(), 
				uri.getPort(), 
				uri.getPath(), 
				query, 
				uri.getFragment()
				);
		return uriMod.toString();
	}

}

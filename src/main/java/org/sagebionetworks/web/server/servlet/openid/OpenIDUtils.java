package org.sagebionetworks.web.server.servlet.openid;

import static org.sagebionetworks.repo.model.AuthorizationConstants.ACCEPTS_TERMS_OF_USE_ATTRIBUTE;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import org.openid4java.consumer.ConsumerManager;
import org.sagebionetworks.authutil.AuthenticationException;
import org.sagebionetworks.authutil.CrowdAuthUtil;
import org.sagebionetworks.repo.model.ServiceConstants;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.auth.User;
import org.sagebionetworks.repo.web.ForbiddenException;
import org.sagebionetworks.repo.web.NotFoundException;
import org.sagebionetworks.web.shared.WebConstants;
import org.springframework.http.HttpStatus;

public class OpenIDUtils {
	private static Random rand = new Random();
	
	
	public static final String OPENID_CALLBACK_URI = "/Portal/openidcallback";
	
	private static final String OPEN_ID_ATTRIBUTE = "OPENID";
	
	private static final String RETURN_TO_URL_COOKIE_NAME = "org.sagebionetworks.auth.returnToUrl";
	private static final String ACCEPTS_TERMS_OF_USE_COOKIE_NAME = "org.sagebionetworks.auth.acceptsTermsOfUse";
	private static final String REDIRECT_MODE_COOKIE_NAME = "org.sagebionetworks.auth.redirectMode";
	private static final int COOKIE_MAX_AGE_SECONDS = 60; // seconds
	
	private static ConsumerManager createConsumerManager() {
		return new ConsumerManager();
	}
	
	/**
	 * 
	 * @param openIdProvider
	 * @param acceptsTermsOfUse
	 * @param returnToURL
	 * @param request
	 * @param response
	 * @param redirectEndpoint  this is the end point to which the OpenID provider should redirect
	 * to complete the first part of the OpenID handshake
	 * @throws Exception
	 */
	public static void openID(
			String openIdProvider,
			Boolean acceptsTermsOfUse,
			String redirectMode,
			String returnToURL,
              HttpServletRequest request,
              HttpServletResponse response,
              String redirectEndpoint) throws IOException, ServletException {

		HttpServlet servlet = null;
		
		ConsumerManager manager = createConsumerManager();
		SampleConsumer sampleConsumer = new SampleConsumer(manager);
		
		String openIDCallbackURL = redirectEndpoint+OPENID_CALLBACK_URI;

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
		
		sampleConsumer.authRequest(openIdProvider, openIDCallbackURL, servlet, request, response);
	}

	public static void openIDCallback(
			HttpServletRequest request,
			HttpServletResponse response) throws IOException, NotFoundException, AuthenticationException, XPathExpressionException, URISyntaxException {
		try {
			
			ConsumerManager manager = createConsumerManager();
			
			SampleConsumer sampleConsumer = new SampleConsumer(manager);

			OpenIDInfo openIDInfo = sampleConsumer.verifyResponse(request);
			String openID = openIDInfo.getIdentifier();
						
			List<String> emails = openIDInfo.getMap().get(SampleConsumer.AX_EMAIL);
			String email = (emails==null || emails.size()<1 ? null : emails.get(0));
			List<String> fnames = openIDInfo.getMap().get(SampleConsumer.AX_FIRST_NAME);
			String fname = (fnames==null || fnames.size()<1 ? null : fnames.get(0));
			List<String> lnames = openIDInfo.getMap().get(SampleConsumer.AX_LAST_NAME);
			String lname = (lnames==null || lnames.size()<1 ? null : lnames.get(0));
			
			if (email==null) throw new AuthenticationException(400, "Unable to authenticate", null);
			
			User credentials = new User();			
			credentials.setEmail(email);

			Map<String,Collection<String>> attrs = null;
			try {
				attrs = new HashMap<String,Collection<String>>(CrowdAuthUtil.getUserAttributes(email));
			} catch (NotFoundException nfe) {
				// user doesn't exist yet, so create them
				credentials.setPassword((new Long(rand.nextLong())).toString());
				credentials.setFirstName(fname);
				credentials.setLastName(lname);
				if (fname!=null && lname!=null) credentials.setDisplayName(fname+" "+lname);
				CrowdAuthUtil.createUser(credentials);
				attrs = new HashMap<String,Collection<String>>(CrowdAuthUtil.getUserAttributes(email));
			}
			// save the OpenID in Crowd
			Collection<String> openIDs = attrs.get(OPEN_ID_ATTRIBUTE);
			if (openIDs==null) {
				attrs.put(OPEN_ID_ATTRIBUTE, Arrays.asList(new String[]{openID}));
			} else {
				Set<String> modOpenIDs = new HashSet<String>(openIDs);
				modOpenIDs.add(openID);
				attrs.put(OPEN_ID_ATTRIBUTE, modOpenIDs);
			}

			CrowdAuthUtil.setUserAttributes(email, attrs);
			
			Session crowdSession = CrowdAuthUtil.authenticate(credentials, false);


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
			if (returnToURL==null) throw new RuntimeException("Missing required return-to URL.");
			
			boolean isGWTMode = redirectMode!=null && WebConstants.OPEN_ID_MODE_GWT.equals(redirectMode);
			boolean crowdAcceptsTermsOfUse = CrowdAuthUtil.acceptsTermsOfUse(email, acceptsTermsOfUse);
			String redirectUrl = null;
			if (isGWTMode) {
				redirectUrl = returnToURL+":";
				if (crowdAcceptsTermsOfUse) {
					redirectUrl += crowdSession.getSessionToken();
				} else {
					redirectUrl += ServiceConstants.ACCEPTS_TERMS_OF_USE_REQUIRED_TOKEN;
				}
			} else {
				redirectUrl = addRequestParameter(returnToURL, "sessionToken="+crowdSession.getSessionToken());
			}
			String location = response.encodeRedirectURL(redirectUrl);
			if (isGWTMode || crowdAcceptsTermsOfUse) {
				response.sendRedirect(location);
			} else {
				// if standard mode and user has not accepted the ToU, then return a 403
				response.setStatus(HttpStatus.FORBIDDEN.value());
				response.getWriter().println("{\"reason\":\"You must accept the Synapse Terms of Use.\"}");
			}
		} catch (AuthenticationException ae) {
			// include the URL used to authenticate
			ae.setAuthURL(request.getRequestURL().toString());
			throw ae;
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

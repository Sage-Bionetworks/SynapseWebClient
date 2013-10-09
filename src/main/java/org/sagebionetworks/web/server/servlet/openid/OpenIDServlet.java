package org.sagebionetworks.web.server.servlet.openid;

import static org.sagebionetworks.web.shared.WebConstants.ACCEPTS_TERMS_OF_USE_PARAM;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.authutil.AuthenticationException;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.SynapseProviderImpl;
import org.sagebionetworks.web.shared.WebConstants;
import org.springframework.http.HttpStatus;

import com.google.inject.Inject;

public class OpenIDServlet extends HttpServlet {
	private static final long serialVersionUID = 95256472471083244L;
	private static final String PORTAL_USER_NAME = StackConfiguration.getPortalUsername();
	private static final String PORTAL_API_KEY = StackConfiguration.getPortalAPIKey();
	
	/**
	 * Injected with Gin
	 */
	private ServiceUrlProvider urlProvider;
	private SynapseProvider synapseProvider = new SynapseProviderImpl();
	
	/**
	 * Used to setup the Synapse client
	 */
	@Inject
	public void setServiceUrlProvider(ServiceUrlProvider provider) {
		this.urlProvider = provider;
	}
	
	@Override
    public void doPost(final HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {        
		if (request.getRequestURI().equals(WebConstants.OPEN_ID_URI)) {
			handleOpenIDRequest(request, response);
		} else {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
	}	

	private void handleOpenIDRequest(final HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {        
		String thisUrl = request.getRequestURL().toString();
		int i = thisUrl.indexOf(WebConstants.OPEN_ID_URI);
		if (i<0)  {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.getWriter().println("Request URL is missing suffix "+WebConstants.OPEN_ID_URI);
			return;
		}
		String redirectEndpoint = thisUrl.substring(0, i);
		String openIdProviderName = request.getParameter(WebConstants.OPEN_ID_PROVIDER);
		if (openIdProviderName==null) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.getWriter().println("Missing parameter "+WebConstants.OPEN_ID_PROVIDER);
			return;
		}
		String explicitlyAcceptsTermsOfUseString = request.getParameter(ACCEPTS_TERMS_OF_USE_PARAM);
		Boolean explicitlyAcceptsTermsOfUse = explicitlyAcceptsTermsOfUseString==null ? false : new Boolean(explicitlyAcceptsTermsOfUseString);
		String redirectMode = request.getParameter(WebConstants.OPEN_ID_MODE);
		String returnToURL = request.getParameter(WebConstants.RETURN_TO_URL_PARAM);
		if (returnToURL==null) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.getWriter().println("Missing parameter "+WebConstants.RETURN_TO_URL_PARAM);
			return;
		}

		OpenIDUtils.openID(
				openIdProviderName, 
				explicitlyAcceptsTermsOfUse, 
				redirectMode,
				returnToURL, 
				request, 
				response, 
				redirectEndpoint);
		
	}
	
	@Override
    public void doGet(final HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {        
		String requestURI = request.getRequestURI();
		if (requestURI.equals(WebConstants.OPEN_ID_URI)) {
			handleOpenIDRequest(request, response);
		} else if (requestURI.equals(OpenIDUtils.OPENID_CALLBACK_URI)) {
			handleOpenIDCallbackRequest(request, response);
		} else {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
	}
	
	private void handleOpenIDCallbackRequest(final HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException { 
		try {
			OpenIDUtils.openIDCallback(request, response, createSynapseClient(PORTAL_USER_NAME, PORTAL_API_KEY));
		} catch (AuthenticationException e) {
			response.setStatus(e.getRespStatus());
			response.getWriter().println("{\"reason\":\""+e.getMessage()+"\"}");
		} catch (URISyntaxException e) {
			// 400 error
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.getWriter().println("{\"reason\":\""+e.getMessage()+"\"}");
		}
	}

	/**
	 * Creates a Synapse client that authenticates via API key
	 */
	private SynapseClient createSynapseClient(String username, String apikey) {
		SynapseClient synapseClient = synapseProvider.createNewClient();
		synapseClient.setUserName(username);
		synapseClient.setApiKey(apikey);
		synapseClient.setAuthEndpoint(urlProvider.getPrivateAuthBaseUrl());
		return synapseClient;
	}

}

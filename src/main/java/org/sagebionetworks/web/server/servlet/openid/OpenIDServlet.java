package org.sagebionetworks.web.server.servlet.openid;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.OpenIDException;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseUnauthorizedException;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.SynapseProviderImpl;
import org.sagebionetworks.web.shared.WebConstants;
import org.springframework.http.HttpStatus;

import com.google.inject.Inject;

public class OpenIDServlet extends HttpServlet {
	private static final long serialVersionUID = 95256472471083244L;
	
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
		if (i < 0)  {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.getWriter().println("Request URL is missing suffix "+WebConstants.OPEN_ID_URI);
			return;
		}
		String redirectEndpoint = thisUrl.substring(0, i);
		String openIdProviderName = request.getParameter(WebConstants.OPEN_ID_PROVIDER);
		if (openIdProviderName == null) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.getWriter().println("Missing parameter "+WebConstants.OPEN_ID_PROVIDER);
			return;
		}
		String redirectMode = request.getParameter(WebConstants.OPEN_ID_MODE);
		String returnToURL = request.getParameter(WebConstants.RETURN_TO_URL_PARAM);
		if (returnToURL == null) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.getWriter().println("Missing parameter "+WebConstants.RETURN_TO_URL_PARAM);
			return;
		}

		try {
			OpenIDUtils.openID(openIdProviderName, redirectMode, returnToURL, request, response,
					redirectEndpoint);
		} catch (OpenIDException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		
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
			OpenIDUtils.openIDCallback(request, response, createSynapseClient());
		} catch (SynapseUnauthorizedException e) {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.getWriter().println("{\"reason\":\"" + e.getMessage() + "\"}");
		} catch (URISyntaxException e) {
			// 400 error
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.getWriter().println("{\"reason\":\"" + e.getMessage() + "\"}");
		}
	}

	/**
	 * Creates a Synapse client that can only make anonymous calls
	 */
	private SynapseClient createSynapseClient() {
		SynapseClient synapseClient = synapseProvider.createNewClient();
		synapseClient.setAuthEndpoint(urlProvider.getPrivateAuthBaseUrl());
		return synapseClient;
	}

}

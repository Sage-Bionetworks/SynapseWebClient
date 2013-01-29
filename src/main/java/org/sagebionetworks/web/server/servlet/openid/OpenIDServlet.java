package org.sagebionetworks.web.server.servlet.openid;

import static org.sagebionetworks.repo.model.ServiceConstants.ACCEPTS_TERMS_OF_USE_PARAM;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import org.sagebionetworks.authutil.AuthenticationException;
import org.sagebionetworks.repo.web.NotFoundException;
import org.sagebionetworks.web.shared.WebConstants;
import org.springframework.http.HttpStatus;

public class OpenIDServlet extends HttpServlet {
	private static final long serialVersionUID = 95256472471083244L;
	
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
		String openIdProvider = request.getParameter(WebConstants.OPEN_ID_PROVIDER);
		if (openIdProvider==null) {
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
				openIdProvider, 
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
			OpenIDUtils.openIDCallback(request, response);
		} catch (NotFoundException e) {
			// 404 error
			response.setStatus(HttpStatus.NOT_FOUND.value());
		} catch (AuthenticationException e) {
			response.setStatus(e.getRespStatus());
			response.getWriter().println("{\"reason\":\""+e.getMessage()+"\"}");
		} catch (XPathExpressionException e) {
			// 500 error
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		} catch (URISyntaxException e) {
			// 400 error
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.getWriter().println("{\"reason\":\""+e.getMessage()+"\"}");
		}
	}


}

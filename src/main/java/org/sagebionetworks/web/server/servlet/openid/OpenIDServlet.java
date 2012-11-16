package org.sagebionetworks.web.server.servlet.openid;

import static org.sagebionetworks.repo.model.ServiceConstants.ACCEPTS_TERMS_OF_USE_PARAM;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import org.sagebionetworks.authutil.AuthenticationException;
import org.sagebionetworks.repo.web.NotFoundException;
import org.springframework.http.HttpStatus;

public class OpenIDServlet extends HttpServlet {
	private static final long serialVersionUID = 95256472471083244L;
	
	@Override
    public void doPost(final HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {        
		if (!request.getRequestURI().equals(OpenIDUtils.OPEN_ID_URI)) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return;
		}
		String thisUrl = request.getRequestURL().toString();
		int i = thisUrl.indexOf(OpenIDUtils.OPEN_ID_URI);
		if (i<0)  {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.getWriter().println("Request URL is missing suffix "+OpenIDUtils.OPEN_ID_URI);
			return;
		}
		String redirectEndpoint = thisUrl.substring(0, i);
		String openIdProvider = request.getParameter(OpenIDUtils.OPEN_ID_PROVIDER);
		if (openIdProvider==null) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.getWriter().println("Missing parameter "+OpenIDUtils.OPEN_ID_PROVIDER);
			return;
		}
		String explicitlyAcceptsTermsOfUseString = request.getParameter(ACCEPTS_TERMS_OF_USE_PARAM);
		Boolean explicitlyAcceptsTermsOfUse = explicitlyAcceptsTermsOfUseString==null ? false : new Boolean(explicitlyAcceptsTermsOfUseString);
		String returnToURL = request.getParameter(OpenIDUtils.RETURN_TO_URL_PARAM);
		if (returnToURL==null) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.getWriter().println("Missing parameter "+OpenIDUtils.RETURN_TO_URL_PARAM);
			return;
		}

		OpenIDUtils.openID(
				openIdProvider, 
				explicitlyAcceptsTermsOfUse, 
				returnToURL, 
				request, 
				response, 
				redirectEndpoint);
	}	

	
	@Override
    public void doGet(final HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {        
		if (!request.getRequestURI().equals(OpenIDUtils.OPENID_CALLBACK_URI)) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return;
		}
		try {
			OpenIDUtils.openIDCallback(request, response);
		} catch (NotFoundException e) {
			// 404 error
			response.setStatus(HttpStatus.NOT_FOUND.value());
		} catch (AuthenticationException e) {
			response.setStatus(e.getRespStatus());
			response.getWriter().println(e.getMessage());
		} catch (XPathExpressionException e) {
			// 500 error
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}	

}

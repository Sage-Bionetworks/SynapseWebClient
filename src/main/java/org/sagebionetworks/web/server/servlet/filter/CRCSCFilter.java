package org.sagebionetworks.web.server.servlet.filter;

import java.io.IOException;
import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This filter redirects traffic heading to /dream to "#!Challenge:DREAM"
 * 
 *
 */
public class CRCSCFilter implements Filter {

	
	@Override
	public void destroy() {
		// nothing to do
	}
	

	@Override
	public void doFilter(ServletRequest rqst, ServletResponse rspn,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest httpRqst = (HttpServletRequest)rqst;
			//redirect to challenge page
		URL requestURL = new URL(httpRqst.getRequestURL().toString());
		URL redirectURL = new URL(requestURL.getProtocol(), requestURL.getHost(), requestURL.getPort(), requestURL.getPath().substring(0, requestURL.getPath().length()-"crcsc".length()) + "#!Synapse:syn2623706");
		HttpServletResponse httpRsp = (HttpServletResponse)rspn;
		httpRsp.sendRedirect(httpRsp.encodeRedirectURL(redirectURL.toString()));
	}

	@Override
	public void init(FilterConfig config) throws ServletException {

	}
}

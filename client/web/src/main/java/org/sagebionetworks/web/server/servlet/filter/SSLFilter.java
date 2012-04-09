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
 * This filter redirects traffic received via http to https
 * 
 * @author brucehoff
 *
 */
public class SSLFilter implements Filter {

	@Override
	public void destroy() {
		// nothing to do
	}
	
	public static final String HTTPS_PROTOCOL = "https";

	@Override
	public void doFilter(ServletRequest rqst, ServletResponse rspn,
			FilterChain chain) throws IOException, ServletException {
		if (!rqst.isSecure()) {
			HttpServletRequest httpRqst = (HttpServletRequest)rqst;
			URL requestURL = new URL(httpRqst.getRequestURL().toString());
			URL redirectURL = new URL(HTTPS_PROTOCOL, requestURL.getHost(), requestURL.getPort(), requestURL.getFile());
			HttpServletResponse httpRsp = (HttpServletResponse)rspn;
			httpRsp.sendRedirect(httpRsp.encodeRedirectURL(redirectURL.toString()));
		} else {
			chain.doFilter(rqst, rspn);
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// nothing to do
	}

}

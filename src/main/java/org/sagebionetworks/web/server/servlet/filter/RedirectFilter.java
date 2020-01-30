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
 * This filter redirects traffic heading to /xxx to a different place
 * 
 *
 */
public abstract class RedirectFilter implements Filter {


	@Override
	public void destroy() {
		// nothing to do
	}

	@Override
	public void doFilter(ServletRequest rqst, ServletResponse rspn, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpRqst = (HttpServletRequest) rqst;
		URL requestURL = new URL(httpRqst.getRequestURL().toString());
		URL redirectURL = new URL(requestURL.getProtocol(), requestURL.getHost(), requestURL.getPort(), requestURL.getPath().substring(0, requestURL.getPath().length() - getUrlPath().length()) + getTargetPage());
		HttpServletResponse httpRsp = (HttpServletResponse) rspn;
		httpRsp.sendRedirect(httpRsp.encodeRedirectURL(redirectURL.toString()));
	}

	@Override
	public void init(FilterConfig config) throws ServletException {}

	protected abstract String getTargetPage();

	protected abstract String getUrlPath();
}

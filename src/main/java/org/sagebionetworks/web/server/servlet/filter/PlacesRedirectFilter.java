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
 * This filter redirects traffic heading to /Place:Token to /#!Place:Token
 *
 */
public class PlacesRedirectFilter implements Filter {

	@Override
	public void destroy() {
		// nothing to do
	}

	@Override
	public void doFilter(ServletRequest rqst, ServletResponse rspn, FilterChain chain) throws IOException, ServletException {
		try {
			HttpServletRequest httpRqst = (HttpServletRequest) rqst;

			URL requestURL = new URL(httpRqst.getRequestURL().toString());
			String path = fixPath(requestURL.getPath());
			URL redirectURL = new URL(requestURL.getProtocol(), requestURL.getHost(), requestURL.getPort(), path);
			HttpServletResponse httpRsp = (HttpServletResponse) rspn;
			httpRsp.sendRedirect(httpRsp.encodeRedirectURL(redirectURL.toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String fixPath(String requestPath) {
		if (requestPath == null)
			return null;
		StringBuilder path = new StringBuilder(requestPath);
		if (path.length() > 1 && path.charAt(0) == '/')
			path.insert(1, "#!");
		return path.toString();
	}

	@Override
	public void init(FilterConfig config) throws ServletException {}
}

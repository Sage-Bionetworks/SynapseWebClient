package org.sagebionetworks.web.server.servlet.filter;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * prevent any communications from being sent over HTTP to this domain.
 * 
 * @author jayhodgson
 * 
 */
public class HSTSFilter implements Filter {
	
	public static final long MAX_AGE_SECONDS=60*60*24;  //a day
	
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		URL requestURL = new URL(httpRequest.getRequestURL().toString());
		long now = new Date().getTime();
		httpResponse.setDateHeader("Date", now);
		String hostName = requestURL.getHost();
		if (!(hostName.equals("127.0.0.1") || hostName.toLowerCase().equals("localhost"))) {
			httpResponse.setHeader("Strict-Transport-Security", "max-age=" + MAX_AGE_SECONDS);
		}
		filterChain.doFilter(request, response);
	}
	
	public void init(FilterConfig config) throws ServletException {
	}

	@Override
	public void destroy() {
	}
}

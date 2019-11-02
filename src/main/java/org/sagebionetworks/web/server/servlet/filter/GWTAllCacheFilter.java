package org.sagebionetworks.web.server.servlet.filter;

import java.io.IOException;
import java.util.Date;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GWTAllCacheFilter implements Filter {

	public static final long CACHE_TIME = 1000 * 60 * 60 * 24 * 365; // 1 year
	public static final long CACHE_TIME_SECONDS = 60 * 60 * 24 * 365; // 1 year

	private FilterConfig filterConfig;

	@Override
	public void destroy() {
		this.filterConfig = null;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String requestURI = httpRequest.getRequestURI().toLowerCase();
		long now = new Date().getTime();
		httpResponse.setDateHeader("Date", now);
		httpResponse.setHeader("Cache-Control", "max-age=" + CACHE_TIME_SECONDS);
		httpResponse.setHeader("Pragma", "");
		httpResponse.setDateHeader("Expires", now + CACHE_TIME);
		filterChain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		this.filterConfig = config;
	}
}

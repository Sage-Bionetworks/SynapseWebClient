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

import org.sagebionetworks.web.client.ClientProperties;

/**
 * very basic filter to cache resources
 * 
 * @author jayhodgson
 * 
 */
public class GWTCacheControlFilter implements Filter {
	
	//break up into three buckets.  never cache, cache for some time, or cache forever (when changed, GWT will rename the file)
	public static final long CACHE_TIME=1000*60*60*8;  //8 hours.  cache for some time
	public static final long MONTH_CACHE_TIME=1000*60*60*24*30;  //30 days.  cache "forever"
	private FilterConfig filterConfig;

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String requestURI = httpRequest.getRequestURI().toLowerCase();
		if (requestURI.contains(".cache.")) {
			setCacheTime(response, MONTH_CACHE_TIME);
		}
		else if (!requestURI.contains(".nocache.") && !requestURI.contains("portal.html")) {
			setCacheTime(response, CACHE_TIME);
		}
		filterChain.doFilter(request, response);
	}

	private void setCacheTime(ServletResponse response, long cacheTime) {
		long now = new Date().getTime();
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		httpResponse.setDateHeader("Expires", now+cacheTime);
	}
	
	public void init(FilterConfig config) throws ServletException {
		this.filterConfig = config;
	}

	@Override
	public void destroy() {
		this.filterConfig = null;
	}
}

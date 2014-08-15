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
	private FilterConfig filterConfig;

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String requestURI = httpRequest.getRequestURI().toLowerCase();
		if (requestURI.contains(".cache.")) {
			setCacheMaxAge(response);
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
		httpResponse.setDateHeader("Date", now);
	}
	
	private void setCacheMaxAge(ServletResponse response) {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		httpResponse.setHeader("Cache-Control", "max-age");
		httpResponse.setDateHeader("Date", new Date().getTime());
	}
	
	public void init(FilterConfig config) throws ServletException {
		this.filterConfig = config;
	}

	@Override
	public void destroy() {
		this.filterConfig = null;
	}
}

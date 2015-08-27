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
	
	//never cache nocache, or cache forever (when changed GWT will rename the file path, but SWC-2556 indicates that Chrome may happily return a missing resource)
	public static final long CACHE_TIME=1000*60*60*8;  //8 hours.
	public static final long CACHE_TIME_SECONDS=60*60*8;  //8 hours.
	
	private FilterConfig filterConfig;

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String requestURI = httpRequest.getRequestURI().toLowerCase();
		long now = new Date().getTime();
		httpResponse.setDateHeader("Date", now);
		if (requestURI.contains(".cache.")) {
			//safe to cache
			//https://developers.google.com/web/fundamentals/performance/optimizing-content-efficiency/http-caching#cache-control
			httpResponse.setHeader("Cache-Control", "max-age="+CACHE_TIME_SECONDS);
			httpResponse.setHeader("Pragma", "");
			httpResponse.setDateHeader("Expires", now+CACHE_TIME);
		}
		else if (requestURI.contains(".nocache.")) {
			//do not cache
			//http://stackoverflow.com/questions/1341089/using-meta-tags-to-turn-off-caching-in-all-browsers
			httpResponse.setDateHeader("Expires", 0);
			httpResponse.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, pre-check=0, post-check=0");
			httpResponse.setHeader("Pragma", "no-cache");
		} else {
			httpResponse.setHeader("Cache-Control", "");
			httpResponse.setHeader("Pragma", "");
		}
		filterChain.doFilter(request, response);
	}
	
	public void init(FilterConfig config) throws ServletException {
		this.filterConfig = config;
	}

	@Override
	public void destroy() {
		this.filterConfig = null;
	}
}

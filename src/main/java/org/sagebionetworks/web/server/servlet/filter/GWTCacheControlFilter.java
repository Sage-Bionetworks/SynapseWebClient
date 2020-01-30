package org.sagebionetworks.web.server.servlet.filter;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * very basic filter to cache resources
 * 
 * @author jayhodgson
 * 
 */
public class GWTCacheControlFilter implements Filter {

	// never cache nocache, or cache forever (when changed GWT will rename the file path, but SWC-2556
	// indicates that Chrome may happily return a missing resource)
	public static final long EIGHT_HOURS = 1000 * 60 * 60 * 8; // 8 hours.
	public static final long EIGHT_HOURS_IN_SECONDS = 60 * 60 * 8; // 8 hours.

	public static final long ONE_HOUR = 1000 * 60 * 60;
	public static final long ONE_HOUR_IN_SECONDS = 60 * 60;

	private FilterConfig filterConfig;
	public static final Set<String> HOUR_EXPIRATION_EXTENSIONS = new HashSet<>();
	static {
		String[] extensions = new String[] {".css", ".eot", ".woff2", ".woff", ".ttf", ".svg"};
		for (String extension : extensions) {
			HOUR_EXPIRATION_EXTENSIONS.add(extension);
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String requestURI = httpRequest.getRequestURI().toLowerCase().trim();
		long now = new Date().getTime();
		httpResponse.setDateHeader("Date", now);
		String extension = "";
		int lastDotIndex = requestURI.lastIndexOf(".");
		if (lastDotIndex > -1) {
			extension = requestURI.substring(lastDotIndex).toLowerCase();
		}
		if (requestURI.contains(".cache.")) {
			// safe to cache
			// https://developers.google.com/web/fundamentals/performance/optimizing-content-efficiency/http-caching#cache-control
			httpResponse.setHeader("Cache-Control", "max-age=" + EIGHT_HOURS_IN_SECONDS);
			httpResponse.setHeader("Pragma", "");
			httpResponse.setDateHeader("Expires", now + EIGHT_HOURS);
		} else if (requestURI.contains(".nocache.") || requestURI.equals("/") || requestURI.isEmpty()) {
			// do not cache
			// http://stackoverflow.com/questions/1341089/using-meta-tags-to-turn-off-caching-in-all-browsers
			httpResponse.setDateHeader("Expires", 0);
			httpResponse.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, pre-check=0, post-check=0");
			httpResponse.setHeader("Pragma", "no-cache");
		} else if (HOUR_EXPIRATION_EXTENSIONS.contains(extension)) {
			httpResponse.setHeader("Cache-Control", "max-age=" + ONE_HOUR_IN_SECONDS);
			httpResponse.setHeader("Pragma", "");
			httpResponse.setDateHeader("Expires", now + ONE_HOUR);
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

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
 * This filter redirects traffic received via http to https - kinda!
 * 
 * This is a little tricky. The load balancer actually does all https communication with clients and
 * talks to the protal-servlets using http:
 * 
 * Request: client->https->load-balancer->http->portal Response:
 * client<-https<-load-balancer<-http<-portal
 * 
 * This is important, as load-balancer can take on the resource intensive encryption work involved
 * in SSL communication.
 * 
 * Q: So if we want to re-direct clients from http to https how do we do so from the portal? A: Look
 * for the header: "X-Forwarded-Proto - a de facto standard for identifying the originating protocol
 * of an HTTP request, since a reverse proxy (load balancer) may communicate with a web server using
 * HTTP even if the request to the reverse proxy is HTTPS"
 * 
 * @see http://en.wikipedia.org/wiki/List_of_HTTP_header_fields
 * 
 *      Therefore, this filter will redirect all http trafic with the following header:
 * 
 *      X-Forwarded-Proto: http
 * 
 *
 */
public class SSLFilter implements Filter {

	public static final String HEADER_X_FORWARDED_PROTO = "X-Forwarded-Proto";
	public static final String HTTP_PROTOCOL = "http";
	public static final String HTTPS_PROTOCOL = "https";

	@Override
	public void destroy() {
		// nothing to do
	}


	@Override
	public void doFilter(ServletRequest rqst, ServletResponse rspn, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpRqst = (HttpServletRequest) rqst;
		// Was this originally a http request?
		if (isForwardedHttpRequest(httpRqst)) {
			// The client made an http request, so redirect them to https.
			URL requestURL = new URL(httpRqst.getRequestURL().toString());
			URL redirectURL = new URL(HTTPS_PROTOCOL, requestURL.getHost(), requestURL.getPort(), requestURL.getFile());
			HttpServletResponse httpRsp = (HttpServletResponse) rspn;
			httpRsp.sendRedirect(httpRsp.encodeRedirectURL(redirectURL.toString()));
		} else {
			chain.doFilter(rqst, rspn);
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {

	}

	/**
	 * Is the passed request an HTTP forwarded request?
	 * 
	 * @param httpRqst
	 * @return
	 */
	public static boolean isForwardedHttpRequest(HttpServletRequest httpRqst) {
		String forwardedProtocol = httpRqst.getHeader(HEADER_X_FORWARDED_PROTO);
		if (forwardedProtocol == null)
			return false;
		forwardedProtocol = forwardedProtocol.toLowerCase();
		return HTTP_PROTOCOL.equals(forwardedProtocol);
	}


}

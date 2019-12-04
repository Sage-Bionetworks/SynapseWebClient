package org.sagebionetworks.web.server.servlet.filter;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

/**
 * Logs timing and error messages
 * 
 * @author John
 *
 */
public class TimingFilter implements Filter {

	public static final String SESSION_ID = "sessionId";

	static private Logger log = LogManager.getLogger(TimingFilter.class);

	@Override
	public void destroy() {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		// push a new UUID as session id to the logging thread context
		// this ID will be every log entry and will tie together all
		// entries for this call.
		ThreadContext.put(SESSION_ID, UUID.randomUUID().toString());
		// Log the time
		long start = System.currentTimeMillis();
		try {
			// Pass it along.
			chain.doFilter(request, response);
		} catch (IOException e) {
			// Log any exceptions.
			log.error(e);
			throw e;
		} catch (ServletException e) {
			// Log any exceptions.
			log.error(e);
			throw e;
		} finally {
			// Log the timing.
			long end = System.currentTimeMillis();
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			log.trace(httpRequest.getServletPath() + " elapse: " + (end - start) + " ms");
			// Clear the logging thread context
			ThreadContext.clearAll();
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

}

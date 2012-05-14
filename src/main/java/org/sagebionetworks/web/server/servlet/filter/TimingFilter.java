package org.sagebionetworks.web.server.servlet.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;

/**
 * Logs timing and error messages 
 * @author John
 *
 */
public class TimingFilter implements Filter {
	
	static private Log log = LogFactory.getLog(SynapseClientImpl.class);

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		// Log the time
		long start = System.currentTimeMillis();
		try{
			// Pass it along.
			chain.doFilter(request, response);
		}catch (IOException e){
			// Log any exceptions.
			log.error(e);
			throw e;
		}catch (ServletException e){
			// Log any exceptions.
			log.error(e);
			throw e;
		}finally{
			// Log the timing.
			long end = System.currentTimeMillis();
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			log.trace(httpRequest.getServletPath()+" elapse: "+(end-start)+" ms");
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}

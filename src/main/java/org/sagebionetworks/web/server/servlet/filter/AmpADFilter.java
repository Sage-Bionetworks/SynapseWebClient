package org.sagebionetworks.web.server.servlet.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class AmpADFilter implements Filter {

	@Override
	public void destroy() {
		// nothing to do
	}

	@Override
	public void doFilter(ServletRequest rqst, ServletResponse rspn, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse httpRsp = (HttpServletResponse) rspn;
		httpRsp.sendRedirect("https://adknowledgeportal.synapse.org/");
	}

	@Override
	public void init(FilterConfig config) throws ServletException {}
}

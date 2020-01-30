package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.versionInfo.SynapseVersionInfo;
import org.sagebionetworks.web.client.StackEndpoints;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl.PortalVersionHolder;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import com.google.gwt.thirdparty.guava.common.base.Supplier;
import com.google.gwt.thirdparty.guava.common.base.Suppliers;

/**
 * Servlet for getting the current repo and portal version
 */
public class VersionsServlet extends HttpServlet {
	public static final String TEXT_CONTENT_TYPE = "text/plain";
	static private Log log = LogFactory.getLog(VersionsServlet.class);
	private static final long serialVersionUID = 1L;
	protected static final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();

	private final Supplier<SynapseVersionInfo> synapseVersionCache = Suppliers.memoizeWithExpiration(versionSupplier(), 5, TimeUnit.MINUTES);

	public SynapseVersionInfo getSynapseVersionInfo() {
		return synapseVersionCache.get();
	}

	private SynapseProvider synapseProvider = new SynapseProviderImpl();

	private SynapseClient createNewClient() {
		SynapseClient client = synapseProvider.createNewClient();
		client.setAuthEndpoint(StackEndpoints.getAuthenticationServicePublicEndpoint());
		client.setRepositoryEndpoint(StackEndpoints.getRepositoryServiceEndpoint());
		client.setFileEndpoint(StackEndpoints.getFileServiceEndpoint());
		return client;
	}

	private Supplier<SynapseVersionInfo> versionSupplier() {
		return new Supplier<SynapseVersionInfo>() {
			public SynapseVersionInfo get() {
				try {
					org.sagebionetworks.client.SynapseClient synapseClient = createNewClient();
					return synapseClient.getVersionInfo();
				} catch (SynapseException e) {
					log.error(e);
					return null;
				}
			}
		};
	}

	public String getSynapseVersions() throws RestServiceException {
		return PortalVersionHolder.getVersionInfo() + "," + getSynapseVersionInfo().getVersion();
	}

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		VersionsServlet.perThreadRequest.set(arg0);
		super.service(arg0, arg1);
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {
		super.service(arg0, arg1);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// instruct not to cache
		response.setHeader(WebConstants.CACHE_CONTROL_KEY, WebConstants.CACHE_CONTROL_VALUE_NO_CACHE); // Set standard HTTP/1.1 no-cache headers.
		response.setHeader(WebConstants.PRAGMA_KEY, WebConstants.NO_CACHE_VALUE); // Set standard HTTP/1.0 no-cache header.
		response.setContentType(WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		response.setDateHeader(WebConstants.EXPIRES_KEY, 0L); // Proxy

		response.setStatus(HttpServletResponse.SC_OK);

		try {
			response.getOutputStream().write(getSynapseVersions().getBytes("UTF-8"));
			response.getOutputStream().flush();
		} catch (RestServiceException e) {
			// redirect to error place with an entry
			response.sendRedirect(FileHandleAssociationServlet.getBaseUrl(request) + FileHandleAssociationServlet.ERROR_PLACE + URLEncoder.encode(e.getMessage()));
		}
	}
}

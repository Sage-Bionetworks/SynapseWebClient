package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.StackEndpoints;
import org.sagebionetworks.web.server.servlet.filter.GWTAllCacheFilter;
import org.sagebionetworks.web.shared.WebConstants;

/**
 * Handles file handler uploads.
 *
 * @author jay
 *
 */
public class FileHandleAssociationServlet extends HttpServlet {
	public static final String ERROR_PLACE = "#!Error:";
	private static final long serialVersionUID = 1L;
	protected static final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();
	private SynapseProvider synapseProvider = new SynapseProviderImpl();
	public static final long CACHE_TIME_SECONDS = 30; // 30 seconds
	private TokenProvider tokenProvider = new TokenProvider() {
		@Override
		public String getSessionToken() {
			return UserDataProvider.getThreadLocalUserToken(FileHandleAssociationServlet.perThreadRequest.get());
		}
	};

	/**
	 * Unit test can override this.
	 *
	 * @param fileHandleProvider
	 */
	public void setSynapseProvider(SynapseProvider synapseProvider) {
		this.synapseProvider = synapseProvider;
	}

	/**
	 * Unit test uses this to provide a mock token provider
	 *
	 * @param tokenProvider
	 */
	public void setTokenProvider(TokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		FileHandleAssociationServlet.perThreadRequest.set(arg0);
		super.service(arg0, arg1);
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {
		super.service(arg0, arg1);
	}


	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setHeader(WebConstants.CACHE_CONTROL_KEY, "max-age=" + CACHE_TIME_SECONDS);
		String token = getSessionToken(request);
		SynapseClient client = createNewClient(token);

		String objectId = request.getParameter(WebConstants.ASSOCIATED_OBJECT_ID_PARAM_KEY);
		String objectType = request.getParameter(WebConstants.ASSOCIATED_OBJECT_TYPE_PARAM_KEY);
		String fileHandleId = request.getParameter(WebConstants.FILE_HANDLE_ID_PARAM_KEY);

		try {
			if (fileHandleId != null && (objectId == null || objectType == null)) {
				// try to return the raw file handle (will work if user owns the file handle
				URL resolvedUrl = client.getFileHandleTemporaryUrl(fileHandleId);
				response.sendRedirect(resolvedUrl.toString());
			} else {
				FileHandleAssociation fha = new FileHandleAssociation();
				fha.setAssociateObjectId(objectId);
				fha.setAssociateObjectType(FileHandleAssociateType.valueOf(objectType));
				fha.setFileHandleId(fileHandleId);
				URL resolvedUrl = client.getFileURL(fha);
				if (FileHandleAssociateType.UserProfileAttachment.equals(fha.getAssociateObjectType()) || FileHandleAssociateType.TeamAttachment.equals(fha.getAssociateObjectType())) {
					// cache for a long time, and send the bytes back
					InputStream in = null;
					OutputStream out = null;
					try {
						response.setHeader("Cache-Control", "max-age=" + GWTAllCacheFilter.CACHE_TIME_SECONDS);
						in = resolvedUrl.openStream();
						out = response.getOutputStream();
						IOUtils.copy(in, out);
					} finally {
						IOUtils.closeQuietly(in);
						IOUtils.closeQuietly(out);
					}
				} else {
					response.sendRedirect(resolvedUrl.toString());
				}
			}
		} catch (SynapseException e) {
			// redirect to error place with an entry
			response.sendRedirect(FileHandleAssociationServlet.getBaseUrl(request) + FileHandleAssociationServlet.ERROR_PLACE + URLEncoder.encode(e.getMessage()));
		}
	}

	/**
	 * Get the session token
	 * 
	 * @param request
	 * @return
	 */
	public String getSessionToken(final HttpServletRequest request) {
		return tokenProvider.getSessionToken();
	}

	/**
	 * Create a new Synapse client.
	 *
	 * @return
	 */
	private SynapseClient createNewClient(String sessionToken) {
		SynapseClient client = synapseProvider.createNewClient();
		client.setAuthEndpoint(StackEndpoints.getAuthenticationServicePublicEndpoint());
		client.setRepositoryEndpoint(StackEndpoints.getRepositoryServiceEndpoint());
		client.setFileEndpoint(StackEndpoints.getFileServiceEndpoint());
		if (sessionToken != null)
			client.setSessionToken(sessionToken);
		return client;
	}

	public static final String getBaseUrl(HttpServletRequest request) {
		StringBuffer url = request.getRequestURL();
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		String base = url.substring(0, url.length() - uri.length() + ctx.length()) + "/";
		return base;
	}

}

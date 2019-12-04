package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.dao.WikiPageKeyHelper;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.table.RowReference;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.StackEndpoints;
import org.sagebionetworks.web.shared.WebConstants;

/**
 * Handles file handler uploads.
 *
 * @author jay
 *
 */
public class FileHandleServlet extends HttpServlet {

	private static Logger logger = Logger.getLogger(FileHandleServlet.class.getName());
	private static final long serialVersionUID = 1L;

	protected static final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();

	private SynapseProvider synapseProvider = new SynapseProviderImpl();
	private TokenProvider tokenProvider = new TokenProvider() {
		@Override
		public String getSessionToken() {
			return UserDataProvider.getThreadLocalUserToken(FileHandleServlet.perThreadRequest.get());
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
		FileHandleServlet.perThreadRequest.set(arg0);
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
		response.setDateHeader(WebConstants.EXPIRES_KEY, 0L); // Proxy

		String token = getSessionToken(request);
		SynapseClient client = createNewClient(token);
		boolean isProxy = false;
		String proxy = request.getParameter(WebConstants.PROXY_PARAM_KEY);
		if (proxy != null)
			isProxy = Boolean.parseBoolean(proxy);

		String teamId = request.getParameter(WebConstants.TEAM_PARAM_KEY);

		String entityId = request.getParameter(WebConstants.ENTITY_PARAM_KEY);
		String entityVersion = request.getParameter(WebConstants.ENTITY_VERSION_PARAM_KEY);

		// table params
		String tableColumnId = request.getParameter(WebConstants.TABLE_COLUMN_ID);
		String tableRowId = request.getParameter(WebConstants.TABLE_ROW_ID);
		String tableRowVersionNumbrer = request.getParameter(WebConstants.TABLE_ROW_VERSION_NUMBER);

		String ownerId = request.getParameter(WebConstants.WIKI_OWNER_ID_PARAM_KEY);
		String ownerType = request.getParameter(WebConstants.WIKI_OWNER_TYPE_PARAM_KEY);
		String fileName = request.getParameter(WebConstants.WIKI_FILENAME_PARAM_KEY);
		Boolean isPreview = Boolean.parseBoolean(request.getParameter(WebConstants.FILE_HANDLE_PREVIEW_PARAM_KEY));
		String redirectUrlString = request.getParameter(WebConstants.REDIRECT_URL_KEY);
		URL resolvedUrl = null;
		String rawFileHandleId = request.getParameter(WebConstants.RAW_FILE_HANDLE_PARAM);

		try {
			resolveUrlAndRedirect(request, response, client, isProxy, teamId, entityId, entityVersion, tableColumnId, tableRowId, tableRowVersionNumbrer, ownerId, ownerType, fileName, isPreview, redirectUrlString, resolvedUrl, rawFileHandleId);
		} catch (SynapseNotFoundException e) {
			// Retry preview once, after 1.5 seconds
			if (isPreview) {
				try {
					Thread.sleep(1500);
					resolveUrlAndRedirect(request, response, client, isProxy, teamId, entityId, entityVersion, tableColumnId, tableRowId, tableRowVersionNumbrer, ownerId, ownerType, fileName, isPreview, redirectUrlString, resolvedUrl, rawFileHandleId);
				} catch (InterruptedException e1) {
					// ...
				} catch (SynapseNotFoundException e1) {
					// show generic image
					doRedirect(request, response, isProxy, new URL(getBaseUrl(request) + WebConstants.PREVIEW_UNAVAILABLE_PATH));
				} catch (SynapseException e1) {
					throw new ServletException(e);
				}
			}
		} catch (SynapseException e) {
			// redirect to error place
			response.sendRedirect(FileHandleAssociationServlet.getBaseUrl(request) + FileHandleAssociationServlet.ERROR_PLACE + URLEncoder.encode(e.getMessage()));
		}
	}

	private void resolveUrlAndRedirect(HttpServletRequest request, HttpServletResponse response, SynapseClient client, boolean isProxy, String teamId, String entityId, String entityVersion, String tableColumnId, String tableRowId, String tableRowVersionNumbrer, String ownerId, String ownerType, String fileName, Boolean isPreview, String redirectUrlString, URL resolvedUrl, String rawFileHandleId) throws MalformedURLException, UnsupportedEncodingException, ClientProtocolException, IOException, SynapseException, ServletException {
		if (redirectUrlString != null) {
			// simple redirect
			resolvedUrl = new URL(URLDecoder.decode(redirectUrlString, "UTF-8"));
		}
		if (rawFileHandleId != null) {
			resolvedUrl = client.getFileHandleTemporaryUrl(rawFileHandleId);
		} else if (ownerId != null && ownerType != null) {
			ObjectType type = ObjectType.valueOf(ownerType);
			String wikiId = request.getParameter(WebConstants.WIKI_ID_PARAM_KEY);
			WikiPageKey properKey = WikiPageKeyHelper.createWikiPageKey(ownerId, type, wikiId);
			String wikiVersion = request.getParameter(WebConstants.WIKI_VERSION_PARAM_KEY);

			// Redirect the user to the url
			// If we're rendering a version of a wiki page,
			// we must get the URL for the attachment from that version of the wiki
			if (wikiVersion != null) {
				if (isPreview) {
					resolvedUrl = client.getVersionOfV2WikiAttachmentPreviewTemporaryUrl(properKey, fileName, new Long(wikiVersion));
				} else {
					resolvedUrl = client.getVersionOfV2WikiAttachmentTemporaryUrl(properKey, fileName, new Long(wikiVersion));
				}
			} else {
				if (isPreview) {
					resolvedUrl = client.getV2WikiAttachmentPreviewTemporaryUrl(properKey, fileName);
				} else {
					resolvedUrl = client.getV2WikiAttachmentTemporaryUrl(properKey, fileName);
				}
			}
			// Done
		} else if (entityId != null) {
			// Table File Handle
			if (tableColumnId != null || tableRowId != null || tableRowVersionNumbrer != null) {
				if (tableColumnId != null && tableRowId != null && tableRowVersionNumbrer != null) {
					try {
						RowReference row = new RowReference();
						row.setRowId(Long.parseLong(tableRowId));
						row.setVersionNumber(Long.parseLong(tableRowVersionNumbrer));
						if (isPreview)
							resolvedUrl = client.getTableFileHandlePreviewTemporaryUrl(entityId, row, tableColumnId);
						else
							resolvedUrl = client.getTableFileHandleTemporaryUrl(entityId, row, tableColumnId);
					} catch (NumberFormatException e) {
						throw new ServletException(WebConstants.TABLE_ROW_ID + " and " + WebConstants.TABLE_ROW_VERSION_NUMBER + " must be Long values. Actual values: " + tableRowId + ", " + tableRowVersionNumbrer);
					}

				} else {
					throw new ServletException("All table fields must be defined, if any are defined: " + WebConstants.TABLE_COLUMN_ID + ", " + WebConstants.TABLE_ROW_ID + ", " + WebConstants.TABLE_ROW_VERSION_NUMBER);
				}
			} else {
				// Entity
				if (entityVersion == null) {
					if (isPreview)
						resolvedUrl = client.getFileEntityPreviewTemporaryUrlForCurrentVersion(entityId);
					else
						resolvedUrl = client.getFileEntityTemporaryUrlForCurrentVersion(entityId);
				} else {
					Long versionNumber = Long.parseLong(entityVersion);
					if (isPreview)
						resolvedUrl = client.getFileEntityPreviewTemporaryUrlForVersion(entityId, versionNumber);
					else
						resolvedUrl = client.getFileEntityTemporaryUrlForVersion(entityId, versionNumber);
				}
			}
		} else if (teamId != null) {
			try {
				resolvedUrl = client.getTeamIcon(teamId);
			} catch (SynapseException e) {
				return;
			}
		}

		doRedirect(request, response, isProxy, resolvedUrl);
	}

	private void doRedirect(HttpServletRequest request, HttpServletResponse response, boolean isProxy, URL resolvedUrl) throws ClientProtocolException, IOException {
		if (resolvedUrl != null) {
			if (isProxy) {
				CloseableHttpClient client = HttpClients.createDefault();
				try {
					// do the get
					HttpGet httpGet = new HttpGet(resolvedUrl.toString());
					// copy headers
					Enumeration<?> headerValues = request.getHeaders("Cookie");
					while (headerValues.hasMoreElements()) {
						String headerValue = (String) headerValues.nextElement();
						httpGet.addHeader("Cookie", headerValue);
					}
					HttpResponse newResponse = client.execute(httpGet);
					HttpEntity responseEntity = (null != newResponse.getEntity()) ? newResponse.getEntity() : null;
					if (responseEntity != null) {
						responseEntity.writeTo(response.getOutputStream());
						response.setContentType(responseEntity.getContentType().getValue());
					}
				} finally {
					client.close();
				}
			} else
				response.sendRedirect(resolvedUrl.toString());
		}
	}

	public static void fillResponseWithSuccess(HttpServletResponse response, String id) throws JSONObjectAdapterException, UnsupportedEncodingException, IOException {
		UploadResult result = new UploadResult();
		if (id != null)
			result.setMessage(id);
		else
			result.setMessage("File upload successful");

		result.setUploadStatus(UploadStatus.SUCCESS);
		String out = EntityFactory.createJSONStringForEntity(result);
		response.setStatus(HttpServletResponse.SC_CREATED);
		response.getOutputStream().write(out.getBytes("UTF-8"));
		response.getOutputStream().flush();
	}

	public static void fillResponseWithFailure(HttpServletResponse response, Exception e) throws UnsupportedEncodingException, IOException {
		UploadResult result = new UploadResult();
		result.setMessage(e.getMessage());
		result.setUploadStatus(UploadStatus.FAILED);
		String out;
		try {
			out = EntityFactory.createJSONStringForEntity(result);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getOutputStream().write(out.getBytes("UTF-8"));
			response.getOutputStream().flush();
		} catch (JSONObjectAdapterException e1) {
			throw new RuntimeException(e1);
		}
	}

	public static FileEntity getNewFileEntity(String parentEntityId, String fileHandleId, String name, SynapseClient client) throws SynapseException {
		FileEntity fileEntity = new FileEntity();
		fileEntity.setParentId(parentEntityId);
		if (name != null)
			fileEntity.setName(name);
		// set data file handle id before creation
		fileEntity.setDataFileHandleId(fileHandleId);
		fileEntity = (FileEntity) client.createEntity(fileEntity);
		return fileEntity;
	}

	public static void fixName(FileEntity fileEntity, FileHandle newFileHandle, SynapseClient client) throws SynapseException {
		String originalFileEntityName = fileEntity.getName();
		try {
			// and try to set the name to the filename
			fileEntity.setName(newFileHandle.getFileName());
			fileEntity = (FileEntity) client.putEntity(fileEntity);
		} catch (Throwable t) {
			fileEntity.setName(originalFileEntityName);
		} ;
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
	 * The call was forbidden
	 *
	 * @param response
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public static void setForbiddenMessage(HttpServletResponse response) throws IOException, UnsupportedEncodingException {
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.getOutputStream().write("No session token found".getBytes("UTF-8"));
		response.getOutputStream().flush();
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

	private String getBaseUrl(HttpServletRequest request) {
		StringBuffer url = request.getRequestURL();
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		String base = url.substring(0, url.length() - uri.length() + ctx.length()) + "/";
		return base;
	}

}

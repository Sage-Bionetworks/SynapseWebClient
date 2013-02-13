package org.sagebionetworks.web.server.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.inject.Inject;

/**
 * Handles file handler uploads.
 *
 * @author jay
 *
 */
public class FileHandleServlet extends HttpServlet {

	public static final int MAX_TIME_OUT = 10 * 1000;
	public static final long BYTES_PER_MEGABYTE = 1048576;
	public static final long MAX_ATTACHMENT_MEGABYTES = 10;
	public static final long MAX_ATTACHMENT_SIZE_IN_BYTES = MAX_ATTACHMENT_MEGABYTES*BYTES_PER_MEGABYTE; // 10 MB
	private static Logger logger = Logger.getLogger(FileUpload.class.getName());
	private static final long serialVersionUID = 1L;

	protected static final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();

	/**
	 * Injected with Gin
	 */
	@SuppressWarnings("unused")
	private ServiceUrlProvider urlProvider;
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
	 * @param provider
	 */
	public void setSynapseProvider(SynapseProvider synapseProvider) {
		this.synapseProvider = synapseProvider;
	}

	/**
	 * Essentially the constructor. Setup synapse client.
	 *
	 * @param provider
	 */
	@Inject
	public void setServiceUrlProvider(ServiceUrlProvider provider) {
		this.urlProvider = provider;
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
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		FileHandleServlet.perThreadRequest.set(arg0);
		super.service(arg0, arg1);
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		super.service(arg0, arg1);
	}

	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String token = getSessionToken(request);
		Synapse client = createNewClient(token);
		String ownerId = request.getParameter(DisplayUtils.WIKI_OWNER_ID_PARAM_KEY);
		String ownerType = request.getParameter(DisplayUtils.WIKI_OWNER_TYPE_PARAM_KEY);
		String fileName = request.getParameter(DisplayUtils.WIKI_FILENAME_PARAM_KEY);
		Boolean isPreview = Boolean.parseBoolean(request.getParameter(DisplayUtils.WIKI_PREVIEW_PARAM_KEY));
		if (ownerId != null && ownerType != null) {
			ObjectType type = ObjectType.valueOf(ownerType);
			String wikiId = request.getParameter(DisplayUtils.WIKI_ID_PARAM_KEY);
			WikiPageKey properKey = new WikiPageKey(ownerId, type, wikiId);

			// Redirect the user to the temp preview url
			URL resolvedUrl;
			if (isPreview)
				resolvedUrl = client.getWikiAttachmentPreviewTemporaryUrl(properKey, fileName);
			else
				resolvedUrl = client.getWikiAttachmentTemporaryUrl(properKey, fileName);
			
			response.sendRedirect(resolvedUrl.toString());
		    //Done
		}
	}

	
	@Override
	public void doPost(final HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		ServletFileUpload upload = new ServletFileUpload();
		FileHandleResults results = null;
		// Before we do aything make sure we can get the users token
		String token = getSessionToken(request);
		if (token == null) {
			setForbiddenMessage(response);
			return;
		}

		try {
			// Connect to syanpse
			Synapse client = createNewClient(token);
			FileItemIterator iter = upload.getItemIterator(request);
			while (iter.hasNext()) {
				FileItemStream item = iter.next();

				String name = item.getFieldName();
				InputStream stream = item.openStream();
				String fileName = item.getName();
				File temp = ServiceUtils.writeToTempFile(stream, MAX_ATTACHMENT_SIZE_IN_BYTES);
				try{
					// Now upload the file
					List<File> files = new ArrayList<File>();
					File renamedFile = new File(fileName);
					//wait until we can have this file name
					while (!temp.renameTo(renamedFile)){
						Thread.sleep(1000);
					}
					temp = renamedFile;
					files.add(temp);
					client.setFileEndpoint(StackConfiguration.getFileServiceEndpoint());
					results = client.createFileHandles(files);
				}finally{
					// Unconditionally delete the tmp file
					temp.delete();
				}
			}

			//and update the wiki page (if the wiki key info was given as parameters).
			if (results != null) {
				String ownerId = request.getParameter(DisplayUtils.WIKI_OWNER_ID_PARAM_KEY);
				String ownerType = request.getParameter(DisplayUtils.WIKI_OWNER_TYPE_PARAM_KEY);
				if (ownerId != null && ownerType != null) {
					ObjectType type = ObjectType.valueOf(ownerType);
					String wikiId = request.getParameter(DisplayUtils.WIKI_ID_PARAM_KEY);
					WikiPageKey properKey = new WikiPageKey(ownerId, type, wikiId);
					WikiPage page = client.getWikiPage(properKey);
					List<String> fileHandleIds = page.getAttachmentFileHandleIds();
					for (Iterator<FileHandle> iterator = results.getList().iterator(); iterator.hasNext();) {
						FileHandle handle = iterator.next();
						if (!fileHandleIds.contains(handle.getId()))
							fileHandleIds.add(handle.getId());
					}
					client.updateWikiPage(ownerId, type, page);
				}
			}
			
			UploadResult result = new UploadResult();
			result.setMessage("File upload successfully");
			result.setUploadStatus(UploadStatus.SUCCESS);
//			if (results != null)
//				result.setFileHandleResults(results);
			String out = EntityFactory.createJSONStringForEntity(result);
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.getOutputStream().write(out.getBytes("UTF-8"));
			response.getOutputStream().flush();
		} catch (Exception e) {
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
			return;
		}
	}

	/**
	 * Get the session token
	 * @param request
	 * @return
	 */
	public String getSessionToken(final HttpServletRequest request){
		return tokenProvider.getSessionToken();
	}

	/**
	 * The call was forbidden
	 *
	 * @param response
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public void setForbiddenMessage(HttpServletResponse response)
			throws IOException, UnsupportedEncodingException {
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.getOutputStream().write(
				"No session token found".getBytes("UTF-8"));
		response.getOutputStream().flush();
	}

	/**
	 * Create a new Synapse client.
	 *
	 * @return
	 */
	private Synapse createNewClient(String sessionToken) {
		Synapse client = synapseProvider.createNewClient();
		client.setAuthEndpoint(urlProvider.getPrivateAuthBaseUrl());
		client.setRepositoryEndpoint(urlProvider.getRepositoryServiceUrl());
		client.setSessionToken(sessionToken);
		return client;
	}


}

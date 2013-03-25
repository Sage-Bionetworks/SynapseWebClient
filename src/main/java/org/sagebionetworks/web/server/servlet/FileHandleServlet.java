package org.sagebionetworks.web.server.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
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
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.VariableContentPaginatedResults;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.shared.EntityUtil;

import com.google.common.io.Files;
import com.google.inject.Inject;

/**
 * Handles file handler uploads.
 *
 * @author jay
 *
 */
public class FileHandleServlet extends HttpServlet {

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
		
		String entityId = request.getParameter(DisplayUtils.ENTITY_PARAM_KEY);
		String entityVersion = request.getParameter(DisplayUtils.ENTITY_VERSION_PARAM_KEY);
		
		String ownerId = request.getParameter(DisplayUtils.WIKI_OWNER_ID_PARAM_KEY);
		String ownerType = request.getParameter(DisplayUtils.WIKI_OWNER_TYPE_PARAM_KEY);
		String fileName = request.getParameter(DisplayUtils.WIKI_FILENAME_PARAM_KEY);
		Boolean isPreview = Boolean.parseBoolean(request.getParameter(DisplayUtils.FILE_HANDLE_PREVIEW_PARAM_KEY));
		URL resolvedUrl = null;
		if (ownerId != null && ownerType != null) {
			ObjectType type = ObjectType.valueOf(ownerType);
			String wikiId = request.getParameter(DisplayUtils.WIKI_ID_PARAM_KEY);
			WikiPageKey properKey = new WikiPageKey(ownerId, type, wikiId);

			// Redirect the user to the temp preview url
			
			if (isPreview)
				resolvedUrl = client.getWikiAttachmentPreviewTemporaryUrl(properKey, fileName);
			else
				resolvedUrl = client.getWikiAttachmentTemporaryUrl(properKey, fileName);
			//Done
		}
		else if (entityId != null) {
			if (entityVersion == null) {
				if (isPreview)
					resolvedUrl = client.getFileEntityPreviewTemporaryUrlForCurrentVersion(entityId);
				else
					resolvedUrl = client.getFileEntityTemporaryUrlForCurrentVersion(entityId);
			}
				
			else {
				Long versionNumber = Long.parseLong(entityVersion);
				if (isPreview)
					resolvedUrl = client.getFileEntityPreviewTemporaryUrlForVersion(entityId, versionNumber);
				else
					resolvedUrl = client.getFileEntityTemporaryUrlForVersion(entityId, versionNumber);
			}
		}
		
		if (resolvedUrl != null)
			response.sendRedirect(resolvedUrl.toString());
	}

	
	@Override
	public void doPost(final HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		ServletFileUpload upload = new ServletFileUpload();
		FileHandle newFileHandle = null;
		// Before we do anything make sure we can get the users token
		String token = getSessionToken(request);
		if (token == null) {
			setForbiddenMessage(response);
			return;
		}

		try {
			// Connect to synapse
			Synapse client = createNewClient(token);
			FileItemIterator iter = upload.getItemIterator(request);
			String entityId = null;
			while (iter.hasNext()) {
				FileItemStream item = iter.next();
				String contentType = item.getContentType();
				String name = item.getFieldName();
				InputStream stream = item.openStream();
				String fileName = item.getName();
                File tempDir = Files.createTempDir();
				File temp = new File(tempDir.getAbsolutePath() + File.separator + fileName);

				ServiceUtils.writeToFile(temp, stream, Long.MAX_VALUE);
				try{
					// Now upload the file
					client.setFileEndpoint(StackConfiguration.getFileServiceEndpoint());
					newFileHandle = client.createFileHandle(temp, contentType);
				}finally{
					// Unconditionally delete the tmp file
					temp.delete();
				}
			}

			//and update the wiki page (if the wiki key info was given as parameters) or FileEntity (if entity id was given)
			if (newFileHandle != null) {
				entityId = request.getParameter(DisplayUtils.ENTITY_PARAM_KEY);
				Boolean isCreateEntity = Boolean.parseBoolean(request.getParameter(DisplayUtils.FILE_HANDLE_CREATE_FILEENTITY_PARAM_KEY));
				String ownerId = request.getParameter(DisplayUtils.WIKI_OWNER_ID_PARAM_KEY);
				String ownerType = request.getParameter(DisplayUtils.WIKI_OWNER_TYPE_PARAM_KEY);
				FileEntity fileEntity = null;
				
				if (ownerId != null && ownerType != null) {
					ObjectType type = ObjectType.valueOf(ownerType);
					String wikiId = request.getParameter(DisplayUtils.WIKI_ID_PARAM_KEY);
					WikiPageKey properKey = new WikiPageKey(ownerId, type, wikiId);
					WikiPage page = client.getWikiPage(properKey);
					List<String> fileHandleIds = page.getAttachmentFileHandleIds();
					if (!fileHandleIds.contains(newFileHandle.getId()))
						fileHandleIds.add(newFileHandle.getId());
					client.updateWikiPage(ownerId, type, page);
				}
				else if (isCreateEntity) {
					//create the file entity
					String parentEntityId = request.getParameter(DisplayUtils.FILE_HANDLE_FILEENTITY_PARENT_PARAM_KEY);
					fileEntity = new FileEntity();
					fileEntity.setParentId(parentEntityId);
					fileEntity.setEntityType(FileEntity.class.getName());
					//set data file handle id before creation
					fileEntity.setDataFileHandleId(newFileHandle.getId());
					fileEntity = client.createEntity(fileEntity);
					entityId = fileEntity.getId();
				}
				else if (entityId != null) {
					//get the file entity to update
					fileEntity = (FileEntity) client.getEntityById(entityId);
					//update data file handle id
					fileEntity.setDataFileHandleId(newFileHandle.getId());
					fileEntity = client.putEntity(fileEntity);
				}
				
				if (fileEntity != null) {
					String restrictedParam = request.getParameter(DisplayUtils.IS_RESTRICTED_PARAM_KEY);
					if (restrictedParam==null) throw new RuntimeException("restrictedParam=null");
					boolean isRestricted = Boolean.parseBoolean(restrictedParam);
					String originalFileEntityName = fileEntity.getName();
					try{
						//and try to set the name to the filename
						fileEntity.setName(newFileHandle.getFileName());
						fileEntity = client.putEntity(fileEntity);
					} catch(Throwable t){
						fileEntity.setName(originalFileEntityName);
					};
					
					// now lock down restricted data
					if (isRestricted) {
						// we only proceed if there aren't currently any access restrictions
						VariableContentPaginatedResults<AccessRequirement> currentARs = client.getAccessRequirements(entityId);
						if (currentARs.getTotalNumberOfResults()==0L) {
							AccessRequirement ar = EntityUtil.createLockDownDataAccessRequirement(entityId);
							client.createAccessRequirement(ar);
						}
					}

				}
			}
			
			UploadResult result = new UploadResult();
			if (entityId != null)
				result.setMessage(entityId);
			else
				result.setMessage("File upload successful");
			
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

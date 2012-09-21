package org.sagebionetworks.web.server.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.repo.model.ServiceConstants.AttachmentType;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.attachment.PresignedUrl;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.inject.Inject;

/**
 * Handles user profile upload.
 *
 */
public class UserProfileAttachmentServlet extends HttpServlet {

	public static final int MAX_TIME_OUT = 10 * 1000;
	public static final long BYTES_PER_MEGABYTE = 1048576;
	public static final long MAX_ATTACHMENT_MEGABYTES = 4;
	public static final long MAX_ATTACHMENT_SIZE_IN_BYTES = MAX_ATTACHMENT_MEGABYTES*BYTES_PER_MEGABYTE; // 4 MB
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
			return UserDataProvider.getThreadLocalUserToken(FileAttachmentServlet.perThreadRequest.get());
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
		FileAttachmentServlet.perThreadRequest.set(arg0);
		super.service(arg0, arg1);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String token = getSessionToken(request);

		// Now get the signed url
		Synapse client = createNewClient(token);
		String userId = request.getParameter(DisplayUtils.USER_PROFILE_PARAM_KEY);
		String tokenId = request.getParameter(DisplayUtils.TOKEN_ID_PARAM_KEY);
		try {
			PresignedUrl url = null;
			url = client.waitForPreviewToBeCreated(userId, AttachmentType.USER_PROFILE, tokenId, MAX_TIME_OUT);
			// Redirect the user to the url
			response.sendRedirect(url.getPresignedUrl());
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getOutputStream().write(("Failed to get the pre-signed url"+e.getMessage()).getBytes("UTF-8"));
			response.getOutputStream().flush();
			return;
		}

	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.service(arg0, arg1);
	}

	@Override
	public void doPost(final HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		ServletFileUpload upload = new ServletFileUpload();

		// Before we do anything make sure we can get the users token
		String token = getSessionToken(request);
		if (token == null) {
			setForbiddenMessage(response);
			return;
		}

		try {
			List<AttachmentData> list = new ArrayList<AttachmentData>();
			// Connect to synapse
			Synapse client = createNewClient(token);
			// get user and store file in location
			String userId = request.getParameter(DisplayUtils.USER_PROFILE_PARAM_KEY);
			FileItemIterator iter = upload.getItemIterator(request);
			while (iter.hasNext()) {
				FileItemStream item = iter.next();

				String name = item.getFieldName();
				InputStream stream = item.openStream();
				String fileName = item.getName();
				File temp = ServiceUtils.writeToTempFile(stream, MAX_ATTACHMENT_SIZE_IN_BYTES);
				try{
					// Now upload the file
					AttachmentData data = client.uploadUserProfileAttachmentToSynapse(userId, temp, fileName);
					// If this had a preview then wait for it
					list.add(data);
				}finally{
					// Unconditionally delete the tmp file
					temp.delete();
				}
			}
			// Now add all of the attachments to the entity.
			UserProfile userProfile = client.getUserProfile(userId);
			//set the profile picture
			if (!list.isEmpty())
				userProfile.setPic(list.get(0));
			// Save the changes.
			client.updateMyProfile(userProfile);
			UploadResult result = new UploadResult();
			result.setMessage("File upload successfully");
			result.setUploadStatus(UploadStatus.SUCCESS);
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
		client.setSearchEndpoint(urlProvider.getSearchServiceUrl());
		client.setSessionToken(sessionToken);
		return client;
	}


}

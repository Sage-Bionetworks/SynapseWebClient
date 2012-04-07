package org.sagebionetworks.web.server.servlet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.attachment.PresignedUrl;
import org.sagebionetworks.repo.model.attachment.PreviewState;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.inject.Inject;

/**
 * Handles attachment uplaods.
 * 
 * @author jmhill
 * 
 */
public class FileAttachmentServelet extends HttpServlet {

	public static final int MAX_TIME_OUT = 10 * 1000;
	private static Logger logger = Logger.getLogger(FileUpload.class.getName());
	private static final long serialVersionUID = 1L;

	protected static final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();

	/**
	 * Injected with Gin
	 */
	@SuppressWarnings("unused")
	private ServiceUrlProvider urlProvider;
	private SynapseProvider synapseProvider = new SynapseProviderImpl();

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

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.service(arg0, arg1);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String token = getSessionToken(request);
		if (token == null) {
			setForbiddenMessage(response);
			return;
		}
		// Now get the signed url
		Synapse client = createNewClient(token);
		String entityId = request.getParameter(DisplayUtils.ENTITY_PARAM_KEY);
		String tokenId = request.getParameter(DisplayUtils.TOKEN_ID_PARAM_KEY);
		String fileName = request.getParameter(DisplayUtils.FILE_NAME_PARAM_KEY);
		try {
			PresignedUrl url = client.getAttachmentPresignedUrl(entityId, tokenId);
			// Redirect the user to the url
			if(fileName != null){
				// Tell the browser the name of the file.
				response.setHeader("Content-Disposition", "attachment; filename="+fileName);
			}
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

		// Before we do aything make sure we can get the users token
		String token = getSessionToken(request);
		if (token == null) {
			setForbiddenMessage(response);
			return;
		}

		try {
			List<AttachmentData> list = new ArrayList<AttachmentData>();
			// Connect to syanpse
			Synapse client = createNewClient(token);
			// get Entity and store file in location
			String entityId = request
					.getParameter(DisplayUtils.ENTITY_PARAM_KEY);
			FileItemIterator iter = upload.getItemIterator(request);
			while (iter.hasNext()) {
				FileItemStream item = iter.next();

				String name = item.getFieldName();
				InputStream stream = item.openStream();
				String fileName = item.getName();

				File temp = writeToTempFile(stream);
				// Now upload the file
				AttachmentData data = client.uploadAttachmentToSynapse(entityId, temp, fileName);
				// If this had a preview then wait for it
				list.add(data);
			}
			// Now add all of the attachments to the entity.
			Entity e = client.getEntityById(entityId);
			if (e.getAttachments() == null) {
				e.setAttachments(new ArrayList<AttachmentData>());
			}
			// Add all of the new attachments.
			e.getAttachments().addAll(list);
			// Save the changes.
			client.putEntity(e);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getOutputStream().write(("Failed to attach data: "+e.getMessage()).getBytes("UTF-8"));
			response.getOutputStream().flush();
			return;
		}

	}
	
	/**
	 * Get the session token
	 * @param request
	 * @return
	 */
	public String getSessionToken(final HttpServletRequest request){
		TokenProvider tokenProvider = new TokenProvider() {
			@Override
			public String getSessionToken() {
				return UserDataProvider.getThreadLocalUserToken(request);
			}
		};
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

	/**
	 * Write the data in the passed input stream to a temp file.
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public File writeToTempFile(InputStream stream) throws IOException {
		File temp = File.createTempFile("tempUploadedFile", ".tmp");
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(temp, false));
		try {
			byte[] buffer = new byte[1024];
			int length = 0;
			while ((length = stream.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
		} finally {
			if (out != null) {
				out.close();
			}
		}
		return temp;
	}

}

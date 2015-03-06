package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.inject.Inject;

/**
 * Handles user profile upload.
 *
 */
public class UserProfileAttachmentServlet extends HttpServlet {

	/**
	 * 10 seconds divided by 3.
	 */
	private static final int WAIT_FOR_PRVIEW_MS = 3333;

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
	 * @param fileHandleProvider
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
		super.service(arg0, arg1);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Now get the signed url
		SynapseClient client = createNewClient();
		String userId = request.getParameter(WebConstants.USER_PROFILE_PARAM_KEY);
		/*
		 * We do not need the file ID but adding it to the URL ensures the browser
		 * will fetch a new image if the user's profile picture changes.
		 */
		String fileId = request.getParameter(WebConstants.USER_PROFILE_IMIAGE_ID);
		String previewString = request.getParameter(WebConstants.USER_PROFILE_PREVIEW);
		try {
			boolean preview = true;
			if(previewString != null){
				preview = Boolean.parseBoolean(previewString);
			}
			URL url = getUrlWithWait(client, userId, preview);
			// Redirect the user to the url
			response.sendRedirect(url.toString());
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getOutputStream().write(("Failed to get the pre-signed url"+e.getMessage()).getBytes("UTF-8"));
			response.getOutputStream().flush();
			return;
		}
	}

	private URL getUrlWithWait(SynapseClient client, String userId, boolean preview) throws ClientProtocolException, MalformedURLException, IOException, SynapseException, InterruptedException{
		if(preview){
			return waitForPreview(client, userId);
		}else{
			return client.getUserProfilePictureUrl(userId);
		}
	}
	
	/**
	 * Try three times to get the preview image.
	 * @param client
	 * @param userId
	 * @return
	 * @throws ClientProtocolException
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SynapseException
	 * @throws InterruptedException
	 */
	private URL waitForPreview(SynapseClient client, String userId) throws ClientProtocolException, MalformedURLException, IOException, SynapseException, InterruptedException{
		try{
			return client.getUserProfilePicturePreviewUrl(userId);
		}catch (SynapseNotFoundException e){
			// wait an try again
			Thread.sleep(WAIT_FOR_PRVIEW_MS);
			try{
				return client.getUserProfilePicturePreviewUrl(userId);
			}catch (SynapseNotFoundException e2){
				// wait again
				Thread.sleep(WAIT_FOR_PRVIEW_MS);
				try{
					return client.getUserProfilePicturePreviewUrl(userId);
				}catch (SynapseNotFoundException e3){
					// this is our last chance
					Thread.sleep(WAIT_FOR_PRVIEW_MS);
					// If it fails again, there is no preview.
					return client.getUserProfilePicturePreviewUrl(userId);
				}
			}
		}
	}
	
	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		super.service(arg0, arg1);
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
	private SynapseClient createNewClient() {
		SynapseClient client = synapseProvider.createNewClient();
		client.setAuthEndpoint(urlProvider.getPrivateAuthBaseUrl());
		client.setRepositoryEndpoint(urlProvider.getRepositoryServiceUrl());
		return client;
	}


}

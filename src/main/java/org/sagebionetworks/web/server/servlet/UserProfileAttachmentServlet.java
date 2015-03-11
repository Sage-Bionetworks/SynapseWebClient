package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.client.SynapseClient;
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

	/**
	 * Injected with Gin
	 */
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
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Now get the signed url
		String sessionToken = UserDataProvider.getThreadLocalUserToken(request);
		SynapseClient client = createNewClient(sessionToken);
		String userId = request
				.getParameter(WebConstants.USER_PROFILE_USER_ID);
		String fileId = request
				.getParameter(WebConstants.USER_PROFILE_IMIAGE_ID);
		String previewString = request
				.getParameter(WebConstants.USER_PROFILE_PREVIEW);
		// default to true
		boolean preview = true;
		if (previewString != null) {
			preview = Boolean.parseBoolean(previewString);
		}
		String appliedString = request
				.getParameter(WebConstants.USER_PROFILE_APPLIED);
		// Default to true
		boolean applied = true;
		if (appliedString != null) {
			applied = Boolean.parseBoolean(appliedString);
		}
		try {
			URL url = null;
			if (applied) {
				/*
				 * The file has been applied to the user's profile so anyone can
				 * see it using only the user's id.
				 */
				url = getProfileUrlForUserWithWait(client, userId, preview);
			} else {
				/*
				 * The file has not been applied to the profile so it must be
				 * accessed directly. Only the user that created the file handle
				 * can see this file.
				 */
				url = getFileHandleUrlWithWait(client, fileId);
			}
			// Redirect the user to the url
			response.sendRedirect(url.toString());
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getOutputStream().write(
					("Failed to get the pre-signed url " + e.getMessage())
							.getBytes("UTF-8"));
			response.getOutputStream().flush();
			return;
		}
	}

	/**
	 * Get a URL for a FileHandle ID. Note: Only the creator of the file handle
	 * can successfully use this method.
	 * 
	 * @param client
	 * @param fileId
	 * @return
	 * @throws Exception
	 */
	private URL getFileHandleUrlWithWait(final SynapseClient client,
			final String fileId) throws Exception {
		// Retry until we get a URL or timeout.
		return retryUntilSuccessful(new Callable<URL>() {

			@Override
			public URL call() throws Exception {
				return client.getFileHandleTemporaryUrl(fileId);
			}
		});
	}

	/**
	 * Get a URL for a User's profile picture using the user's ID. Note: Anyone
	 * can access a user's profile picture using the user's ID.
	 * 
	 * @param client
	 * @param userId
	 * @param preview
	 *            If true, then a URL to the preview will be returned. If false
	 *            a URL to actual image will be returned.
	 * @return
	 * @throws Exception
	 */
	private URL getProfileUrlForUserWithWait(final SynapseClient client,
			final String userId, final boolean preview) throws Exception {
		return retryUntilSuccessful(new Callable<URL>() {
			@Override
			public URL call() throws Exception {
				if (preview) {
					return client.getUserProfilePicturePreviewUrl(userId);
				} else {
					return client.getUserProfilePictureUrl(userId);
				}
			}
		});
	}

	/**
	 * Retry calling the passed callable until it succeeds or times out.
	 * 
	 * @param callabled
	 * @return
	 * @throws Exception
	 */
	public <T> T retryUntilSuccessful(Callable<T> callable) throws Exception {
		long start = System.currentTimeMillis();
		while (true) {
			try {
				return callable.call();
			} catch (Exception e) {
				if (System.currentTimeMillis() - start > WAIT_FOR_PRVIEW_MS) {
					throw e;
				}
				// Sleep and try again
				Thread.sleep(1000);
			}
		}
	}

	/**
	 * Create a new Synapse client.
	 * 
	 * @return
	 */
	private SynapseClient createNewClient(String sessionToken) {
		SynapseClient client = synapseProvider.createNewClient();
		client.setAuthEndpoint(urlProvider.getPrivateAuthBaseUrl());
		client.setRepositoryEndpoint(urlProvider.getRepositoryServiceUrl());
		client.setFileEndpoint(StackConfiguration.getFileServiceEndpoint());
		client.setSessionToken(sessionToken);
		return client;
	}

}

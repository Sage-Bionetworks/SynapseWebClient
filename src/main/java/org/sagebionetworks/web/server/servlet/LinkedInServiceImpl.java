package org.sagebionetworks.web.server.servlet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.file.CloudProviderFileHandleInterface;
import org.sagebionetworks.web.client.LinkedInService;
import org.sagebionetworks.web.client.StackEndpoints;
import org.sagebionetworks.web.shared.LinkedInInfo;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.springframework.web.client.RestClientException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class LinkedInServiceImpl extends RemoteServiceServlet implements LinkedInService, TokenProvider {
	private static Logger logger = Logger.getLogger(LinkedInServiceImpl.class.getName());

	public static final long BYTES_PER_MEGABYTE = 1048576;
	public static final long MAX_ATTACHMENT_MEGABYTES = 4;
	public static final long MAX_ATTACHMENT_SIZE_IN_BYTES = MAX_ATTACHMENT_MEGABYTES * BYTES_PER_MEGABYTE; // 4 MB

	// OAuth service for authentication and integration with LinkedIn
	private OAuthService oAuthService;

	private String portalCallbackUrl;

	private SynapseProvider synapseProvider = new SynapseProviderImpl();

	/**
	 * Returns the authorization URL for LinkedIn as well as any exception that occurs and the
	 * requestToken secret
	 */
	@Override
	public LinkedInInfo returnAuthUrl(String callbackUrl) {
		validateService(callbackUrl);

		Token requestToken = oAuthService.getRequestToken();
		String authUrl = oAuthService.getAuthorizationUrl(requestToken);
		LinkedInInfo linkedInInfo = new LinkedInInfo(authUrl, requestToken.getSecret(), null);
		return linkedInInfo;
	}

	@Override
	public UserProfile getCurrentUserInfo(String requestToken, String secret, String verifier, String callbackUrl) {
		validateService(callbackUrl);
		// Create the access token
		Token rToken = new Token(requestToken, secret);
		Verifier v = new Verifier(verifier);
		Token accessToken = oAuthService.getAccessToken(rToken, v);

		// Post a request to LinkedIn to get the user's public information
		// Note: three-current-positions is used for position and company
		OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.linkedin.com/v1/people/~:(id,first-name,last-name,summary,industry,location:(name),positions)");
		oAuthService.signRequest(accessToken, request);
		Response response = request.send();
		// parse the response
		String responseBody = response.getBody();
		UserProfile linkedInProfile = parseLinkedInResponse(responseBody);
		// also ask for the original profile picture url
		request = new OAuthRequest(Verb.GET, "http://api.linkedin.com/v1/people/~/picture-urls::(original)");
		oAuthService.signRequest(accessToken, request);
		response = request.send();
		responseBody = response.getBody();
		// This URL can be used to download a user's picture
		String picUrl = parseLinkedInPictureResponse(responseBody);
		linkedInProfile.setProfilePicureFileHandleId(saveImageToFileHandle(picUrl));
		return linkedInProfile;
	}

	private String saveImageToFileHandle(String imageUrl) {
		if (imageUrl == null) {
			return null;
		}
		SynapseClient client = createSynapseClient();
		// Download from LinkedIn, then upload to Synapse.
		logger.info("Downloading picture from url: " + imageUrl);

		File temp = null;
		URLConnection conn = null;
		try {
			URL url = new URL(imageUrl);
			conn = url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(false);
			temp = ServiceUtils.writeToTempFile(conn.getInputStream(), MAX_ATTACHMENT_SIZE_IN_BYTES);
			// Now upload the file
			CloudProviderFileHandleInterface s3fh = client.multipartUpload(temp, null, false, false);
			return s3fh.getId();
		} catch (Throwable t) {
			// couldn't pull the picture from the external server. log
			// and move on with the update
			logger.log(Level.SEVERE, t.getMessage(), t);
			return null;
		} finally {
			// Unconditionally delete the tmp file and close the input
			// stream
			if (temp != null)
				temp.delete();
			try {
				conn.getInputStream().close();
			} catch (Throwable t) {
				logger.log(Level.SEVERE, t.getMessage(), t);
			}
		}
	}

	/**
	 * Given the linkedin response, parse out profile information and return json representing a
	 * UserProfile object (with LinkedIn information filled in).
	 * 
	 * @param response
	 * @return
	 */
	public static UserProfile parseLinkedInResponse(String response) {
		// sax parsing will work for this small xml string
		UserProfile linkedInProfile = new UserProfile();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(response.getBytes());
			Document doc = db.parse(is);
			String firstName = getLinkedInProfileElementValue(doc, "first-name");
			String lastName = getLinkedInProfileElementValue(doc, "last-name");
			String summary = getLinkedInProfileElementValue(doc, "summary");
			String industry = getLinkedInProfileElementValue(doc, "industry");
			// location is in child element
			// <location><name>locationname</name></location>
			String location = "";
			// parse out position
			StringBuilder position = new StringBuilder();
			// and company
			StringBuilder company = new StringBuilder();

			try {
				location = ((Element) doc.getElementsByTagName("location").item(0)).getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
				Element threeCurrentPositionsElement = (Element) doc.getElementsByTagName("positions").item(0);
				Element positionElement = (Element) threeCurrentPositionsElement.getElementsByTagName("position").item(0);
				position.append(positionElement.getElementsByTagName("title").item(0).getFirstChild().getNodeValue());
				Element companyElement = (Element) positionElement.getElementsByTagName("company").item(0);
				company.append(companyElement.getElementsByTagName("name").item(0).getFirstChild().getNodeValue());
			} catch (Throwable t) {
				// error trying to import position, company, or location. go
				// ahead and send back the profile with partial results
				t.printStackTrace();
			}
			linkedInProfile.setFirstName(firstName);
			linkedInProfile.setLastName(lastName);
			linkedInProfile.setCompany(company.toString());
			linkedInProfile.setIndustry(industry);
			linkedInProfile.setLocation(location);
			linkedInProfile.setPosition(position.toString());
			linkedInProfile.setSummary(summary);
		} catch (Exception e) {
			throw new RestClientException("Unable to obtain LinkedIn profile information.", e);
		}
		return linkedInProfile;
	}

	public static String parseLinkedInPictureResponse(String response) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(response.getBytes());
			Document doc = db.parse(is);

			// get the profile picture data from picture-url
			String picUrl = getLinkedInProfileElementValue(doc, "picture-url");
			return picUrl;
		} catch (Exception e) {
			throw new RestClientException("Unable to obtain LinkedIn profile picture information.", e);
		}
	}

	private static String getLinkedInProfileElementValue(Document linkedInProfile, String elementName) {
		String val = "";
		NodeList elements = linkedInProfile.getElementsByTagName(elementName);
		if (elements.getLength() > 0) {
			Node n = elements.item(0);
			if (n.hasChildNodes())
				val = n.getFirstChild().getNodeValue();
		}
		return val;
	}

	/**
	 * Validate that the service is ready to go. If any of the injected data is missing then it cannot
	 * run. Public for tests.
	 */
	public void validateService(String newCallbackUrl) {
		if (oAuthService == null || !newCallbackUrl.equals(portalCallbackUrl)) {
			portalCallbackUrl = newCallbackUrl;
			// TODO: use repo service for this feature (portal has no access to Synapse credentials.
			// oAuthService = new ServiceBuilder().provider(LinkedInApi.class)
			// .apiKey(StackConfiguration.getPortalLinkedInKey())
			// .apiSecret(StackConfiguration.getPortalLinkedInSecret())
			// .callback(portalCallbackUrl + "#!Profile:").build();
		}
	}

	@Override
	public String getSessionToken() {
		// By default, we get the token from the request cookies.
		return UserDataProvider.getThreadLocalUserToken(this.getThreadLocalRequest());
	}

	private org.sagebionetworks.client.SynapseClient createSynapseClient() {
		return createSynapseClient(getSessionToken());
	}

	/**
	 * The org.sagebionetworks.client.SynapseClient client is stateful so we must create a new one for
	 * each request
	 */
	private SynapseClient createSynapseClient(String sessionToken) {
		// Create a new syanpse
		SynapseClient synapseClient = synapseProvider.createNewClient();
		synapseClient.setSessionToken(sessionToken);
		synapseClient.setRepositoryEndpoint(StackEndpoints.getRepositoryServiceEndpoint());
		synapseClient.setAuthEndpoint(StackEndpoints.getAuthenticationServicePublicEndpoint());
		synapseClient.setFileEndpoint(StackEndpoints.getFileServiceEndpoint());
		// Append the portal's version information to the user agent.
		synapseClient.appendUserAgent(SynapseClientBase.PORTAL_USER_AGENT);
		return synapseClient;
	}
}

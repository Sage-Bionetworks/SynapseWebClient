package org.sagebionetworks.web.server.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.LinkedInService;
import org.sagebionetworks.web.server.RestTemplateProvider;
import org.sagebionetworks.web.shared.LinkedInInfo;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
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
import org.xml.sax.SAXException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;

public class LinkedInServiceImpl extends RemoteServiceServlet implements LinkedInService {
	private static Logger logger = Logger.getLogger(LinkedInServiceImpl.class.getName());
	
	// OAuth service for authentication and integration with LinkedIn
	private OAuthService oAuthService;

	private String portalCallbackUrl;
	
	/**
	 * The template is injected with Gin
	 */
	private RestTemplateProvider templateProvider;

	/**
	 * Injected with Gin
	 */
	private ServiceUrlProvider urlProvider;
		
	/**
	 * Injected via Gin.
	 * 
	 * @param template
	 */
	@Inject
	public void setRestTemplate(RestTemplateProvider template) {
		this.templateProvider = template;
	}
	
	/**
	 * Injected via Gin
	 * @param provider
	 */
	@Inject
	public void setServiceUrlProvider(ServiceUrlProvider provider){
		this.urlProvider = provider;
	}
	
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
	public String getCurrentUserInfo(String requestToken, String secret, String verifier, String callbackUrl) {
		validateService(callbackUrl);
		// Create the access token
		Token rToken = new Token(requestToken, secret);
		Verifier v = new Verifier(verifier);
		Token accessToken = oAuthService.getAccessToken(rToken, v);
		
		// Post a request to LinkedIn to get the user's public information
		// Note: three-current-positions is used for position and company
		OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.linkedin.com/v1/people/~:(id,first-name,last-name,summary,industry,location:(name),three-current-positions,picture-url::(original))");
		oAuthService.signRequest(accessToken, request);
		Response response = request.send();
		//parse the response
		return parseLinkedInResponse(response.getBody());
	}
	
	/**
	 * Given the linkedin response, parse out profile information and return json representing a UserProfile object (with LinkedIn information filled in).
	 * @param response
	 * @return
	 */
	public static String parseLinkedInResponse(String response){
		//sax parsing will work for this small xml string
		String linkedInProfileJson = "";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			UserProfile linkedInProfile = new UserProfile();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(response.getBytes());
			Document doc = db.parse(is);
			String firstName = getLinkedInProfileElementValue(doc, "first-name");
		    String lastName = getLinkedInProfileElementValue(doc, "last-name");
		    String summary = getLinkedInProfileElementValue(doc, "summary");
		    String industry = getLinkedInProfileElementValue(doc, "industry");
		    //location is in child element <location><name>locationname</name></location>
		    String location = "";
		    //parse out position
		    StringBuilder position = new StringBuilder();
		    //and company
		    StringBuilder company = new StringBuilder();
		    
		    try {
		    	location = ((Element)doc.getElementsByTagName("location").item(0)).getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
			    Element threeCurrentPositionsElement = (Element)doc.getElementsByTagName("three-current-positions").item(0);
			    Element positionElement = (Element) threeCurrentPositionsElement.getElementsByTagName("position").item(0);
			    position.append(positionElement.getElementsByTagName("title").item(0).getFirstChild().getNodeValue());
			    Element companyElement = (Element) positionElement.getElementsByTagName("company").item(0);
			    company.append(companyElement.getElementsByTagName("name").item(0).getFirstChild().getNodeValue());
		    }
		    catch (Throwable t) {
		    	//error trying to import position, company, or location. go ahead and send back the profile with partial results
		    	t.printStackTrace();
		    }
			 
			//get the profile picture data from picture-url
		    String picUrl = getLinkedInProfileElementValue(doc, "picture-url");
		    AttachmentData pic = null;
		    //update the profile, if the image is successfully saved.
		    //if url is the only thing set in the AttachmentData, the Synapse client will pull from the url and upload to S3 (and fill in the rest) for me.
		    if (picUrl.length() > 0) {
			    pic = new AttachmentData();
			    pic.setUrl(picUrl);
		    }
		    linkedInProfile.setFirstName(firstName);
		    linkedInProfile.setLastName(lastName);
		    linkedInProfile.setCompany(company.toString());
		    linkedInProfile.setIndustry(industry);
		    linkedInProfile.setLocation(location);
		    linkedInProfile.setPic(pic);
		    linkedInProfile.setPosition(position.toString());
		    linkedInProfile.setSummary(summary);
		    linkedInProfileJson = EntityFactory.createJSONStringForEntity(linkedInProfile);
		}catch(Exception e) {
			throw new RestClientException("Unable to obtain LinkedIn profile information.", e);
		}
		return linkedInProfileJson;
	}
	
	private static String getLinkedInProfileElementValue(Document linkedInProfile, String elementName) {
		String val = "";
		NodeList elements = linkedInProfile.getElementsByTagName(elementName);
		if (elements.getLength() > 0){
			Node n = elements.item(0);
			if (n.hasChildNodes())
				val = n.getFirstChild().getNodeValue();
		}
		return val;
	}

	
	/**
	 * Validate that the service is ready to go. If any of the injected data is
	 * missing then it cannot run. Public for tests.
	 */
	public void validateService(String newCallbackUrl) {
		if (templateProvider == null)
			throw new IllegalStateException(
					"The org.sagebionetworks.web.server.RestTemplateProvider was not injected into this service");
		if (templateProvider.getTemplate() == null)
			throw new IllegalStateException(
					"The org.sagebionetworks.web.server.RestTemplateProvider returned a null template");
		if (urlProvider == null)
			throw new IllegalStateException(
					"The org.sagebionetworks.rest.api.root.url was not set");
		if(oAuthService == null || !newCallbackUrl.equals(portalCallbackUrl)) {
			portalCallbackUrl = newCallbackUrl;
			oAuthService = new ServiceBuilder().provider(LinkedInApi.class)
											   .apiKey(StackConfiguration.getPortalLinkedInKey())
											   .apiSecret(StackConfiguration.getPortalLinkedInSecret())
											   .callback(portalCallbackUrl + "#Profile:")
											   .build();			
		}
	}
}
package org.sagebionetworks.web.server.servlet;

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.sagebionetworks.repo.model.ServiceConstants;
import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.UserAccountService;
import org.sagebionetworks.web.client.security.AuthenticationException;
import org.sagebionetworks.web.server.RestTemplateProvider;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.TermsOfUseException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.users.GetUser;
import org.sagebionetworks.web.shared.users.UserRegistration;
import org.sagebionetworks.web.shared.users.UserSession;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.UnexpectedException;
import com.google.inject.Inject;

public class UserAccountServiceImpl extends RemoteServiceServlet implements UserAccountService, TokenProvider {
	
	public static final long serialVersionUID = 498269726L;

	private static Logger logger = Logger.getLogger(UserAccountServiceImpl.class.getName());
			
	/**
	 * The template is injected with Gin
	 */
	private RestTemplateProvider templateProvider;

	/**
	 * Injected with Gin
	 */
	private ServiceUrlProvider urlProvider;

	private TokenProvider tokenProvider = this;
	
	@SuppressWarnings("unused")
	private SynapseProvider synapseProvider = new SynapseProviderImpl();


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
	 * Injected vid Gin
	 * @param provider
	 */
	@Inject
	public void setServiceUrlProvider(ServiceUrlProvider provider){
		this.urlProvider = provider;
	}

	/**
	 * This allows integration tests to override the token provider.
	 * 
	 * @param tokenProvider
	 */
	public void setTokenProvider(TokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}
	
	
	@Override
	public void sendPasswordResetEmail(String userId) throws RestServiceException {
		// First make sure the service is ready to go.
		validateService();
		
		JSONObject obj = new JSONObject();
		try {
			obj.put("email", userId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// Build up the path
		String url = urlProvider.getPrivateAuthBaseUrl() + "/" + ServiceUtils.AUTHSVC_SEND_PASSWORD_CHANGE_PATH;
		String jsonString = obj.toString();
		
		// Setup the header
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);
		HttpMethod method = HttpMethod.POST;
		
		logger.info(method.toString() + ": " + url + ", JSON: " + jsonString);
		
		// Make the actual call.
		try {
			@SuppressWarnings("unused")
			ResponseEntity<String> response = templateProvider.getTemplate().exchange(url, method, entity, String.class);
			if(response.getBody().equals("")) {
				return;
			}
		} catch (UnexpectedException ex) {
			return;
		} catch (NullPointerException nex) {
			// TODO : change this to properly deal with a 204!!!
			return; // this is expected
		} catch (RestClientException ex) {
			// Not ideal. DELETE returns no content type
			return;
		}		
		
		throw new RestClientException("An error occured. Please try again.");
		
//		if (response.getStatusCode() != HttpStatus.CREATED && response.getStatusCode() != HttpStatus.OK) {
//			throw new RestClientException("Status code:" + response.getStatusCode().value());
//		}						
	}

	public void sendSetApiPasswordEmail(String emailAddress) throws RestServiceException {
		// First make sure the service is ready to go.
		validateService();
		
		JSONObject obj = new JSONObject();
		try {
			obj.put("email", emailAddress);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// Build up the path
		String url = urlProvider.getPrivateAuthBaseUrl() + "/" + ServiceUtils.AUTHSVC_SEND_API_PASSWORD_PATH;
		String jsonString = obj.toString();
		
		// Setup the header
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);
		HttpMethod method = HttpMethod.POST;
		
		logger.info(method.toString() + ": " + url + ", JSON: " + jsonString);
		
		// Make the actual call.
		try {
			@SuppressWarnings("unused")
			ResponseEntity<String> response = templateProvider.getTemplate().exchange(url, method, entity, String.class);
			if(response.getBody().equals("")) {
				return;
			}
		} catch (UnexpectedException ex) {
			return;
		} catch (NullPointerException nex) {
			// TODO : change this to properly deal with a 204!!!
			return; // this is expected
		} catch (RestClientException ex) {
			// Not ideal. DELETE returns no content type
			return;
		}		
	
		
		throw new RestClientException("An error occured. Please try again.");		
	}

	@Override
	public void setRegistrationUserPassword(String registrationToken, String newPassword) {
		// First make sure the service is ready to go.
			validateService();
			
			JSONObject obj = new JSONObject();
			try {
				obj.put("registrationToken", registrationToken);
				obj.put("password", newPassword);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			// Build up the path
			String url = urlProvider.getPrivateAuthBaseUrl() + "/" + ServiceUtils.AUTHSVC_SET_REGISTRATION_USER_PASSWORD_PATH;
			String jsonString = obj.toString();
			
			// Setup the header
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);
			HttpMethod method = HttpMethod.POST;
			
			logger.info(method.toString() + ": " + url + ", with registration token " + registrationToken); 
			
			// Make the actual call.
			try {
				ResponseEntity<String> response = templateProvider.getTemplate().exchange(url, method, entity, String.class);
				if(response.getBody().equals("")) {
					return;
				}
			} catch (RestClientException ex) {
				if (ex.getMessage().toLowerCase().contains("no content-type found"))
					return;
				else throw ex;
			} catch (UnexpectedException ex) {
				return;
			} catch (NullPointerException nex) {
				// TODO : change this to properly deal with a 204!!!
				return; // this is expected
			}
			
			throw new RestClientException("An error occured. Please try again.");
	}

	@Override
	public void setPassword(String email, String newPassword) {
		// First make sure the service is ready to go.
		validateService();
		
		JSONObject obj = new JSONObject();
		try {
			obj.put("email", email);
			obj.put("password", newPassword);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// Build up the path
		String url = urlProvider.getPrivateAuthBaseUrl() + "/" + ServiceUtils.AUTHSVC_SET_PASSWORD_PATH;
		String jsonString = obj.toString();
		
		// Setup the header
		HttpHeaders headers = new HttpHeaders();
		// If the user data is stored in a cookie, then fetch it and the session token to the header.
		UserDataProvider.addUserDataToHeader(this.getThreadLocalRequest(), headers);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);
		HttpMethod method = HttpMethod.POST;
		
		// NOTE: do not log the JSON as it includes the user's new clear text password!
		logger.info(method.toString() + ": " + url + ", for user " + email); 
		
		// Make the actual call.
		try {
			ResponseEntity<String> response = templateProvider.getTemplate().exchange(url, method, entity, String.class);
			if(response.getBody().equals("")) {
				return;
			}
		} catch (RestClientException ex) {
			if (ex.getMessage().toLowerCase().contains("no content-type found"))
				return;
			else throw ex;
		} catch (UnexpectedException ex) {
			return;
		} catch (NullPointerException nex) {
			// TODO : change this to properly deal with a 204!!!
			return; // this is expected
		}
		
		throw new RestClientException("An error occured. Please try again.");
	}

	/**
	 * This needs to be replaced with a Synapse Java Client call
	 */
	@Deprecated
	@Override
	public String initiateSession(String username, String password, boolean explicitlyAcceptsTermsOfUse) throws RestServiceException {
		// First make sure the service is ready to go.
		validateService();
		
		JSONObject obj = new JSONObject();
		try {
			obj.put("email", username);
			obj.put("password", password);
			if (explicitlyAcceptsTermsOfUse) obj.put("acceptsTermsOfUse", "true");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// Build up the path
		String url = urlProvider.getPrivateAuthBaseUrl() + "/" + ServiceUtils.AUTHSVC_INITIATE_SESSION_PATH;
		String jsonString = obj.toString();
		
		// Setup the header
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);
		HttpMethod method = HttpMethod.POST;
		
		logger.info(method.toString() + ": " + url + ", for user " + username); // DO NOT log the entire json string as it includes the user's password
		
		ResponseEntity<UserSession> response = null;
		try {
			response = templateProvider.getTemplate().exchange(url, method, entity, UserSession.class);
		} catch (HttpClientErrorException ex) {
			HttpStatus status = ex.getStatusCode();
			switch (status.value()) {
			case 403:
				String termsOfUseContent = "";
				try {
					String responseBody = ex.getResponseBodyAsString();
					JSONObject json = new JSONObject(responseBody);
					termsOfUseContent = json.getString("reason");
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
				throw new TermsOfUseException(termsOfUseContent);
			default:
				throw new UnauthorizedException(ex.getMessage());
			}
		}
		
		String userSessionJson = null;		
		if((response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) && response.hasBody()) {
			try {
				UserSession initSession = response.getBody();
				//String displayName = initSession.getDisplayName();
				String sessionToken = initSession.getSessionToken();
				UserProfile profile = getUserProfile(sessionToken);
				String fName = null;
				String lName = null;
				if (initSession.getDisplayName() != null) {
					String[] firstLast = initSession.getDisplayName().split(" ");
					if (firstLast.length > 1) {
						fName = firstLast[0];
						//everything else can go in the last name
						lName = initSession.getDisplayName().substring(fName.length()+1).trim();
					}
				}
				
				fillInCrowdInfo(fName, lName, username, profile);
				UserSessionData userData = new UserSessionData();
				userData.setIsSSO(false);
				userData.setSessionToken(sessionToken);
				userData.setProfile(profile);
				userSessionJson = EntityFactory.createJSONStringForEntity(userData);
			} catch (JSONObjectAdapterException e) {
				e.printStackTrace();
				throw new UnauthorizedException(e.getMessage());
			}
		} else {			
			throw new UnauthorizedException("Unable to authenticate.");
		}
		return userSessionJson;			
	}

	private void fillInCrowdInfo(String fName, String lName, String email, UserProfile profile)
	{
		profile.setUserName(email);
		if (profile.getFirstName() == null || profile.getFirstName().length() == 0){
			profile.setFirstName(fName);
		}
		if (profile.getLastName() == null || profile.getLastName().length() == 0){
			profile.setLastName(lName);
		}
		if (profile.getEmail() == null || profile.getEmail().length() == 0){
			profile.setEmail(email);
		}
	}
	
	/**
	 * This needs to be replaced with a Synapse Java Client call
	 */
	@Deprecated
	@Override
	public String getUser(String sessionToken) throws AuthenticationException, RestServiceException {
		// First make sure the service is ready to go.
		validateService();
		
		// Build up the path
		String url = urlProvider.getPrivateAuthBaseUrl() + "/" + ServiceUtils.AUTHSVC_GET_USER_PATH;				
		
		// Setup the header
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(UserDataProvider.SESSION_TOKEN_KEY, sessionToken);
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		HttpMethod method = HttpMethod.GET;
		
		logger.info(method.toString() + ": " + url); 
		
		ResponseEntity<GetUser> response = null;
		try {
			response = templateProvider.getTemplate().exchange(url, method, entity, GetUser.class);
		} catch (HttpClientErrorException ex) {
			throw new AuthenticationException("Unable to authenticate.");
		}
		
		String userSessionJson = null;
		if((response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) && response.hasBody()) {
			try {
				GetUser getUser = response.getBody();
				UserProfile profile = getUserProfile(sessionToken);
				fillInCrowdInfo(getUser.getFirstName(), getUser.getLastName(), getUser.getEmail(), profile);
				UserSessionData userData = new UserSessionData();
				userData.setIsSSO(false);
				userData.setSessionToken(sessionToken);
				userData.setProfile(profile);
				userSessionJson = EntityFactory.createJSONStringForEntity(userData);
			} catch (JSONObjectAdapterException e) {
				throw new AuthenticationException(e);
			}
		} else {			
			throw new AuthenticationException("Unable to authenticate.");
		}
		return userSessionJson;		
	}	

	
	@Override
	public void createUser(UserRegistration userInfo) throws RestServiceException {
		// First make sure the service is ready to go.
		validateService();
		
		JSONObject obj = new JSONObject();
		try {
			obj.put("email", userInfo.getEmail());
			obj.put("firstName", userInfo.getFirstName());
			obj.put("lastName", userInfo.getLastName());
			obj.put("displayName", userInfo.getDisplayName());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// Build up the path
		String url = urlProvider.getPrivateAuthBaseUrl() + "/" + ServiceUtils.AUTHSVC_CREATE_USER_PATH;
		String jsonString = obj.toString();
		
		// Setup the header
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);
		HttpMethod method = HttpMethod.POST;
		
		logger.info(method.toString() + ": " + url + ", JSON: " + jsonString);
		ResponseEntity<String> response;
		try {
			response = templateProvider.getTemplate().exchange(url, method, entity, String.class);
		} catch (RestClientException ex) {
			if (ex.getMessage().toLowerCase().contains("no content-type found"))
				return;
			else throw ex;
		}

		if (response.getStatusCode() != HttpStatus.CREATED && response.getStatusCode() != HttpStatus.OK) {
			if(response.getStatusCode() == HttpStatus.BAD_REQUEST) {
				throw new BadRequestException(response.getBody());
			}
			
			// all other exceptions are general
			throw new RestClientException("Status code:" + response.getStatusCode().value());
		}		
	}


	/**
	 * This needs to be replaced with a Synapse Java Client call
	 */
	@Deprecated
	@Override
	public void updateUser(String firstName, String lastName, String displayName) throws RestServiceException {
		// First make sure the service is ready to go.
		validateService();
		
		JSONObject obj = new JSONObject();
		try {
			obj.put("firstName", firstName);
			obj.put("lastName", lastName);
			obj.put("displayName", displayName);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// Build up the path
		String url = urlProvider.getPrivateAuthBaseUrl() + "/" + ServiceUtils.AUTHSVC_UPDATE_USER_PATH;
		String jsonString = obj.toString();
		
		// Setup the header
		HttpHeaders headers = new HttpHeaders();
		// If the user data is stored in a cookie, then fetch it and the session token to the header.
		UserDataProvider.addUserDataToHeader(this.getThreadLocalRequest(), headers);
//		headers.set(DisplayConstants.SERVICE_HEADER_ETAG_KEY, "1");
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);
		HttpMethod method = HttpMethod.PUT;
		
		logger.info(method.toString() + ": " + url + ", JSON: " + jsonString);
		
		// Make the actual call.
		try {
			ResponseEntity<String> response = templateProvider.getTemplate().exchange(url, method, entity, String.class);
		} catch (NullPointerException nex) {
			// TODO : change this to properly deal with a 204!!!
		}
	}
	
	@Override
	public void terminateSession(String sessionToken) throws RestServiceException {
		// First make sure the service is ready to go.
		validateService();
		
		// Build up the path
		String url = urlProvider.getPrivateAuthBaseUrl() + "/" + ServiceUtils.AUTHSVC_TERMINATE_SESSION_PATH;
		String jsonString = "{\"sessionToken\":\""+ sessionToken + "\"}";
		
		logger.info("DELETE: " + url + ", JSON: " + jsonString);
		
		// Setup the header
		HttpHeaders headers = new HttpHeaders();
		// If the user data is stored in a cookie, then fetch it and the session token to the header.
		UserDataProvider.addUserDataToHeader(this.getThreadLocalRequest(), headers);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);
		HttpMethod method = HttpMethod.DELETE;
		
		// Make the actual call.
		ResponseEntity<String> response = templateProvider.getTemplate().exchange(url, method, entity, String.class);

		if (response.getStatusCode() == HttpStatus.OK) {
		} else {
			throw new RestClientException("Status code:" + response.getStatusCode().value());
		}		
	}

	/**
	 * This needs to be replaced with a Synapse Java Client call
	 */
	@Deprecated
	@Override
	public boolean ssoLogin(String sessionToken) throws RestServiceException {
		// First make sure the service is ready to go.
		validateService();
		
		JSONObject obj = new JSONObject();
		try {
			obj.put("sessionToken", sessionToken);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// Build up the path
		String url = urlProvider.getPrivateAuthBaseUrl() + "/" + ServiceUtils.AUTHSVC_REFRESH_SESSION_PATH;
		String jsonString = obj.toString();
		
		// Setup the header
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);
		HttpMethod method = HttpMethod.PUT;
		
		logger.info(method.toString() + ": " + url + ", JSON: " + jsonString);
		
		// Make the actual call.
		try {
			ResponseEntity<String> response = templateProvider.getTemplate().exchange(url, method, entity, String.class);
		} catch (UnexpectedException ex) {
			return true;
		} catch (NullPointerException nex) {
			// TODO : change this to properly deal with a 204!!!
			return true; // this is expected
		} catch (HttpClientErrorException ex) {
			HttpStatus status = ex.getStatusCode();
			switch (status.value()) {
			case 403:
				throw new TermsOfUseException("Please log and sign the Terms of Use.");
			default:
				throw new UnauthorizedException(ex.getMessage());
			}
		}
		return false;
	}
	



	@Override
	public String getPrivateAuthServiceUrl() {
		return urlProvider.getPrivateAuthBaseUrl();
	}

	@Override
	public String getPublicAuthServiceUrl() {
		return urlProvider.getPublicAuthBaseUrl();
	}

	@Override
	public String getSynapseWebUrl() {
		return urlProvider.getPortalBaseUrl();
	}

	/**
	 * Validate that the service is ready to go. If any of the injected data is
	 * missing then it cannot run. Public for tests.
	 */
	public void validateService() {
		if (templateProvider == null)
			throw new IllegalStateException(
					"The org.sagebionetworks.web.server.RestTemplateProvider was not injected into this service");
		if (templateProvider.getTemplate() == null)
			throw new IllegalStateException(
					"The org.sagebionetworks.web.server.RestTemplateProvider returned a null template");
		if (urlProvider == null)
			throw new IllegalStateException(
					"The org.sagebionetworks.rest.api.root.url was not set");
		if (tokenProvider == null) {
			throw new IllegalStateException("The token provider was not set");
		}
	}

	
	/*
	 * Private Methods
	 */
	private String getJsonStringForUrl(String url, HttpMethod method) {
		// First make sure the service is ready to go.
		validateService();

		logger.info(method.toString() + ": " + url);
		
		// Setup the header
		HttpHeaders headers = new HttpHeaders();
		// If the user data is stored in a cookie, then fetch it and the session token to the header.
		UserDataProvider.addUserDataToHeader(this.getThreadLocalRequest(), headers);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		
		// Make the actual call.
		ResponseEntity<String> response = templateProvider.getTemplate().exchange(url, method, entity, String.class);

		if (response.getStatusCode() == HttpStatus.OK) {			
			return response.getBody();
		} else {
			// TODO: better error handling
			throw new UnknownError("Status code:"
					+ response.getStatusCode().value());
		}
		
	}
	
	/**
	 * This needs to be replaced with a Synapse Java Client call
	 */
	@Deprecated
	@Override
	public String getTermsOfUse() {
		// call Synapse client instead of making its own call
		String url = getPrivateAuthServiceUrl() + "/termsOfUse.html";
		ResponseEntity<String> response = templateProvider.getTemplate().exchange(url, HttpMethod.GET, null, String.class);
		return response.getBody();
	}
	
	@Override
	public String getSessionToken() {
		// By default, we get the token from the request cookies.
		return UserDataProvider.getThreadLocalUserToken(this
				.getThreadLocalRequest());
	}
	
	/**
	 * The synapse client is stateful so we must create a new one for each
	 * request
	 */
	private Synapse createSynapseClient() {
		return createSynapseClient(null);
	}

	private Synapse createSynapseClient(String sessionToken) {
		// Create a new syanpse
		Synapse synapseClient = synapseProvider.createNewClient();
		if(sessionToken == null) {
			sessionToken = tokenProvider.getSessionToken();
		}
		synapseClient.setSessionToken(sessionToken);
		synapseClient.setRepositoryEndpoint(urlProvider
				.getRepositoryServiceUrl());
		synapseClient.setAuthEndpoint(urlProvider.getPublicAuthBaseUrl());
		return synapseClient;
	}

	private UserProfile getUserProfile(String sessionToken) throws RestServiceException {
		Synapse synapseClient = createSynapseClient(sessionToken);
		UserProfile userProfile;
		try {
			userProfile = synapseClient.getMyProfile();
		} catch (SynapseException e) {
			throw new RestServiceException(e.getMessage());
		}
		return userProfile;
	}


}

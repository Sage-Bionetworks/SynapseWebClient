package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseTermsOfUseException;
import org.sagebionetworks.repo.model.AuthorizationConstants;
import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.UserGroup;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.NewUser;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.UserAccountService;
import org.sagebionetworks.web.client.security.AuthenticationException;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.TermsOfUseException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.users.UserRegistration;
import org.springframework.web.client.RestClientException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;

public class UserAccountServiceImpl extends RemoteServiceServlet implements UserAccountService, TokenProvider {
	
	public static final long serialVersionUID = 498269726L;
	
	public static PublicPrincipalIds publicPrincipalIds = null;

	/**
	 * Injected with Gin
	 */
	private ServiceUrlProvider urlProvider;

	private TokenProvider tokenProvider = this;
	
	private SynapseProvider synapseProvider = new SynapseProviderImpl();
	
	/**
	 * Injected with Gin
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
		validateService();
		
		SynapseClient client = createAnonymousSynapseClient();
		try {
			client.sendPasswordResetEmail(userId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public void sendSetApiPasswordEmail() throws RestServiceException {
		validateService();
		
		SynapseClient client = createSynapseClient();
		try {
			client.sendPasswordResetEmail();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void setRegistrationUserPassword(String registrationToken, String newPassword) {
		validateService();
		
		SynapseClient client = createAnonymousSynapseClient();
		try {
			String sessionToken = registrationToken.substring(AuthorizationConstants.REGISTRATION_TOKEN_PREFIX.length());
			client.changePassword(sessionToken, newPassword);
		} catch (SynapseException e) {
			throw new RestClientException("Password change failed", e);
		}
	}
	
	
	@Override
	public void changeEmailAddress(String changeEmailToken, String newPassword) {
		validateService();
		
		SynapseClient client = createAnonymousSynapseClient();
		try {
			String sessionToken = changeEmailToken.substring(AuthorizationConstants.CHANGE_EMAIL_TOKEN_PREFIX.length());
			client.changeEmail(sessionToken, newPassword);
		} catch (SynapseException e) {
			throw new RestClientException("Email change failed", e);
		}
	}


	@Override
	public void setPassword(String newPassword) {
		validateService();

		SynapseClient client = createSynapseClient();
		try {
			client.changePassword(newPassword);
		} catch (SynapseException e) {
			throw new RestClientException("Password change failed", e);
		}
	}

	@Override
	public String initiateSession(String username, String password, boolean explicitlyAcceptsTermsOfUse) throws RestServiceException {
		validateService();
		
		SynapseClient synapseClient = createSynapseClient();
		String userSessionJson = null;
		try {
			UserSessionData userData = synapseClient.login(username, password, explicitlyAcceptsTermsOfUse);
			userSessionJson = EntityFactory.createJSONStringForEntity(userData);
		} catch (JSONObjectAdapterException e) {
			e.printStackTrace();
			throw new UnauthorizedException(e.getMessage());
		} catch (SynapseTermsOfUseException e) {
			throw new TermsOfUseException(e.getMessage());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
		
		return userSessionJson;
	}
	
	@Override
	public String getUser(String sessionToken) throws AuthenticationException, RestServiceException {
		validateService();
		
		String userSessionJson = null;
		try {
			UserSessionData userData = getUserSessionData(sessionToken);
			userSessionJson = EntityFactory.createJSONStringForEntity(userData);
		} catch (JSONObjectAdapterException e) {
			e.printStackTrace();
			throw new UnauthorizedException(e.getMessage());
		} catch (SynapseTermsOfUseException e) {
			throw new TermsOfUseException(e.getMessage());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
		
		return userSessionJson;
	}	
	
	private UserSessionData getUserSessionData(String sessionToken) throws SynapseException{
		SynapseClient synapseClient = createSynapseClient(sessionToken);
		return synapseClient.getUserSessionData();
	}

	
	@Override
	public void createUser(UserRegistration userInfo) throws RestServiceException {
		validateService();

		SynapseClient client = createAnonymousSynapseClient();
		NewUser user = new NewUser();
		user.setEmail(userInfo.getEmail());
		user.setFirstName(userInfo.getFirstName());
		user.setLastName(userInfo.getLastName());
		user.setDisplayName(userInfo.getDisplayName());
		try {
			client.createUser(user);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public void terminateSession(String sessionToken) throws RestServiceException {
		validateService();

		SynapseClient client = createSynapseClient();
		try {
			client.logout();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public String getStorageUsage() {
		validateService();

		SynapseClient client = createSynapseClient();
		try {
			return EntityFactory.createJSONStringForEntity(client.getStorageUsageSummary(null));
		} catch (SynapseException e) {
			throw new RestClientException("Unable to get storage usage", e);
		} catch (JSONObjectAdapterException e) {
			throw new RestClientException("Unable to get storage usage", e);
		}
	}

	
	@Override
	public String getPrivateAuthServiceUrl() {
		return urlProvider.getPrivateAuthBaseUrl();
	}

	@Override
	public String getPublicAuthServiceUrl() {
		return urlProvider.getPublicAuthBaseUrl();
	}

	/**
	 * Validate that the service is ready to go. If any of the injected data is
	 * missing then it cannot run. Public for tests.
	 */
	public void validateService() {
		if (urlProvider == null) {
			throw new IllegalStateException("The org.sagebionetworks.rest.api.root.url was not set");
		}
		if (tokenProvider == null) {
			throw new IllegalStateException("The token provider was not set");
		}
	}

	@Deprecated
	@Override
	public String getTermsOfUse() {
		SynapseClient client = createAnonymousSynapseClient();
		try {
			return client.getSynapseTermsOfUse();
		} catch (SynapseException e) {
			throw new RestClientException("Unable to get Synapse's terms of use", e);
		}
	}
	
	@Override
	public String getSessionToken() {
		// By default, we get the token from the request cookies.
		return UserDataProvider.getThreadLocalUserToken(this
				.getThreadLocalRequest());
	}
	
	@Override
	public PublicPrincipalIds getPublicAndAuthenticatedGroupPrincipalIds() {
		if (publicPrincipalIds == null) {
			try {
				validateService();
				SynapseClient synapseClient = createSynapseClient();
				SynapseClient anonymousClient = createAnonymousSynapseClient();
				UserProfile anonymousProfile = anonymousClient.getMyProfile();
				String anonymousPrincipalId = anonymousProfile.getOwnerId();
				initPublicAndAuthenticatedPrincipalIds(synapseClient, anonymousPrincipalId);
			} catch (Exception e) {
				throw new RestClientException(e.getMessage());
			}
		}
		return publicPrincipalIds;
	}
	
	public static void initPublicAndAuthenticatedPrincipalIds(SynapseClient synapseClient, String anonymousPrincipalId) {
		try {
			//TODO:  change to synapseClient.getUserGroupHeadersByPrefix() after exposure?
			PublicPrincipalIds results = new PublicPrincipalIds();
			results.setAnonymousUserId(Long.parseLong(anonymousPrincipalId));
			PaginatedResults<UserGroup> allGroups = synapseClient.getGroups(0, Integer.MAX_VALUE);
			
			for (UserGroup userGroup : allGroups.getResults()) {
				if (userGroup.getName() != null){
					if (userGroup.getName().equals(AuthorizationConstants.DEFAULT_GROUPS.PUBLIC.name()))
						results.setPublicAclPrincipalId(Long.parseLong(userGroup.getId()));
					else if (userGroup.getName().equals(AuthorizationConstants.DEFAULT_GROUPS.AUTHENTICATED_USERS.name()))
						results.setAuthenticatedAclPrincipalId(Long.parseLong(userGroup.getId()));
				}
			}
			
			publicPrincipalIds = results;
		} catch (Exception e) {
			throw new RestClientException(e.getMessage());
		}
	}
	
	/**
	 * The synapse client is stateful so we must create a new one for each
	 * request
	 */
	private SynapseClient createSynapseClient() {
		return createSynapseClient(null);
	}

	private SynapseClient createSynapseClient(String sessionToken) {
		// Create a new syanpse
		SynapseClient synapseClient = synapseProvider.createNewClient();
		if(sessionToken == null) {
			sessionToken = tokenProvider.getSessionToken();
		}
		synapseClient.setSessionToken(sessionToken);
		synapseClient.setRepositoryEndpoint(urlProvider
				.getRepositoryServiceUrl());
		synapseClient.setAuthEndpoint(urlProvider.getPublicAuthBaseUrl());
		return synapseClient;
	}
	
	private SynapseClient createAnonymousSynapseClient() {
		SynapseClient synapseClient = synapseProvider.createNewClient();
		synapseClient.setRepositoryEndpoint(urlProvider
				.getRepositoryServiceUrl());
		synapseClient.setAuthEndpoint(urlProvider.getPublicAuthBaseUrl());
		return synapseClient;
	}
}

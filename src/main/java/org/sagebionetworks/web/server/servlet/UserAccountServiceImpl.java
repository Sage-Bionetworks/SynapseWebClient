package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.AuthorizationConstants;
import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.UserGroup;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.NewUser;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.UserAccountService;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
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
	 */
	@Inject
	public void setServiceUrlProvider(ServiceUrlProvider provider){
		this.urlProvider = provider;
	}

	/**
	 * This allows integration tests to override the token provider.
	 */
	public void setTokenProvider(TokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	/**
	 * Validate that the service is ready to go. If any of the injected data is
	 * missing then it cannot run. Public for tests.
	 */
	private void validateService() {
		if (urlProvider == null) {
			throw new IllegalStateException("The org.sagebionetworks.rest.api.root.url was not set");
		}
		if (tokenProvider == null) {
			throw new IllegalStateException("The token provider was not set");
		}
	}
	
	@Override
	public void sendPasswordResetEmail(String emailAddress) throws RestServiceException {
		validateService();
		
		SynapseClient client = createAnonymousSynapseClient();
		try {
			client.sendPasswordResetEmail(emailAddress);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void changePassword(String sessionToken, String newPassword) {
		validateService();
		
		SynapseClient client = createAnonymousSynapseClient();
		try {
			client.changePassword(sessionToken, newPassword);
		} catch (SynapseException e) {
			throw new RestClientException("Password change failed", e);
		}
	}

	@Override
	public String initiateSession(String username, String password) throws RestServiceException {
		validateService();
		
		SynapseClient synapseClient = createSynapseClient();
		try {
			Session session = synapseClient.login(username, password);
			return EntityFactory.createJSONStringForEntity(session);
		} catch (JSONObjectAdapterException e) {
			throw new UnauthorizedException(e.getMessage());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override 
	public String getUserSessionData(String sessionToken) throws RestServiceException {
		validateService();
		
		SynapseClient synapseClient = createSynapseClient(sessionToken);
		try {
			UserSessionData userData = synapseClient.getUserSessionData();
			return EntityFactory.createJSONStringForEntity(userData);
		} catch (JSONObjectAdapterException e) {
			throw new UnauthorizedException(e.getMessage());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public void signTermsOfUse(String sessionToken, boolean acceptsTerms) throws RestServiceException {
		validateService();
		
		SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.signTermsOfUse(sessionToken, acceptsTerms);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
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
	public String getPrivateAuthServiceUrl() {
		return urlProvider.getPrivateAuthBaseUrl();
	}

	@Override
	public String getPublicAuthServiceUrl() {
		return urlProvider.getPublicAuthBaseUrl();
	}

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
//					if (userGroup.getName().equals(AuthorizationConstants.DEFAULT_GROUPS.PUBLIC.name()))
//						results.setPublicAclPrincipalId(Long.parseLong(userGroup.getId()));
//					else if (userGroup.getName().equals(AuthorizationConstants.DEFAULT_GROUPS.AUTHENTICATED_USERS.name()))
//						results.setAuthenticatedAclPrincipalId(Long.parseLong(userGroup.getId()));
				}
			}
			
			publicPrincipalIds = results;
		} catch (Exception e) {
			throw new RestClientException(e.getMessage());
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
	
	/**
	 * The synapse client is stateful so we must create a new one for each request
	 */
	private SynapseClient createSynapseClient() {
		return createSynapseClient(null);
	}

	private SynapseClient createSynapseClient(String sessionToken) {
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

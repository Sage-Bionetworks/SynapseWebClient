package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.AuthorizationConstants;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.LoginRequest;
import org.sagebionetworks.repo.model.auth.LoginResponse;
import org.sagebionetworks.repo.model.auth.NewUser;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.principal.AccountSetupInfo;
import org.sagebionetworks.repo.model.storage.StorageUsageSummaryList;
import org.sagebionetworks.web.client.UserAccountService;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.UserLoginBundle;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.springframework.web.client.RestClientException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;

public class UserAccountServiceImpl extends RemoteServiceServlet implements UserAccountService, TokenProvider {
	
	public static final long serialVersionUID = 498269726L;
	
	/**
	 * Injected with Gin
	 */
	private ServiceUrlProvider urlProvider;

	private TokenProvider tokenProvider = this;
	
	private SynapseProvider synapseProvider = new SynapseProviderImpl();
	
	public static PublicPrincipalIds publicPrincipalIds = null;
	
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
	 * This allows tests provide mock org.sagebionetworks.client.SynapseClient
	 * 
	 * @param provider
	 */
	public void setSynapseProvider(SynapseProvider provider) {
		this.synapseProvider = provider;
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
	public void changePassword(String sessionToken, String newPassword) throws RestServiceException {
		validateService();
		
		SynapseClient client = createAnonymousSynapseClient();
		try {
			client.changePassword(sessionToken, newPassword);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public LoginResponse initiateSession(LoginRequest loginRequest) throws RestServiceException {
		validateService();
		SynapseClient synapseClient = createAnonymousSynapseClient();
		try {
			return synapseClient.login(loginRequest);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override 
	public UserSessionData getUserSessionData(String sessionToken) throws RestServiceException {
		validateService();
		SynapseClient synapseClient = createSynapseClient(sessionToken);
		try {
			return synapseClient.getUserSessionData();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override 
	public UserLoginBundle getUserLoginBundle(String sessionToken) throws RestServiceException {
		validateService();
		SynapseClient synapseClient = createSynapseClient(sessionToken);
		try {
			UserSessionData userSessionData = synapseClient.getUserSessionData();
			UserBundle userBundle = null;
			if (userSessionData.getSession().getAcceptsTermsOfUse()) {
				long principalId = Long.valueOf(userSessionData.getProfile().getOwnerId());
				// 63 is the mask equivalent for getting every UserBundle component
				userBundle = synapseClient.getUserBundle(principalId, 63);
			}
			return new UserLoginBundle(userSessionData, userBundle);
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
	public void createUserStep1(String email, String portalEndpoint) throws RestServiceException {
		validateService();

		SynapseClient client = createAnonymousSynapseClient();
		NewUser user = new NewUser();
		user.setEmail(email);
		try {
			client.newAccountEmailValidation(user, portalEndpoint);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public String createUserStep2(String userName, String fName, String lName, String password, String emailValidationToken) throws RestServiceException {
		validateService();

		SynapseClient client = createAnonymousSynapseClient();
		try {
			AccountSetupInfo accountSetup = new AccountSetupInfo();
			accountSetup.setFirstName(fName);
			accountSetup.setLastName(lName);
			accountSetup.setUsername(userName);
			accountSetup.setPassword(password);
			accountSetup.setEmailValidationToken(emailValidationToken);
			Session s = client.createNewAccount(accountSetup);
			return s.getSessionToken();
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
				initPublicAndAuthenticatedPrincipalIds();
			} catch (Exception e) {
				throw new RestClientException(e.getMessage());
			}
		}
		return publicPrincipalIds;
	}

	public static void initPublicAndAuthenticatedPrincipalIds() {
		try {
			PublicPrincipalIds results = new PublicPrincipalIds();
			results.setPublicAclPrincipalId(AuthorizationConstants.BOOTSTRAP_PRINCIPAL.PUBLIC_GROUP.getPrincipalId());
			results.setAuthenticatedAclPrincipalId(AuthorizationConstants.BOOTSTRAP_PRINCIPAL.AUTHENTICATED_USERS_GROUP.getPrincipalId());
			results.setAnonymousUserId(AuthorizationConstants.BOOTSTRAP_PRINCIPAL.ANONYMOUS_USER.getPrincipalId());
			
			publicPrincipalIds = results;
		} catch (Exception e) {
			throw new RestClientException(e.getMessage());
		}
	}
	
	@Override
	public StorageUsageSummaryList getStorageUsage() {
		validateService();

		SynapseClient client = createSynapseClient();
		try {
			return client.getStorageUsageSummary(null);
		} catch (SynapseException e) {
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

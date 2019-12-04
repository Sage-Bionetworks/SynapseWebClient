package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.AuthorizationConstants;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.NewUser;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.principal.AccountSetupInfo;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;
import org.sagebionetworks.web.client.StackEndpoints;
import org.sagebionetworks.web.client.UserAccountService;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.springframework.web.client.RestClientException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class UserAccountServiceImpl extends RemoteServiceServlet implements UserAccountService, TokenProvider {

	public static final long serialVersionUID = 498269726L;

	private TokenProvider tokenProvider = this;

	private SynapseProvider synapseProvider = new SynapseProviderImpl();

	public static PublicPrincipalIds publicPrincipalIds = null;

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
	 * Validate that the service is ready to go. If any of the injected data is missing then it cannot
	 * run. Public for tests.
	 */
	private void validateService() {
		if (tokenProvider == null) {
			throw new IllegalStateException("The token provider was not set");
		}
	}

	@Override
	public String getCurrentSessionToken() throws RestServiceException {
		validateService();
		String sessionToken = tokenProvider.getSessionToken();
		try {
			if (sessionToken != null) {
				// validate
				createSynapseClient(sessionToken).getUserSessionData();
			}
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
		return sessionToken;
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
	public void createUserStep1(NewUser newUser, String portalEndpoint) throws RestServiceException {
		validateService();

		SynapseClient client = createAnonymousSynapseClient();
		try {
			client.newAccountEmailValidation(newUser, portalEndpoint);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public UserSessionData getCurrentUserSessionData() throws RestServiceException {
		validateService();

		SynapseClient client = createSynapseClient();
		try {
			return client.getUserSessionData();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}


	@Override
	public String createUserStep2(String userName, String fName, String lName, String password, EmailValidationSignedToken emailValidationSignedToken) throws RestServiceException {
		validateService();

		SynapseClient client = createAnonymousSynapseClient();
		try {
			AccountSetupInfo accountSetup = new AccountSetupInfo();
			accountSetup.setFirstName(fName);
			accountSetup.setLastName(lName);
			accountSetup.setUsername(userName);
			accountSetup.setPassword(password);
			accountSetup.setEmailValidationSignedToken(emailValidationSignedToken);
			Session s = client.createNewAccount(accountSetup);
			return s.getSessionToken();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public String getSessionToken() {
		// By default, we get the token from the request cookies.
		return UserDataProvider.getThreadLocalUserToken(this.getThreadLocalRequest());
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

	/**
	 * The synapse client is stateful so we must create a new one for each request
	 */
	private SynapseClient createSynapseClient() {
		return createSynapseClient(null);
	}

	private SynapseClient createSynapseClient(String sessionToken) {
		SynapseClient synapseClient = synapseProvider.createNewClient();
		if (sessionToken == null) {
			sessionToken = tokenProvider.getSessionToken();
		}
		synapseClient.setSessionToken(sessionToken);
		synapseClient.setRepositoryEndpoint(StackEndpoints.getRepositoryServiceEndpoint());
		synapseClient.setAuthEndpoint(StackEndpoints.getAuthenticationServicePublicEndpoint());
		return synapseClient;
	}

	private SynapseClient createAnonymousSynapseClient() {
		SynapseClient synapseClient = synapseProvider.createNewClient();
		synapseClient.setRepositoryEndpoint(StackEndpoints.getRepositoryServiceEndpoint());
		synapseClient.setAuthEndpoint(StackEndpoints.getAuthenticationServicePublicEndpoint());
		return synapseClient;
	}
}

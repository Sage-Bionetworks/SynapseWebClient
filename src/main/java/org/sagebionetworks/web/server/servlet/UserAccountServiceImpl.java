package org.sagebionetworks.web.server.servlet;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.AuthorizationConstants;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.auth.LoginResponse;
import org.sagebionetworks.repo.model.auth.NewUser;
import org.sagebionetworks.repo.model.principal.AccountSetupInfo;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;
import org.sagebionetworks.web.client.UserAccountService;
import org.sagebionetworks.web.server.StackEndpoints;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.springframework.web.client.RestClientException;

public class UserAccountServiceImpl
  extends RemoteServiceServlet
  implements UserAccountService, TokenProvider, RequestHostProvider {

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
  public void signTermsOfUse(String accessToken) throws RestServiceException {
    validateService();

    SynapseClient synapseClient = createSynapseClient();
    try {
      synapseClient.signTermsOfUse(accessToken);
    } catch (SynapseException e) {
      throw ExceptionUtil.convertSynapseException(e);
    }
  }

  @Override
  public void createUserStep1(NewUser newUser, String portalEndpoint)
    throws RestServiceException {
    validateService();

    SynapseClient client = createAnonymousSynapseClient();
    try {
      client.newAccountEmailValidation(newUser, portalEndpoint);
    } catch (SynapseException e) {
      throw ExceptionUtil.convertSynapseException(e);
    }
  }

  @Override
  public String createUserStep2(
    String userName,
    String fName,
    String lName,
    String password,
    EmailValidationSignedToken emailValidationSignedToken
  ) throws RestServiceException {
    validateService();

    SynapseClient client = createAnonymousSynapseClient();
    try {
      AccountSetupInfo accountSetup = new AccountSetupInfo();
      accountSetup.setFirstName(fName);
      accountSetup.setLastName(lName);
      accountSetup.setUsername(userName);
      accountSetup.setPassword(password);
      accountSetup.setEmailValidationSignedToken(emailValidationSignedToken);
      LoginResponse s = client.createNewAccountForAccessToken(accountSetup);
      return s.getAccessToken();
    } catch (SynapseException e) {
      throw ExceptionUtil.convertSynapseException(e);
    }
  }

  @Override
  public String getToken() {
    // By default, we get the token from the request cookies.
    return UserDataProvider.getThreadLocalUserToken(
      this.getThreadLocalRequest()
    );
  }

  @Override
  public String getRequestHost() {
    return UserDataProvider.getThreadLocalRequestHost(
      this.getThreadLocalRequest()
    );
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
      results.setPublicAclPrincipalId(
        AuthorizationConstants.BOOTSTRAP_PRINCIPAL.PUBLIC_GROUP.getPrincipalId()
      );
      results.setAuthenticatedAclPrincipalId(
        AuthorizationConstants.BOOTSTRAP_PRINCIPAL.AUTHENTICATED_USERS_GROUP.getPrincipalId()
      );
      results.setAnonymousUserId(
        AuthorizationConstants.BOOTSTRAP_PRINCIPAL.ANONYMOUS_USER.getPrincipalId()
      );

      publicPrincipalIds = results;
    } catch (Exception e) {
      throw new RestClientException(e.getMessage());
    }
  }

  @Override
  public UserProfile getMyProfile() throws RestServiceException {
    validateService();

    SynapseClient synapseClient = createSynapseClient();
    try {
      return synapseClient.getMyProfile();
    } catch (SynapseException e) {
      throw ExceptionUtil.convertSynapseException(e);
    }
  }

  /**
   * The synapse client is stateful so we must create a new one for each request
   */
  private SynapseClient createSynapseClient() {
    return createSynapseClient(null);
  }

  private SynapseClient createSynapseClient(String accessToken) {
    SynapseClient synapseClient = synapseProvider.createNewClient(
      this.getRequestHost()
    );
    if (accessToken == null) {
      accessToken = tokenProvider.getToken();
    }
    if (accessToken != null) {
      synapseClient.setBearerAuthorizationToken(accessToken);
    }
    return synapseClient;
  }

  private SynapseClient createAnonymousSynapseClient() {
    SynapseClient synapseClient = synapseProvider.createNewClient(
      this.getRequestHost()
    );
    return synapseClient;
  }
}

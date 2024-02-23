package org.sagebionetworks.web.unitserver.servlet.oaut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.server.servlet.oauth2.OAuth2SessionServlet.*;

import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseBadRequestException;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.repo.model.auth.LoginResponse;
import org.sagebionetworks.repo.model.oauth.OAuthAccountCreationRequest;
import org.sagebionetworks.repo.model.oauth.OAuthProvider;
import org.sagebionetworks.repo.model.oauth.OAuthUrlRequest;
import org.sagebionetworks.repo.model.oauth.OAuthUrlResponse;
import org.sagebionetworks.repo.model.oauth.OAuthValidationRequest;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.oauth2.OAuth2SessionServlet;
import org.sagebionetworks.web.shared.WebConstants;

@RunWith(MockitoJUnitRunner.class)
public class OAuth2SessionServletTest {

  @Mock
  HttpServletRequest mockRequest;

  @Mock
  HttpServletResponse mockResponse;

  @Mock
  SynapseProvider mockSynapseProvider;

  @Mock
  SynapseClient mockClient;

  String url;
  OAuth2SessionServlet servlet;

  @Captor
  ArgumentCaptor<Cookie> cookieCaptor;

  @Before
  public void before() {
    when(mockSynapseProvider.createNewClient()).thenReturn(mockClient);
    servlet = new OAuth2SessionServlet();
    servlet.setSynapseProvider(mockSynapseProvider);
    url = "http://127.0.0.1:8888/";
    when(mockRequest.getRequestURL()).thenReturn(new StringBuffer(url));
    when(mockRequest.getRequestURI()).thenReturn("");
    when(mockRequest.getContextPath()).thenReturn("");
    when(mockRequest.getScheme()).thenReturn("https");
    when(mockRequest.getServerName()).thenReturn("www.synapse.org");
  }

  @Test
  public void testCreateRedirectUrl()
    throws ServletException, IOException, SynapseException {
    String url = servlet.createRedirectUrl(
      mockRequest,
      OAuthProvider.GOOGLE_OAUTH_2_0
    );
    assertEquals("http://127.0.0.1:8888/?oauth2provider=GOOGLE_OAUTH_2_0", url);
  }

  @Test
  public void testAuthUrl()
    throws ServletException, IOException, SynapseException {
    ArgumentCaptor<OAuthUrlRequest> argument = ArgumentCaptor.forClass(
      OAuthUrlRequest.class
    );
    OAuthUrlResponse authResponse = new OAuthUrlResponse();
    authResponse.setAuthorizationUrl("http://google.com");
    when(mockClient.getOAuth2AuthenticationUrl(argument.capture()))
      .thenReturn(authResponse);
    when(mockRequest.getParameter(WebConstants.OAUTH2_PROVIDER))
      .thenReturn(OAuthProvider.GOOGLE_OAUTH_2_0.name());
    // this case we have no code.
    when(mockRequest.getParameter(WebConstants.OAUTH2_CODE)).thenReturn(null);
    servlet.doGet(mockRequest, mockResponse);
    OAuthUrlRequest request = argument.getValue();
    assertNotNull(request);
    assertEquals(
      "http://127.0.0.1:8888/?oauth2provider=GOOGLE_OAUTH_2_0",
      request.getRedirectUrl()
    );
    assertEquals(OAuthProvider.GOOGLE_OAUTH_2_0, request.getProvider());
    verify(mockResponse).sendRedirect(authResponse.getAuthorizationUrl());
  }

  @Test
  public void testValidate()
    throws ServletException, IOException, SynapseException {
    ArgumentCaptor<OAuthValidationRequest> argument = ArgumentCaptor.forClass(
      OAuthValidationRequest.class
    );
    LoginResponse resp = new LoginResponse();
    String testAccessToken = "accesstoken";
    resp.setAccessToken(testAccessToken);
    when(
      mockClient.validateOAuthAuthenticationCodeForAccessToken(
        argument.capture()
      )
    )
      .thenReturn(resp);
    when(mockRequest.getParameter(WebConstants.OAUTH2_PROVIDER))
      .thenReturn(OAuthProvider.GOOGLE_OAUTH_2_0.name());
    String authCode = "authCode";
    when(mockRequest.getParameter(WebConstants.OAUTH2_CODE))
      .thenReturn(authCode);
    servlet.doGet(mockRequest, mockResponse);
    OAuthValidationRequest request = argument.getValue();
    assertNotNull(request);
    assertEquals(
      "http://127.0.0.1:8888/?oauth2provider=GOOGLE_OAUTH_2_0",
      request.getRedirectUrl()
    );
    assertEquals(OAuthProvider.GOOGLE_OAUTH_2_0, request.getProvider());
    assertEquals(authCode, request.getAuthenticationCode());
    verify(mockResponse)
      .sendRedirect("/#!LoginPlace:" + WebConstants.REDIRECT_TO_LAST_PLACE);
    verify(mockResponse).addCookie(cookieCaptor.capture());
    Cookie cookie = cookieCaptor.getValue();
    assertEquals(CookieKeys.USER_LOGIN_TOKEN, cookie.getName());
    assertEquals(testAccessToken, cookie.getValue());
  }

  @Test
  public void testCreateAccountViaOAuth()
    throws ServletException, IOException, SynapseException {
    String testAccessToken = "accessToken";
    ArgumentCaptor<OAuthAccountCreationRequest> argument =
      ArgumentCaptor.forClass(OAuthAccountCreationRequest.class);
    LoginResponse resp = new LoginResponse();
    String state = "my-username";
    resp.setAccessToken(testAccessToken);
    when(mockClient.createAccountViaOAuth2ForAccessToken(argument.capture()))
      .thenReturn(resp);
    when(mockRequest.getParameter(WebConstants.OAUTH2_PROVIDER))
      .thenReturn(OAuthProvider.GOOGLE_OAUTH_2_0.name());
    when(mockRequest.getParameter(WebConstants.OAUTH2_STATE)).thenReturn(state);
    String authCode = "authCode";
    when(mockRequest.getParameter(WebConstants.OAUTH2_CODE))
      .thenReturn(authCode);
    servlet.doGet(mockRequest, mockResponse);
    OAuthAccountCreationRequest request = argument.getValue();
    assertNotNull(request);
    assertEquals(
      "http://127.0.0.1:8888/?oauth2provider=GOOGLE_OAUTH_2_0",
      request.getRedirectUrl()
    );
    assertEquals(OAuthProvider.GOOGLE_OAUTH_2_0, request.getProvider());
    assertEquals(authCode, request.getAuthenticationCode());
    assertEquals(state, request.getUserName());
    verify(mockResponse).addCookie(cookieCaptor.capture());
    Cookie cookie = cookieCaptor.getValue();
    assertEquals(CookieKeys.USER_LOGIN_TOKEN, cookie.getName());
    assertEquals(testAccessToken, cookie.getValue());
    verify(mockResponse)
      .sendRedirect("/#!LoginPlace:" + WebConstants.REDIRECT_TO_LAST_PLACE);
  }

  @Test
  public void testCreateAccountViaOAuthError()
    throws ServletException, IOException, SynapseException {
    String state = "my-username";
    String errorMessage =
      "this be an error during oauth based account creation";
    when(
      mockClient.createAccountViaOAuth2ForAccessToken(
        any(OAuthAccountCreationRequest.class)
      )
    )
      .thenThrow(new SynapseBadRequestException(errorMessage));
    when(mockRequest.getParameter(WebConstants.OAUTH2_PROVIDER))
      .thenReturn(OAuthProvider.GOOGLE_OAUTH_2_0.name());
    when(mockRequest.getParameter(WebConstants.OAUTH2_STATE)).thenReturn(state);
    String authCode = "authCode";
    when(mockRequest.getParameter(WebConstants.OAUTH2_CODE))
      .thenReturn(authCode);

    servlet.doGet(mockRequest, mockResponse);

    verify(mockResponse)
      .sendRedirect(contains(URLEncoder.encode(errorMessage)));
  }

  @Test
  public void testValidateNotFound()
    throws ServletException, IOException, SynapseException {
    ArgumentCaptor<OAuthValidationRequest> argument = ArgumentCaptor.forClass(
      OAuthValidationRequest.class
    );
    when(
      mockClient.validateOAuthAuthenticationCodeForAccessToken(
        argument.capture()
      )
    )
      .thenThrow(new SynapseNotFoundException("an error message"));
    when(mockRequest.getParameter(WebConstants.OAUTH2_PROVIDER))
      .thenReturn(OAuthProvider.GOOGLE_OAUTH_2_0.name());
    when(mockRequest.getParameter(WebConstants.OAUTH2_CODE))
      .thenReturn("auth code");
    servlet.doGet(mockRequest, mockResponse);
    verify(mockResponse).sendRedirect(REGISTER_ACCOUNT);
  }

  @Test
  public void testValidateNotFoundORCiD()
    throws ServletException, IOException, SynapseException {
    ArgumentCaptor<OAuthValidationRequest> argument = ArgumentCaptor.forClass(
      OAuthValidationRequest.class
    );
    when(
      mockClient.validateOAuthAuthenticationCodeForAccessToken(
        argument.capture()
      )
    )
      .thenThrow(new SynapseNotFoundException("an error message"));
    when(mockRequest.getParameter(WebConstants.OAUTH2_PROVIDER))
      .thenReturn(OAuthProvider.ORCID.name());
    when(mockRequest.getParameter(WebConstants.OAUTH2_CODE))
      .thenReturn("auth code");
    servlet.doGet(mockRequest, mockResponse);
    verify(mockResponse)
      .sendRedirect(LOGIN_PLACE + WebConstants.ORCID_NOT_LINKED);
  }
}

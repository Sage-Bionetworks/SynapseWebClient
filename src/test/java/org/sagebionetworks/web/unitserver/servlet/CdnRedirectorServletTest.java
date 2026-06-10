package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.server.servlet.CdnRedirectorServlet;

public class CdnRedirectorServletTest {

  @Mock
  HttpServletRequest mockRequest;

  @Mock
  HttpServletResponse mockResponse;

  CdnRedirectorServlet servlet;
  ArgumentCaptor<String> redirectUrlCaptor;

  @Before
  public void setup() throws IOException {
    MockitoAnnotations.initMocks(this);
    servlet = new CdnRedirectorServlet();
    redirectUrlCaptor = ArgumentCaptor.forClass(String.class);
  }

  private void setupRequest(
    String protocol,
    String serverName,
    int serverPort,
    String pathInfo
  ) throws IOException {
    StringBuffer requestUrl = new StringBuffer(
      protocol + "://" + serverName + ":" + serverPort + pathInfo
    );
    when(mockRequest.getScheme()).thenReturn(protocol);
    when(mockRequest.getRequestURL()).thenReturn(requestUrl);
    when(mockRequest.getQueryString()).thenReturn(null);
    when(mockRequest.getPathInfo()).thenReturn(pathInfo);
    when(mockRequest.getServerName()).thenReturn(serverName);
  }

  @Test
  public void testDoGetSynapseOrg() throws Exception {
    String serverName = "www.synapse.org";
    String pathInfo = "/some/asset.png";
    setupRequest("https", serverName, 443, pathInfo);

    servlet.doGet(mockRequest, mockResponse);

    verify(mockResponse).sendRedirect(redirectUrlCaptor.capture());
    String redirectUrl = redirectUrlCaptor.getValue();
    assertEquals("https://cdn-www.synapse.org:443/some/asset.png", redirectUrl);
  }

  @Test
  public void testDoGetStagingSynapseOrg() throws Exception {
    String serverName = "staging.synapse.org";
    String pathInfo = "/some/asset.png";
    setupRequest("https", serverName, 443, pathInfo);

    servlet.doGet(mockRequest, mockResponse);

    verify(mockResponse).sendRedirect(redirectUrlCaptor.capture());
    String redirectUrl = redirectUrlCaptor.getValue();
    assertEquals(
      "https://cdn-staging.synapse.org:443/some/asset.png",
      redirectUrl
    );
  }

  @Test
  public void testDoGetLocalhost() throws Exception {
    String serverName = "127.0.0.1";
    int serverPort = 8888;
    String pathInfo = "/some/asset.png";
    setupRequest("http", serverName, serverPort, pathInfo);

    servlet.doGet(mockRequest, mockResponse);

    verify(mockResponse).sendRedirect(redirectUrlCaptor.capture());
    String redirectUrl = redirectUrlCaptor.getValue();
    assertEquals("http://127.0.0.1:8888/some/asset.png", redirectUrl);
  }
}

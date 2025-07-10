package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseForbiddenException;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.table.RowReference;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.server.servlet.CdnRedirectorServlet;
import org.sagebionetworks.web.server.servlet.RequestHostProvider;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.unitserver.SynapseClientBaseTest;

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

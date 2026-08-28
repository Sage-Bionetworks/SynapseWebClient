package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseForbiddenException;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.server.servlet.FileHandleAssociationServlet;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.server.servlet.filter.GWTAllCacheFilter;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.unitserver.SynapseClientBaseTest;

public class FileHandleAssociationServletTest {

  @Mock
  HttpServletRequest mockRequest;

  @Mock
  HttpServletResponse mockResponse;

  @Mock
  SynapseProvider mockSynapseProvider;

  @Mock
  TokenProvider mockTokenProvider;

  @Mock
  SynapseClient mockSynapse;

  @Mock
  ServletOutputStream responseOutputStream;

  FileHandleAssociationServlet servlet;

  String objectId = "22";
  String objectType = FileHandleAssociateType.VerificationSubmission.toString();
  String fileHandleId = "333";
  String sessionToken = "fake";
  URL resolvedUrl, rawFileUrl;
  File tempStreamedFile;
  byte[] tempStreamedBytes;

  @Before
  public void setup()
    throws IOException, SynapseException, JSONObjectAdapterException {
    MockitoAnnotations.initMocks(this);
    servlet = new FileHandleAssociationServlet();

    when(mockSynapseProvider.createNewClient(any())).thenReturn(mockSynapse);

    resolvedUrl = new URL("http://localhost/file.png");
    when(mockSynapse.getFileURL(any(FileHandleAssociation.class)))
      .thenReturn(resolvedUrl);

    rawFileUrl = new URL("http://raw.file.url/");
    when(mockSynapse.getFileHandleTemporaryUrl(anyString()))
      .thenReturn(rawFileUrl);

    servlet.setSynapseProvider(mockSynapseProvider);
    servlet.setTokenProvider(mockTokenProvider);
    when(mockResponse.getOutputStream()).thenReturn(responseOutputStream);
    when(mockRequest.getParameter(WebConstants.ASSOCIATED_OBJECT_ID_PARAM_KEY))
      .thenReturn(objectId);
    when(
      mockRequest.getParameter(WebConstants.ASSOCIATED_OBJECT_TYPE_PARAM_KEY)
    )
      .thenReturn(objectType);
    when(mockRequest.getParameter(WebConstants.FILE_HANDLE_ID_PARAM_KEY))
      .thenReturn(fileHandleId);
    when(mockRequest.getRequestURL())
      .thenReturn(new StringBuffer("https://www.synapse.org/"));
    when(mockRequest.getRequestURI()).thenReturn("");
    when(mockRequest.getContextPath()).thenReturn("");
    when(mockTokenProvider.getToken()).thenReturn(sessionToken);
    Cookie[] cookies = {
      new Cookie(CookieKeys.USER_LOGIN_TOKEN, sessionToken),
    };
    when(mockRequest.getCookies()).thenReturn(cookies);

    SynapseClientBaseTest.setupTestEndpoints();
  }

  @After
  public void teardown() {
    if (tempStreamedFile != null && tempStreamedFile.exists()) {
      tempStreamedFile.delete();
    }
  }

  /**
   * Configure {@link #mockSynapse#getFileURL} to return a file:// URL pointing at a temp file with
   * the given contents and suffix, so that the servlet can actually stream real bytes without any
   * network dependency. The suffix drives the Content-Type reported by the JDK's file URL
   * handler (e.g. ".pdf" → "application/pdf", ".txt" → "text/plain"), mirroring what S3 would
   * echo back from a presigned URL. Also captures bytes written to
   * {@link #responseOutputStream}.
   */
  private ByteArrayOutputStream stubStreamingUrlAndCaptureResponse(
    byte[] contents,
    String suffix
  ) throws IOException, SynapseException {
    tempStreamedFile = File.createTempFile("fha-servlet-test-", suffix);
    try (FileOutputStream fos = new FileOutputStream(tempStreamedFile)) {
      fos.write(contents);
    }
    tempStreamedBytes = contents;
    URL fileUrl = tempStreamedFile.toURI().toURL();
    when(mockSynapse.getFileURL(any(FileHandleAssociation.class)))
      .thenReturn(fileUrl);

    ByteArrayOutputStream captured = new ByteArrayOutputStream();
    doAnswer(invocation -> {
        byte[] buf = invocation.getArgument(0);
        int off = invocation.getArgument(1);
        int len = invocation.getArgument(2);
        captured.write(buf, off, len);
        return null;
      })
      .when(responseOutputStream)
      .write(any(byte[].class), anyInt(), anyInt());
    doAnswer(invocation -> {
        byte[] buf = invocation.getArgument(0);
        captured.write(buf);
        return null;
      })
      .when(responseOutputStream)
      .write(any(byte[].class));
    return captured;
  }

  @Test
  public void testDoGet() throws Exception {
    servlet.doGet(mockRequest, mockResponse);

    ArgumentCaptor<FileHandleAssociation> fhaCaptor = ArgumentCaptor.forClass(
      FileHandleAssociation.class
    );
    verify(mockSynapse).getFileURL(fhaCaptor.capture());
    verify(mockResponse).sendRedirect(resolvedUrl.toString());
    FileHandleAssociation fha = fhaCaptor.getValue();
    assertEquals(objectId, fha.getAssociateObjectId());
    assertEquals(
      FileHandleAssociateType.VerificationSubmission,
      fha.getAssociateObjectType()
    );
    assertEquals(fileHandleId, fha.getFileHandleId());

    // as an additional test, verify that synapse client is set up
    verify(mockSynapse).setBearerAuthorizationToken(sessionToken);

    // look for 30 second cache header
    verify(mockResponse)
      .setHeader(
        WebConstants.CACHE_CONTROL_KEY,
        "max-age=" + FileHandleAssociationServlet.CACHE_TIME_SECONDS
      );
  }

  @Test
  public void testDoGetRawFileHandle() throws Exception {
    when(mockRequest.getParameter(WebConstants.ASSOCIATED_OBJECT_ID_PARAM_KEY))
      .thenReturn(null);
    when(
      mockRequest.getParameter(WebConstants.ASSOCIATED_OBJECT_TYPE_PARAM_KEY)
    )
      .thenReturn(null);

    servlet.doGet(mockRequest, mockResponse);

    ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockSynapse).getFileHandleTemporaryUrl(stringCaptor.capture());
    verify(mockResponse).sendRedirect(rawFileUrl.toString());
    assertEquals(fileHandleId, stringCaptor.getValue());

    // look for 30 second cache header
    verify(mockResponse)
      .setHeader(
        WebConstants.CACHE_CONTROL_KEY,
        "max-age=" + FileHandleAssociationServlet.CACHE_TIME_SECONDS
      );
  }

  @Test
  public void testDoGetError() throws Exception {
    String errorMessage = "An error from the service call";
    when(mockSynapse.getFileURL(any(FileHandleAssociation.class)))
      .thenThrow(new SynapseForbiddenException(errorMessage));
    servlet.doGet(mockRequest, mockResponse);

    // redirects to an error place
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(mockResponse).sendRedirect(captor.capture());
    String v = captor.getValue();
    assertTrue(v.contains("Error:"));
  }

  @Test
  public void testDoGetUserProfileAttachmentStreamsAndCaches()
    throws Exception {
    when(
      mockRequest.getParameter(WebConstants.ASSOCIATED_OBJECT_TYPE_PARAM_KEY)
    )
      .thenReturn(FileHandleAssociateType.UserProfileAttachment.toString());
    byte[] payload = "profile-image-bytes".getBytes();
    ByteArrayOutputStream captured = stubStreamingUrlAndCaptureResponse(
      payload,
      ".bin"
    );

    servlet.doGet(mockRequest, mockResponse);

    // No redirect on the streaming path.
    verify(mockResponse, never()).sendRedirect(anyString());
    // Long-lived cache header for public profile/team attachments.
    verify(mockResponse)
      .setHeader(
        eq("Cache-Control"),
        eq("max-age=" + GWTAllCacheFilter.CACHE_TIME_SECONDS)
      );
    assertArrayEquals(payload, captured.toByteArray());
  }

  @Test
  public void testDoGetDataAccessRequestAttachmentStreamsPdfContentType()
    throws Exception {
    when(
      mockRequest.getParameter(WebConstants.ASSOCIATED_OBJECT_TYPE_PARAM_KEY)
    )
      .thenReturn(
        FileHandleAssociateType.DataAccessRequestAttachment.toString()
      );
    byte[] payload = "%PDF-1.4 fake eDUC contents".getBytes();
    ByteArrayOutputStream captured = stubStreamingUrlAndCaptureResponse(
      payload,
      ".pdf"
    );

    servlet.doGet(mockRequest, mockResponse);

    // SWC-7960: served same-origin so an iframe can preview it.
    verify(mockResponse, never()).sendRedirect(anyString());
    verify(mockResponse).setContentType("application/pdf");
    verify(mockResponse).setHeader("Content-Disposition", "inline");
    // Contents may include identifying information; must not be stored anywhere.
    verify(mockResponse).setHeader("Cache-Control", "no-store");
    assertArrayEquals(payload, captured.toByteArray());
  }

  @Test
  public void testDoGetDataAccessRequestAttachmentForwardsNonPdfContentType()
    throws Exception {
    // A traditional (non-eDUC) DUC could be uploaded as e.g. plain text or DOCX; the servlet must
    // forward whatever Content-Type the upstream file handle URL reports, not hardcode PDF.
    when(
      mockRequest.getParameter(WebConstants.ASSOCIATED_OBJECT_TYPE_PARAM_KEY)
    )
      .thenReturn(
        FileHandleAssociateType.DataAccessRequestAttachment.toString()
      );
    byte[] payload = "traditional duc contents".getBytes();
    ByteArrayOutputStream captured = stubStreamingUrlAndCaptureResponse(
      payload,
      ".txt"
    );

    servlet.doGet(mockRequest, mockResponse);

    verify(mockResponse, never()).sendRedirect(anyString());
    // ".txt" resolves to text/plain via the JDK's content-type map, standing in for whatever
    // Content-Type S3 would echo back from the presigned URL.
    ArgumentCaptor<String> contentTypeCaptor = ArgumentCaptor.forClass(
      String.class
    );
    verify(mockResponse).setContentType(contentTypeCaptor.capture());
    assertTrue(
      "expected non-PDF content type to be forwarded, got: " +
      contentTypeCaptor.getValue(),
      contentTypeCaptor.getValue().startsWith("text/plain")
    );
    verify(mockResponse).setHeader("Content-Disposition", "inline");
    verify(mockResponse).setHeader("Cache-Control", "no-store");
    assertArrayEquals(payload, captured.toByteArray());
  }

  @Test
  public void testDoGetDataAccessRequestAttachmentErrorRedirects()
    throws Exception {
    when(
      mockRequest.getParameter(WebConstants.ASSOCIATED_OBJECT_TYPE_PARAM_KEY)
    )
      .thenReturn(
        FileHandleAssociateType.DataAccessRequestAttachment.toString()
      );
    when(mockSynapse.getFileURL(any(FileHandleAssociation.class)))
      .thenThrow(new SynapseForbiddenException("nope"));

    servlet.doGet(mockRequest, mockResponse);

    // Same failure handling as all other branches: redirect to error place.
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(mockResponse).sendRedirect(captor.capture());
    assertTrue(captor.getValue().contains("Error:"));
    // Content-Type must not be set when the URL lookup failed.
    verify(mockResponse, never()).setContentType(anyString());
  }
}

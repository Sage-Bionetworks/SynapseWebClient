package org.sagebionetworks.web.unitserver.servlet;

//import com.amazonaws.services.appconfigdata.AWSAppConfigData;
//import com.amazonaws.services.appconfigdata.model.GetLatestConfigurationRequest;
//import com.amazonaws.services.appconfigdata.model.GetLatestConfigurationResult;
//import com.amazonaws.services.appconfigdata.model.StartConfigurationSessionRequest;
//import com.amazonaws.services.appconfigdata.model.StartConfigurationSessionResult;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.Mock;
//import org.sagebionetworks.StackConfiguration;
//import org.sagebionetworks.web.server.servlet.AppConfigServlet;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.nio.ByteBuffer;
//import com.google.gwt.thirdparty.guava.common.base.Supplier;
//
//import static org.junit.Assert.assertEquals;
//import static org.mockito.Mockito.*;
//import org.junit.*;

public class AppConfigServletTest {
  //    private AppConfigServlet servlet;
  //    @Mock
  //    private AWSAppConfigData mockAppConfigDataClient;
  //    @Mock
  //    private StackConfiguration mockStackConfiguration;
  //    @Mock
  //    private HttpServletRequest mockRequest;
  //    @Mock
  //    private HttpServletResponse mockResponse;
  //    @Mock
  //    private Supplier<String> mockConfigSupplier;
  //    private StringWriter responseWriter;
  //
  //    @Before
  //    public void setup() throws Exception {
  //        mockAppConfigDataClient = mock(AWSAppConfigData.class);
  //        mockConfigSupplier = mock(Supplier.class);
  //        servlet = new AppConfigServlet(mockAppConfigDataClient,mockStackConfiguration);
  //        servlet.appConfigDataClient = mockAppConfigDataClient;
  //        servlet.configSupplier = mockConfigSupplier;
  //        when(servlet.configSupplier.get()).thenReturn("test configuration"); // Do I want to mock configSupplier directly?
  //
  //        mockRequest = mock(HttpServletRequest.class);
  //        mockResponse = mock(HttpServletResponse.class);
  //        responseWriter = new StringWriter();
  //        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
  //    }
  //
  //    @Test
  //    public void testStartConfigurationSession_Success() {
  //        StartConfigurationSessionResult mockSessionResult = new StartConfigurationSessionResult();
  //        mockSessionResult.setInitialConfigurationToken("mockToken");
  //
  //        when(mockAppConfigDataClient.startConfigurationSession(any(StartConfigurationSessionRequest.class)))
  //                .thenReturn(mockSessionResult);
  //
  //        servlet.startConfigurationSession();  // Assuming it's accessible for testing
  //        assertEquals("mockToken", servlet.configurationToken);
  //        verify(mockAppConfigDataClient, times(1)).startConfigurationSession(any(StartConfigurationSessionRequest.class));
  //    }
  //
  //    @Test
  //    public void testStartConfigurationSession_Failure() {
  //        when(mockAppConfigDataClient.startConfigurationSession(any(StartConfigurationSessionRequest.class)))
  //                .thenThrow(new RuntimeException("Failed to start session"));
  //        try {
  //            servlet.startConfigurationSession();
  //        } catch (RuntimeException e) {
  //            assertEquals("Failed to start session", e.getMessage());
  //        }
  //    }
  //
  //    @Test
  //    public void testDoGetMock() throws Exception {
  //        StartConfigurationSessionResult mockSessionResult = new StartConfigurationSessionResult()
  //                .withInitialConfigurationToken("mock-token");
  //        when(mockAppConfigDataClient.startConfigurationSession(any(StartConfigurationSessionRequest.class)))
  //                .thenReturn(mockSessionResult);
  //
  //        GetLatestConfigurationResult mockConfigResult = new GetLatestConfigurationResult()
  //                .withConfiguration(ByteBuffer.wrap("mock-config-value".getBytes()))
  //                .withNextPollConfigurationToken("new-mock-token");
  //        when(mockAppConfigDataClient.getLatestConfiguration(any(GetLatestConfigurationRequest.class)))
  //                .thenReturn(mockConfigResult);
  //
  //        servlet.doGet(mockRequest, mockResponse);
  //
  //        System.out.println(responseWriter.toString()); // the response corresponds to the configSupplier?
  //        Assert.assertTrue(responseWriter.toString().contains("Configuration Value: mock-config-value"));
  //    }
}

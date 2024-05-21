package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import com.amazonaws.services.appconfigdata.AWSAppConfigData;
import com.amazonaws.services.appconfigdata.model.GetLatestConfigurationRequest;
import com.amazonaws.services.appconfigdata.model.GetLatestConfigurationResult;
import com.amazonaws.services.appconfigdata.model.StartConfigurationSessionRequest;
import com.amazonaws.services.appconfigdata.model.StartConfigurationSessionResult;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.server.servlet.AppConfigServlet;

public class AppConfigServletTest {

  private AppConfigServlet servlet;

  @Mock
  private AWSAppConfigData mockAppConfigDataClient;

  @Mock
  private StackConfiguration mockStackConfiguration;

  @Mock
  private HttpServletRequest mockRequest;

  @Mock
  private HttpServletResponse mockResponse;

  private StringWriter responseWriter;

  private JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();

  private JSONObjectAdapter mockConfiguration;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    servlet =
      new AppConfigServlet(mockAppConfigDataClient, mockStackConfiguration);
    servlet.appConfigDataClient = mockAppConfigDataClient;

    // Set up the response writer
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

    // Configure mock StackConfiguration
    when(mockStackConfiguration.getStack()).thenReturn("testStack");
    when(mockStackConfiguration.getStackInstance()).thenReturn("testInstance");

    // Mock the configuration supplier to return a test configuration
    mockConfiguration =
      jsonObjectAdapter.createNew("{\"test configuration\":true}");
    servlet.configSupplier = () -> mockConfiguration;
  }

  @Test
  public void testStartConfigurationSession_Success() {
    StartConfigurationSessionResult mockSessionResult =
      new StartConfigurationSessionResult();
    mockSessionResult.setInitialConfigurationToken("mockToken");

    when(
      mockAppConfigDataClient.startConfigurationSession(
        any(StartConfigurationSessionRequest.class)
      )
    )
      .thenReturn(mockSessionResult);

    servlet.startConfigurationSession();
    assertEquals("mockToken", servlet.configurationToken);
    verify(mockAppConfigDataClient, times(1))
      .startConfigurationSession(any(StartConfigurationSessionRequest.class));
  }

  @Test
  public void testStartConfigurationSession_Failure() {
    when(
      mockAppConfigDataClient.startConfigurationSession(
        any(StartConfigurationSessionRequest.class)
      )
    )
      .thenThrow(new RuntimeException("Failed to start session"));

    servlet.startConfigurationSession();
    // If an exception is thrown, the configuration token should remain null
    assertEquals(null, servlet.configurationToken);
  }

  @Test
  public void testDoGetMock() throws Exception {
    servlet.doGet(mockRequest, mockResponse);

    // Verify the response contains the expected configuration value
    verify(mockResponse).setContentType("application/json");
    assertEquals(mockConfiguration.toString(), responseWriter.toString());
  }

  @Test
  public void testGetLatestConfiguration_Success() {
    ByteBuffer mockByteBuffer = ByteBuffer
      .wrap("{\"test configuration\":true}".getBytes())
      .asReadOnlyBuffer();
    GetLatestConfigurationResult mockConfigResult =
      new GetLatestConfigurationResult()
        .withConfiguration(mockByteBuffer)
        .withNextPollConfigurationToken("new-mock-token");

    when(
      mockAppConfigDataClient.getLatestConfiguration(
        any(GetLatestConfigurationRequest.class)
      )
    )
      .thenReturn(mockConfigResult);

    servlet.configurationToken = "mockToken"; // Setting the initial configuration token
    JSONObjectAdapter configValue = servlet.getLatestConfiguration();

    assertEquals(mockConfiguration.toString(), configValue.toString());
    assertEquals("new-mock-token", servlet.configurationToken);
  }

  @Test
  public void testGetLatestConfiguration_Failure() {
    String DEFAULT_CONFIG_VALUE = "{}";
    when(
      mockAppConfigDataClient.getLatestConfiguration(
        any(GetLatestConfigurationRequest.class)
      )
    )
      .thenThrow(new RuntimeException("Failed to retrieve configuration"));

    servlet.configurationToken = "mockToken"; // Setting the initial configuration token
    JSONObjectAdapter configValue = servlet.getLatestConfiguration();

    assertEquals(DEFAULT_CONFIG_VALUE, configValue.toString());
  }

  @Test
  public void testInitializeAppConfigClient() {
    servlet.appConfigDataClient = null; // Simulate the client not being injected
    servlet.initializeAppConfigClient();

    assertNotNull(servlet.appConfigDataClient);
  }
}

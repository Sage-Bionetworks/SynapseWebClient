package org.sagebionetworks.web.client;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.cookie.CookieProvider;

public class FeatureFlagConfigTest {

  @Mock
  CookieProvider mockCookieProvider;

  @Mock
  JSONObject mockJsonObject;

  private FeatureFlagConfig featureFlagConfig;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testFeatureEnabled() {
    when(mockJsonObject.get(FeatureFlagKey.TEST_FLAG_ONLY.getKey()))
      .thenReturn(JSONBoolean.getInstance(true));
    featureFlagConfig =
      new FeatureFlagConfig(mockJsonObject, mockCookieProvider);

    assertTrue(
      featureFlagConfig.isFeatureEnabled(FeatureFlagKey.TEST_FLAG_ONLY)
    );
  }

  @Test
  public void testFeatureDisabled() {
    when(mockJsonObject.get(FeatureFlagKey.TEST_FLAG_ONLY.getKey()))
      .thenReturn(JSONBoolean.getInstance(false));
    featureFlagConfig =
      new FeatureFlagConfig(mockJsonObject, mockCookieProvider);

    assertFalse(
      featureFlagConfig.isFeatureEnabled(FeatureFlagKey.TEST_FLAG_ONLY)
    );
  }

  @Test
  public void testFeatureDisabledButExperimentalModeEnabled() {
    when(mockCookieProvider.getCookie(eq("SynapseTestWebsite")))
      .thenReturn("true");
    when(mockJsonObject.get(FeatureFlagKey.TEST_FLAG_ONLY.getKey()))
      .thenReturn(JSONBoolean.getInstance(false));
    featureFlagConfig =
      new FeatureFlagConfig(mockJsonObject, mockCookieProvider);

    assertTrue(
      featureFlagConfig.isFeatureEnabled(FeatureFlagKey.TEST_FLAG_ONLY)
    );
  }

  @Test
  public void testExperimentalModeValueReturnedOnException() {
    when(mockJsonObject.get(FeatureFlagKey.TEST_FLAG_ONLY.getKey()))
      .thenThrow(new RuntimeException("Test exception"));
    when(mockCookieProvider.getCookie(eq("SynapseTestWebsite")))
      .thenReturn("true");
    featureFlagConfig =
      new FeatureFlagConfig(mockJsonObject, mockCookieProvider);

    assertTrue(
      featureFlagConfig.isFeatureEnabled(FeatureFlagKey.TEST_FLAG_ONLY)
    );
  }
}

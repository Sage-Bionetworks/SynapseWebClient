package org.sagebionetworks.web.unitclient.widget.footer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.footer.FooterView;
import org.sagebionetworks.web.client.widget.footer.VersionState;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FooterTest {

  Footer footer;

  @Mock
  FooterView mockView;

  @Mock
  GlobalApplicationState mockGlobalAppState;

  @Mock
  AuthenticationController mockAuthController;

  @Mock
  UserProfile mockUserProfile;

  @Mock
  GWTWrapper mockGwt;

  @Mock
  SynapseJSNIUtils mockJsniUtils;

  public static final String OWNER_ID = "282711";
  public static final String FIRST_NAME = "Bob";
  public static final String LAST_NAME = "Vance";
  public static final String USERNAME = "bvance";
  public static final String EMAIL = "bob@vancerefrigeration.com";
  public static final String CURRENT_URL =
    "https://www.synapse.org/flag-this-data";

  @Before
  public void setup() {
    footer =
      new Footer(
        mockView,
        mockGlobalAppState,
        mockAuthController,
        mockGwt,
        mockJsniUtils
      );
    VersionState versionState = new VersionState("v,v", true);
    AsyncMockStubber
      .callSuccessWith(versionState)
      .when(mockGlobalAppState)
      .checkVersionCompatibility(any());
    verify(mockView).setPresenter(footer);
    when(mockUserProfile.getEmails())
      .thenReturn(Collections.singletonList(EMAIL));
    when(mockUserProfile.getFirstName()).thenReturn(FIRST_NAME);
    when(mockUserProfile.getLastName()).thenReturn(LAST_NAME);
    when(mockUserProfile.getUserName()).thenReturn(USERNAME);
    when(mockUserProfile.getOwnerId()).thenReturn(OWNER_ID);
    when(mockGwt.getCurrentURL()).thenReturn(CURRENT_URL);
  }

  @Test
  public void testConstruction() {
    verify(mockGlobalAppState)
      .checkVersionCompatibility(any(AsyncCallback.class));
    verify(mockView).refresh();
  }

  @Test
  public void testConstructionNullVersion() {
    VersionState versionState = new VersionState(null, false);
    AsyncMockStubber
      .callSuccessWith(versionState)
      .when(mockGlobalAppState)
      .checkVersionCompatibility(any());
    footer =
      new Footer(
        mockView,
        mockGlobalAppState,
        mockAuthController,
        mockGwt,
        mockJsniUtils
      );
    verify(mockView).setVersion(eq(Footer.UNKNOWN), eq(Footer.UNKNOWN), any());
  }

  @Test
  public void testAsWidget() {
    footer.asWidget();
    verify(mockView).asWidget();
  }
}

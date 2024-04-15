package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMemberCountWidget;
import org.sagebionetworks.web.client.widget.team.BigTeamBadge;
import org.sagebionetworks.web.client.widget.team.BigTeamBadgeView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

@RunWith(MockitoJUnitRunner.class)
public class BigTeamBadgeTest {

  @Mock
  AuthenticationController mockAuthenticationController;

  @Mock
  BigTeamBadgeView mockView;

  @Mock
  SynapseJavascriptClient mockJsClient;

  @Mock
  SynapseJSNIUtils mockJsniUtils;

  @Mock
  TeamMemberCountWidget mockTeamMemberCountWidget;

  @Mock
  TeamMembershipStatus mockTeamMembershipStatus;

  BigTeamBadge presenter;

  @Mock
  Team mockTeam;

  public static final String TEAM_ICON_URL = "http://team.icon.png";
  public static final String TEAM_DESCRIPTION = "describing the team";

  @Before
  public void setUp() throws Exception {
    presenter =
      new BigTeamBadge(
        mockView,
        mockJsClient,
        mockJsniUtils,
        mockAuthenticationController,
        mockTeamMemberCountWidget
      );
    when(mockTeam.getName()).thenReturn("simpleteam");
    when(mockTeam.getDescription()).thenReturn(TEAM_DESCRIPTION);
    when(mockTeam.getIcon()).thenReturn("1111");
    AsyncMockStubber
      .callSuccessWith(TEAM_ICON_URL)
      .when(mockJsClient)
      .getTeamPicturePreviewURL(anyString(), any(AsyncCallback.class));
    AsyncMockStubber
      .callSuccessWith(mockTeam)
      .when(mockJsClient)
      .getTeam(anyString(), any(AsyncCallback.class));
  }

  @Test
  public void testConfigure() {
    presenter.configure("123");

    verify(mockView).setTeam(mockTeam, TEAM_DESCRIPTION, TEAM_ICON_URL);
  }

  @Test
  public void testConfigureFailedToGetIcon() {
    AsyncMockStubber
      .callFailureWith(new Exception("failed"))
      .when(mockJsClient)
      .getTeamPicturePreviewURL(anyString(), any(AsyncCallback.class));

    presenter.configure("123");

    verify(mockView).setTeam(mockTeam, TEAM_DESCRIPTION, null);
  }

  @Test
  public void testGetTeamEmail() {
    assertEquals("basic@synapse.org", presenter.getTeamEmail("basic"));
    assertEquals(
      "StandardCaseHere@synapse.org",
      presenter.getTeamEmail("Standard Case Here")
    );
    assertEquals(
      "unlikelycase@synapse.org",
      presenter.getTeamEmail(" \n\r unlikely\t case ")
    );
    assertEquals(
      "Another_UnlikelyCase@synapse.org",
      presenter.getTeamEmail(" %^$##* Another_Unlikely\t &*#$)(!!@~Case ")
    );
  }

  @Test
  public void testEmailVisible() {
    when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
    when(mockTeamMembershipStatus.getCanSendEmail()).thenReturn(true);

    presenter.setViewTeam(
      mockTeam,
      TEAM_DESCRIPTION,
      mockTeamMembershipStatus,
      TEAM_ICON_URL
    );

    verify(mockView).setTeamEmailVisible(true);
  }

  @Test
  public void testEmailNotVisibleWhenAnonymous() {
    when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
    when(mockTeamMembershipStatus.getCanSendEmail()).thenReturn(true);

    presenter.setViewTeam(
      mockTeam,
      TEAM_DESCRIPTION,
      mockTeamMembershipStatus,
      TEAM_ICON_URL
    );

    verify(mockView).setTeamEmailVisible(false);
  }

  @Test
  public void testEmailNotVisibleWhenUnauthorized() {
    when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
    when(mockTeamMembershipStatus.getCanSendEmail()).thenReturn(false);

    presenter.setViewTeam(
      mockTeam,
      TEAM_DESCRIPTION,
      mockTeamMembershipStatus,
      TEAM_ICON_URL
    );

    verify(mockView).setTeamEmailVisible(false);
  }
}

package org.sagebionetworks.web.unitclient.widget.user;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.asynch.UserGroupHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.UserGroupHeaderFromAliasAsyncHandler;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.client.widget.team.UserTeamBadge;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.junit.GWTMockUtilities;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * Unit test for the Team Badge widget.
 *
 */
public class UserTeamBadgeTest {
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	UserBadge mockUserBadge;
	@Mock
	TeamBadge mockTeamBadge;
	UserTeamBadge badge;
	String principalId = "id1";
	Map<String, String> widgetDescriptor;
	@Mock
	UserGroupHeaderFromAliasAsyncHandler mockUserGroupHeaderAsyncHandler;
	@Mock
	UserGroupHeaderAsyncHandler mockUserGroupHeaderFromIdAsyncHandler;
	@Mock
	UserGroupHeader mockUserGroupHeader;
	@Mock
	DivView mockDiv;

	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		GWTMockUtilities.disarm();
		Widget mockView = mock(Widget.class);
		when(mockGinInjector.getUserBadgeWidget()).thenReturn(mockUserBadge);
		when(mockGinInjector.getTeamBadgeWidget()).thenReturn(mockTeamBadge);
		when(mockUserBadge.asWidget()).thenReturn(mockView);
		when(mockTeamBadge.asWidget()).thenReturn(mockView);

		badge = new UserTeamBadge(mockGinInjector, mockUserGroupHeaderFromIdAsyncHandler, mockUserGroupHeaderAsyncHandler, mockDiv);
		widgetDescriptor = new HashMap<String, String>();
		widgetDescriptor.put(WidgetConstants.USER_TEAM_BADGE_WIDGET_IS_INDIVIDUAL_KEY, "true");
		widgetDescriptor.put(WidgetConstants.USER_TEAM_BADGE_WIDGET_ID_KEY, principalId);
		AsyncMockStubber.callSuccessWith(mockUserGroupHeader).when(mockUserGroupHeaderAsyncHandler).getUserGroupHeader(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockUserGroupHeader).when(mockUserGroupHeaderFromIdAsyncHandler).getUserGroupHeader(anyString(), any(AsyncCallback.class));
	}

	@After
	public void tearDown() {
		// Be nice to the next test
		GWTMockUtilities.restore();
	}

	@Test
	public void testConfigure() {
		badge.configure(null, widgetDescriptor, null, null);
		verify(mockGinInjector).getUserBadgeWidget();
		verify(mockUserBadge).configure(eq(principalId));
	}

	@Test
	public void testConfigureIgnoreClick() {
		widgetDescriptor.put(WidgetConstants.IS_TOC_KEY, "true");
		badge.configure(null, widgetDescriptor, null, null);
		verify(mockGinInjector).getUserBadgeWidget();
		verify(mockUserBadge).configure(eq(principalId));
		verify(mockUserBadge).setDoNothingOnClick();
	}


	@Test
	public void testConfigureWithUsername() {
		String username = "Potter";
		widgetDescriptor.remove(WidgetConstants.USER_TEAM_BADGE_WIDGET_ID_KEY);
		widgetDescriptor.put(WidgetConstants.USER_TEAM_BADGE_WIDGET_USERNAME_KEY, username);
		String ownerId = "userId";
		when(mockUserGroupHeader.getIsIndividual()).thenReturn(true);
		when(mockUserGroupHeader.getOwnerId()).thenReturn(ownerId);
		badge.configure(null, widgetDescriptor, null, null);
		verify(mockUserGroupHeaderAsyncHandler).getUserGroupHeader(eq(username), any(AsyncCallback.class));
		verify(mockUserBadge).configure(ownerId);
	}

	@Test
	public void testConfigureUserProfileFromId() {
		boolean isIndividual = true;
		String ownerId = "userId";
		when(mockUserGroupHeader.getIsIndividual()).thenReturn(isIndividual);
		when(mockUserGroupHeader.getOwnerId()).thenReturn(ownerId);

		badge.configure(ownerId);

		verify(mockGinInjector).getUserBadgeWidget();
		verify(mockUserBadge).configure(ownerId);
	}

	@Test
	public void testConfigureTeamFromId() {
		boolean isIndividual = false;
		String ownerId = "teamId";
		when(mockUserGroupHeader.getIsIndividual()).thenReturn(isIndividual);
		when(mockUserGroupHeader.getOwnerId()).thenReturn(ownerId);

		badge.configure(ownerId);

		verify(mockGinInjector).getTeamBadgeWidget();
		verify(mockTeamBadge).configure(ownerId, (ClickHandler) null);
	}

	@Test
	public void testConfigureTeam() {
		widgetDescriptor.put(WidgetConstants.USER_TEAM_BADGE_WIDGET_IS_INDIVIDUAL_KEY, "false");
		badge.configure(null, widgetDescriptor, null, null);
		verify(mockGinInjector).getTeamBadgeWidget();
		verify(mockTeamBadge).configure(principalId, (ClickHandler) null);
	}

	@Test
	public void testConfigureTeamIgnoreClick() {
		widgetDescriptor.put(WidgetConstants.USER_TEAM_BADGE_WIDGET_IS_INDIVIDUAL_KEY, "false");
		widgetDescriptor.put(WidgetConstants.IS_TOC_KEY, "true");
		badge.configure(null, widgetDescriptor, null, null);
		verify(mockGinInjector).getTeamBadgeWidget();
		verify(mockTeamBadge).configure(principalId, UserBadge.DO_NOTHING_ON_CLICK);
	}


	@Test
	public void testConfigureNullIsIndividual() {
		widgetDescriptor.remove(WidgetConstants.USER_TEAM_BADGE_WIDGET_IS_INDIVIDUAL_KEY);
		badge.configure(null, widgetDescriptor, null, null);
		verify(mockGinInjector).getTeamBadgeWidget();
		verify(mockTeamBadge).configure(principalId, (ClickHandler) null);
	}

	@Test
	public void testConfigureNullPrincipalId() {
		// userbadge or teambadge widget deals with null principal id, this should pass along
		widgetDescriptor.remove(WidgetConstants.USER_TEAM_BADGE_WIDGET_ID_KEY);
		badge.configure(null, widgetDescriptor, null, null);
		verify(mockUserBadge).configure(eq((String) null));
	}

	@Test
	public void testConfigureFromUsernameAlias() {
		widgetDescriptor.clear();
		String alias = "my-alias";
		widgetDescriptor.put(WidgetConstants.ALIAS_KEY, alias);
		boolean isIndividual = true;
		String ownerId = "userId";
		when(mockUserGroupHeader.getIsIndividual()).thenReturn(isIndividual);
		when(mockUserGroupHeader.getOwnerId()).thenReturn(ownerId);
		badge.configure(null, widgetDescriptor, null, null);
		verify(mockUserGroupHeaderAsyncHandler).getUserGroupHeader(eq(alias), any(AsyncCallback.class));
		verify(mockUserBadge).configure(ownerId);
	}

	@Test
	public void testConfigureFromTeamAlias() {
		widgetDescriptor.clear();
		String alias = "my-team-alias";
		widgetDescriptor.put(WidgetConstants.ALIAS_KEY, alias);
		boolean isIndividual = false;
		String ownerId = "teamId";
		when(mockUserGroupHeader.getIsIndividual()).thenReturn(isIndividual);
		when(mockUserGroupHeader.getOwnerId()).thenReturn(ownerId);
		badge.configure(null, widgetDescriptor, null, null);
		verify(mockUserGroupHeaderAsyncHandler).getUserGroupHeader(eq(alias), any(AsyncCallback.class));
		verify(mockTeamBadge).configure(ownerId, (ClickHandler) null);

	}
}

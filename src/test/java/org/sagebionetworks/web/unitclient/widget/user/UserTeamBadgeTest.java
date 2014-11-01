package org.sagebionetworks.web.unitclient.widget.user;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.client.widget.team.UserTeamBadge;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WidgetConstants;

import com.google.gwt.user.client.ui.Widget;

/**
 * Unit test for the Team Badge widget.
 *
 */
public class UserTeamBadgeTest {

	PortalGinInjector mockGinInjector;
	UserBadge mockUserBadge;
	TeamBadge mockTeamBadge;
	UserTeamBadge badge;
	String principalId = "id1";
	Map<String, String> widgetDescriptor;
	
	@Before
	public void before() throws JSONObjectAdapterException{
		mockGinInjector = mock(PortalGinInjector.class);
		mockUserBadge = mock(UserBadge.class);
		mockTeamBadge = mock(TeamBadge.class);
		Widget mockView = mock(Widget.class);
		when(mockGinInjector.getUserBadgeWidget()).thenReturn(mockUserBadge);
		when(mockGinInjector.getTeamBadgeWidget()).thenReturn(mockTeamBadge);
		when(mockUserBadge.asWidget()).thenReturn(mockView);
		when(mockTeamBadge.asWidget()).thenReturn(mockView);
		
		badge = new UserTeamBadge(mockGinInjector);
		widgetDescriptor = new HashMap<String, String>();
		widgetDescriptor.put(WidgetConstants.USER_TEAM_BADGE_WIDGET_IS_INDIVIDUAL_KEY, "true");
		widgetDescriptor.put(WidgetConstants.USER_TEAM_BADGE_WIDGET_ID_KEY, principalId);
	}
	
	@Test
	public void testConfigure(){
		badge.configure(null, widgetDescriptor, null, null);
		verify(mockGinInjector).getUserBadgeWidget();
		verify(mockUserBadge).configure(eq(principalId));
	}
	
	@Test
	public void testConfigureTeam(){
		widgetDescriptor.put(WidgetConstants.USER_TEAM_BADGE_WIDGET_IS_INDIVIDUAL_KEY, "false");
		badge.configure(null, widgetDescriptor, null, null);
		verify(mockGinInjector).getTeamBadgeWidget();
		verify(mockTeamBadge).configure(eq(principalId));
	}
	
	@Test
	public void testConfigureNullIsIndividual(){
		widgetDescriptor.remove(WidgetConstants.USER_TEAM_BADGE_WIDGET_IS_INDIVIDUAL_KEY);
		badge.configure(null, widgetDescriptor, null, null);
		verify(mockGinInjector).getTeamBadgeWidget();
		verify(mockTeamBadge).configure(eq(principalId));
	}
	
	@Test
	public void testConfigureNullPrincipalId(){
		//userbadge or teambadge widget deals with null principal id, this should pass along
		widgetDescriptor.remove(WidgetConstants.USER_TEAM_BADGE_WIDGET_ID_KEY);
		badge.configure(null, widgetDescriptor, null, null);
		verify(mockUserBadge).configure(eq((String)null));
	}


}

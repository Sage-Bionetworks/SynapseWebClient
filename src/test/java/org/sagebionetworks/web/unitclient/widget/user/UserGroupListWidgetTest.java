package org.sagebionetworks.web.unitclient.widget.user;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.team.BigTeamBadge;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.client.widget.user.UserGroupListWidget;
import org.sagebionetworks.web.client.widget.user.UserGroupListWidgetView;


public class UserGroupListWidgetTest {
	
	private UserGroupListWidgetView mockView;
	private GlobalApplicationState mockGlobalApplicationState;
	private PortalGinInjector mockPortalGinInjector;
	
	private UserGroupListWidget widget;
	
	@Before
	public void before() {
		mockView = mock(UserGroupListWidgetView.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPortalGinInjector = mock(PortalGinInjector.class);
		widget = new UserGroupListWidget(mockView, mockGlobalApplicationState, mockPortalGinInjector);
	}
	
	@Test
	public void testConfigure() {
		List<UserGroupHeader> testUsers = getIndividualTestUsers();
		widget.configure(testUsers);
		verify(mockView).configure(eq(testUsers));
	}
	
	@Test
	public void testGetUsers() {
		List<UserGroupHeader> testUsers = getIndividualTestUsers();
		widget.configure(testUsers);
		assertTrue(widget.getUsers().equals(testUsers));
	}
	
	@Test
	public void testBadgeWidgetIndividualBig() {
		UserBadge mockBadge = mock(UserBadge.class);
		when(mockPortalGinInjector.getUserBadgeWidget()).thenReturn(mockBadge);
		
		List<UserGroupHeader> testUsers = getIndividualTestUsers();
		widget.configure(testUsers);
		widget.getBadgeWidget(testUsers.get(0).getOwnerId(), testUsers.get(0).getIsIndividual(), testUsers.get(0).getUserName());
		verify(mockPortalGinInjector).getUserBadgeWidget();
	}
	
	@Test
	public void testBadgeWidgetIndividualSmall() {
		UserBadge mockBadge = mock(UserBadge.class);
		when(mockPortalGinInjector.getUserBadgeWidget()).thenReturn(mockBadge);
		when(mockBadge.asWidget()).thenReturn(null);
		
		List<UserGroupHeader> testUsers = getIndividualTestUsers();
		widget.configure(testUsers, false);
		widget.getBadgeWidget(testUsers.get(0).getOwnerId(), testUsers.get(0).getIsIndividual(), testUsers.get(0).getUserName());
		verify(mockPortalGinInjector).getUserBadgeWidget();
	}
	
	@Test
	public void testBadgeWidgetGroupBig() {
		BigTeamBadge mockBadge = mock(BigTeamBadge.class);
		when(mockPortalGinInjector.getBigTeamBadgeWidget()).thenReturn(mockBadge);
		when(mockBadge.asWidget()).thenReturn(null);
		
		List<UserGroupHeader> testUsers = getGroupTestUsers();
		widget.configure(testUsers);
		widget.getBadgeWidget(testUsers.get(0).getOwnerId(), testUsers.get(0).getIsIndividual(), testUsers.get(0).getUserName());
		verify(mockPortalGinInjector).getBigTeamBadgeWidget();
	}
	
	@Test
	public void testBadgeWidgetGroupSmall() {
		TeamBadge mockBadge = mock(TeamBadge.class);
		when(mockPortalGinInjector.getTeamBadgeWidget()).thenReturn(mockBadge);
		when(mockBadge.asWidget()).thenReturn(null);
		
		List<UserGroupHeader> testUsers = getGroupTestUsers();
		widget.configure(testUsers, false);
		widget.getBadgeWidget(testUsers.get(0).getOwnerId(), testUsers.get(0).getIsIndividual(), testUsers.get(0).getUserName());
		verify(mockPortalGinInjector).getTeamBadgeWidget();
	}
	
	private static List<UserGroupHeader> getIndividualTestUsers() {
		List<UserGroupHeader> peopleList = new ArrayList<UserGroupHeader>();
		UserGroupHeader header = new UserGroupHeader();
		header.setOwnerId("2112");
		header.setFirstName("Geddy");
		header.setLastName("Lee");
		header.setIsIndividual(true);
		peopleList.add(header);
		header = new UserGroupHeader();
		header.setOwnerId("1221");
		header.setFirstName("Alex");
		header.setLastName("Lifeson");
		header.setIsIndividual(true);
		peopleList.add(header);
		header = new UserGroupHeader();
		header.setOwnerId("1212");
		header.setFirstName("Neil");
		header.setLastName("Peart");
		header.setIsIndividual(true);
		peopleList.add(header);
		return peopleList;
	}
	
	private static List<UserGroupHeader> getGroupTestUsers() {
		List<UserGroupHeader> groupList = new ArrayList<UserGroupHeader>();
		UserGroupHeader header = new UserGroupHeader();
		header.setOwnerId("2112");
		header.setUserName("RUSH");
		header.setIsIndividual(false);
		groupList.add(header);
		return groupList;
	}
	
}

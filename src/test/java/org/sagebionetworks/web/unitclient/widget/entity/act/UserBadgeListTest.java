package org.sagebionetworks.web.unitclient.widget.entity.act;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;


import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.entity.act.UserBadgeItem;
import org.sagebionetworks.web.client.widget.entity.act.UserBadgeList;
import org.sagebionetworks.web.client.widget.entity.act.UserBadgeListView;

import com.google.gwt.user.client.ui.Widget;


public class UserBadgeListTest {

	UserBadgeList list;
	
	@Mock
	UserBadgeListView mockView;
	@Mock
	PortalGinInjector mockGinInjector;
	
	String userId;
	String userId2;
	
	@Mock
	UserBadgeItem mockUserBadgeItem;
	@Mock
	UserBadgeItem mockUserBadgeItem2;

	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		list = new UserBadgeList(mockView, mockGinInjector);
		userId = "1234567";
		userId2 = "9876543";
		when(mockGinInjector.getUserBadgeItem()).thenReturn(mockUserBadgeItem);
		when(mockUserBadgeItem.getUserId()).thenReturn(userId);
		when(mockUserBadgeItem2.getUserId()).thenReturn(userId2);
	}
	
	@Test
	public void testConfigure() {
		list.configure();
		verify(mockView).setToolbarVisible(false);
	}	
	
	@Test
	public void testAddUser() {
		list.configure();
		list.addUserBadge(userId);
		verify(mockGinInjector).getUserBadgeItem();
		verify(mockView).addUserBadge(any(Widget.class));
	}
	
	@Test
	public void testSetDeleteTrueNoUsers() {
		list.configure();
		list.setCanDelete(true);
		verify(mockView, times(2)).setToolbarVisible(false);
	}
	
	@Test
	public void testSetDeleteTrueWithUsers() {
		list.configure();
		list.addUserBadge(userId);
		list.setCanDelete(true);
		verify(mockView).setToolbarVisible(true);
	}
	
	@Test
	public void testSetDeleteFalseWithUsers() {
		list.configure();
		list.setCanDelete(false);
		verify(mockView, times(2)).setToolbarVisible(false);	
	}
	
	@Test
	public void testRefreshListUIWithUser() {
		list.configure();
		list.addUserBadge(userId);
		list.refreshListUI();
		verify(mockView).clearUserBadges();
		verify(mockView, times(2)).addUserBadge(any(Widget.class));
	}
	
	@Test
	public void testRefreshListUINoUsers() {
		list.configure();
		list.refreshListUI();
		verify(mockView).clearUserBadges();
		verify(mockView, times(0)).addUserBadge(any(Widget.class));
	}
	
	@Test
	public void testDeleteSelected() {
		list.configure();
		list.addUserBadge(userId);
		when(mockUserBadgeItem.isSelected()).thenReturn(true);
		list.deleteSelected();
		verify(mockView).clearUserBadges();
		verify(mockView, times(1)).addUserBadge(any(Widget.class)); //only called when user added initially
	}
	
	@Test
	public void testDeleteSelectedMultiple() {
		list.configure();
		when(mockUserBadgeItem.isSelected()).thenReturn(true);
		when(mockUserBadgeItem2.isSelected()).thenReturn(false);
		when(mockGinInjector.getUserBadgeItem()).thenReturn(mockUserBadgeItem, mockUserBadgeItem2);
		list.addUserBadge(userId);
		list.addUserBadge(userId2);
		verify(mockView, times(2)).addUserBadge(any(Widget.class)); 
		reset(mockView);
		list.deleteSelected();
		verify(mockView).clearUserBadges();
		verify(mockView, times(1)).addUserBadge(any(Widget.class)); 
	}
	
	@Test
	public void testCheckSelectionStateSelected() {
		list.configure();
		when(mockUserBadgeItem.isSelected()).thenReturn(true);
		list.setCanDelete(true);
		list.addUserBadge(userId);
		list.checkSelectionState();
		verify(mockView).setCanDelete(true);
	}
	
	@Test
	public void testCheckSelectionStateNotSelected() {
		list.configure();
		when(mockUserBadgeItem.isSelected()).thenReturn(false);
		list.setCanDelete(true);
		list.addUserBadge(userId);
		list.checkSelectionState();
		verify(mockView).setCanDelete(false);
	}
	
	@Test
	public void testGetUserIds() {
		list.configure();
		list.addUserBadge(userId);
		List<String> userIdList = list.getUserIds();
		assertTrue(userIdList.size() == 1);
		assertTrue(userIdList.contains(userId));
	}
	
	@Test
	public void testGetUserIdsAfterDeleting() {
		list.configure();
		when(mockGinInjector.getUserBadgeItem()).thenReturn(mockUserBadgeItem, mockUserBadgeItem2);
		when(mockUserBadgeItem.isSelected()).thenReturn(true);
		when(mockUserBadgeItem2.isSelected()).thenReturn(false);
		
		list.addUserBadge(userId);
		list.addUserBadge(userId2);
		list.deleteSelected();
		List<String> userIdList = list.getUserIds();
		assertTrue(userIdList.size() == 1);
		assertTrue(userIdList.contains(userId2));
	}

	@Test
	public void testSelectAll() {
		list.configure();
		when(mockGinInjector.getUserBadgeItem()).thenReturn(mockUserBadgeItem, mockUserBadgeItem2);
		list.addUserBadge(userId);
		list.addUserBadge(userId2);
		list.selectAll();
		verify(mockUserBadgeItem).setSelected(true);
		verify(mockUserBadgeItem2).setSelected(true);
	}
	
	@Test
	public void testSelectNone() {
		list.configure();
		when(mockGinInjector.getUserBadgeItem()).thenReturn(mockUserBadgeItem, mockUserBadgeItem2);
		list.addUserBadge(userId);
		list.addUserBadge(userId2);
		list.selectNone();
		verify(mockUserBadgeItem).setSelected(false);
		verify(mockUserBadgeItem2).setSelected(false);
	}
	
}

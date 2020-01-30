package org.sagebionetworks.web.unitclient.widget.entity.act;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.dataaccess.AccessType;
import org.sagebionetworks.repo.model.dataaccess.AccessorChange;
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
	@Captor
	ArgumentCaptor<List<String>> listCaptor;
	@Mock
	AccessorChange mockChange1;
	@Mock
	AccessorChange mockChange1b;

	@Mock
	AccessorChange mockChange2;
	public final static String USER_ID_1 = "1234567";
	public final static String USER_ID_2 = "9876543";

	@Mock
	UserBadgeItem mockUserBadgeItem;
	@Mock
	UserBadgeItem mockUserBadgeItem2;


	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		list = new UserBadgeList(mockView, mockGinInjector);
		when(mockGinInjector.getUserBadgeItem()).thenReturn(mockUserBadgeItem);
		when(mockUserBadgeItem.getUserId()).thenReturn(USER_ID_1);
		when(mockUserBadgeItem2.getUserId()).thenReturn(USER_ID_2);
		when(mockChange1.getUserId()).thenReturn(USER_ID_1);
		when(mockChange1.getType()).thenReturn(AccessType.GAIN_ACCESS);
		when(mockChange2.getUserId()).thenReturn(USER_ID_2);
		when(mockChange2.getType()).thenReturn(AccessType.GAIN_ACCESS);
		when(mockChange1b.getUserId()).thenReturn(USER_ID_1);
		when(mockChange1b.getType()).thenReturn(AccessType.RENEW_ACCESS);
	}

	@Test
	public void testConfigure() {
		list.configure();
		verify(mockView).setToolbarVisible(false);
	}

	@Test
	public void testAddUser() {
		list.configure();
		list.addAccessorChange(mockChange1);
		verify(mockGinInjector).getUserBadgeItem();
		verify(mockView).addUserBadge(any(Widget.class));

		// verify attempting to add the same user, it should ignore.
		list.addAccessorChange(mockChange1b);
		// not called again (still a single call, from the first addAccessorChange)
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
		list.addAccessorChange(mockChange1);
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
		list.addAccessorChange(mockChange1);
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
		list.addAccessorChange(mockChange1);
		when(mockUserBadgeItem.isSelected()).thenReturn(true);
		list.deleteSelected();
		verify(mockView).clearUserBadges();
		verify(mockView, times(1)).addUserBadge(any(Widget.class)); // only called when user added initially

		// if we add it again, it should add a new user badge
		list.addAccessorChange(mockChange1);
		verify(mockView, times(2)).addUserBadge(any(Widget.class)); // only called when user added initially
	}

	@Test
	public void testDeleteSelectedMultiple() {
		list.configure();
		when(mockUserBadgeItem.isSelected()).thenReturn(true);
		when(mockUserBadgeItem2.isSelected()).thenReturn(false);
		when(mockGinInjector.getUserBadgeItem()).thenReturn(mockUserBadgeItem, mockUserBadgeItem2);
		list.addAccessorChange(mockChange1);
		list.addAccessorChange(mockChange2);
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
		list.addAccessorChange(mockChange1);
		list.checkSelectionState();
		verify(mockView).setCanDelete(true);
	}

	@Test
	public void testCheckSelectionStateNotSelected() {
		list.configure();
		when(mockUserBadgeItem.isSelected()).thenReturn(false);
		list.setCanDelete(true);
		list.addAccessorChange(mockChange1);
		list.checkSelectionState();
		verify(mockView).setCanDelete(false);
	}

	@Test
	public void testGetUserIds() {
		list.configure();
		list.addAccessorChange(mockChange1);
		List<AccessorChange> changeList = list.getAccessorChanges();
		assertTrue(changeList.size() == 1);
		AccessorChange change = changeList.get(0);
		assertEquals(USER_ID_1, change.getUserId());
	}

	@Test
	public void testGetUserIdsAfterDeleting() {
		list.configure();
		when(mockGinInjector.getUserBadgeItem()).thenReturn(mockUserBadgeItem, mockUserBadgeItem2);
		when(mockUserBadgeItem.isSelected()).thenReturn(true);
		when(mockUserBadgeItem2.isSelected()).thenReturn(false);

		list.addAccessorChange(mockChange1);
		list.addAccessorChange(mockChange2);
		list.deleteSelected();
		List<AccessorChange> changeList = list.getAccessorChanges();
		assertTrue(changeList.size() == 1);
		AccessorChange change = changeList.get(0);
		assertEquals(USER_ID_2, change.getUserId());
	}

	@Test
	public void testAddSubmitterUserBadge() {
		list.configure();
		when(mockGinInjector.getUserBadgeItem()).thenReturn(mockUserBadgeItem);
		list.addSubmitterAccessorChange(mockChange1);
		verify(mockUserBadgeItem).configure(mockChange1);
		verify(mockUserBadgeItem).setSelectVisible(false);
	}

	@Test
	public void testSelectAll() {
		list.configure();
		when(mockGinInjector.getUserBadgeItem()).thenReturn(mockUserBadgeItem, mockUserBadgeItem2);
		list.addAccessorChange(mockChange1);
		list.addAccessorChange(mockChange2);
		list.selectAll();
		verify(mockUserBadgeItem).setSelected(true);
		verify(mockUserBadgeItem2).setSelected(true);
	}

	@Test
	public void testSelectNone() {
		list.configure();
		when(mockGinInjector.getUserBadgeItem()).thenReturn(mockUserBadgeItem, mockUserBadgeItem2);
		list.addAccessorChange(mockChange1);
		list.addAccessorChange(mockChange2);
		list.selectNone();
		verify(mockUserBadgeItem).setSelected(false);
		verify(mockUserBadgeItem2).setSelected(false);
	}

}

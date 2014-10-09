package org.sagebionetworks.web.unitclient.widget.user;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.widget.user.UserGroupListWidget;
import org.sagebionetworks.web.client.widget.user.UserGroupListWidgetView;


public class UserGroupListWidgetTest {
	
	private UserGroupListWidgetView mockView;
	private GlobalApplicationState mockGlobalApplicationState;
	
	private UserGroupListWidget widget;
	
	@Before
	public void before() {
		mockView = mock(UserGroupListWidgetView.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		widget = new UserGroupListWidget(mockView, mockGlobalApplicationState);
	}
	
	@Test
	public void testConfigure() {
		List<UserGroupHeader> testUsers = getTestUsers();
		widget.configure(testUsers);
		verify(mockView).configure(eq(testUsers), eq(true));
	}
	
	@Test
	public void testGetUsers() {
		List<UserGroupHeader> testUsers = getTestUsers();
		widget.configure(testUsers);
		assertTrue(widget.getUsers().equals(testUsers));
	}
	
	private static List<UserGroupHeader> getTestUsers() {
		List<UserGroupHeader> peopleList = new ArrayList<UserGroupHeader>();
		UserGroupHeader header = new UserGroupHeader();
		header.setOwnerId("2112");
		header.setFirstName("Geddy");
		header.setLastName("Lee");
		peopleList.add(header);
		header = new UserGroupHeader();
		header.setOwnerId("1221");
		header.setFirstName("Alex");
		header.setLastName("Lifeson");
		peopleList.add(header);
		header = new UserGroupHeader();
		header.setOwnerId("1212");
		header.setFirstName("Neil");
		header.setLastName("Peart");
		peopleList.add(header);
		return peopleList;
	}
}

package org.sagebionetworks.web.unitclient.widget.sharing;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.sharing.AddPeopleToAclPanel;
import org.sagebionetworks.web.client.widget.sharing.AddPeopleToAclPanelView;
import org.sagebionetworks.web.shared.users.PermissionLevel;

public class AddPeopleToAclPanelTest {

	private AddPeopleToAclPanel panel;
	private AddPeopleToAclPanelView mockView;
	
	@Before
	public void before() {
		mockView = mock(AddPeopleToAclPanelView.class);
		panel = new AddPeopleToAclPanel(mockView);
	}
	
	@Test
	public void testGetAndSetSelectedPermissionLevel() {
		PermissionLevel perm = PermissionLevel.CAN_ADMINISTER;
		panel.setSelectedPermissionLevel(perm);
		assertTrue(panel.getSelectedPermissionLevel().equals(perm));
	}
}

package org.sagebionetworks.web.unitclient.widget.sharing;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.sharing.SharingPermissionsGrid;
import org.sagebionetworks.web.client.widget.sharing.SharingPermissionsGridView;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.PermissionLevel;

public class SharingPermissionsGridTest {

	private SharingPermissionsGrid grid;
	private SharingPermissionsGridView mockView;
	
	@Before
	public void before() {
		mockView = mock(SharingPermissionsGridView.class);
		grid = new SharingPermissionsGrid(mockView);
	}
	
	@Test
	public void testAdd() {
		AclEntry entry1 = new AclEntry();
		entry1.setTitle("Entry 1");
		
		AclEntry entry2 = new AclEntry();
		entry1.setTitle("Entry 2");
		
		AclEntry entry3 = new AclEntry();
		entry1.setTitle("Entry 3");
		
		AclEntry[] entries = { entry1, entry2, entry3 };
		
		for (AclEntry entry : entries) {
			grid.add(entry, null, null);
		}
		
		verify(mockView, times(3)).add(any(AclEntry.class), eq((PermissionLevel[]) null), eq((Map<PermissionLevel,String>) null));
	}
	
	@Test
	public void testGetAt() {
		AclEntry entry1 = new AclEntry();
		entry1.setTitle("Entry 1");
		
		AclEntry entry2 = new AclEntry();
		entry1.setTitle("Entry 2");
		
		AclEntry entry3 = new AclEntry();
		entry1.setTitle("Entry 3");
		
		AclEntry[] entries = { entry1, entry2, entry3 };
		
		for (AclEntry entry : entries) {
			grid.add(entry, null, null);
		}
		
		assertTrue(grid.getAt(1) == entry2);
	}
	
	@Test
	public void testInsert() {
		AclEntry entry1 = new AclEntry();
		entry1.setTitle("Entry 1");
		
		AclEntry entry2 = new AclEntry();
		entry1.setTitle("Entry 2");
		
		AclEntry entry3 = new AclEntry();
		entry1.setTitle("Entry 3");
		
		grid.add(entry1, null, null);
		grid.add(entry3, null, null);
		
		boolean deleteButtonVisible = true;
		grid.insert(entry2, 1, null, null, deleteButtonVisible);
		
		verify(mockView).insert(entry2, 1,(PermissionLevel[]) null, (Map<PermissionLevel,String>) null, deleteButtonVisible);
		
		assertTrue(grid.getAt(1) == entry2);
	}
}

package org.sagebionetworks.web.unitclient.widget.sharing;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.sharing.SharingPermissionsGrid;
import org.sagebionetworks.web.client.widget.sharing.SharingPermissionsGridView;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.PermissionLevel;

public class SharingPermissionsGridTest {

	public static final String PRINCIPAL_ID_2 = "2";
	private SharingPermissionsGrid grid;
	@Mock
	private SharingPermissionsGridView mockView;
	@Mock
	AuthenticationController mockAuthenticationController;
	AclEntry entry1, entry2, entry3;
	AclEntry[] entries;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		grid = new SharingPermissionsGrid(mockView, mockAuthenticationController);

		entry1 = new AclEntry();
		entry1.setTitle("Entry 1");
		entry1.setOwnerId("1");

		entry2 = new AclEntry();
		entry2.setTitle("Entry 2");
		entry2.setOwnerId(PRINCIPAL_ID_2);

		entry3 = new AclEntry();
		entry3.setTitle("Entry 3");
		entry3.setOwnerId("3");

		entries = new AclEntry[] {entry1, entry2, entry3};
	}

	@Test
	public void testAdd() {
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(PRINCIPAL_ID_2);
		for (AclEntry entry : entries) {
			grid.add(entry, null, null);
		}
		verify(mockView, times(2)).add(any(AclEntry.class), eq((PermissionLevel[]) null), eq((Map<PermissionLevel, String>) null), eq(true));
		verify(mockView, times(1)).add(any(AclEntry.class), eq((PermissionLevel[]) null), eq((Map<PermissionLevel, String>) null), eq(false));
	}



	@Test
	public void testGetAt() {
		for (AclEntry entry : entries) {
			grid.add(entry, null, null);
		}

		assertTrue(grid.getAt(1) == entry2);
	}

	@Test
	public void testInsert() {
		grid.add(entry1, null, null);
		grid.add(entry3, null, null);

		boolean deleteButtonVisible = true;
		grid.insert(entry2, 1, null, null, deleteButtonVisible);

		verify(mockView).insert(entry2, 1, (PermissionLevel[]) null, (Map<PermissionLevel, String>) null, deleteButtonVisible);

		assertTrue(grid.getAt(1) == entry2);
	}
}

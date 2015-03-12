package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView;

public class EntityMetadataTest {
	EntityMetadataView mockView;
	String entityId = "syn123";
	EntityMetadata widget;
	AuthenticationController mockAuthenticationController;

	@Before
	public void before() throws JSONObjectAdapterException {
		mockAuthenticationController = mock(AuthenticationController.class);
		mockView = mock(EntityMetadataView.class);
		widget = new EntityMetadata(mockView, mockAuthenticationController);
	}
	
	@Test
	public void testSetEntityBundleProject() {
		UserEntityPermissions permissions = mock(UserEntityPermissions.class);
		boolean canChangePermissions = false;
		boolean canCertifiedUserEdit = true;
		when(permissions.getCanChangePermissions()).thenReturn(canChangePermissions);
		when(permissions.getCanCertifiedUserEdit()).thenReturn(canCertifiedUserEdit);
		Project project = new Project();
		EntityBundle bundle = new EntityBundle(project, null, permissions, null, null, null, null, null);
		Long versionNumber = -122L;
		widget.setEntityBundle(bundle, versionNumber);
		verify(mockView).setEntityBundle(bundle, canChangePermissions, canCertifiedUserEdit, true);
		verify(mockView).setDetailedMetadataVisible(true);
		verify(mockView).setEntityNameVisible(true);
	}
	@Test
	public void testSetEntityBundleFileEntity() {
		UserEntityPermissions permissions = mock(UserEntityPermissions.class);
		boolean canChangePermissions = true;
		boolean canCertifiedUserEdit = false;
		when(permissions.getCanChangePermissions()).thenReturn(canChangePermissions);
		when(permissions.getCanCertifiedUserEdit()).thenReturn(canCertifiedUserEdit);
		FileEntity fileEntity = new FileEntity();
		EntityBundle bundle = new EntityBundle(fileEntity, null, permissions, null, null, null, null, null);
		Long versionNumber = null;
		widget.setEntityBundle(bundle, versionNumber);
		verify(mockView).setEntityBundle(bundle, canChangePermissions, canCertifiedUserEdit, false);
		verify(mockView).setDetailedMetadataVisible(true);
		verify(mockView).setEntityNameVisible(false);
	}
	
}

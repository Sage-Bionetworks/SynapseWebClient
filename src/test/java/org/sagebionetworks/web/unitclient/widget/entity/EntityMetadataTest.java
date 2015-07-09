package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView;

public class EntityMetadataTest {
	EntityMetadataView mockView;
	String entityId = "syn123";
	EntityMetadata widget;
	AuthenticationController mockAuthenticationController;

	@Before
	public void before() {
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
		EntityBundle bundle = new EntityBundle();
		bundle.setEntity(project);
		bundle.setPermissions(permissions);
		Long versionNumber = -122L;
		widget.setEntityBundle(bundle, versionNumber);
		verify(mockView).setEntityBundle(bundle, canChangePermissions, canCertifiedUserEdit, true);
		verify(mockView).setDetailedMetadataVisible(true);
	}
	@Test
	public void testSetEntityBundleFileEntity() {
		UserEntityPermissions permissions = mock(UserEntityPermissions.class);
		boolean canChangePermissions = true;
		boolean canCertifiedUserEdit = false;
		when(permissions.getCanChangePermissions()).thenReturn(canChangePermissions);
		when(permissions.getCanCertifiedUserEdit()).thenReturn(canCertifiedUserEdit);
		FileEntity fileEntity = new FileEntity();
		EntityBundle bundle = new EntityBundle();
		bundle.setEntity(fileEntity);
		bundle.setPermissions(permissions);
		Long versionNumber = null;
		widget.setEntityBundle(bundle, versionNumber);
		verify(mockView).setEntityBundle(bundle, canChangePermissions, canCertifiedUserEdit, false);
		verify(mockView).setDetailedMetadataVisible(true);
	}
	
}

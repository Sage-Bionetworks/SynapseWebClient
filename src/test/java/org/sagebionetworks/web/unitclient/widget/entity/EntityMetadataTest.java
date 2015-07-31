package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.DoiWidget;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;
import org.sagebionetworks.web.client.widget.entity.FileHistoryWidget;
import org.sagebionetworks.web.client.widget.entity.RestrictionWidget;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationsRendererWidget;

public class EntityMetadataTest {
	EntityMetadataView mockView;
	String entityId = "syn123";
	String entityName = "testEntity";
	EntityMetadata widget;
	PortalGinInjector mockInjector;
	AuthenticationController mockAuthenticationController;
	private AnnotationsRendererWidget mockAnnotationsWidget;
	private FavoriteWidget mockFavoriteWidget;
	private DoiWidget mockDoiWidget;
	private RestrictionWidget mockRestrictionWidget;
	private FileHistoryWidget mockFileHistoryWidget;
	
	@Before
	public void before() {
		mockView = mock(EntityMetadataView.class);
		mockInjector = mock(PortalGinInjector.class);
		mockAnnotationsWidget = mock(AnnotationsRendererWidget.class);
		mockFavoriteWidget = mock(FavoriteWidget.class);
		mockDoiWidget = mock(DoiWidget.class);
		mockRestrictionWidget = mock(RestrictionWidget.class);
		mockFileHistoryWidget = mock(FileHistoryWidget.class);
		widget = new EntityMetadata(mockView, mockFavoriteWidget, mockDoiWidget, mockAnnotationsWidget, mockRestrictionWidget, mockFileHistoryWidget);
		when(mockInjector.getFileHistoryWidget()).thenReturn(mockFileHistoryWidget);
	}
	
	@Test
	public void testSetEntityBundleProject() {
		UserEntityPermissions permissions = mock(UserEntityPermissions.class);
		boolean canChangePermissions = false;
		boolean canCertifiedUserEdit = true;
		when(permissions.getCanChangePermissions()).thenReturn(canChangePermissions);
		when(permissions.getCanCertifiedUserEdit()).thenReturn(canCertifiedUserEdit);
		Project project = new Project();
		project.setName(entityName);
		project.setId(entityId);
		EntityBundle bundle = new EntityBundle();
		bundle.setEntity(project);
		bundle.setPermissions(permissions);
		Long versionNumber = -122L;
		widget.setEntityBundle(bundle, versionNumber);
		verify(mockView).setDetailedMetadataVisible(true);
		verify(mockView).setEntityNameVisible(true);
		verify(mockView).setEntityId(entityId);
		verify(mockView).setEntityName(entityName);
		verify(mockView).setFileHistoryVisible(false);
		verify(mockFavoriteWidget).configure(entityId);
		verify(mockDoiWidget).configure(entityId, canCertifiedUserEdit, versionNumber);
		verify(mockAnnotationsWidget).configure(bundle, canCertifiedUserEdit);
	}
	
	@Test
	public void testSetEntityBundleFileEntityMostRecent() {
		UserEntityPermissions permissions = mock(UserEntityPermissions.class);
		boolean canChangePermissions = true;
		boolean canCertifiedUserEdit = false;
		when(permissions.getCanChangePermissions()).thenReturn(canChangePermissions);
		when(permissions.getCanCertifiedUserEdit()).thenReturn(canCertifiedUserEdit);
		FileEntity fileEntity = new FileEntity();
		fileEntity.setName(entityName);
		fileEntity.setId(entityId);
		EntityBundle bundle = new EntityBundle();
		bundle.setEntity(fileEntity);
		bundle.setPermissions(permissions);
		Long versionNumber = null;
		widget.setEntityBundle(bundle, versionNumber);
		verify(mockView).setDetailedMetadataVisible(true);
		verify(mockView).setEntityNameVisible(false);
		verify(mockView).setEntityId(entityId);
		verify(mockView).setEntityName(entityName);
		verify(mockView).setFileHistoryVisible(false);
		verify(mockFileHistoryWidget).setEntityBundle(bundle, versionNumber);
		verify(mockFileHistoryWidget).setEntityUpdatedHandler(any(EntityUpdatedHandler.class));
		verify(mockFavoriteWidget).configure(entityId);
		verify(mockDoiWidget).configure(entityId, canCertifiedUserEdit, versionNumber);
		verify(mockAnnotationsWidget).configure(bundle, canCertifiedUserEdit);
	}
	
	@Test
	public void testSetEntityBundleFileEntityNotMostRecentVersion() {
		UserEntityPermissions permissions = mock(UserEntityPermissions.class);
		boolean canChangePermissions = true;
		boolean canCertifiedUserEdit = false;
		when(permissions.getCanChangePermissions()).thenReturn(canChangePermissions);
		when(permissions.getCanCertifiedUserEdit()).thenReturn(canCertifiedUserEdit);
		FileEntity fileEntity = new FileEntity();
		fileEntity.setName(entityName);
		fileEntity.setId(entityId);
		EntityBundle bundle = new EntityBundle();
		bundle.setEntity(fileEntity);
		bundle.setPermissions(permissions);
		Long versionNumber = -122L;
		widget.setEntityBundle(bundle, versionNumber);
		verify(mockView).setDetailedMetadataVisible(true);
		verify(mockView).setEntityNameVisible(false);
		verify(mockView).setEntityId(entityId);
		verify(mockView).setEntityName(entityName);
		verify(mockFileHistoryWidget).setEntityBundle(bundle, versionNumber);
		verify(mockFileHistoryWidget).setEntityUpdatedHandler(any(EntityUpdatedHandler.class));
		verify(mockView).setFileHistoryVisible(true);
		verify(mockFavoriteWidget).configure(entityId);
		verify(mockDoiWidget).configure(entityId, canCertifiedUserEdit, versionNumber);
		verify(mockAnnotationsWidget).configure(bundle, canCertifiedUserEdit);
	}
	
}

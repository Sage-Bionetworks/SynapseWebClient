package org.sagebionetworks.web.unitclient.widget.entity.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.CertifiedUserController;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightControllerImpl;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

public class PreflightControllerImplTest {

	CertifiedUserController mockCertifiedUserController;
	Callback mockCallback;
	PreflightControllerImpl controller;
	UserEntityPermissions permissions;
	EntityBundle bundle;

	@Before
	public void before() {
		mockCertifiedUserController = Mockito.mock(CertifiedUserController.class);
		mockCallback = Mockito.mock(Callback.class);
		controller = new PreflightControllerImpl(mockCertifiedUserController);
		Project entity = new Project();
		permissions = new UserEntityPermissions();
		permissions.setIsCertifiedUser(true);
		bundle = new EntityBundle();
		bundle.setEntity(entity);
		bundle.setPermissions(permissions);
	}

	@Test
	public void testCheckUpload() {
		// Delegated to certified controller.
		AsyncMockStubber.callWithInvoke().when(mockCertifiedUserController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		controller.checkUploadToEntity(bundle, mockCallback);
		verify(mockCallback).invoke();
	}

	@Test
	public void testCheckDownload() {
		controller.checkDownloadFromEntity(bundle, mockCallback);
		verify(mockCallback).invoke();
	}

	@Test
	public void testCheckCreate() {
		// Delegated to certified controller.
		AsyncMockStubber.callWithInvoke().when(mockCertifiedUserController).checkCreateEntity(any(EntityBundle.class), anyString(), any(Callback.class));
		controller.checkCreateEntity(bundle, Project.class.getName(), mockCallback);
		verify(mockCallback).invoke();
	}

	@Test
	public void testCheckDelete() {
		// Delegated to certified controller.
		AsyncMockStubber.callWithInvoke().when(mockCertifiedUserController).checkDeleteEntity(any(EntityBundle.class), any(Callback.class));
		controller.checkDeleteEntity(bundle, mockCallback);
		verify(mockCallback).invoke();
	}

	@Test
	public void testCheckUpdate() {
		// Delegated to certified controller.
		AsyncMockStubber.callWithInvoke().when(mockCertifiedUserController).checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
		controller.checkUpdateEntity(bundle, mockCallback);
		verify(mockCallback).invoke();
	}

	@Test
	public void testCheckCreateAndUploadHappy() {
		// Delegated to certified controller.
		AsyncMockStubber.callWithInvoke().when(mockCertifiedUserController).checkCreateEntity(any(EntityBundle.class), anyString(), any(Callback.class));
		AsyncMockStubber.callWithInvoke().when(mockCertifiedUserController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		controller.checkCreateEntityAndUpload(bundle, Project.class.getName(), mockCallback);
		verify(mockCallback).invoke();
	}

	@Test
	public void testCheckCreateAndUploadCannotCreate() {
		// Delegated to certified controller.
		AsyncMockStubber.callNoInvovke().when(mockCertifiedUserController).checkCreateEntity(any(EntityBundle.class), anyString(), any(Callback.class));
		AsyncMockStubber.callWithInvoke().when(mockCertifiedUserController).checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
		controller.checkCreateEntityAndUpload(bundle, Project.class.getName(), mockCallback);
		verify(mockCallback, never()).invoke();
	}
}

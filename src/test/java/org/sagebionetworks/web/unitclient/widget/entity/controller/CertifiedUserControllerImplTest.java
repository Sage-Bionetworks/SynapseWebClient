package org.sagebionetworks.web.unitclient.widget.entity.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.CertifiedUserControllerImpl;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoDialog;

public class CertifiedUserControllerImplTest {

	@Mock
	QuizInfoDialog mockQuizInfoDialog;
	CertifiedUserControllerImpl controller;
	@Mock
	Callback mockCallback;
	UserEntityPermissions permissions;
	EntityBundle bundle;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	GlobalApplicationState mockGlobalAppState;
	@Mock
	PlaceChanger mockPlaceChanger;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(mockGlobalAppState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockGinInjector.getQuizInfoDialog()).thenReturn(mockQuizInfoDialog);
		when(mockAuthController.isLoggedIn()).thenReturn(true);

		controller = new CertifiedUserControllerImpl(mockGinInjector, mockAuthController, mockGlobalAppState);
		Project entity = new Project();
		permissions = new UserEntityPermissions();
		permissions.setIsCertifiedUser(true);
		bundle = new EntityBundle();
		bundle.setEntity(entity);
		bundle.setPermissions(permissions);
	}

	@Test
	public void testCheckUploadCertified() {
		permissions.setIsCertifiedUser(true);
		controller.checkUploadToEntity(bundle, mockCallback);
		verify(mockCallback).invoke();
		verify(mockQuizInfoDialog, never()).show();
	}

	@Test
	public void testCheckUploadNotCertified() {
		permissions.setIsCertifiedUser(false);
		controller.checkUploadToEntity(bundle, mockCallback);
		verify(mockCallback, never()).invoke();
		verify(mockQuizInfoDialog).show();
	}

	@Test
	public void testCreateProjectNotCertified() {
		permissions.setIsCertifiedUser(false);
		controller.checkCreateEntity(bundle, Project.class.getName(), mockCallback);
		// Do not need to certified to create projects
		verify(mockCallback).invoke();
		verify(mockQuizInfoDialog, never()).show();
	}

	@Test
	public void testCreateNonProjectNotCertified() {
		permissions.setIsCertifiedUser(false);
		controller.checkCreateEntity(bundle, TableEntity.class.getName(), mockCallback);
		// must be certified to create any non-project
		verify(mockCallback, never()).invoke();
		verify(mockQuizInfoDialog).show();
	}

	@Test
	public void testCreateNonProjectCertified() {
		permissions.setIsCertifiedUser(true);
		controller.checkCreateEntity(bundle, TableEntity.class.getName(), mockCallback);
		// must be certified to create any non-project
		verify(mockCallback).invoke();
		verify(mockQuizInfoDialog, never()).show();
	}

	@Test
	public void testUpadateNotCertified() {
		permissions.setIsCertifiedUser(false);
		controller.checkUpdateEntity(bundle, mockCallback);
		verify(mockCallback).invoke();
		verify(mockQuizInfoDialog, never()).show();
	}

	@Test
	public void testUpdateCertified() {
		permissions.setIsCertifiedUser(true);
		controller.checkUpdateEntity(bundle, mockCallback);
		verify(mockCallback).invoke();
		verify(mockQuizInfoDialog, never()).show();
	}

	@Test
	public void testUpdateAnonymous() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);

		controller.checkUpdateEntity(bundle, mockCallback);

		verify(mockCallback, never()).invoke();
		verify(mockQuizInfoDialog, never()).show();
		verify(mockPlaceChanger).goTo(any(LoginPlace.class));
	}

	@Test
	public void testDeleteAnonymous() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);

		controller.checkDeleteEntity(bundle, mockCallback);

		verify(mockCallback, never()).invoke();
		verify(mockQuizInfoDialog, never()).show();
		verify(mockPlaceChanger).goTo(any(LoginPlace.class));
	}

}

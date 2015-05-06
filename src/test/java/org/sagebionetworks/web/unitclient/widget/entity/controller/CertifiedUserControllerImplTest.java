package org.sagebionetworks.web.unitclient.widget.entity.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.CertifiedUserControllerImpl;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoDialog;
import org.sagebionetworks.repo.model.EntityBundle;

public class CertifiedUserControllerImplTest {

	QuizInfoDialog mockQuizInfoDialog;
	CertifiedUserControllerImpl controller;
	Callback mockCallback;
	UserEntityPermissions permissions;
	EntityBundle bundle;
	
	@Before
	public void before(){
		mockQuizInfoDialog = Mockito.mock(QuizInfoDialog.class);
		mockCallback = Mockito.mock(Callback.class);
		controller = new CertifiedUserControllerImpl(mockQuizInfoDialog);
		Project entity = new Project();
		permissions = new UserEntityPermissions();
		permissions.setIsCertifiedUser(true);
		bundle = new EntityBundle();
		bundle.setEntity(entity);
		bundle.setPermissions(permissions);
	}
	
	@Test
	public void testCheckUploadCertified(){
		permissions.setIsCertifiedUser(true);
		controller.checkUploadToEntity(bundle, mockCallback);
		verify(mockCallback).invoke();
		verify(mockQuizInfoDialog, never()).show();
	}
	
	@Test
	public void testCheckUploadNotCertified(){
		permissions.setIsCertifiedUser(false);
		controller.checkUploadToEntity(bundle, mockCallback);
		verify(mockCallback, never()).invoke();
		verify(mockQuizInfoDialog).show();
	}
	
	@Test
	public void testCreateProjectNotCertified(){
		permissions.setIsCertifiedUser(false);
		controller.checkCreateEntity(bundle, Project.class.getName(), mockCallback);
		// Do not need to certified to create projects
		verify(mockCallback).invoke();
		verify(mockQuizInfoDialog, never()).show();
	}
	
	@Test
	public void testCreateNonProjectNotCertified(){
		permissions.setIsCertifiedUser(false);
		controller.checkCreateEntity(bundle, TableEntity.class.getName(), mockCallback);
		// must be certified to create any non-project
		verify(mockCallback, never()).invoke();
		verify(mockQuizInfoDialog).show();
	}
	
	@Test
	public void testCreateNonProjectCertified(){
		permissions.setIsCertifiedUser(true);
		controller.checkCreateEntity(bundle, TableEntity.class.getName(), mockCallback);
		// must be certified to create any non-project
		verify(mockCallback).invoke();
		verify(mockQuizInfoDialog, never()).show();
	}
	
	@Test
	public void testUpadateNotCertified(){
		permissions.setIsCertifiedUser(false);
		controller.checkUpdateEntity(bundle, mockCallback);
		verify(mockCallback).invoke();
		verify(mockQuizInfoDialog, never()).show();
	}
	
	@Test
	public void testUpdateCertified(){
		permissions.setIsCertifiedUser(true);
		controller.checkUpdateEntity(bundle, mockCallback);
		verify(mockCallback).invoke();
		verify(mockQuizInfoDialog, never()).show();
	}
}

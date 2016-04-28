package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowserView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class FilesBrowserTest {

	FilesBrowserView mockView;
	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	FilesBrowser filesBrowser;
	CookieProvider mockCookies;
	String configuredEntityId = "syn123";
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockView = mock(FilesBrowserView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockCookies = mock(CookieProvider.class);
		filesBrowser = new FilesBrowser(mockView, mockSynapseClient,
				mockGlobalApplicationState, mockAuthenticationController, mockCookies);
		verify(mockView).setPresenter(filesBrowser);
		boolean isCertified = true;
		boolean canCertifiedUserAddChild = true;
		filesBrowser.configure(configuredEntityId, canCertifiedUserAddChild, isCertified);
		String newId = "syn456";
		AsyncMockStubber.callSuccessWith(newId).when(mockSynapseClient).createOrUpdateEntity(any(Entity.class), any(Annotations.class), eq(true), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("").when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
		reset(mockView);
	}
	
	@Test
	public void testConfigure() {		
		String entityId = "syn123";
		boolean canCertifiedUserAddChild = false;
		filesBrowser.configure(entityId, canCertifiedUserAddChild, true);
		verify(mockView).configure(entityId, canCertifiedUserAddChild);
	}
	
	@Test
	public void testConfigureCanAddChild() {		
		String entityId = "syn123";
		boolean canCertifiedUserAddChild = true;
		filesBrowser.configure(entityId, canCertifiedUserAddChild, true);
		verify(mockView).configure(entityId, canCertifiedUserAddChild);
	}

	
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateFolder() throws Exception {
		filesBrowser.createFolder();
		verify(mockSynapseClient).createOrUpdateEntity(any(Entity.class), any(Annotations.class), eq(true), any(AsyncCallback.class));
		verify(mockView).showFolderEditDialog(anyString());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateFolderFail() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).createOrUpdateEntity(any(Entity.class), any(Annotations.class), eq(true), any(AsyncCallback.class));
		
		filesBrowser.createFolder();
		
		verify(mockSynapseClient).createOrUpdateEntity(any(Entity.class), any(Annotations.class), eq(true), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_FOLDER_CREATION_FAILED);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteFolder() throws Exception {
		String id = "syn456";
		boolean skipTrashCan = true;
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).deleteEntityById(anyString(), anyBoolean(), any(AsyncCallback.class));

		filesBrowser.setCurrentFolderEntityId(id);
		filesBrowser.deleteFolder(skipTrashCan);
		verify(mockSynapseClient).deleteEntityById(eq(id), eq(skipTrashCan), any(AsyncCallback.class));
		verify(mockView).refreshTreeView(anyString());
		verify(mockView).setNewFolderDialogVisible(false);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteFolderFail() throws Exception {
		String id = "syn456";
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).deleteEntityById(anyString(), anyBoolean(), any(AsyncCallback.class));

		filesBrowser.setCurrentFolderEntityId(id);
		filesBrowser.deleteFolder(true);
		verify(mockSynapseClient).deleteEntityById(anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_FOLDER_DELETE_FAILED);
		verify(mockView, Mockito.never()).setNewFolderDialogVisible(false);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateFolderName() throws Exception {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).updateEntity(any(Entity.class), any(AsyncCallback.class));
		Folder f = new Folder();
		f.setName("raven");
		filesBrowser.updateFolderName(f);
		verify(mockSynapseClient).updateEntity(any(Entity.class), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockView).refreshTreeView(configuredEntityId);
		verify(mockView).setNewFolderDialogVisible(false);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateFolderNameFail() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).updateEntity(any(Entity.class), any(AsyncCallback.class));
		
		Folder f = new Folder();
		f.setName("raven");
		filesBrowser.updateFolderName(f);
		
		verify(mockSynapseClient).updateEntity(any(Entity.class), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_FOLDER_RENAME_FAILED);
		verify(mockView, Mockito.never()).setNewFolderDialogVisible(false);
	}
	
	@Test
	public void testUploadStep2Certified(){
		FilesBrowser.uploadButtonClicked(configuredEntityId, mockView, mockSynapseClient, mockAuthenticationController, true);
		verify(mockView).showUploadDialog(anyString());
	}
	
	@Test
	public void testUploadStep2NotCertified(){
		boolean isCertifiedUser = false;
		FilesBrowser.uploadButtonClicked(configuredEntityId, mockView, mockSynapseClient, mockAuthenticationController, isCertifiedUser);
		verify(mockView).showQuizInfoDialog();
	}
	
	@Test
	public void testAddFolderButtonClickedCertified(){
		filesBrowser.configure(configuredEntityId, true, true);
		filesBrowser.addFolderClicked();
		verify(mockView).showFolderEditDialog(anyString());
	}
	
	@Test
	public void testAddFolderButtonClickedNotCertified(){
		filesBrowser.configure(configuredEntityId, true, false);
		filesBrowser.addFolderClicked();
		verify(mockView).showQuizInfoDialog();
	}
}


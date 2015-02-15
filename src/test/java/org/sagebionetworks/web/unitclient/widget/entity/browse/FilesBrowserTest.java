package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.EntityAccessRequirementsWidget;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowserView;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class FilesBrowserTest {

	FilesBrowserView mockView;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	AdapterFactory adapterFactory;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	FilesBrowser filesBrowser;
	CookieProvider mockCookies;
	String configuredEntityId = "syn123";
	EntityAccessRequirementsWidget mockAccessRequirementsWidget;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockView = mock(FilesBrowserView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockAccessRequirementsWidget = mock(EntityAccessRequirementsWidget.class);
		adapterFactory = new AdapterFactoryImpl();
		mockCookies = mock(CookieProvider.class);
		filesBrowser = new FilesBrowser(mockView, mockSynapseClient,
				mockNodeModelCreator, adapterFactory,
				mockGlobalApplicationState, mockAuthenticationController, mockCookies, mockAccessRequirementsWidget);
		verify(mockView).setPresenter(filesBrowser);
		boolean isCertified = true;
		boolean canCertifiedUserAddChild = true;
		filesBrowser.configure(configuredEntityId, canCertifiedUserAddChild, isCertified);
		String newId = "syn456";
		AsyncMockStubber.callSuccessWith(newId).when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(true), any(AsyncCallback.class));
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
		verify(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(true), any(AsyncCallback.class));
		verify(mockView).showFolderEditDialog(anyString());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateFolderFail() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(true), any(AsyncCallback.class));
		
		filesBrowser.createFolder();
		
		verify(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(true), any(AsyncCallback.class));
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
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateFolderName() throws Exception {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		Folder f = new Folder();
		f.setName("raven");
		filesBrowser.updateFolderName(f);
		verify(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockView).refreshTreeView(configuredEntityId);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateFolderNameFail() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		
		Folder f = new Folder();
		f.setName("raven");
		filesBrowser.updateFolderName(f);
		
		verify(mockSynapseClient).updateEntity(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_FOLDER_RENAME_FAILED);
	}
	

	@Test
	public void testUploadStep1ARsAccepted(){
		FilesBrowser.uploadButtonClickedStep1(mockAccessRequirementsWidget, configuredEntityId, mockView, mockSynapseClient, mockAuthenticationController, true);
		ArgumentCaptor<CallbackP> arg = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockAccessRequirementsWidget).showUploadAccessRequirements(eq(configuredEntityId), arg.capture());
		CallbackP callback = arg.getValue();
		callback.invoke(true);
		//verify if accepted then it should continue (eventually showing the upload dialog)
		verify(mockView).showUploadDialog(anyString());
	}
	
	@Test
	public void testUploadStep1ARsNotAccepted(){
		FilesBrowser.uploadButtonClickedStep1(mockAccessRequirementsWidget, configuredEntityId, mockView, mockSynapseClient, mockAuthenticationController, true);
		ArgumentCaptor<CallbackP> arg = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockAccessRequirementsWidget).showUploadAccessRequirements(eq(configuredEntityId), arg.capture());
		CallbackP callback = arg.getValue();
		callback.invoke(false);
		//verify if not accepted then the upload dialog is not shown
		verify(mockView, never()).showUploadDialog(anyString());
	}
	
	@Test
	public void testUploadStep2Certified(){
		FilesBrowser.uploadButtonClickedStep2(configuredEntityId, mockView, mockSynapseClient, mockAuthenticationController, true);
		verify(mockView).showUploadDialog(anyString());
	}
	
	@Test
	public void testUploadStep2NotCertified(){
		boolean isCertifiedUser = false;
		FilesBrowser.uploadButtonClickedStep2(configuredEntityId, mockView, mockSynapseClient, mockAuthenticationController, isCertifiedUser);
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












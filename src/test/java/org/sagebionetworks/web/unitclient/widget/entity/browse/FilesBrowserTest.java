package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

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
	AutoGenFactory autoGenFactory;
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
		autoGenFactory = new AutoGenFactory();
		mockCookies = mock(CookieProvider.class);
		filesBrowser = new FilesBrowser(mockView, mockSynapseClient,
				mockNodeModelCreator, adapterFactory, autoGenFactory,
				mockGlobalApplicationState, mockAuthenticationController, mockCookies, mockAccessRequirementsWidget);
		verify(mockView).setPresenter(filesBrowser);
		filesBrowser.configure(configuredEntityId);
		String newId = "syn456";
		AsyncMockStubber.callSuccessWith(newId).when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), eq(true), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("").when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
		reset(mockView);
	}
	
	@Test
	public void testConfigure() {		
		String entityId = "syn123";
		filesBrowser.configure(entityId);
		verify(mockView).configure(entityId, false);
		reset(mockView);
		
		String title = "title";
		filesBrowser.configure(entityId, title);
		verify(mockView).configure(entityId, false, title);
		reset(mockView);
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
		
		
		filesBrowser.deleteFolder(id, skipTrashCan);
		verify(mockSynapseClient).deleteEntityById(eq(id), eq(skipTrashCan), any(AsyncCallback.class));
		verify(mockView).refreshTreeView(anyString());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteFolderFail() throws Exception {
		String id = "syn456";
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).deleteEntityById(anyString(), anyBoolean(), any(AsyncCallback.class));
		
		filesBrowser.deleteFolder(id, true);
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
		FilesBrowser.uploadButtonClickedStep1(mockAccessRequirementsWidget, configuredEntityId, mockView, mockSynapseClient, mockCookies, mockAuthenticationController);
		ArgumentCaptor<CallbackP> arg = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockAccessRequirementsWidget).showUploadAccessRequirements(eq(configuredEntityId), arg.capture());
		CallbackP callback = arg.getValue();
		callback.invoke(true);
		//verify if accepted then it should continue (eventually showing the upload dialog)
		verify(mockView).showUploadDialog(anyString());
	}
	
	@Test
	public void testUploadStep1ARsNotAccepted(){
		FilesBrowser.uploadButtonClickedStep1(mockAccessRequirementsWidget, configuredEntityId, mockView, mockSynapseClient, mockCookies, mockAuthenticationController);
		ArgumentCaptor<CallbackP> arg = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockAccessRequirementsWidget).showUploadAccessRequirements(eq(configuredEntityId), arg.capture());
		CallbackP callback = arg.getValue();
		callback.invoke(false);
		//verify if not accepted then the upload dialog is not shown
		verify(mockView, never()).showUploadDialog(anyString());
	}
	
	@Test
	public void testUploadStep2Certified(){
		FilesBrowser.uploadButtonClickedStep2(configuredEntityId, mockView, mockSynapseClient, mockCookies, mockAuthenticationController);
		verify(mockView).showUploadDialog(anyString());
	}
	
	@Test
	public void testUploadStep2NotCertified(){
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		FilesBrowser.uploadButtonClickedStep2(configuredEntityId, mockView, mockSynapseClient, mockCookies, mockAuthenticationController);
		
		ArgumentCaptor<CallbackP> arg = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockView).showQuizInfoDialog(arg.capture());
		CallbackP callback = arg.getValue();
		//if the view calls back that the tutorial was clicked, then the upload dialog is not shown
		callback.invoke(true);
		verify(mockView, never()).showUploadDialog(anyString());
		//but if the tutorial was not clicked, then it should show the upload dialog
		callback.invoke(false);
		verify(mockView).showUploadDialog(anyString());
	}
	
	@Test
	public void testUploadStep2NotCertifiedIgnore(){
		//simulate that the dialog was previously ignored
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		when(mockCookies.getCookie(eq(CookieKeys.IGNORE_CERTIFICATION_REMINDER))).thenReturn("yes");
		FilesBrowser.uploadButtonClickedStep2(configuredEntityId, mockView, mockSynapseClient, mockCookies, mockAuthenticationController);
		
		//should not show the quiz info dialog, it should go directly to upload 
		verify(mockView, Mockito.times(0)).showQuizInfoDialog(any(CallbackP.class));
		verify(mockView).showUploadDialog(anyString());
	}
	
	@Test
	public void testUploadStep2Failure(){
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		FilesBrowser.uploadButtonClickedStep2(configuredEntityId, mockView, mockSynapseClient, mockCookies, mockAuthenticationController);
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testAddFolderButtonClickedCertified(){
		filesBrowser.addFolderClicked();
		verify(mockView).showFolderEditDialog(anyString());
	}
}












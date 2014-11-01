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
	AutoGenFactory autoGenFactory;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	FilesBrowser filesBrowser;
	CookieProvider mockCookies;
	String configuredEntityId = "syn123";
	EntityAccessRequirementsWidget mockAccessRequirementsWidget;
	boolean canAddChild = true;
	boolean canCertifiedUserAddChild = false;
	
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
		filesBrowser.configure(configuredEntityId, canAddChild, canCertifiedUserAddChild);
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
		filesBrowser.configure(entityId, canAddChild, canCertifiedUserAddChild);
		verify(mockView).configure(entityId, canCertifiedUserAddChild);
	}
	
	@Test
	public void testConfigureCanAddChild() {		
		String entityId = "syn123";
		boolean canCertifiedUserAddChild = true;
		filesBrowser.configure(entityId, canAddChild, canCertifiedUserAddChild);
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
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		boolean isCertificationRequired = true;
		FilesBrowser.uploadButtonClickedStep2(configuredEntityId, mockView, mockSynapseClient, mockAuthenticationController, isCertificationRequired);
		
		ArgumentCaptor<Callback> arg = ArgumentCaptor.forClass(Callback.class);
		verify(mockView).showQuizInfoDialog(eq(isCertificationRequired), arg.capture());
		Callback callback = arg.getValue();
		//if the user clicks remind me later, then we should invoke
		callback.invoke();
		verify(mockView).showUploadDialog(anyString());
	}
	
	@Test
	public void testUploadStep2NotCertifiedNotRequired(){
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		boolean isCertificationRequired = false;
		FilesBrowser.uploadButtonClickedStep2(configuredEntityId, mockView, mockSynapseClient, mockAuthenticationController, isCertificationRequired);
		verify(mockView).showQuizInfoDialog(eq(isCertificationRequired), any(Callback.class));
	}
	
	@Test
	public void testUploadStep2Failure(){
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		FilesBrowser.uploadButtonClickedStep2(configuredEntityId, mockView, mockSynapseClient, mockAuthenticationController, true);
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testAddFolderButtonClickedCertified(){
		filesBrowser.addFolderClicked();
		verify(mockView).showFolderEditDialog(anyString());
	}
	
	@Test
	public void testIsCertificationRequired() {
		//note method signature:
		//FilesBrowser.isCertificationRequired(canAddChild, canCertifiedUserAddChild);
		
		//BEFORE LOCKDOWN
		//certification not required if can add child regardless of certification
		assertFalse(FilesBrowser.isCertificationRequired(true, true));
		
		//the case when you can add a child before certification, but after certification you cannot.  This is an invalid state:
		//assertNA(FilesBrowser.isCertificationRequired(true, false));
		
		//AFTER LOCKDOWN
		//certification is required if you can't add a child without certification.
		assertTrue(FilesBrowser.isCertificationRequired(false, true));
		
		//regardless of certification, this user cannot add children
		assertFalse(FilesBrowser.isCertificationRequired(false, false));
	}
	
	@Test
	public void testCallbackIfCertifiedIfEnabled(){
		//set up so that certification is required
		filesBrowser.configure(configuredEntityId, false, true);
		Callback callback = mock(Callback.class);
		filesBrowser.callbackIfCertifiedIfEnabled(callback);
		
		//should pop up quiz info dialog 
		verify(mockView).showQuizInfoDialog(true, null);
		//and should not call back
		verify(callback, never()).invoke();
	}
	
	@Test
	public void testCallbackIfCertified(){
		//certified user
		filesBrowser.configure(configuredEntityId, true, true);
		
		Callback callback = mock(Callback.class);
		filesBrowser.callbackIfCertifiedIfEnabled(callback);
		
		//should not pop up quiz info dialog 
		verify(mockView, never()).showQuizInfoDialog(true, null);
		//and should invoke the callback immediately
		verify(callback).invoke();
	}
}












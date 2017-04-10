package org.sagebionetworks.web.unitclient.widget.accessrequirements.requestaccess;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.web.client.utils.Callback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.MockUtil;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.DataAccessRenewal;
import org.sagebionetworks.repo.model.dataaccess.DataAccessRequest;
import org.sagebionetworks.repo.model.dataaccess.DataAccessRequestInterface;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessSubmissionStep2;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessSubmissionWizardStep2View;
import org.sagebionetworks.web.client.widget.entity.act.UserBadgeList;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidgetImpl;
import org.sagebionetworks.web.client.widget.upload.FileHandleList;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public class CreateDataAccessSubmissionStep2Test {
	CreateDataAccessSubmissionStep2 widget;
	@Mock
	ModalWizardWidgetImpl mockModalPresenter;
	@Mock
	CreateDataAccessSubmissionWizardStep2View mockView;
	@Mock
	DataAccessClientAsync mockClient;
	@Mock
	FileHandleWidget mockTemplateFileRenderer;
	@Mock
	FileHandleUploadWidget mockDucUploader;
	@Mock
	FileHandleUploadWidget mockIrbUploader;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	UserBadgeList mockAccessorsList;
	@Mock
	SynapseSuggestBox mockPeopleSuggestBox;
	@Mock
	UserGroupSuggestionProvider mockProvider;
	@Mock
	FileHandleList mockOtherDocuments;
	@Mock
	DataAccessRequest mockDataAccessRequest;
	@Mock
	DataAccessRenewal mockDataAccessRenewal;
	@Mock
	FileUpload mockFileUpload;
	@Mock
	FileMetadata mockFileMetadata;
	@Captor
	ArgumentCaptor<CallbackP<FileUpload>> callbackPCaptor;
	@Mock
	SynapseSuggestion mockSynapseSuggestion;
	@Captor
	ArgumentCaptor<CallbackP<SynapseSuggestion>> callbackPUserSuggestionCaptor;
	
	@Mock
	FileHandleWidget mockFileHandleWidget;
	@Mock
	ResearchProject mockResearchProject;
	@Mock
	ACTAccessRequirement mockACTAccessRequirement;
	
	@Mock
	List<String> mockAccessorUserIds;
	@Mock
	List<String> mockOtherFileHandleIds;
	
	@Captor
	ArgumentCaptor<FileHandleAssociation> fhaCaptor;
	
	@Captor
	ArgumentCaptor<ModalWizardWidget.WizardCallback> wizardCallbackCaptor;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	
	public static final String FILE_HANDLE_ID = "543345";
	public static final String FILE_HANDLE_ID2 = "2";
	public static final String UPLOADED_FILE_NAME = "important-signed.pdf";
	public static final String SUGGESTED_USER_ID = "62";
	public static final Long ACCESS_REQUIREMENT_ID = 4444L;
	public static final String DATA_ACCESS_REQUEST_ID = "55555";
	public static final String CURRENT_USER_ID = "9878987";
	public static final String USER_ID2 = "7777777";
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockOtherDocuments.configure()).thenReturn(mockOtherDocuments);
		when(mockOtherDocuments.setUploadButtonText(anyString())).thenReturn(mockOtherDocuments);
		when(mockOtherDocuments.setCanDelete(anyBoolean())).thenReturn(mockOtherDocuments);
		when(mockOtherDocuments.setCanUpload(anyBoolean())).thenReturn(mockOtherDocuments);
		
		widget = new CreateDataAccessSubmissionStep2(mockView, mockClient, mockTemplateFileRenderer, mockDucUploader, mockIrbUploader, mockJsniUtils, mockAuthController, mockGinInjector, mockAccessorsList, mockPeopleSuggestBox, mockProvider, mockOtherDocuments);
		widget.setModalPresenter(mockModalPresenter);
		AsyncMockStubber.callSuccessWith(mockDataAccessRequest).when(mockClient).getDataAccessRequest(anyLong(),  any(AsyncCallback.class));
		when(mockFileUpload.getFileMeta()).thenReturn(mockFileMetadata);
		when(mockFileUpload.getFileHandleId()).thenReturn(FILE_HANDLE_ID);
		when(mockFileMetadata.getFileName()).thenReturn(UPLOADED_FILE_NAME);
		when(mockSynapseSuggestion.getId()).thenReturn(SUGGESTED_USER_ID);
		when(mockGinInjector.getFileHandleWidget()).thenReturn(mockFileHandleWidget);
		when(mockACTAccessRequirement.getId()).thenReturn(ACCESS_REQUIREMENT_ID);
		when(mockDataAccessRequest.getId()).thenReturn(DATA_ACCESS_REQUEST_ID);
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(CURRENT_USER_ID);
		
		AsyncMockStubber.callSuccessWith(null).when(mockClient).updateDataAccessRequest(any(DataAccessRequestInterface.class), anyBoolean(), any(AsyncCallback.class));
	}
	
	@Test
	public void testConstruction() {
		verify(mockView).setAccessorListWidget(any(IsWidget.class));
		verify(mockView).setDUCTemplateFileWidget(any(IsWidget.class));
		verify(mockView).setDUCUploadWidget(any(IsWidget.class));
		verify(mockView).setIRBUploadWidget(any(IsWidget.class));
		verify(mockView).setOtherDocumentUploaded(any(IsWidget.class));
		verify(mockView).setPeopleSuggestWidget(any(IsWidget.class));
		verify(mockPeopleSuggestBox).setSuggestionProvider(mockProvider);
		verify(mockAccessorsList).setCanDelete(true);
		verify(mockOtherDocuments).setCanDelete(true);
		verify(mockOtherDocuments).setCanUpload(true);
	}
	
	@Test
	public void testUploadDuc() {
		widget.configure(mockResearchProject, mockACTAccessRequirement);
		verify(mockDucUploader).configure(anyString(), callbackPCaptor.capture());
		CallbackP<FileUpload> callback = callbackPCaptor.getValue();
		callback.invoke(mockFileUpload);
		
		verify(mockDataAccessRequest).setDucFileHandleId(FILE_HANDLE_ID);
		verify(mockGinInjector).getFileHandleWidget();
		verify(mockFileHandleWidget).configure(UPLOADED_FILE_NAME, FILE_HANDLE_ID);
		verify(mockView).setDUCUploadedFileWidget(mockFileHandleWidget);
	}
	@Test
	public void testUploadIrb() {
		widget.configure(mockResearchProject, mockACTAccessRequirement);
		verify(mockIrbUploader).configure(anyString(), callbackPCaptor.capture());
		CallbackP<FileUpload> callback = callbackPCaptor.getValue();
		callback.invoke(mockFileUpload);
		
		verify(mockDataAccessRequest).setIrbFileHandleId(FILE_HANDLE_ID);
		verify(mockGinInjector).getFileHandleWidget();
		verify(mockFileHandleWidget).configure(UPLOADED_FILE_NAME, FILE_HANDLE_ID);
		verify(mockView).setIRBUploadedFileWidget(mockFileHandleWidget);
	}
	
	@Test
	public void testUserSynapseSuggestion() {
		widget.configure(mockResearchProject, mockACTAccessRequirement);
		verify(mockPeopleSuggestBox).addItemSelectedHandler(callbackPUserSuggestionCaptor.capture());
		CallbackP<SynapseSuggestion> callback = callbackPUserSuggestionCaptor.getValue();
		callback.invoke(mockSynapseSuggestion);
		verify(mockAccessorsList).addUserBadge(SUGGESTED_USER_ID);
	}
	
	@Test
	public void testConfigure() {
		when(mockACTAccessRequirement.getIsIRBApprovalRequired()).thenReturn(true);
		when(mockACTAccessRequirement.getIsDUCRequired()).thenReturn(true);
		when(mockACTAccessRequirement.getAreOtherAttachmentsRequired()).thenReturn(true);
		when(mockACTAccessRequirement.getDucTemplateFileHandleId()).thenReturn(FILE_HANDLE_ID);
		widget.configure(mockResearchProject, mockACTAccessRequirement);
		verify(mockView).setIRBVisible(true);
		verify(mockView).setDUCVisible(true);
		verify(mockOtherDocuments).clear();
		verify(mockAccessorsList).clear();
		verify(mockView, times(2)).setPublicationsVisible(false);
		verify(mockView, times(2)).setSummaryOfUseVisible(false);
		verify(mockPeopleSuggestBox).clear();
		verify(mockView).setOtherDocumentUploadVisible(true);
		verify(mockView).setDUCTemplateVisible(true);
		verify(mockTemplateFileRenderer).configure(fhaCaptor.capture());
		FileHandleAssociation fha = fhaCaptor.getValue();
		assertEquals(FILE_HANDLE_ID, fha.getFileHandleId());
		// TODO: should be access requirement type
		assertEquals(FileHandleAssociateType.VerificationSubmission, fha.getAssociateObjectType());
		assertEquals(ACCESS_REQUIREMENT_ID.toString(), fha.getAssociateObjectId());
		verify(mockClient).getDataAccessRequest(anyLong(),  any(AsyncCallback.class));
	}
	
	@Test
	public void testConfigureFailure() {
		String error = "error getting data access request";
		Exception ex = new Exception(error);
		AsyncMockStubber.callFailureWith(ex).when(mockClient).getDataAccessRequest(anyLong(),  any(AsyncCallback.class));
		widget.configure(mockResearchProject, mockACTAccessRequirement);
		verify(mockClient).getDataAccessRequest(anyLong(),  any(AsyncCallback.class));
		verify(mockModalPresenter).setErrorMessage(error);
	}
	
	@Test
	public void testConfigureWithDuc() {
		when(mockDataAccessRequest.getDucFileHandleId()).thenReturn(FILE_HANDLE_ID);
		when(mockACTAccessRequirement.getIsDUCRequired()).thenReturn(true);
		widget.configure(mockResearchProject, mockACTAccessRequirement);
		verify(mockView).setIRBVisible(false);
		verify(mockView).setDUCVisible(true);
		verify(mockView, times(2)).setPublicationsVisible(false);
		verify(mockView, times(2)).setSummaryOfUseVisible(false);
		verify(mockView).setOtherDocumentUploadVisible(false);
		verify(mockView).setDUCTemplateVisible(false);
		verify(mockClient).getDataAccessRequest(anyLong(),  any(AsyncCallback.class));
		
		verify(mockGinInjector).getFileHandleWidget();
		verify(mockFileHandleWidget).configure(fhaCaptor.capture());
		FileHandleAssociation fha = fhaCaptor.getValue();
		assertEquals(FILE_HANDLE_ID, fha.getFileHandleId());
		assertEquals(FileHandleAssociateType.DataAccessRequestAttachment, fha.getAssociateObjectType());
		assertEquals(DATA_ACCESS_REQUEST_ID, fha.getAssociateObjectId());
		verify(mockView).setDUCUploadedFileWidget(mockFileHandleWidget);
	}
	
	@Test
	public void testConfigureWithIrb() {
		when(mockDataAccessRequest.getIrbFileHandleId()).thenReturn(FILE_HANDLE_ID);
		when(mockACTAccessRequirement.getIsIRBApprovalRequired()).thenReturn(true);
		widget.configure(mockResearchProject, mockACTAccessRequirement);
		verify(mockView).setIRBVisible(true);
		verify(mockView).setDUCVisible(false);
		verify(mockView, times(2)).setPublicationsVisible(false);
		verify(mockView, times(2)).setSummaryOfUseVisible(false);
		verify(mockView).setOtherDocumentUploadVisible(false);
		verify(mockView).setDUCTemplateVisible(false);
		verify(mockClient).getDataAccessRequest(anyLong(),  any(AsyncCallback.class));
		
		verify(mockGinInjector).getFileHandleWidget();
		verify(mockFileHandleWidget).configure(fhaCaptor.capture());
		FileHandleAssociation fha = fhaCaptor.getValue();
		assertEquals(FILE_HANDLE_ID, fha.getFileHandleId());
		assertEquals(FileHandleAssociateType.DataAccessRequestAttachment, fha.getAssociateObjectType());
		assertEquals(DATA_ACCESS_REQUEST_ID, fha.getAssociateObjectId());
		verify(mockView).setIRBUploadedFileWidget(mockFileHandleWidget);
	}
	
	@Test
	public void testConfigureWithAttachments() {
		List<String> otherDocumentIds = new ArrayList<String>();
		otherDocumentIds.add(FILE_HANDLE_ID);
		otherDocumentIds.add(FILE_HANDLE_ID2);
		when(mockDataAccessRequest.getAttachments()).thenReturn(otherDocumentIds);
		when(mockACTAccessRequirement.getAreOtherAttachmentsRequired()).thenReturn(true);
		widget.configure(mockResearchProject, mockACTAccessRequirement);
		verify(mockView).setIRBVisible(false);
		verify(mockView).setDUCVisible(false);
		verify(mockView, times(2)).setPublicationsVisible(false);
		verify(mockView, times(2)).setSummaryOfUseVisible(false);
		verify(mockView).setOtherDocumentUploadVisible(true);
		verify(mockView).setDUCTemplateVisible(false);
		verify(mockClient).getDataAccessRequest(anyLong(),  any(AsyncCallback.class));
		verify(mockOtherDocuments, times(2)).addFileLink(fhaCaptor.capture());
		List<FileHandleAssociation> fhas = fhaCaptor.getAllValues();
		assertEquals(FILE_HANDLE_ID, fhas.get(0).getFileHandleId());
		assertEquals(FileHandleAssociateType.DataAccessRequestAttachment, fhas.get(0).getAssociateObjectType());
		assertEquals(DATA_ACCESS_REQUEST_ID, fhas.get(0).getAssociateObjectId());
		
		assertEquals(FILE_HANDLE_ID2, fhas.get(1).getFileHandleId());
		assertEquals(FileHandleAssociateType.DataAccessRequestAttachment, fhas.get(1).getAssociateObjectType());
		assertEquals(DATA_ACCESS_REQUEST_ID, fhas.get(1).getAssociateObjectId());
	}
	
	@Test
	public void testConfigureWithAccessors() {
		List<String> accessorUserIds = new ArrayList<String>();
		accessorUserIds.add(CURRENT_USER_ID);
		accessorUserIds.add(USER_ID2);
		when(mockDataAccessRequest.getAccessors()).thenReturn(accessorUserIds);
		widget.configure(mockResearchProject, mockACTAccessRequirement);
		verify(mockClient).getDataAccessRequest(anyLong(),  any(AsyncCallback.class));
		verify(mockAccessorsList, times(2)).addUserBadge(anyString());
		verify(mockAccessorsList).addUserBadge(CURRENT_USER_ID);
		verify(mockAccessorsList).addUserBadge(USER_ID2);
	}
	
	@Test
	public void testSubmit() {
		widget.configure(mockResearchProject, mockACTAccessRequirement);
		when(mockAccessorsList.getUserIds()).thenReturn(mockAccessorUserIds);
		when(mockOtherDocuments.getFileHandleIds()).thenReturn(mockOtherFileHandleIds);
		widget.onPrimary();
		boolean isSubmit = true;
		verify(mockDataAccessRequest).setAccessors(mockAccessorUserIds);
		verify(mockDataAccessRequest).setAttachments(mockOtherFileHandleIds);
		verify(mockClient).updateDataAccessRequest(any(DataAccessRequestInterface.class), eq(isSubmit), any(AsyncCallback.class));
		
		verify(mockView).showInfo(CreateDataAccessSubmissionStep2.SUCCESSFULLY_SUBMITTED_MESSAGE);
		InOrder order = inOrder(mockModalPresenter);
		order.verify(mockModalPresenter).setLoading(true);
		order.verify(mockModalPresenter).setLoading(false);
		verify(mockModalPresenter).onFinished();
	}
	

	@Test
	public void testSubmitFailure() {
		widget.configure(mockResearchProject, mockACTAccessRequirement);
		String error = "error submitting data access request";
		Exception ex = new Exception(error);
		AsyncMockStubber.callFailureWith(ex).when(mockClient).updateDataAccessRequest(any(DataAccessRequestInterface.class), anyBoolean(), any(AsyncCallback.class));
		widget.onPrimary();
		boolean isSubmit = true;
		verify(mockClient).updateDataAccessRequest(any(DataAccessRequestInterface.class), eq(isSubmit), any(AsyncCallback.class));
		verify(mockModalPresenter).setErrorMessage(error);
	}
	
	@Test
	public void testSave() {
		widget.configure(mockResearchProject, mockACTAccessRequirement);
		when(mockAccessorsList.getUserIds()).thenReturn(mockAccessorUserIds);
		when(mockOtherDocuments.getFileHandleIds()).thenReturn(mockOtherFileHandleIds);
		verify(mockModalPresenter).addCallback(wizardCallbackCaptor.capture());
		// simulate user cancel
		wizardCallbackCaptor.getValue().onCanceled();
		
		verify(mockView).showConfirmDialog(anyString(), eq(CreateDataAccessSubmissionStep2.SAVE_CHANGES_MESSAGE), callbackCaptor.capture());
		
		//simulate user clicks Yes to save
		callbackCaptor.getValue().invoke();
		
		boolean isSubmit = false;
		verify(mockDataAccessRequest).setAccessors(mockAccessorUserIds);
		verify(mockDataAccessRequest).setAttachments(mockOtherFileHandleIds);
		verify(mockClient).updateDataAccessRequest(any(DataAccessRequestInterface.class), eq(isSubmit), any(AsyncCallback.class));
		
		verify(mockView).showInfo(CreateDataAccessSubmissionStep2.SAVED_PROGRESS_MESSAGE);
		InOrder order = inOrder(mockModalPresenter);
		order.verify(mockModalPresenter).setLoading(true);
		order.verify(mockModalPresenter).setLoading(false);
		verify(mockModalPresenter).onFinished();
	}
}

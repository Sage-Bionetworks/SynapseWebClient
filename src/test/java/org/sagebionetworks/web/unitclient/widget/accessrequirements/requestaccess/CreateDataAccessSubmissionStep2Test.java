package org.sagebionetworks.web.unitclient.widget.accessrequirements.requestaccess;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessSubmissionStep2.SUCCESSFULLY_SUBMITTED_MESSAGE;
import static org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessSubmissionStep2.SUCCESSFULLY_SUBMITTED_TITLE;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.dataaccess.AccessType;
import org.sagebionetworks.repo.model.dataaccess.AccessorChange;
import org.sagebionetworks.repo.model.dataaccess.CreateSubmissionRequest;
import org.sagebionetworks.repo.model.dataaccess.Renewal;
import org.sagebionetworks.repo.model.dataaccess.Request;
import org.sagebionetworks.repo.model.dataaccess.RequestInterface;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessSubmissionStep2;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessSubmissionWizardStep2View;
import org.sagebionetworks.web.client.widget.entity.act.UserBadgeList;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
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
	Request mockDataAccessRequest;
	@Mock
	Renewal mockDataAccessRenewal;
	@Mock
	FileUpload mockFileUpload;
	@Mock
	FileMetadata mockFileMetadata;
	@Captor
	ArgumentCaptor<CallbackP<FileUpload>> callbackPCaptor;
	@Mock
	UserGroupSuggestion mockSynapseSuggestion;
	@Captor
	ArgumentCaptor<CallbackP<UserGroupSuggestion>> callbackPUserSuggestionCaptor;
	@Mock
	FileHandleWidget mockFileHandleWidget;
	@Mock
	ResearchProject mockResearchProject;
	@Mock
	ManagedACTAccessRequirement mockACTAccessRequirement;
	
	@Mock
	List<AccessorChange> mockAccessorChanges;
	@Mock
	List<String> mockOtherFileHandleIds;
	@Captor
	ArgumentCaptor<FileHandleAssociation> fhaCaptor;
	
	@Captor
	ArgumentCaptor<ModalWizardWidget.WizardCallback> wizardCallbackCaptor;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	@Captor
	ArgumentCaptor<CallbackP<List<String>>> callbackPStringListCaptor;
	@Captor
	ArgumentCaptor<AccessorChange> accessorChangeCaptor;
	@Mock
	PopupUtilsView mockPopupUtils;
	@Mock
	RestrictableObjectDescriptor mockSubject;
	@Captor
	ArgumentCaptor<CreateSubmissionRequest> submissionRequestCaptor;
	
	public static final String FILE_HANDLE_ID = "543345";
	public static final String FILE_HANDLE_ID2 = "2";
	public static final String UPLOADED_FILE_NAME = "important-signed.pdf";
	public static final String SUGGESTED_USER_ID = "62";
	public static final Long ACCESS_REQUIREMENT_ID = 4444L;
	public static final String DATA_ACCESS_REQUEST_ID = "55555";
	public static final String CURRENT_USER_ID = "9878987";
	public static final String USER_ID2 = "7777777";
	public static final String DATA_ACCESS_REQUEST_ETAG = "xxxxxx";
	
	public static final String TARGET_SUBJECT_ID = "syn1";
	public static final RestrictableObjectType TARGET_SUBJECT_TYPE = RestrictableObjectType.ENTITY;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockOtherDocuments.configure()).thenReturn(mockOtherDocuments);
		when(mockOtherDocuments.setUploadButtonText(anyString())).thenReturn(mockOtherDocuments);
		when(mockOtherDocuments.setCanDelete(anyBoolean())).thenReturn(mockOtherDocuments);
		when(mockOtherDocuments.setCanUpload(anyBoolean())).thenReturn(mockOtherDocuments);
		
		widget = new CreateDataAccessSubmissionStep2(mockView, 
				mockClient, 
				mockTemplateFileRenderer, 
				mockDucUploader, 
				mockIrbUploader, 
				mockJsniUtils, 
				mockAuthController, 
				mockGinInjector, 
				mockAccessorsList, 
				mockPeopleSuggestBox, 
				mockProvider, 
				mockOtherDocuments,
				mockPopupUtils);
		widget.setModalPresenter(mockModalPresenter);
		AsyncMockStubber.callSuccessWith(mockDataAccessRequest).when(mockClient).getDataAccessRequest(anyLong(),  any(AsyncCallback.class));
		when(mockFileUpload.getFileMeta()).thenReturn(mockFileMetadata);
		when(mockFileUpload.getFileHandleId()).thenReturn(FILE_HANDLE_ID);
		when(mockFileMetadata.getFileName()).thenReturn(UPLOADED_FILE_NAME);
		when(mockSynapseSuggestion.getId()).thenReturn(SUGGESTED_USER_ID);
		when(mockGinInjector.getFileHandleWidget()).thenReturn(mockFileHandleWidget);
		when(mockACTAccessRequirement.getId()).thenReturn(ACCESS_REQUIREMENT_ID);
		when(mockDataAccessRequest.getId()).thenReturn(DATA_ACCESS_REQUEST_ID);
		when(mockDataAccessRequest.getEtag()).thenReturn(DATA_ACCESS_REQUEST_ETAG);
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(CURRENT_USER_ID);
		when(mockSubject.getId()).thenReturn(TARGET_SUBJECT_ID);
		when(mockSubject.getType()).thenReturn(TARGET_SUBJECT_TYPE);
		AsyncMockStubber.callSuccessWith(mockDataAccessRequest).when(mockClient).updateDataAccessRequest(any(RequestInterface.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockClient).submitDataAccessRequest(any(CreateSubmissionRequest.class), anyLong(), any(AsyncCallback.class));
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
		verify(mockPeopleSuggestBox).setTypeFilter(TypeFilter.USERS_ONLY);
		verify(mockAccessorsList).setCanDelete(true);
		verify(mockOtherDocuments).setCanDelete(true);
		verify(mockOtherDocuments).setCanUpload(true);
	}
	
	@Test
	public void testUploadDuc() {
		widget.configure(mockResearchProject, mockACTAccessRequirement, mockSubject);
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
		widget.configure(mockResearchProject, mockACTAccessRequirement, mockSubject);
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
		widget.configure(mockResearchProject, mockACTAccessRequirement, mockSubject);
		verify(mockPeopleSuggestBox).addItemSelectedHandler(callbackPUserSuggestionCaptor.capture());
		CallbackP<UserGroupSuggestion> callback = callbackPUserSuggestionCaptor.getValue();
		callback.invoke(mockSynapseSuggestion);
		verify(mockAccessorsList).addAccessorChange(accessorChangeCaptor.capture());
		AccessorChange change = accessorChangeCaptor.getValue();
		assertEquals(SUGGESTED_USER_ID, change.getUserId());
		assertEquals(AccessType.GAIN_ACCESS, change.getType());
	}
	
	@Test
	public void testConfigure() {
		when(mockACTAccessRequirement.getIsValidatedProfileRequired()).thenReturn(true);
		when(mockACTAccessRequirement.getIsIRBApprovalRequired()).thenReturn(true);
		when(mockACTAccessRequirement.getIsDUCRequired()).thenReturn(true);
		when(mockACTAccessRequirement.getAreOtherAttachmentsRequired()).thenReturn(true);
		when(mockACTAccessRequirement.getDucTemplateFileHandleId()).thenReturn(FILE_HANDLE_ID);
		widget.configure(mockResearchProject, mockACTAccessRequirement, mockSubject);
		verify(mockView).setValidatedUserProfileNoteVisible(true);
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
		assertEquals(FileHandleAssociateType.AccessRequirementAttachment, fha.getAssociateObjectType());
		assertEquals(ACCESS_REQUIREMENT_ID.toString(), fha.getAssociateObjectId());
		verify(mockClient).getDataAccessRequest(anyLong(),  any(AsyncCallback.class));
	}
	
	@Test
	public void testConfigureFailure() {
		String error = "error getting data access request";
		Exception ex = new Exception(error);
		AsyncMockStubber.callFailureWith(ex).when(mockClient).getDataAccessRequest(anyLong(),  any(AsyncCallback.class));
		widget.configure(mockResearchProject, mockACTAccessRequirement, mockSubject);
		verify(mockClient).getDataAccessRequest(anyLong(),  any(AsyncCallback.class));
		verify(mockModalPresenter).setErrorMessage(error);
	}
	
	@Test
	public void testConfigureWithRenewal() {
		AsyncMockStubber.callSuccessWith(mockDataAccessRenewal).when(mockClient).getDataAccessRequest(anyLong(),  any(AsyncCallback.class));
		widget.configure(mockResearchProject, mockACTAccessRequirement, mockSubject);
		
		verify(mockView).setPublicationsVisible(true);
		verify(mockView).setSummaryOfUseVisible(true);
		verify(mockView).setPublications(anyString());
		verify(mockView).setSummaryOfUse(anyString());
		
		String publications = "publications";
		when(mockView.getPublications()).thenReturn(publications);
		String summary = "summary of use";
		when(mockView.getSummaryOfUse()).thenReturn(summary);
		// save renewal, verify renewal values are taken from the view
		widget.updateDataAccessRequest(false);
		verify(mockClient).updateDataAccessRequest(eq(mockDataAccessRenewal), any(AsyncCallback.class));
		verify(mockDataAccessRenewal).setPublication(publications);
		verify(mockDataAccessRenewal).setSummaryOfUse(summary);
	}
	
	@Test
	public void testConfigureWithDuc() {
		when(mockACTAccessRequirement.getIsValidatedProfileRequired()).thenReturn(false);
		when(mockDataAccessRequest.getDucFileHandleId()).thenReturn(FILE_HANDLE_ID);
		when(mockACTAccessRequirement.getIsDUCRequired()).thenReturn(true);
		widget.configure(mockResearchProject, mockACTAccessRequirement, mockSubject);
		verify(mockView).setValidatedUserProfileNoteVisible(false);
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
		widget.configure(mockResearchProject, mockACTAccessRequirement, mockSubject);
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
		widget.configure(mockResearchProject, mockACTAccessRequirement, mockSubject);
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
	public void testConfigureWithAccessorChanges() {
		List<AccessorChange> accessorUserIds = new ArrayList<AccessorChange>();
		AccessorChange change1 = new AccessorChange();
		change1.setUserId(CURRENT_USER_ID);
		change1.setType(AccessType.GAIN_ACCESS);
		accessorUserIds.add(change1);
		AccessorChange change2 = new AccessorChange();
		change2.setUserId(USER_ID2);
		change2.setType(AccessType.RENEW_ACCESS);
		accessorUserIds.add(change2);
		when(mockDataAccessRequest.getAccessorChanges()).thenReturn(accessorUserIds);
		widget.configure(mockResearchProject, mockACTAccessRequirement, mockSubject);
		verify(mockClient).getDataAccessRequest(anyLong(),  any(AsyncCallback.class));
		verify(mockAccessorsList).addSubmitterAccessorChange(change1);
		verify(mockAccessorsList).addAccessorChange(change2);
	}
	
	@Test
	public void testSubmit() {
		widget.configure(mockResearchProject, mockACTAccessRequirement, mockSubject);
		when(mockAccessorsList.getAccessorChanges()).thenReturn(mockAccessorChanges);
		when(mockOtherDocuments.getFileHandleIds()).thenReturn(mockOtherFileHandleIds);
		widget.onPrimary();
		
		verify(mockDataAccessRequest).setAccessorChanges(mockAccessorChanges);
		verify(mockDataAccessRequest).setAttachments(mockOtherFileHandleIds);
		verify(mockClient).updateDataAccessRequest(any(RequestInterface.class), any(AsyncCallback.class));
		
		//submitted (primary button was clicked)
		verify(mockClient).submitDataAccessRequest(submissionRequestCaptor.capture(), eq(ACCESS_REQUIREMENT_ID), any(AsyncCallback.class));
		CreateSubmissionRequest submissionRequest = submissionRequestCaptor.getValue();
		assertEquals(TARGET_SUBJECT_ID, submissionRequest.getSubjectId());
		assertEquals(TARGET_SUBJECT_TYPE, submissionRequest.getSubjectType());
		assertEquals(DATA_ACCESS_REQUEST_ID, submissionRequest.getRequestId());
		assertEquals(DATA_ACCESS_REQUEST_ETAG, submissionRequest.getRequestEtag());
		
		verify(mockPopupUtils).showInfoDialog(SUCCESSFULLY_SUBMITTED_TITLE, SUCCESSFULLY_SUBMITTED_MESSAGE, null);
		InOrder order = inOrder(mockModalPresenter);
		order.verify(mockModalPresenter).setLoading(true);
		order.verify(mockModalPresenter).setLoading(false);
		verify(mockModalPresenter).onFinished();
	}
	

	@Test
	public void testSubmitFailure() {
		widget.configure(mockResearchProject, mockACTAccessRequirement, mockSubject);
		String error = "error submitting data access request";
		Exception ex = new Exception(error);
		AsyncMockStubber.callFailureWith(ex).when(mockClient).updateDataAccessRequest(any(RequestInterface.class), any(AsyncCallback.class));
		widget.onPrimary();
		verify(mockClient).updateDataAccessRequest(any(RequestInterface.class), any(AsyncCallback.class));
		verify(mockModalPresenter).setErrorMessage(error);
	}
	
	@Test
	public void testSave() {
		widget.configure(mockResearchProject, mockACTAccessRequirement, mockSubject);
		when(mockAccessorsList.getAccessorChanges()).thenReturn(mockAccessorChanges);
		when(mockOtherDocuments.getFileHandleIds()).thenReturn(mockOtherFileHandleIds);
		verify(mockModalPresenter).addCallback(wizardCallbackCaptor.capture());
		// simulate user cancel
		wizardCallbackCaptor.getValue().onCanceled();
		
		verify(mockPopupUtils).showConfirmDialog(anyString(), eq(CreateDataAccessSubmissionStep2.SAVE_CHANGES_MESSAGE), callbackCaptor.capture());
		
		//simulate user clicks Yes to save
		callbackCaptor.getValue().invoke();
		
		verify(mockDataAccessRequest).setAccessorChanges(mockAccessorChanges);
		verify(mockDataAccessRequest).setAttachments(mockOtherFileHandleIds);
		verify(mockClient).updateDataAccessRequest(any(RequestInterface.class), any(AsyncCallback.class));
		verify(mockClient, never()).submitDataAccessRequest(any(CreateSubmissionRequest.class), anyLong(), any(AsyncCallback.class));
		
		verify(mockPopupUtils).showInfo(CreateDataAccessSubmissionStep2.SAVED_PROGRESS_MESSAGE);
		InOrder order = inOrder(mockModalPresenter);
		order.verify(mockModalPresenter).setLoading(true);
		order.verify(mockModalPresenter).setLoading(false);
		verify(mockModalPresenter).onFinished();
	}
}

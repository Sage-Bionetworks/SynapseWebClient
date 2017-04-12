package org.sagebionetworks.web.unitclient.widget.accessrequirements.submission;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmission;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionState;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.ACTDataAccessSubmissionWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.ACTDataAccessSubmissionWidgetView;
import org.sagebionetworks.web.client.widget.entity.PromptModalView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.upload.FileHandleList;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public class ACTDataAccessSubmissionWidgetTest {
	
	ACTDataAccessSubmissionWidget widget;
	@Mock
	ACTDataAccessSubmissionWidgetView mockView;
	@Mock
	SynapseAlert mockSynapseAlert;
	@Mock
	DataAccessClientAsync mockClient;
	@Mock
	PromptModalView mockPromptModalView;
	@Mock
	FileHandleWidget mockDucFileRenderer;
	@Mock
	FileHandleWidget mockIrbFileRenderer;
	@Mock
	FileHandleList mockFileHandleList;
	@Mock
	SynapseJSNIUtils mockJSNIUtils;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	DataAccessSubmission mockDataAccessSubmission;
	@Mock
	ResearchProject mockResearchProjectSnapshot;
	@Captor
	ArgumentCaptor<PromptModalView.Presenter> promptModalPresenterCaptor;
	PromptModalView.Presenter confirmRejectionCallback;
	@Captor
	ArgumentCaptor<FileHandleAssociation> fhaCaptor;
	@Mock
	UserBadge mockUserBadge;
	public static final String SUBMISSION_ID = "9876545678987";
	public static final String INSTITUTION = "Univerisity of Washington";
	public static final String INTENDED_DATA_USE = "lorem ipsum";
	public static final String PROJECT_LEAD = "Mr. Rogers";
	public static final String SMALL_DATE_STRING = "1/2/33";
	
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		when(mockDataAccessSubmission.getResearchProjectSnapshot()).thenReturn(mockResearchProjectSnapshot);
		when(mockFileHandleList.configure()).thenReturn(mockFileHandleList);
		when(mockFileHandleList.setCanDelete(anyBoolean())).thenReturn(mockFileHandleList);
		when(mockFileHandleList.setCanUpload(anyBoolean())).thenReturn(mockFileHandleList);
		when(mockDataAccessSubmission.getState()).thenReturn(DataAccessSubmissionState.APPROVED);
		when(mockGinInjector.getUserBadgeWidget()).thenReturn(mockUserBadge);
		when(mockDataAccessSubmission.getId()).thenReturn(SUBMISSION_ID);
		when(mockResearchProjectSnapshot.getInstitution()).thenReturn(INSTITUTION);
		when(mockResearchProjectSnapshot.getIntendedDataUseStatement()).thenReturn(INTENDED_DATA_USE);
		when(mockResearchProjectSnapshot.getProjectLead()).thenReturn(PROJECT_LEAD);
		when(mockJSNIUtils.convertDateToSmallString(any(Date.class))).thenReturn(SMALL_DATE_STRING);
		
		widget = new ACTDataAccessSubmissionWidget(mockView, mockSynapseAlert, mockClient, mockPromptModalView, mockDucFileRenderer, mockIrbFileRenderer, mockFileHandleList, mockJSNIUtils, mockGinInjector);
		AsyncMockStubber.callSuccessWith(mockDataAccessSubmission).when(mockClient).updateDataAccessSubmissionState(anyString(), any(DataAccessSubmissionState.class), anyString(), any(AsyncCallback.class));
		verify(mockPromptModalView).setPresenter(promptModalPresenterCaptor.capture());
		confirmRejectionCallback = promptModalPresenterCaptor.getValue();
	}

	@Test
	public void testConstruction() {
		verify(mockFileHandleList).configure();
		verify(mockFileHandleList).setCanDelete(false);
		verify(mockFileHandleList).setCanUpload(false);
		verify(mockView).setPresenter(widget);
	}
	
	@Test
	public void testConfigure() {
		// set up accessors
		String userId1 = "12";
		String userId2 = "34";
		List<String> userIds = new ArrayList<String>();
		userIds.add(userId1);
		userIds.add(userId2);
		when(mockDataAccessSubmission.getAccessors()).thenReturn(userIds);
		// set up other documents
		String fileHandleId1 = "873";
		String fileHandleId2 = "5432";
		List<String> fileHandleIds = new ArrayList<String>();
		fileHandleIds.add(fileHandleId1);
		fileHandleIds.add(fileHandleId2);
		when(mockDataAccessSubmission.getAttachments()).thenReturn(fileHandleIds);
		
		when(mockDataAccessSubmission.getIsRenewalSubmission()).thenReturn(false);
		String fileHandleId3 = "565499";
		when(mockDataAccessSubmission.getDucFileHandleId()).thenReturn(fileHandleId3);
		String fileHandleId4 = "1111112";
		when(mockDataAccessSubmission.getIrbFileHandleId()).thenReturn(fileHandleId4);
		
		widget.configure(mockDataAccessSubmission);
		
		verify(mockView).hideActions();
		// verify accessors
		verify(mockView).clearAccessors();
		verify(mockGinInjector, times(2)).getUserBadgeWidget();
		verify(mockUserBadge).configure(userId1);
		verify(mockUserBadge).configure(userId2);
		verify(mockView, times(2)).addAccessors(any(IsWidget.class));
		// verify other documents
		verify(mockFileHandleList).clear();
		verify(mockFileHandleList, times(2)).addFileLink(fhaCaptor.capture());
		List<FileHandleAssociation> fhas = fhaCaptor.getAllValues();
		FileHandleAssociation fha = fhas.get(0);
		assertEquals(SUBMISSION_ID, fha.getAssociateObjectId());
		assertEquals(FileHandleAssociateType.DataAccessSubmissionAttachment, fha.getAssociateObjectType());
		assertTrue(fileHandleId1.equals(fha.getFileHandleId()) || fileHandleId2.equals(fha.getFileHandleId()));
		// verify duc
		verify(mockDucFileRenderer).configure(fhaCaptor.capture());
		fha = fhaCaptor.getValue();
		assertEquals(SUBMISSION_ID, fha.getAssociateObjectId());
		assertEquals(FileHandleAssociateType.DataAccessSubmissionAttachment, fha.getAssociateObjectType());
		assertEquals(fileHandleId3, fha.getFileHandleId());
		// verify irb
		verify(mockIrbFileRenderer).configure(fhaCaptor.capture());
		fha = fhaCaptor.getValue();
		assertEquals(SUBMISSION_ID, fha.getAssociateObjectId());
		assertEquals(FileHandleAssociateType.DataAccessSubmissionAttachment, fha.getAssociateObjectType());
		assertEquals(fileHandleId4, fha.getFileHandleId());
		// verify view
		verify(mockView).setInstitution(INSTITUTION);
		verify(mockView).setIntendedDataUse(INTENDED_DATA_USE);
		verify(mockView).setIsRenewal(false);
		verify(mockView).setProjectLead(PROJECT_LEAD);
		verify(mockView).setSubmittedOn(SMALL_DATE_STRING);
	}
	
	@Test
	public void testConfigureApproved() {
		when(mockDataAccessSubmission.getState()).thenReturn(DataAccessSubmissionState.APPROVED);
		widget.configure(mockDataAccessSubmission);
		verify(mockView).hideActions();
		verify(mockView).showRejectButton();
	}
	
	@Test
	public void testConfigureSubmitted() {
		when(mockDataAccessSubmission.getState()).thenReturn(DataAccessSubmissionState.SUBMITTED);
		widget.configure(mockDataAccessSubmission);
		verify(mockView).hideActions();
		verify(mockView).showApproveButton();
		verify(mockView).showRejectButton();
	}
	
	@Test
	public void testConfigureOtherStates() {
		when(mockDataAccessSubmission.getState()).thenReturn(DataAccessSubmissionState.CANCELLED);
		widget.configure(mockDataAccessSubmission);
		
		when(mockDataAccessSubmission.getState()).thenReturn(DataAccessSubmissionState.NOT_SUBMITTED);
		widget.configure(mockDataAccessSubmission);
		
		when(mockDataAccessSubmission.getState()).thenReturn(DataAccessSubmissionState.REJECTED);
		widget.configure(mockDataAccessSubmission);
		
		verify(mockView, times(3)).hideActions();
		verify(mockView, never()).showApproveButton();
		verify(mockView, never()).showRejectButton();
	}
	
	@Test
	public void testUpdateDataAccessSubmissionState() {
		widget.configure(mockDataAccessSubmission);
		
		when(mockDataAccessSubmission.getState()).thenReturn(DataAccessSubmissionState.REJECTED);
		String rejectionReason = "missing info";
		widget.updateDataAccessSubmissionState(DataAccessSubmissionState.REJECTED, rejectionReason);
		verify(mockClient).updateDataAccessSubmissionState(eq(SUBMISSION_ID), eq(DataAccessSubmissionState.REJECTED), eq(rejectionReason), any(AsyncCallback.class));
		
		verify(mockView).setState(DataAccessSubmissionState.REJECTED.name());
	}
	
	@Test
	public void testUpdateDataAccessSubmissionStateFailure() {
		widget.configure(mockDataAccessSubmission);
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockClient).updateDataAccessSubmissionState(anyString(), any(DataAccessSubmissionState.class), anyString(), any(AsyncCallback.class));
		widget.updateDataAccessSubmissionState(DataAccessSubmissionState.APPROVED, "");
		verify(mockClient).updateDataAccessSubmissionState(eq(SUBMISSION_ID), eq(DataAccessSubmissionState.APPROVED), anyString(), any(AsyncCallback.class));
		verify(mockSynapseAlert).handleException(ex);
	}
}

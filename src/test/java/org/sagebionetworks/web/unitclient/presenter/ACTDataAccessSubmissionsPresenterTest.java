package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.presenter.ACTDataAccessSubmissionsPresenter.*;
import static org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmission;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionOrder;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionState;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.presenter.ACTDataAccessSubmissionsPresenter;
import org.sagebionetworks.web.client.presenter.AccessRequirementsPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.ACTDataAccessSubmissionsView;
import org.sagebionetworks.web.client.view.PlaceView;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.CreateAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.TermsOfUseAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRendererImpl;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class ACTDataAccessSubmissionsPresenterTest {
	
	ACTDataAccessSubmissionsPresenter presenter;
	
	@Mock
	ACTDataAccessSubmissionsPlace mockPlace;
	
	@Mock
	ACTDataAccessSubmissionsView mockView;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	LoadMoreWidgetContainer mockLoadMoreContainer;
	@Mock
	ACTAccessRequirementWidget mockACTAccessRequirementWidget;
	@Mock
	Button mockButton;
	@Mock
	FileHandleWidget mockDucTemplateFileHandleWidget;
	@Mock
	DataAccessClientAsync mockDataAccessClient;
	
	@Mock
	ACTAccessRequirement mockACTAccessRequirement;
	@Mock
	DataAccessSubmissionPage mockDataAccessSubmissionPage;
	@Mock
	DataAccessSubmission mockDataAccessSubmission;
	@Captor
	ArgumentCaptor<FileHandleAssociation> fhaCaptor;
	
	public static final String FILE_HANDLE_ID = "9999";
	public static final Long AR_ID = 76555L;
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		presenter = new ACTDataAccessSubmissionsPresenter(mockView, mockSynAlert, mockGinInjector, mockGlobalApplicationState, mockLoadMoreContainer, mockACTAccessRequirementWidget, mockButton, mockDucTemplateFileHandleWidget, mockDataAccessClient);
		AsyncMockStubber.callSuccessWith(mockACTAccessRequirement).when(mockDataAccessClient).getAccessRequirement(anyLong(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockDataAccessSubmissionPage).when(mockDataAccessClient).getDataAccessSubmissions(anyLong(), anyString(), any(DataAccessSubmissionState.class), any(DataAccessSubmissionOrder.class), anyBoolean(), any(AsyncCallback.class));
		when(mockDataAccessSubmissionPage.getResults()).thenReturn(Collections.singletonList(mockDataAccessSubmission));
		when(mockACTAccessRequirement.getDucTemplateFileHandleId()).thenReturn(FILE_HANDLE_ID);
		when(mockACTAccessRequirement.getId()).thenReturn(AR_ID);
	}	
	
	@Test
	public void testConstruction() {
		verify(mockView).setStates(anyList());
		verify(mockButton).setText(SHOW_AR_TEXT);
		verify(mockView).setAccessRequirementUIVisible(false);
		verify(mockView).setSynAlert(any(IsWidget.class));
		verify(mockView).setAccessRequirementWidget(any(IsWidget.class));
		verify(mockView).setLoadMoreContainer(any(IsWidget.class));
		verify(mockView).setShowHideButton(any(IsWidget.class));
		verify(mockView).setPresenter(presenter);
		verify(mockLoadMoreContainer).configure(any(Callback.class));
		verify(mockButton).addClickHandler(any(ClickHandler.class));
	}
	
	@Test
	public void testSetPlace() {
		String time1 = "8765";
		String time2 = "5678";
		when(mockPlace.getParam(MIN_DATE_PARAM)).thenReturn(time1);
		when(mockPlace.getParam(MAX_DATE_PARAM)).thenReturn(time2);
		when(mockPlace.getParam(ACCESS_REQUIREMENT_ID_PARAM)).thenReturn(AR_ID.toString());
		
		when(mockACTAccessRequirement.getDucTemplateFileHandleId()).thenReturn(FILE_HANDLE_ID);
		when(mockACTAccessRequirement.getAreOtherAttachmentsRequired()).thenReturn(true);
		when(mockACTAccessRequirement.getIsAnnualReviewRequired()).thenReturn(false);
		when(mockACTAccessRequirement.getIsCertifiedUserRequired()).thenReturn(true);
		when(mockACTAccessRequirement.getIsDUCRequired()).thenReturn(false);
		when(mockACTAccessRequirement.getIsIDUPublic()).thenReturn(true);
		when(mockACTAccessRequirement.getIsIRBApprovalRequired()).thenReturn(false);
		when(mockACTAccessRequirement.getIsValidatedProfileRequired()).thenReturn(true);
		
		presenter.setPlace(mockPlace);
		verify(mockDataAccessClient).getAccessRequirement(eq(AR_ID), any(AsyncCallback.class));
		
		//verify duc template file handle widget is configured properly (basd on act duc file handle id)
		verify(mockDucTemplateFileHandleWidget).configure(fhaCaptor.capture());
		FileHandleAssociation fha = fhaCaptor.getValue();
		// TODO: change to Access Requirement once supported.
		assertEquals(FileHandleAssociateType.TeamAttachment, fha.getAssociateObjectType());
		assertEquals(AR_ID.toString(), fha.getAssociateObjectId());
		assertEquals(FILE_HANDLE_ID, fha.getFileHandleId());
				
		verify(mockView).setAreOtherAttachmentsRequired(true);
		verify(mockView).setIsAnnualReviewRequired(false);
		verify(mockView).setIsCertifiedUserRequired(true);
		verify(mockView).setIsDUCRequired(false);
		verify(mockView).setIsIDUPublic(true);
		verify(mockView).setIsIRBApprovalRequired(false);
		verify(mockView).setIsValidatedProfileRequired(true);
		
		verify(mockACTAccessRequirementWidget).setRequirement(mockACTAccessRequirement);
		verify(mockView).setDucColumnVisible(false);
		verify(mockView).setIrbColumnVisible(false);
		verify(mockView).setOtherAttachmentsColumnVisible(true);
		verify(mockView).setRenewalColumnsVisible(false);
	}
}

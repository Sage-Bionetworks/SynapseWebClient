package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace.ACCESS_REQUIREMENT_ID_PARAM;
import static org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace.MAX_DATE_PARAM;
import static org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace.MIN_DATE_PARAM;
import static org.sagebionetworks.web.client.presenter.ACTDataAccessSubmissionsPresenter.SHOW_AR_TEXT;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.dataaccess.Submission;
import org.sagebionetworks.repo.model.dataaccess.SubmissionOrder;
import org.sagebionetworks.repo.model.dataaccess.SubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.SubmissionState;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace;
import org.sagebionetworks.web.client.presenter.ACTDataAccessSubmissionsPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.ACTDataAccessSubmissionsView;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.SubjectsWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.ACTDataAccessSubmissionWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

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
	LoadMoreWidgetContainer mockLoadMoreContainer;
	@Mock
	ManagedACTAccessRequirementWidget mockACTAccessRequirementWidget;
	@Mock
	Button mockButton;
	@Mock
	FileHandleWidget mockDucTemplateFileHandleWidget;
	@Mock
	DataAccessClientAsync mockDataAccessClient;

	@Mock
	ManagedACTAccessRequirement mockACTAccessRequirement;
	@Mock
	SubmissionPage mockDataAccessSubmissionPage;
	@Mock
	Submission mockDataAccessSubmission;
	@Captor
	ArgumentCaptor<FileHandleAssociation> fhaCaptor;
	@Mock
	ACTDataAccessSubmissionWidget mockACTDataAccessSubmissionWidget;
	@Mock
	SubjectsWidget mockSubjectsWidget;
	@Mock
	List<RestrictableObjectDescriptor> mockSubjects;
	@Mock
	GWTWrapper mockGWT;
	@Mock
	DateTimeFormat mockDateTimeFormat;
	@Captor
	ArgumentCaptor<Place> placeCaptor;
	public static final String FILE_HANDLE_ID = "9999";
	public static final Long AR_ID = 76555L;
	public static final String NEXT_PAGE_TOKEN = "abc678";

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(mockGWT.getDateTimeFormat(any(PredefinedFormat.class))).thenReturn(mockDateTimeFormat);
		presenter = new ACTDataAccessSubmissionsPresenter(mockView, mockSynAlert, mockGinInjector, mockLoadMoreContainer, mockACTAccessRequirementWidget, mockButton, mockDucTemplateFileHandleWidget, mockDataAccessClient, mockSubjectsWidget, mockGWT);
		AsyncMockStubber.callSuccessWith(mockACTAccessRequirement).when(mockDataAccessClient).getAccessRequirement(anyLong(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockDataAccessSubmissionPage).when(mockDataAccessClient).getDataAccessSubmissions(anyLong(), anyString(), any(SubmissionState.class), any(SubmissionOrder.class), anyBoolean(), any(AsyncCallback.class));
		when(mockDataAccessSubmissionPage.getResults()).thenReturn(Collections.singletonList(mockDataAccessSubmission));
		when(mockDataAccessSubmissionPage.getNextPageToken()).thenReturn(NEXT_PAGE_TOKEN);
		when(mockACTAccessRequirement.getDucTemplateFileHandleId()).thenReturn(FILE_HANDLE_ID);
		when(mockACTAccessRequirement.getId()).thenReturn(AR_ID);
		when(mockGinInjector.getACTDataAccessSubmissionWidget()).thenReturn(mockACTDataAccessSubmissionWidget);
		when(mockACTAccessRequirement.getSubjectIds()).thenReturn(mockSubjects);
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
	public void testLoadData() {
		String time1 = "8765";
		String time2 = "5678";
		when(mockPlace.getParam(MIN_DATE_PARAM)).thenReturn(time1);
		when(mockPlace.getParam(MAX_DATE_PARAM)).thenReturn(time2);
		when(mockPlace.getParam(ACCESS_REQUIREMENT_ID_PARAM)).thenReturn(AR_ID.toString());

		when(mockACTAccessRequirement.getDucTemplateFileHandleId()).thenReturn(FILE_HANDLE_ID);
		when(mockACTAccessRequirement.getAreOtherAttachmentsRequired()).thenReturn(true);
		Long expirationPeriod = 0L;
		when(mockACTAccessRequirement.getExpirationPeriod()).thenReturn(expirationPeriod);
		when(mockACTAccessRequirement.getIsCertifiedUserRequired()).thenReturn(true);
		when(mockACTAccessRequirement.getIsDUCRequired()).thenReturn(false);
		when(mockACTAccessRequirement.getIsIDUPublic()).thenReturn(true);
		when(mockACTAccessRequirement.getIsIRBApprovalRequired()).thenReturn(false);
		when(mockACTAccessRequirement.getIsValidatedProfileRequired()).thenReturn(true);

		presenter.setPlace(mockPlace);
		verify(mockDataAccessClient).getAccessRequirement(eq(AR_ID), any(AsyncCallback.class));

		// verify duc template file handle widget is configured properly (basd on act duc file handle id)
		verify(mockDucTemplateFileHandleWidget).configure(fhaCaptor.capture());
		FileHandleAssociation fha = fhaCaptor.getValue();
		assertEquals(FileHandleAssociateType.AccessRequirementAttachment, fha.getAssociateObjectType());
		assertEquals(AR_ID.toString(), fha.getAssociateObjectId());
		assertEquals(FILE_HANDLE_ID, fha.getFileHandleId());
		verify(mockSubjectsWidget).configure(mockSubjects);
		verify(mockView).setAreOtherAttachmentsRequired(true);
		verify(mockView).setExpirationPeriod(expirationPeriod);
		verify(mockView).setIsCertifiedUserRequired(true);
		verify(mockView).setIsDUCRequired(false);
		verify(mockView).setIsIDUPublic(true);
		verify(mockView).setIsIRBApprovalRequired(false);
		verify(mockView).setIsValidatedProfileRequired(true);

		verify(mockACTAccessRequirementWidget).setRequirement(eq(mockACTAccessRequirement), any(Callback.class));
		verify(mockLoadMoreContainer).setIsProcessing(true);
		verify(mockDataAccessClient).getDataAccessSubmissions(anyLong(), eq((String) null), any(SubmissionState.class), any(SubmissionOrder.class), anyBoolean(), any(AsyncCallback.class));

		// verify DataAccessSubmission widget is created/configured for the submission (based on the
		// mockACTAccessRequirement configuration)
		verify(mockGinInjector).getACTDataAccessSubmissionWidget();
		verify(mockACTDataAccessSubmissionWidget).setDucColumnVisible(false);
		verify(mockACTDataAccessSubmissionWidget).setIrbColumnVisible(false);
		verify(mockACTDataAccessSubmissionWidget).setOtherAttachmentsColumnVisible(true);
		verify(mockLoadMoreContainer).setIsMore(true);
		verify(mockLoadMoreContainer).setIsProcessing(false);

		// verify final load of empty page
		when(mockDataAccessSubmissionPage.getResults()).thenReturn(Collections.EMPTY_LIST);
		when(mockDataAccessSubmissionPage.getNextPageToken()).thenReturn(null);
		presenter.loadMore();
		verify(mockDataAccessClient).getDataAccessSubmissions(anyLong(), eq(NEXT_PAGE_TOKEN), any(SubmissionState.class), any(SubmissionOrder.class), anyBoolean(), any(AsyncCallback.class));
		verify(mockLoadMoreContainer).setIsMore(false);
		verify(mockView).setProjectedExpirationDateVisible(false);
		verify(mockView, never()).setProjectedExpirationDateVisible(true);
	}

	@Test
	public void testProjectedExpiration() {
		String formattedDateTime = "In the future";
		when(mockDateTimeFormat.format(any(Date.class))).thenReturn(formattedDateTime);
		when(mockPlace.getParam(ACCESS_REQUIREMENT_ID_PARAM)).thenReturn(AR_ID.toString());

		Long expirationPeriod = 1111L;
		when(mockACTAccessRequirement.getExpirationPeriod()).thenReturn(expirationPeriod);
		presenter.setPlace(mockPlace);
		verify(mockView).setProjectedExpirationDateVisible(false);
		verify(mockView).setProjectedExpirationDateVisible(true);
		verify(mockView).setProjectedExpirationDate(formattedDateTime);
	}

	@Test
	public void testLoadDataFailure() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockDataAccessClient).getDataAccessSubmissions(anyLong(), anyString(), any(SubmissionState.class), any(SubmissionOrder.class), anyBoolean(), any(AsyncCallback.class));
		presenter.loadData();
		verify(mockSynAlert).handleException(ex);
		verify(mockLoadMoreContainer).setIsMore(false);
	}
}

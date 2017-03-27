package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionState;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionStatus;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.accessrequirements.CreateAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.DeleteAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.SubjectsWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.TermsOfUseAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.TermsOfUseAccessRequirementWidgetView;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class TermsOfUseAccessRequirementWidgetTest {
	TermsOfUseAccessRequirementWidget widget;
	
	@Mock
	TermsOfUseAccessRequirementWidgetView mockView;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	DataAccessClientAsync mockDataAccessClient;
	@Mock
	WikiPageWidget mockWikiPageWidget;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	TermsOfUseAccessRequirement mockTermsOfUseAccessRequirement;
	@Mock
	CreateAccessRequirementButton mockCreateAccessRequirementButton;
	@Mock
	DeleteAccessRequirementButton mockDeleteAccessRequirementButton;
	@Mock
	SubjectsWidget mockSubjectsWidget;
	@Mock
	List<RestrictableObjectDescriptor> mockSubjectIds;
	@Mock
	LazyLoadHelper mockLazyLoadHelper;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	@Mock
	DataAccessSubmissionStatus mockDataAccessSubmissionStatus;
	
	Callback lazyLoadDataCallback;

	public final static String ROOT_WIKI_ID = "777";
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new TermsOfUseAccessRequirementWidget(mockView, mockAuthController, mockDataAccessClient, mockSynapseClient, mockWikiPageWidget, mockSynAlert, mockSubjectsWidget, mockCreateAccessRequirementButton, mockDeleteAccessRequirementButton, mockLazyLoadHelper);
		when(mockTermsOfUseAccessRequirement.getSubjectIds()).thenReturn(mockSubjectIds);
		AsyncMockStubber.callSuccessWith(ROOT_WIKI_ID).when(mockSynapseClient).getRootWikiId(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockLazyLoadHelper).configure(callbackCaptor.capture(), eq(mockView));
		lazyLoadDataCallback = callbackCaptor.getValue();
		AsyncMockStubber.callSuccessWith(mockDataAccessSubmissionStatus).when(mockDataAccessClient).getDataAccessSubmissionStatus(anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setWikiTermsWidget(any(Widget.class));
		verify(mockView).setEditAccessRequirementWidget(any(Widget.class));
		verify(mockWikiPageWidget).setModifiedCreatedByHistoryVisible(false);
	}

	@Test
	public void testSetRequirementWithContactInfoTerms() {
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getRootWikiId(anyString(), anyString(), any(AsyncCallback.class));
		String tou = "must do things before access is allowed";
		when(mockTermsOfUseAccessRequirement.getTermsOfUse()).thenReturn(tou);
		widget.setRequirement(mockTermsOfUseAccessRequirement);
		verify(mockView).setTerms(tou);
		verify(mockView).showTermsUI();
		verify(mockCreateAccessRequirementButton).configure(mockTermsOfUseAccessRequirement);
		verify(mockDeleteAccessRequirementButton).configure(mockTermsOfUseAccessRequirement);
		boolean isHideIfLoadError = true;
		verify(mockSubjectsWidget).configure(mockSubjectIds, isHideIfLoadError);
	}
	@Test
	public void testSetRequirementWithWikiTerms() {
		widget.setRequirement(mockTermsOfUseAccessRequirement);
		verify(mockWikiPageWidget).configure(any(WikiPageKey.class), eq(false), any(WikiPageWidget.Callback.class), eq(false));
		verify(mockView, never()).setTerms(anyString());
		verify(mockView, never()).showTermsUI();
	}
	
	@Test
	public void testApprovedState() {
		//TODO: will be changed once service changes to return different object for ToU type
		widget.setRequirement(mockTermsOfUseAccessRequirement);
		when(mockDataAccessSubmissionStatus.getState()).thenReturn(DataAccessSubmissionState.APPROVED);
		lazyLoadDataCallback.invoke();
		verify(mockView).showApprovedHeading();
	}
	
	@Test
	public void testUnApprovedState() {
		//TODO: will be changed once service changes to return different object for ToU type
		widget.setRequirement(mockTermsOfUseAccessRequirement);
		when(mockDataAccessSubmissionStatus.getState()).thenReturn(DataAccessSubmissionState.SUBMITTED);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showSignTermsButton();
	}

}

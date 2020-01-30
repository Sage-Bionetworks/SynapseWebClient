package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.BasicAccessRequirementStatus;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.accessrequirements.CreateAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.DeleteAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.ReviewAccessorsButton;
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
	SynapseJavascriptClient mockJsClient;
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
	BasicAccessRequirementStatus mockDataAccessSubmissionStatus;
	@Mock
	ReviewAccessorsButton mockManageAccessButton;
	@Mock
	Callback mockRefreshCallback;
	GlobalApplicationState mockGlobalApplicationState;

	Callback lazyLoadDataCallback;

	public final static String ROOT_WIKI_ID = "777";

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new TermsOfUseAccessRequirementWidget(mockView, mockAuthController, mockDataAccessClient, mockSynapseClient, mockJsClient, mockWikiPageWidget, mockSynAlert, mockSubjectsWidget, mockCreateAccessRequirementButton, mockDeleteAccessRequirementButton, mockLazyLoadHelper, mockManageAccessButton);
		when(mockTermsOfUseAccessRequirement.getSubjectIds()).thenReturn(mockSubjectIds);
		AsyncMockStubber.callSuccessWith(ROOT_WIKI_ID).when(mockJsClient).getRootWikiPageKey(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockLazyLoadHelper).configure(callbackCaptor.capture(), eq(mockView));
		lazyLoadDataCallback = callbackCaptor.getValue();
		AsyncMockStubber.callSuccessWith(mockDataAccessSubmissionStatus).when(mockDataAccessClient).getAccessRequirementStatus(anyString(), any(AsyncCallback.class));
		when(mockAuthController.isLoggedIn()).thenReturn(true);
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
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockJsClient).getRootWikiPageKey(anyString(), anyString(), any(AsyncCallback.class));
		String tou = "must do things before access is allowed";
		when(mockTermsOfUseAccessRequirement.getTermsOfUse()).thenReturn(tou);
		widget.setRequirement(mockTermsOfUseAccessRequirement, mockRefreshCallback);
		verify(mockView).setTerms(tou);
		verify(mockView).showTermsUI();
		verify(mockCreateAccessRequirementButton).configure(mockTermsOfUseAccessRequirement, mockRefreshCallback);
		verify(mockDeleteAccessRequirementButton).configure(mockTermsOfUseAccessRequirement, mockRefreshCallback);
		verify(mockSubjectsWidget).configure(mockSubjectIds);
	}

	@Test
	public void testSetRequirementWithWikiTerms() {
		widget.setRequirement(mockTermsOfUseAccessRequirement, mockRefreshCallback);
		verify(mockWikiPageWidget).configure(any(WikiPageKey.class), eq(false), any(WikiPageWidget.Callback.class));
		verify(mockView, never()).setTerms(anyString());
		verify(mockView, never()).showTermsUI();
	}

	@Test
	public void testApprovedState() {
		widget.setRequirement(mockTermsOfUseAccessRequirement, mockRefreshCallback);
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(true);
		lazyLoadDataCallback.invoke();
		verify(mockView).showApprovedHeading();
	}

	@Test
	public void testAnonymous() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		widget.setRequirement(mockTermsOfUseAccessRequirement, mockRefreshCallback);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showLoginButton();
	}

	@Test
	public void testUnApprovedState() {
		widget.setRequirement(mockTermsOfUseAccessRequirement, mockRefreshCallback);
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(false);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showSignTermsButton();
	}

}

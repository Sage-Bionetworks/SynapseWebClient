package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.accessrequirements.SelfSignAccessRequirementWidget.GET_CERTIFIED_PAGE;
import static org.sagebionetworks.web.client.widget.accessrequirements.SelfSignAccessRequirementWidget.GET_VALIDATED_PROFILE_PAGE;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.SelfSignAccessRequirement;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.dataaccess.BasicAccessRequirementStatus;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.accessrequirements.CreateAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.DeleteAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.ReviewAccessorsButton;
import org.sagebionetworks.web.client.widget.accessrequirements.SelfSignAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.SelfSignAccessRequirementWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.SubjectsWidget;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class SelfSignAccessRequirementWidgetTest {
	SelfSignAccessRequirementWidget widget;
	
	@Mock
	SelfSignAccessRequirementWidgetView mockView;
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
	SelfSignAccessRequirement mockAccessRequirement;
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
	PopupUtilsView mockPopupUtils;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	UserBundle mockUserBundle;
	@Mock
	AccessApproval mockAccessApproval;
	@Captor
	ArgumentCaptor<AccessApproval> accessApprovalCaptor;
	@Mock
	Callback mockRefreshCallback;
	Callback lazyLoadDataCallback;

	public final static String ROOT_WIKI_ID = "777";
	public final static String CURRENT_USER_ID = "6823";
	public final static Long AR_ID = 8999L;
	public final static Long AR_VERSION = 2L;
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new SelfSignAccessRequirementWidget(
				mockView, 
				mockAuthController, 
				mockDataAccessClient, 
				mockSynapseClient, 
				mockWikiPageWidget, 
				mockSynAlert, 
				mockSubjectsWidget, 
				mockCreateAccessRequirementButton, 
				mockDeleteAccessRequirementButton, 
				mockLazyLoadHelper,
				mockManageAccessButton,
				mockPopupUtils,
				mockSynapseJavascriptClient);
		when(mockAccessRequirement.getId()).thenReturn(AR_ID);
		when(mockAccessRequirement.getVersionNumber()).thenReturn(AR_VERSION);
		when(mockAccessRequirement.getSubjectIds()).thenReturn(mockSubjectIds);
		AsyncMockStubber.callSuccessWith(ROOT_WIKI_ID).when(mockSynapseJavascriptClient).getRootWikiPageKey(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockLazyLoadHelper).configure(callbackCaptor.capture(), eq(mockView));
		lazyLoadDataCallback = callbackCaptor.getValue();
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(CURRENT_USER_ID);
		AsyncMockStubber.callSuccessWith(mockUserBundle).when(mockSynapseJavascriptClient).getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));
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
	public void testSetRequirementWithWikiTerms() {
		widget.setRequirement(mockAccessRequirement, mockRefreshCallback);
		verify(mockWikiPageWidget).configure(any(WikiPageKey.class), eq(false), any(WikiPageWidget.Callback.class));
	}
	
	@Test
	public void testApprovedState() {
		widget.setRequirement(mockAccessRequirement, mockRefreshCallback);
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(true);
		lazyLoadDataCallback.invoke();
		verify(mockView).showApprovedHeading();
	}
	
	@Test
	public void testAnonymous() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		widget.setRequirement(mockAccessRequirement, mockRefreshCallback);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showLoginButton();
	}
	
	@Test
	public void testUnapprovedState() {
		widget.setRequirement(mockAccessRequirement, mockRefreshCallback);
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(false);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showSignTermsButton();
	}
	@Test
	public void testUnapprovedStateCertificationRequiredNotCertified() {
		when(mockUserBundle.getIsCertified()).thenReturn(false);
		when(mockAccessRequirement.getIsCertifiedUserRequired()).thenReturn(true);
		widget.setRequirement(mockAccessRequirement, mockRefreshCallback);
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(false);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showGetCertifiedUI();
		verify(mockView, never()).showSignTermsButton();
	}
	
	@Test
	public void testUnapprovedStateCertificationRequiredIsCertified() {
		when(mockUserBundle.getIsCertified()).thenReturn(true);
		when(mockAccessRequirement.getIsCertifiedUserRequired()).thenReturn(true);
		widget.setRequirement(mockAccessRequirement, mockRefreshCallback);
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(false);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showSignTermsButton();
	}
	
	@Test
	public void testUnapprovedStateValidationRequiredNotValidated() {
		when(mockUserBundle.getIsVerified()).thenReturn(false);
		when(mockAccessRequirement.getIsValidatedProfileRequired()).thenReturn(true);
		widget.setRequirement(mockAccessRequirement, mockRefreshCallback);
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(false);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showGetProfileValidatedUI();
		verify(mockView, never()).showSignTermsButton();
	}
	
	@Test
	public void testUnapprovedStateValidationRequiredIsValidated() {
		when(mockUserBundle.getIsVerified()).thenReturn(true);
		when(mockAccessRequirement.getIsValidatedProfileRequired()).thenReturn(true);
		widget.setRequirement(mockAccessRequirement, mockRefreshCallback);
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(false);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showSignTermsButton();
	}

	@Test
	public void testUnapprovedStateCertificationValidationRequiredNotEither() {
		when(mockUserBundle.getIsCertified()).thenReturn(false);
		when(mockUserBundle.getIsVerified()).thenReturn(false);
		when(mockAccessRequirement.getIsCertifiedUserRequired()).thenReturn(true);
		when(mockAccessRequirement.getIsValidatedProfileRequired()).thenReturn(true);
		widget.setRequirement(mockAccessRequirement, mockRefreshCallback);
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(false);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showGetCertifiedUI();
		verify(mockView, never()).showGetProfileValidatedUI();
		verify(mockView, never()).showSignTermsButton();
	}

	@Test
	public void testGetUserBundleFailure() {
		Exception ex = new Exception("error getting user bundle");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));
		when(mockAccessRequirement.getIsValidatedProfileRequired()).thenReturn(true);
		widget.setRequirement(mockAccessRequirement, mockRefreshCallback);
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(false);
		
		lazyLoadDataCallback.invoke();
		
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testOnSignTerms() {
		AsyncMockStubber.callSuccessWith(mockAccessApproval).when(mockSynapseClient).createAccessApproval(any(AccessApproval.class), any(AsyncCallback.class));
		when(mockAccessRequirement.getIsValidatedProfileRequired()).thenReturn(true);
		widget.setRequirement(mockAccessRequirement, mockRefreshCallback);
		
		widget.onSignTerms();

		verify(mockSynapseClient).createAccessApproval(accessApprovalCaptor.capture(), any(AsyncCallback.class));
		verify(mockRefreshCallback).invoke();
		AccessApproval capturedAccessApproval = accessApprovalCaptor.getValue();
		assertEquals(AR_ID, capturedAccessApproval.getRequirementId());
		assertEquals(AR_VERSION, capturedAccessApproval.getRequirementVersion());
		assertEquals(CURRENT_USER_ID, capturedAccessApproval.getAccessorId());
	}
	
	@Test
	public void testOnSignTermsFailure() {
		Exception ex = new Exception("error signing terms");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).createAccessApproval(any(AccessApproval.class), any(AsyncCallback.class));
		widget.setRequirement(mockAccessRequirement, mockRefreshCallback);
		
		widget.onSignTerms();
		
		verify(mockSynapseClient).createAccessApproval(any(AccessApproval.class), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testOnCertify() {
		widget.onCertify();
		verify(mockPopupUtils).openInNewWindow(WebConstants.DOCS_URL + GET_CERTIFIED_PAGE);
	}
	
	@Test
	public void testOnValidateProfile() {
		widget.onValidateProfile();
		verify(mockPopupUtils).openInNewWindow(WebConstants.DOCS_URL + GET_VALIDATED_PROFILE_PAGE);
	}
}

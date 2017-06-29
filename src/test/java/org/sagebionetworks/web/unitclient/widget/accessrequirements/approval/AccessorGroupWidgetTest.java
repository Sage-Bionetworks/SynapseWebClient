package org.sagebionetworks.web.unitclient.widget.accessrequirements.approval;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.accessrequirements.approval.AccessorGroupWidget.ARE_YOU_SURE;
import static org.sagebionetworks.web.client.widget.accessrequirements.approval.AccessorGroupWidget.REVOKE_ACCESS_TO_GROUP;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroup;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.approval.AccessorGroupView;
import org.sagebionetworks.web.client.widget.accessrequirements.approval.AccessorGroupWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class AccessorGroupWidgetTest {
	AccessorGroupWidget widget;
	@Mock
	AccessorGroupView mockView;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	PopupUtilsView mockPopupUtils;
	@Mock
	AccessRequirementWidget mockAccessRequirementWidget;
	@Mock
	DataAccessClientAsync mockDataAccessClient;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	DateTimeUtils mockDateTimeUtils;
	List<UserProfile> userProfiles;
	@Mock
	UserProfile mockUserProfile;
	@Mock
	UserBadge mockUserBadge;
	@Mock
	AccessorGroup mockAccessorGroup;
	@Mock
	Callback onRevokeCallback;
	
	List<String> accessorIds;
	public static final String ACCESSOR_USER_ID = "98888";
	public static final String SUBMITTER_USER_ID = "77776";
	public static final String USER_NAME = "luke";
	public static final String ACCESS_REQUIREMENT_ID = "98765678";
	public static final Date AROUND_NOW = new Date();
	public static final String FORMATTED_DATE = "todayish";
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		widget = new AccessorGroupWidget(
				mockView,
				mockSynAlert,
				mockGinInjector,
				mockPopupUtils,
				mockAccessRequirementWidget,
				mockDataAccessClient,
				mockSynapseClient, 
				mockDateTimeUtils);
		userProfiles = Collections.singletonList(mockUserProfile);
		AsyncMockStubber.callSuccessWith(userProfiles).when(mockSynapseClient).listUserProfiles(anyList(), any(AsyncCallback.class));
		when(mockGinInjector.getUserBadgeWidget()).thenReturn(mockUserBadge);
		when(mockAccessorGroup.getSubmitterId()).thenReturn(SUBMITTER_USER_ID);
		accessorIds = Collections.singletonList(ACCESSOR_USER_ID);
		when(mockAccessorGroup.getAccessorIds()).thenReturn(accessorIds);
		when(mockAccessorGroup.getAccessRequirementId()).thenReturn(ACCESS_REQUIREMENT_ID);
		when(mockUserProfile.getUserName()).thenReturn(USER_NAME);
		when(mockAccessorGroup.getExpiredOn()).thenReturn(AROUND_NOW);
		when(mockDateTimeUtils.convertDateToSmallString(any(Date.class))).thenReturn(FORMATTED_DATE);
	}
	
	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setSynAlert(mockSynAlert);
		verify(mockView).setAccessRequirementWidget(mockAccessRequirementWidget);
	}
	
	@Test
	public void testConfigure() {
		widget.configure(mockAccessorGroup);
		verify(mockUserBadge).configure(SUBMITTER_USER_ID);
		verify(mockUserBadge).configure(ACCESSOR_USER_ID);
		verify(mockView).setSubmittedBy(mockUserBadge);
		verify(mockView).addAccessor(mockUserBadge);
		verify(mockDateTimeUtils).convertDateToSmallString(AROUND_NOW);
		verify(mockView).setExpiresOn(FORMATTED_DATE);
	}
	
	@Test
	public void testShowEmails() {
		widget.configure(mockAccessorGroup);
		widget.onShowEmails();
		
		verify(mockSynapseClient).listUserProfiles(eq(accessorIds), any(AsyncCallback.class));
		verify(mockView).setEmailAddresses(USER_NAME + "@synapse.org");
		verify(mockView).showEmailAddressesDialog();
	}
	
	@Test
	public void testShowEmailsFailure() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).listUserProfiles(anyList(), any(AsyncCallback.class));
		
		widget.configure(mockAccessorGroup);
		widget.onShowEmails();
		
		verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testOnRevoke() {
		widget.onRevoke();
		verify(mockPopupUtils).showConfirmDialog(eq(REVOKE_ACCESS_TO_GROUP), eq(ARE_YOU_SURE), any(Callback.class));
		
	}
	
	@Test
	public void testOnRevokeAfterConfirm() {
		AsyncMockStubber.callSuccessWith(null).when(mockDataAccessClient).revokeGroup(anyString(), anyString(), any(AsyncCallback.class));
		widget.configure(mockAccessorGroup);
		widget.setOnRevokeCallback(onRevokeCallback);
		widget.onRevokeAfterConfirm();
		
		verify(mockDataAccessClient).revokeGroup(eq(ACCESS_REQUIREMENT_ID), eq(SUBMITTER_USER_ID), any(AsyncCallback.class));
		verify(onRevokeCallback).invoke();
	}
}

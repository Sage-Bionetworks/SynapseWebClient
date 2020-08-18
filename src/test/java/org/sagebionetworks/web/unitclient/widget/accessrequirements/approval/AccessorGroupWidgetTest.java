package org.sagebionetworks.web.unitclient.widget.accessrequirements.approval;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.accessrequirements.approval.AccessorGroupWidget.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.dataaccess.AccessApprovalNotification;
import org.sagebionetworks.repo.model.dataaccess.AccessApprovalNotificationResponse;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroup;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.approval.AccessorGroupView;
import org.sagebionetworks.web.client.widget.accessrequirements.approval.AccessorGroupWidget;
import org.sagebionetworks.web.client.widget.asynch.UserProfileAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
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
	DateTimeUtils mockDateTimeUtils;
	@Mock
	UserBadge mockUserBadge;
	@Mock
	AccessorGroup mockAccessorGroup;
	@Mock
	Callback onRevokeCallback;
	@Mock
	UserProfileAsyncHandler mockUserProfileAsyncHandler;
	@Mock
	UserProfile mockUserProfile;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	AccessApprovalNotificationResponse mockAccessApprovalNotificationResponse;
	@Mock
	AccessApprovalNotification mockAccessApprovalNotification;

	List<AccessApprovalNotification> accessApprovalNotifications;
	List<String> accessorIds;
	public static final String ACCESSOR_USER_ID = "98888";
	public static final String SUBMITTER_USER_ID = "77776";
	public static final String ACCESS_REQUIREMENT_ID = "98765678";
	public static final Date AROUND_NOW = new Date();
	public static final String FORMATTED_DATE = "todayish";

	@Before
	public void setUp() throws Exception {
		widget = new AccessorGroupWidget(mockView, mockSynAlert, mockGinInjector, mockPopupUtils, mockAccessRequirementWidget, mockDataAccessClient, mockDateTimeUtils, mockUserProfileAsyncHandler, mockJsClient);
		when(mockGinInjector.getUserBadgeWidget()).thenReturn(mockUserBadge);
		when(mockAccessorGroup.getSubmitterId()).thenReturn(SUBMITTER_USER_ID);
		accessorIds = Collections.singletonList(ACCESSOR_USER_ID);
		when(mockAccessorGroup.getAccessorIds()).thenReturn(accessorIds);
		when(mockAccessorGroup.getAccessRequirementId()).thenReturn(ACCESS_REQUIREMENT_ID);
		when(mockAccessorGroup.getExpiredOn()).thenReturn(AROUND_NOW);
		when(mockDateTimeUtils.getDateTimeString(any(Date.class))).thenReturn(FORMATTED_DATE);
		AsyncMockStubber.callSuccessWith(mockUserProfile).when(mockUserProfileAsyncHandler).getUserProfile(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockAccessApprovalNotificationResponse).when(mockJsClient).getAccessApprovalNotifications(anyString(), anyList(), any(AsyncCallback.class));
		accessApprovalNotifications = new ArrayList<AccessApprovalNotification>();
		when(mockAccessApprovalNotificationResponse.getResults()).thenReturn(accessApprovalNotifications);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setSynAlert(mockSynAlert);
	}

	@Test
	public void testConfigure() {
		widget.configure(mockAccessorGroup);
		verify(mockUserBadge).configure(SUBMITTER_USER_ID);
		verify(mockUserBadge).configure(mockUserProfile);
		verify(mockView).setSubmittedBy(mockUserBadge);
		verify(mockView).addAccessor(mockUserBadge);
		verify(mockDateTimeUtils).getDateTimeString(AROUND_NOW);
		verify(mockView).setExpiresOn(FORMATTED_DATE);
	}

	@Test
	public void testConfigureExpiresOnZero() {
		when(mockAccessorGroup.getExpiredOn()).thenReturn(new Date(0));
		widget.configure(mockAccessorGroup);
		verify(mockView).setExpiresOn("");
	}

	@Test
	public void testOnRevoke() {
		widget.onRevoke();
		verify(mockPopupUtils).showConfirmDialog(eq(REVOKE_ACCESS_TO_GROUP), eq(ARE_YOU_SURE), any(Callback.class));
	}

	@Test
	public void testOnShowAccessRequirement() {
		widget.configure(mockAccessorGroup);
		
		widget.onShowAccessRequirement();
		
		verify(mockAccessRequirementWidget).configure(ACCESS_REQUIREMENT_ID, null);
		verify(mockView).showAccessRequirementDialog(mockAccessRequirementWidget);
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
	
	@Test
	public void testOnShowNotifications() {
		widget.configure(mockAccessorGroup);
		accessApprovalNotifications.add(mockAccessApprovalNotification);
		
		widget.onShowNotifications();
		
		verify(mockView).showNotifications(accessApprovalNotifications);
	}
	
	@Test
	public void testOnShowNotificationsNoNotifications() {
		widget.configure(mockAccessorGroup);
		accessApprovalNotifications.clear();
		
		widget.onShowNotifications();
		
		verify(mockView, never()).showNotifications(accessApprovalNotifications);
		verify(mockPopupUtils).showInfo(NO_NOTIFICATIONS_FOUND_MESSAGE);
	}
	

}

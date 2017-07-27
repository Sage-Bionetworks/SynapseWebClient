package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.dataaccess.AccessType;
import org.sagebionetworks.repo.model.dataaccess.AccessorChange;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.act.UserBadgeItem;
import org.sagebionetworks.web.client.widget.entity.act.UserBadgeList;
import org.sagebionetworks.web.client.widget.entity.act.UserBadgeListView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.team.OpenMembershipRequestsWidget;
import org.sagebionetworks.web.client.widget.team.OpenMembershipRequestsWidgetView;
import org.sagebionetworks.web.shared.MembershipRequestBundle;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;


public class OpenMembershipRequestsWidgetTest {

	OpenMembershipRequestsWidget widget;
	
	@Mock
	OpenMembershipRequestsWidgetView mockView;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	GWTWrapper mockGwt;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	PopupUtilsView mockPopupUtils;
	@Mock
	Callback mockCallback;
	List<MembershipRequestBundle> membershipRequests;
	public static final String TEAM_ID = "8765";
	public static final String USER_ID = "999181";
	public static final String HOST_PAGE_URL = "thispage";
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		widget = new OpenMembershipRequestsWidget(
				mockView, 
				mockSynapseClient, 
				mockGlobalApplicationState, 
				mockGwt, 
				mockSynAlert, 
				mockPopupUtils);
		membershipRequests = new ArrayList<>();
		AsyncMockStubber.callSuccessWith(membershipRequests).when(mockSynapseClient).getOpenRequests(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).addTeamMember(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		when(mockGwt.getHostPageBaseURL()).thenReturn(HOST_PAGE_URL);
	}
	
	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setSynAlert(mockSynAlert);
	}
	
	@Test
	public void testConfigure() {
		widget.configure(TEAM_ID, mockCallback);
		verify(mockSynAlert).clear();
		verify(mockSynapseClient).getOpenRequests(eq(TEAM_ID), any(AsyncCallback.class));
		verify(mockView).configure(anyList(), anyList());
		verify(mockGwt).restoreWindowPosition();
	}
	
	@Test
	public void testConfigureFailure() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getOpenRequests(anyString(), any(AsyncCallback.class));
		widget.configure(TEAM_ID, mockCallback);
		verify(mockSynAlert).clear();
		verify(mockSynapseClient).getOpenRequests(eq(TEAM_ID), any(AsyncCallback.class));
		verify(mockView, never()).configure(anyList(), anyList());
		verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testAcceptRequest() {
		widget.configure(TEAM_ID, mockCallback);
		widget.acceptRequest(USER_ID);
		verify(mockGwt).saveWindowPosition();
		verify(mockSynapseClient).addTeamMember(eq(USER_ID), eq(TEAM_ID), anyString(), eq(HOST_PAGE_URL), any(AsyncCallback.class));
		verify(mockPopupUtils).showInfo(OpenMembershipRequestsWidget.ACCEPTED_REQUEST_MESSAGE, "");
		verify(mockCallback).invoke();
	}
	
	@Test
	public void testAcceptRequestFailure() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).addTeamMember(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		widget.configure(TEAM_ID, mockCallback);
		widget.acceptRequest(USER_ID);
		verify(mockSynapseClient).addTeamMember(eq(USER_ID), eq(TEAM_ID), anyString(), eq(HOST_PAGE_URL), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
		verify(mockCallback, never()).invoke();
	}
}

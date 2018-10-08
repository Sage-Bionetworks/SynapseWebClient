package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.asynch.UserGroupHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellRendererImpl;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserIdCellRendererImplTest {
	
	@Mock
	DivView mockView;
	@Mock
	UserGroupHeaderAsyncHandler mockUserGroupHeaderAsyncHandler;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	ClickHandler mockCustomClickHandler;
	
	UserIdCellRendererImpl renderer;
	@Mock
	UserBadge mockUserBadge;
	@Mock
	TeamBadge mockTeamBadge;
	@Mock
	UserGroupHeader mockUserGroupHeader;
	
	@Mock
	SynapseAlert mockSynAlert;
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		renderer = new UserIdCellRendererImpl(mockView, mockUserGroupHeaderAsyncHandler, mockGinInjector);
		when(mockGinInjector.getUserBadgeWidget()).thenReturn(mockUserBadge);
		when(mockGinInjector.getTeamBadgeWidget()).thenReturn(mockTeamBadge);
		when(mockGinInjector.getSynapseAlertWidget()).thenReturn(mockSynAlert);
		AsyncMockStubber.callSuccessWith(mockUserGroupHeader).when(mockUserGroupHeaderAsyncHandler).getUserGroupHeader(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testSetValue(){
		when(mockUserGroupHeader.getIsIndividual()).thenReturn(true);
		String userId = "1";
		renderer.setValue(userId);
		verify(mockUserBadge).configure(userId);
		verify(mockUserBadge, never()).setCustomClickHandler(any(ClickHandler.class));
	}
	
	@Test
	public void testSetValueWithCustomClickHandler(){
		when(mockUserGroupHeader.getIsIndividual()).thenReturn(true);
		String userId = "1";
		renderer.setValue(userId, mockCustomClickHandler);
		verify(mockUserBadge).configure(userId);
		verify(mockUserBadge).setCustomClickHandler(mockCustomClickHandler);
	}
	
	@Test
	public void testSetValueTeam(){
		when(mockUserGroupHeader.getIsIndividual()).thenReturn(false);
		String userId = "1";
		renderer.setValue(userId);
		verify(mockTeamBadge).configure(userId);
	}
	
	@Test
	public void testSetValueTeamWithCustomClickHandler(){
		when(mockUserGroupHeader.getIsIndividual()).thenReturn(false);
		String userId = "1";
		renderer.setValue(userId, mockCustomClickHandler);
		verify(mockTeamBadge).configure(userId, mockCustomClickHandler);
	}
	
	@Test
	public void testGetUserGroupHeaderFailure(){
		String errorMessage = "errors happen";
		Exception ex = new Exception(errorMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockUserGroupHeaderAsyncHandler).getUserGroupHeader(anyString(), any(AsyncCallback.class));
		renderer.setValue("id", mockCustomClickHandler);
		verify(mockSynAlert).showError(errorMessage);
	}
	
	@Test
	public void testSetValueEmpty(){
		renderer.setValue("");
		verifyZeroInteractions(mockUserGroupHeaderAsyncHandler);
	}
	
	@Test
	public void testSetValueNull(){
		renderer.setValue(null);
		verifyZeroInteractions(mockUserGroupHeaderAsyncHandler);
	}

}

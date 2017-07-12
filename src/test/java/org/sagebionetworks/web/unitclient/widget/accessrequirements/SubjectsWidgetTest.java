package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptorResponse;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.accessrequirements.SubjectsWidget;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRendererImpl;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class SubjectsWidgetTest {
	SubjectsWidget widget;
	
	@Mock
	DivView mockView;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;
	@Mock
	DataAccessClientAsync mockDataAccessClient;
	@Mock
	EntityIdCellRendererImpl mockEntityIdCellRendererImpl;
	@Mock
	TeamBadge mockTeamBadge;
	@Mock
	RestrictableObjectDescriptor mockRestrictableObjectDescriptor;
	@Captor
	ArgumentCaptor<CallbackP<Boolean>> callbackCaptor;
	@Mock
	RestrictableObjectDescriptorResponse mockRestrictableObjectDescriptorResponse;
	@Mock
	SynapseAlert mockSynapseAlert;
	public static final String ID = "876787";
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new SubjectsWidget(mockView, 
				mockGinInjector,
				mockIsACTMemberAsyncHandler,
				mockDataAccessClient);
		when(mockGinInjector.createEntityIdCellRenderer()).thenReturn(mockEntityIdCellRendererImpl);
		when(mockGinInjector.getTeamBadgeWidget()).thenReturn(mockTeamBadge);
		when(mockGinInjector.getSynapseAlertWidget()).thenReturn(mockSynapseAlert);
		when(mockRestrictableObjectDescriptor.getId()).thenReturn(ID);
		AsyncMockStubber.callSuccessWith(mockRestrictableObjectDescriptorResponse).when(mockDataAccessClient).getSubjects(anyString(), anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testConstruction() {
		verify(mockView).setVisible(false);
	}

	@Test
	public void testConfigureEntity() {
		boolean hideIfLoadError = true;
		when(mockRestrictableObjectDescriptor.getType()).thenReturn(RestrictableObjectType.ENTITY);
		when(mockRestrictableObjectDescriptorResponse.getSubjects()).thenReturn(Collections.singletonList(mockRestrictableObjectDescriptor));
		widget.configure(ID, hideIfLoadError);
		
		verify(mockIsACTMemberAsyncHandler).isACTActionAvailable(callbackCaptor.capture());
		CallbackP<Boolean> callback = callbackCaptor.getValue();

		//verify no widget created if not ACT
		callback.invoke(false);
		verifyZeroInteractions(mockGinInjector);
		
		//verify widget created if ACT
		callback.invoke(true);
		verify(mockGinInjector).createEntityIdCellRenderer();
		verify(mockEntityIdCellRendererImpl).setValue(ID, hideIfLoadError);
	}
	
	@Test
	public void testConfigureTeam() {
		when(mockRestrictableObjectDescriptor.getType()).thenReturn(RestrictableObjectType.TEAM);
		when(mockRestrictableObjectDescriptorResponse.getSubjects()).thenReturn(Collections.singletonList(mockRestrictableObjectDescriptor));
		widget.configure(ID, false);
		
		verify(mockIsACTMemberAsyncHandler).isACTActionAvailable(callbackCaptor.capture());
		CallbackP<Boolean> callback = callbackCaptor.getValue();
		callback.invoke(true);
		
		verify(mockGinInjector).getTeamBadgeWidget();
		verify(mockTeamBadge).configure(ID);
	}
	@Test
	public void testGetSubjectsFailure() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockDataAccessClient).getSubjects(anyString(), anyString(), any(AsyncCallback.class));
		
		widget.configure(ID, false);
		verify(mockIsACTMemberAsyncHandler).isACTActionAvailable(callbackCaptor.capture());
		CallbackP<Boolean> callback = callbackCaptor.getValue();
		callback.invoke(true);
		
		verify(mockGinInjector).getSynapseAlertWidget();
		verify(mockSynapseAlert).handleException(ex);
		verify(mockView).add(mockSynapseAlert);
	}
}

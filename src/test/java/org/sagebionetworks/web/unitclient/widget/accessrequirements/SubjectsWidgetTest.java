package org.sagebionetworks.web.unitclient.widget.accessrequirements;

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
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.accessrequirements.SubjectsWidget;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRendererImpl;
import org.sagebionetworks.web.client.widget.team.TeamBadge;

public class SubjectsWidgetTest {
	SubjectsWidget widget;
	
	@Mock
	DivView mockView;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;
	
	@Mock
	EntityIdCellRendererImpl mockEntityIdCellRendererImpl;
	@Mock
	TeamBadge mockTeamBadge;
	@Mock
	RestrictableObjectDescriptor mockRestrictableObjectDescriptor;
	@Captor
	ArgumentCaptor<CallbackP<Boolean>> callbackCaptor;
	
	public static final String ID = "876787";
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new SubjectsWidget(mockView, 
				mockGinInjector,
				mockIsACTMemberAsyncHandler);
		when(mockGinInjector.createEntityIdCellRenderer()).thenReturn(mockEntityIdCellRendererImpl);
		when(mockGinInjector.getTeamBadgeWidget()).thenReturn(mockTeamBadge);
		when(mockRestrictableObjectDescriptor.getId()).thenReturn(ID);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setVisible(false);
	}

	@Test
	public void testConfigureEntity() {
		boolean hideIfLoadError = true;
		when(mockRestrictableObjectDescriptor.getType()).thenReturn(RestrictableObjectType.ENTITY);
		
		widget.configure(Collections.singletonList(mockRestrictableObjectDescriptor), hideIfLoadError);
		
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
		widget.configure(Collections.singletonList(mockRestrictableObjectDescriptor), false);
		
		verify(mockIsACTMemberAsyncHandler).isACTActionAvailable(callbackCaptor.capture());
		CallbackP<Boolean> callback = callbackCaptor.getValue();
		callback.invoke(true);
		
		verify(mockGinInjector).getTeamBadgeWidget();
		verify(mockTeamBadge).configure(ID);
	}

}

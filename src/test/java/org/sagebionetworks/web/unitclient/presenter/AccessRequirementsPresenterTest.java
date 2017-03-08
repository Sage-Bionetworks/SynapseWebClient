package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.place.Account;
import org.sagebionetworks.web.client.presenter.AccessRequirementsPresenter;
import org.sagebionetworks.web.client.presenter.AccountPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.AccountView;
import org.sagebionetworks.web.client.view.PlaceView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRendererImpl;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AccessRequirementsPresenterTest {
	
	AccessRequirementsPresenter presenter;
	@Mock
	PlaceView mockView;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	AccessRequirementsPlace place;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	LoadMoreWidgetContainer mockLoadMoreContainer;
	@Mock
	EntityIdCellRendererImpl mockEntityIdCellRenderer;
	@Mock
	TeamBadge mockTeamBadge;
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		mockSynapseClient = mock(SynapseClientAsync.class);
		presenter = new AccessRequirementsPresenter(mockView, mockSynapseClient, mockSynAlert, mockGinInjector, mockLoadMoreContainer, mockEntityIdCellRenderer, mockTeamBadge);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).addEmail(anyString(), any(AsyncCallback.class));
	}	
	
	@Test
	public void testValidateToken() {
		
	}	
	
	@Test
	public void testValidateTokenFailure() {
	}	
}

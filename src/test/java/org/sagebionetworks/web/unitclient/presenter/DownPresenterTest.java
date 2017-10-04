package org.sagebionetworks.web.unitclient.presenter;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.presenter.DownPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DownView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class DownPresenterTest {
	DownPresenter presenter;
	@Mock
	DownView mockView;
	@Mock
	GWTWrapper mockGWT;
	@Mock
	GlobalApplicationState mockGlobalAppState;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	Down mockDownPlace;
	@Mock
	Place mockLastPlace;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(mockGlobalAppState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		presenter = new DownPresenter(mockView, mockGWT, mockGlobalAppState, mockSynapseJavascriptClient);
	}

	@Test
	public void testSetPlace() {
		when(mockDownPlace.toToken()).thenReturn("upgrading+Synapse");
		String decodedMessage = "upgrading Synapse";
		when(mockGWT.decodeQueryString(anyString())).thenReturn(decodedMessage);
		
		presenter.setPlace(mockDownPlace);
		
		verify(mockView).init();
		verify(mockView).setMessage(decodedMessage);
		verify(mockGWT).scheduleExecution(any(Callback.class), eq(DownPresenter.DELAY));
	}

	@Test
	public void testRepoUp() {
		when(mockSynapseJavascriptClient)
	}
}

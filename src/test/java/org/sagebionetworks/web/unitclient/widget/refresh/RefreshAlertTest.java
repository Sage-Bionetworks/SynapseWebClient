package org.sagebionetworks.web.unitclient.widget.refresh;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.subscription.Etag;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.refresh.RefreshAlert;
import org.sagebionetworks.web.client.widget.refresh.RefreshAlertView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class RefreshAlertTest {
	@Mock
	RefreshAlertView mockView;
	@Mock
	SynapseClientAsync mockSynapseClientAsync;
	@Mock
	GWTWrapper mockGWTWrapper;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	Etag mockEtag;
	RefreshAlert refreshAlert;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		refreshAlert = new RefreshAlert(mockView, mockSynapseClientAsync, mockGWTWrapper, mockGlobalApplicationState, mockSynapseJSNIUtils);
		when(mockView.isAttached()).thenReturn(true);
		AsyncMockStubber.callSuccessWith(mockEtag).when(mockSynapseClientAsync).getEtag(anyString(), any(ObjectType.class), any(AsyncCallback.class));
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(refreshAlert);
	}

	@Test
	public void testConfigureNotAttached() {
		when(mockView.isAttached()).thenReturn(false);
		refreshAlert.configure("123", ObjectType.ENTITY);
		//not ready to ask for the current etag
		verify(mockSynapseClientAsync, never()).getEtag(anyString(), any(ObjectType.class), any(AsyncCallback.class));
	}
	@Test
	public void testAttachedNotConfigured() {
		when(mockView.isAttached()).thenReturn(true);
		refreshAlert.onAttach();
		//not ready to ask for the current etag
		verify(mockSynapseClientAsync, never()).getEtag(anyString(), any(ObjectType.class), any(AsyncCallback.class));
	}
	
	@Test
	public void testConfiguredAndAttached() {
		when(mockView.isAttached()).thenReturn(true);
		refreshAlert.configure("123", ObjectType.ENTITY);
		
		verify(mockSynapseClientAsync).getEtag(anyString(), any(ObjectType.class), any(AsyncCallback.class));
		//and will call this again later
		verify(mockGWTWrapper).scheduleExecution(any(Callback.class), eq(RefreshAlert.DELAY));
	}
	
	@Test
	public void testOnRefresh() {
		refreshAlert.onRefresh();
		verify(mockGlobalApplicationState).refreshPage();
	}
}

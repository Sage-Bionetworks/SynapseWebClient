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
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.refresh.EntityRefreshAlert;
import org.sagebionetworks.web.client.widget.refresh.RefreshAlertView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityRefreshAlertTest {
	@Mock
	RefreshAlertView mockView;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	GWTWrapper mockGWTWrapper;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	Etag mockEtag;
	EntityRefreshAlert entityRefreshAlert;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		entityRefreshAlert = new EntityRefreshAlert(mockView, mockJsClient, mockGWTWrapper, mockGlobalApplicationState, mockSynapseJSNIUtils);
		when(mockView.isAttached()).thenReturn(true);
		AsyncMockStubber.callSuccessWith(mockEtag).when(mockJsClient).getEtag(anyString(), any(ObjectType.class), any(AsyncCallback.class));
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(entityRefreshAlert);
	}

	@Test
	public void testConfigureNotAttached() {
		when(mockView.isAttached()).thenReturn(false);
		entityRefreshAlert.configure("123");
		//not ready to ask for the current etag
		verify(mockJsClient, never()).getEtag(anyString(), any(ObjectType.class), any(AsyncCallback.class));
	}
	@Test
	public void testAttachedNotConfigured() {
		when(mockView.isAttached()).thenReturn(true);
		entityRefreshAlert.onAttach();
		//not ready to ask for the current etag
		verify(mockJsClient, never()).getEtag(anyString(), any(ObjectType.class), any(AsyncCallback.class));
	}
	
	@Test
	public void testConfiguredAndAttached() {
		when(mockView.isAttached()).thenReturn(true);
		entityRefreshAlert.configure("123");
		
		verify(mockJsClient).getEtag(anyString(), any(ObjectType.class), any(AsyncCallback.class));
		//and will call this again later
		verify(mockGWTWrapper).scheduleExecution(any(Callback.class), eq(EntityRefreshAlert.DELAY));
	}
	
	@Test
	public void testOnRefresh() {
		entityRefreshAlert.onRefresh();
		verify(mockGlobalApplicationState).refreshPage();
	}
}

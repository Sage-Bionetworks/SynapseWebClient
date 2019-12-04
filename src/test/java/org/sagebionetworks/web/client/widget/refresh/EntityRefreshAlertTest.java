package org.sagebionetworks.web.client.widget.refresh;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
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

	EntityRefreshAlert entityRefreshAlert;

	Entity entity;
	String entityId;
	String etag;

	@Before
	public void before() {
		entityId = "syn123";
		etag = "TEST-ETAG";
		entity = new FileEntity();
		entity.setId(entityId);
		entity.setEtag(etag);

		MockitoAnnotations.initMocks(this);
		entityRefreshAlert = new EntityRefreshAlert(mockView, mockJsClient, mockGWTWrapper, mockGlobalApplicationState, mockSynapseJSNIUtils);
		when(mockView.isAttached()).thenAnswer((InvocationOnMock invocation) -> true);
		AsyncMockStubber.callSuccessWith(entity).when(mockJsClient).getEntity(anyString(), any(AsyncCallback.class));

	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(entityRefreshAlert);
	}

	@Test
	public void testConfigureNotAttached() {
		when(mockView.isAttached()).thenReturn(false);
		entityRefreshAlert.configure("123");
		// not ready to ask for the current etag
		verify(mockJsClient, never()).getEntity(anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testAttachedNotConfigured() {
		when(mockView.isAttached()).thenReturn(true);
		entityRefreshAlert.onAttach();
		// not ready to ask for the current etag
		verify(mockJsClient, never()).getEntity(anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testConfiguredAndAttached() {
		when(mockView.isAttached()).thenReturn(true);
		entityRefreshAlert.configure("123");

		verify(mockJsClient).getEntity(anyString(), any(AsyncCallback.class));
		// and will call this again later
		verify(mockGWTWrapper).scheduleExecution(any(Callback.class), eq(EntityRefreshAlert.DELAY));
	}

	@Test
	public void testOnRefresh() {
		entityRefreshAlert.onRefresh();
		verify(mockGlobalApplicationState).refreshPage();
	}

	@Test
	public void testCheckEtag_EtagMatch() {
		entityRefreshAlert.configure(entityId);
		// reset mock method call counts
		reset(mockView, mockGWTWrapper);
		when(mockView.isAttached()).thenReturn(true);

		// method under test
		entityRefreshAlert.checkEtag();

		verify(mockView, never()).setVisible(anyBoolean());
		verify(mockGWTWrapper).scheduleExecution(entityRefreshAlert.invokeCheckEtag, EntityRefreshAlert.DELAY);
	}

	@Test
	public void testCheckEtag_EtagMismatch() {
		entityRefreshAlert.configure(entityId);
		// first call remembers the old etag
		entityRefreshAlert.checkEtag();
		// reset mock method call counts
		reset(mockView, mockGWTWrapper);
		when(mockView.isAttached()).thenReturn(true);


		// change etag of entity
		entity.setEtag("UPDATED-ETAG");

		// on second call the etag should be different
		// method under test
		entityRefreshAlert.checkEtag();

		verify(mockView).setVisible(true);
		verify(mockGWTWrapper, never()).scheduleExecution(any(Callback.class), anyInt());
	}
}

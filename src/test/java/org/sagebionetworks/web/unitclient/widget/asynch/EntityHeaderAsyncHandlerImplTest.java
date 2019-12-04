package org.sagebionetworks.web.unitclient.widget.asynch;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.asynch.BaseEntityHeaderAsyncHandlerImpl;
import org.sagebionetworks.web.client.widget.asynch.EntityHeaderAsyncHandlerImpl;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityHeaderAsyncHandlerImplTest {
	EntityHeaderAsyncHandlerImpl entityHeaderAsyncHandler;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	GWTWrapper mockGwt;
	String entityId = "syn239";
	Long entityVersion = 32L;
	@Mock
	AsyncCallback mockCallback;
	@Mock
	EntityHeader mockEntityHeader;
	List<EntityHeader> entityHeaderList;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		entityHeaderAsyncHandler = new EntityHeaderAsyncHandlerImpl(mockSynapseJavascriptClient, mockGwt);
		entityHeaderList = new ArrayList<EntityHeader>();
		AsyncMockStubber.callSuccessWith(entityHeaderList).when(mockSynapseJavascriptClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		when(mockEntityHeader.getId()).thenReturn(entityId);
	}

	@Test
	public void testConstructor() {
		verify(mockGwt).scheduleFixedDelay(any(Callback.class), anyInt());
	}

	@Test
	public void testSuccess() {
		// verify no rpc if no entity headers have been requested.
		entityHeaderAsyncHandler.executeRequests();
		verifyZeroInteractions(mockSynapseJavascriptClient);

		// add one, simulate single entity header response
		entityHeaderAsyncHandler.getEntityHeader(entityId, mockCallback);
		entityHeaderList.add(mockEntityHeader);

		entityHeaderAsyncHandler.executeRequests();
		verify(mockSynapseJavascriptClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		verify(mockCallback).onSuccess(mockEntityHeader);
	}
	
	@Test
	public void testFailure() {
		// simulate exception response
		Exception ex = new Exception("problem loading batch of entity headers");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		entityHeaderAsyncHandler.getEntityHeader(entityId, mockCallback);
		entityHeaderAsyncHandler.executeRequests();

		verify(mockCallback).onFailure(ex);
	}

	@Test
	public void testNotFound() {
		when(mockEntityHeader.getId()).thenReturn("another entity id");
		// add one, simulate single entity header response
		entityHeaderAsyncHandler.getEntityHeader(entityId, mockCallback);
		entityHeaderList.add(mockEntityHeader);

		entityHeaderAsyncHandler.executeRequests();
		verify(mockSynapseJavascriptClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		verify(mockCallback).onFailure(any(Throwable.class));
	}

}

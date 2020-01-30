package org.sagebionetworks.web.unitclient.widget.asynch;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.verify;
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
import org.sagebionetworks.web.client.widget.asynch.VersionedEntityHeaderAsyncHandlerImpl;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class VersionedEntityHeaderAsyncHandlerImplTest {
	VersionedEntityHeaderAsyncHandlerImpl entityHeaderAsyncHandler;
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
		entityHeaderAsyncHandler = new VersionedEntityHeaderAsyncHandlerImpl(mockSynapseJavascriptClient, mockGwt);
		entityHeaderList = new ArrayList<EntityHeader>();
		AsyncMockStubber.callSuccessWith(entityHeaderList).when(mockSynapseJavascriptClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		when(mockEntityHeader.getId()).thenReturn(entityId);
	}

	@Test
	public void testConstructor() {
		verify(mockGwt).scheduleFixedDelay(any(Callback.class), anyInt());
	}
	
	@Test
	public void testVersionedSuccess() {
		entityHeaderAsyncHandler.getEntityHeader(entityId, entityVersion, mockCallback);
		when(mockEntityHeader.getVersionNumber()).thenReturn(entityVersion);
		entityHeaderList.add(mockEntityHeader);
		
		entityHeaderAsyncHandler.executeRequests();
		verify(mockSynapseJavascriptClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		verify(mockCallback).onSuccess(mockEntityHeader);
	}

	@Test
	public void testVersionedNotFound() {
		entityHeaderAsyncHandler.getEntityHeader(entityId, entityVersion, mockCallback);
		// different version
		when(mockEntityHeader.getVersionNumber()).thenReturn(entityVersion + 1);
		entityHeaderList.add(mockEntityHeader);
		
		entityHeaderAsyncHandler.executeRequests();
		verify(mockSynapseJavascriptClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		verify(mockCallback).onFailure(any(NotFoundException.class));
	}
}

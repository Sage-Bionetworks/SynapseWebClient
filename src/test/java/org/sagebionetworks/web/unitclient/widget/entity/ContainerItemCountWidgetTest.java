package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.repo.model.EntityType.entityview;
import static org.sagebionetworks.repo.model.EntityType.file;
import static org.sagebionetworks.repo.model.EntityType.folder;
import static org.sagebionetworks.repo.model.EntityType.link;
import static org.sagebionetworks.repo.model.EntityType.table;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.entity.ContainerItemCountWidget;
import org.sagebionetworks.web.client.widget.entity.ContainerItemCountWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class ContainerItemCountWidgetTest {
	@Mock
	ContainerItemCountWidgetView mockView;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	SynapseJSNIUtils mockJSNI;
	@Mock
	EntityChildrenResponse mockResponse;
	@Captor
	ArgumentCaptor<EntityChildrenRequest> requestCaptor;

	ContainerItemCountWidget widget;
	private String entityId = "syn123";

	@Before
	public void before() {
		widget = new ContainerItemCountWidget(mockView, mockJsClient, mockJSNI);
		AsyncMockStubber.callSuccessWith(mockResponse).when(mockJsClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
	}

	@Test
	public void testClear() {
		widget.clear();

		verify(mockView).hide();
		verify(mockView).clear();
	}

	@Test
	public void testConfigure() {
		Long childCount = 12L;
		when(mockResponse.getTotalChildCount()).thenReturn(childCount);

		widget.configure(entityId);

		verify(mockJsClient).getEntityChildren(requestCaptor.capture(), any(AsyncCallback.class));
		verify(mockView).showCount(childCount);
		// verify request
		EntityChildrenRequest request = requestCaptor.getValue();
		assertEquals(entityId, request.getParentId());
		assertFalse(request.getIncludeSumFileSizes());
		assertTrue(request.getIncludeTotalChildCount());
		List<EntityType> entityTypes = request.getIncludeTypes();
		assertTrue(entityTypes.contains(file));
		assertTrue(entityTypes.contains(folder));
		assertTrue(entityTypes.contains(link));
		assertTrue(entityTypes.contains(table));
		assertTrue(entityTypes.contains(entityview));
	}

	@Test
	public void testConfigureZeroChildren() {
		Long childCount = 0L;
		when(mockResponse.getTotalChildCount()).thenReturn(childCount);

		widget.configure(entityId);

		verify(mockJsClient).getEntityChildren(requestCaptor.capture(), any(AsyncCallback.class));
		verify(mockView).hide();
		verify(mockView).clear();
		verify(mockView, never()).showCount(anyLong());
	}

	@Test
	public void testConfigureFailure() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));

		widget.configure(entityId);

		verify(mockJsClient).getEntityChildren(requestCaptor.capture(), any(AsyncCallback.class));
		verify(mockView).hide();
		verify(mockView).clear();
		verify(mockView, never()).showCount(anyLong());
		verify(mockJSNI).consoleError(ex);
	}
}

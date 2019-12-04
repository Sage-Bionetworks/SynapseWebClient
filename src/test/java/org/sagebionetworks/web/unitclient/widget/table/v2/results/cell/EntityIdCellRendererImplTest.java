package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.asynch.EntityHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.VersionedEntityHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRendererView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityIdCellRendererImplTest {

	EntityIdCellRenderer renderer;
	@Mock
	EntityIdCellRendererView mockView;
	@Mock
	EntityHeaderAsyncHandler mockEntityHeaderAsyncHandler;
	@Mock
	VersionedEntityHeaderAsyncHandler mockVersionedEntityHeaderAsyncHandler;
	@Mock
	EntityHeader mockProjectHeader;
	@Mock
	ClickHandler mockClickHandler;
	@Mock
	SynapseJSNIUtils mockJSNIUtils;
	private static final String PROJECT_NAME = "Project Win";

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		renderer = new EntityIdCellRenderer(mockView, mockEntityHeaderAsyncHandler, mockVersionedEntityHeaderAsyncHandler, mockJSNIUtils);
		AsyncMockStubber.callSuccessWith(mockProjectHeader).when(mockEntityHeaderAsyncHandler).getEntityHeader(anyString(), any(AsyncCallback.class));
		when(mockProjectHeader.getName()).thenReturn(PROJECT_NAME);
	}

	@Test
	public void testAsWidget() {
		renderer.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testSetValue() {
		String entityId = "987654";
		renderer.setValue(entityId);

		verify(mockView).showLoadingIcon();
		verify(mockEntityHeaderAsyncHandler).getEntityHeader(anyString(), any(AsyncCallback.class));
		verify(mockView).setIcon(any(IconType.class));
		verify(mockView).setLinkText(PROJECT_NAME);
		verify(mockView).setEntityId("syn" + entityId);
		verify(mockView, never()).setClickHandler(any(ClickHandler.class));

		// verify that attempting to load data again is a no-op
		reset(mockEntityHeaderAsyncHandler);
		renderer.loadData();
		verifyZeroInteractions(mockEntityHeaderAsyncHandler);
	}

	@Test
	public void testSetValueAndCallback() {
		String entityId = "987654";
		renderer.setValue(entityId, mockClickHandler, true);
		verify(mockView).showLoadingIcon();
		verify(mockView).setEntityId("syn" + entityId);
		verify(mockView).setClickHandler(mockClickHandler);

		// verify that attempting to load data again is a no-op
		reset(mockEntityHeaderAsyncHandler);
		renderer.loadData();
		verifyZeroInteractions(mockEntityHeaderAsyncHandler);
	}

	@Test
	public void testSetValueRpcFailure() {
		String errorMessage = "error";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockEntityHeaderAsyncHandler).getEntityHeader(anyString(), any(AsyncCallback.class));
		String entityId = "syn987654";
		renderer.setValue(entityId);

		verify(mockView).showLoadingIcon();
		verify(mockEntityHeaderAsyncHandler).getEntityHeader(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorIcon(errorMessage);
		verify(mockView).setLinkText(entityId);
	}

	@Test
	public void testSetValueRpcFailureHideIfLoadError() {
		String errorMessage = "error";
		boolean hideIfLoadError = true;
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockEntityHeaderAsyncHandler).getEntityHeader(anyString(), any(AsyncCallback.class));
		String entityId = "syn987654";
		renderer.setValue(entityId, hideIfLoadError);

		verify(mockView).showLoadingIcon();
		verify(mockEntityHeaderAsyncHandler).getEntityHeader(anyString(), any(AsyncCallback.class));
		verify(mockJSNIUtils).consoleError(errorMessage);
		verify(mockView).setVisible(false);
	}
}

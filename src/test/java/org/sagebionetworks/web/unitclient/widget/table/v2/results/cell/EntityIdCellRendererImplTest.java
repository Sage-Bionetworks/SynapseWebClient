package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.asynch.EntityHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRendererImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRendererView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityIdCellRendererImplTest {

	EntityIdCellRendererImpl renderer;
	@Mock
	EntityIdCellRendererView mockView;
	@Mock
	EntityHeaderAsyncHandler mockEntityHeaderAsyncHandler;
	@Mock
	LazyLoadHelper mockLazyLoadHelper;
	@Mock
	EntityHeader mockProjectHeader;
	private static final String PROJECT_NAME = "Project Win";
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		renderer = new EntityIdCellRendererImpl(mockView, mockLazyLoadHelper, mockEntityHeaderAsyncHandler);
		AsyncMockStubber.callSuccessWith(mockProjectHeader).when(mockEntityHeaderAsyncHandler).getEntityHeader(anyString(), any(AsyncCallback.class));
		when(mockProjectHeader.getName()).thenReturn(PROJECT_NAME);
	}
	
	private void simulateInView() {
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockLazyLoadHelper).configure(captor.capture(), eq(mockView));
		captor.getValue().invoke();
	}
	
	@Test
	public void testAsWidget(){
		renderer.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testSetValue(){
		String entityId = "syn987654";
		renderer.setValue(entityId);
		verify(mockLazyLoadHelper).setIsConfigured();
		verify(mockView).setLinkHref("#!Synapse:syn987654");
		verifyZeroInteractions(mockEntityHeaderAsyncHandler);
		
		simulateInView();
		verify(mockView).showLoadingIcon();
		verify(mockEntityHeaderAsyncHandler).getEntityHeader(anyString(), any(AsyncCallback.class));
		verify(mockView).setIcon(any(IconType.class));
		verify(mockView).setLinkText(PROJECT_NAME);
		
		//verify that attempting to load data again is a no-op
		reset(mockEntityHeaderAsyncHandler);
		renderer.loadData();
		verifyZeroInteractions(mockEntityHeaderAsyncHandler);
	}
	
	@Test
	public void testSetValueRpcFailure(){
		String errorMessage = "error";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockEntityHeaderAsyncHandler).getEntityHeader(anyString(), any(AsyncCallback.class));
		String entityId = "syn987654";
		renderer.setValue(entityId);
		
		simulateInView();
		verify(mockView).showLoadingIcon();
		verify(mockEntityHeaderAsyncHandler).getEntityHeader(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorIcon(errorMessage);
		verify(mockView).setLinkText(entityId);
	}
}

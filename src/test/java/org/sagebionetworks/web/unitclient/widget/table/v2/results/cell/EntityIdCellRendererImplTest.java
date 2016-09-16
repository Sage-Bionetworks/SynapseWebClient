package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
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
	SynapseClientAsync mockSynapseClient;
	@Mock
	LazyLoadHelper mockLazyLoadHelper;
	@Mock
	Project mockProject;
	private static final String PROJECT_NAME = "Project Win";
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		renderer = new EntityIdCellRendererImpl(mockView, mockSynapseClient, mockLazyLoadHelper);
		AsyncMockStubber.callSuccessWith(mockProject).when(mockSynapseClient).getEntity(anyString(), any(AsyncCallback.class));
		when(mockProject.getName()).thenReturn(PROJECT_NAME);
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
		verifyZeroInteractions(mockSynapseClient);
		
		simulateInView();
		verify(mockView).showLoadingIcon();
		verify(mockSynapseClient).getEntity(eq(entityId), any(AsyncCallback.class));
		verify(mockView).setIcon(any(IconType.class));
		verify(mockView).setLinkText(PROJECT_NAME);
		
		//verify that attempting to load data again is a no-op
		reset(mockSynapseClient);
		renderer.loadData();
		verifyZeroInteractions(mockSynapseClient);
	}
	
	@Test
	public void testSetValueRpcFailure(){
		String errorMessage = "error";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseClient).getEntity(anyString(), any(AsyncCallback.class));
		String entityId = "syn987654";
		renderer.setValue(entityId);
		
		simulateInView();
		verify(mockView).showLoadingIcon();
		verify(mockSynapseClient).getEntity(eq(entityId), any(AsyncCallback.class));
		verify(mockView).showErrorIcon(errorMessage);
		verify(mockView).setLinkText(entityId);
	}
}

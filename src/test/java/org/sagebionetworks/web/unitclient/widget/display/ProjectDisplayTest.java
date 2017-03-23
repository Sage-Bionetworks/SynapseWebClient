package org.sagebionetworks.web.unitclient.widget.display;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.display.ProjectDisplayDialog;
import org.sagebionetworks.web.client.widget.display.ProjectDisplayView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.ProjectDisplayBundle;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ProjectDisplayTest {

	
	@Mock
	ProjectDisplayView mockView;
	
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	ClientCache mockStorage;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	PlaceChanger mockPlaceChanger;
	
	String userId = "1234567";
	String projectId = "syn123";
	ProjectDisplayDialog modal;
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		modal = new ProjectDisplayDialog(mockView, mockSynapseClient, mockSynAlert, mockStorage, mockGlobalApplicationState);
		modal.configure(projectId, userId);
	}
	
	@Test
	public void testShowDialog() {
		ProjectDisplayBundle result = new ProjectDisplayBundle(false, false, false, false, false, false);
		AsyncMockStubber.callSuccessWith(result).when(mockSynapseClient).getProjectDisplay(anyString(), any(AsyncCallback.class));
		modal.show();
		//verify default set (since all are hidden, and nothing in the cache)
		InOrder order = inOrder(mockView);
		order.verify(mockView).setWiki(false);
		order.verify(mockView).setWiki(true);
		
		order = inOrder(mockView);
		order.verify(mockView).setFiles(false);
		order.verify(mockView).setFiles(true);
		
		order = inOrder(mockView);
		order.verify(mockView).setTables(false);
		order.verify(mockView).setTables(true);
		
		verify(mockView).setChallenge(false);
		verify(mockView, never()).setChallenge(true);
		
		order = inOrder(mockView);
		order.verify(mockView).setDiscussion(false);
		order.verify(mockView).setDiscussion(true);

		verify(mockView).setDocker(false);
		verify(mockView, never()).setDocker(true);
	}
	
	@Test
	public void testShowDialogError() {
		AsyncMockStubber.callFailureWith(new Throwable("error")).when(mockSynapseClient).getProjectDisplay(anyString(), any(AsyncCallback.class));
		modal.show();
		verify(mockView).showErrorMessage("error");
		verify(mockView, never()).show();
		
	}
	
	@Test
	public void testHideDialog() {
		modal.hide();
		verify(mockView).hide();
		verify(mockPlaceChanger).goTo(new Synapse(projectId));
	}
	
	@Test
	public void testAsWidget() {
		modal.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testClear() {
		modal.clear();
		verify(mockView).clear();
	}
	
	@Test
	public void testOnCancel() {
		modal.onCancel();
		verify(mockSynAlert).clear();
		verify(mockView).hide();
		verify(mockPlaceChanger).goTo(new Synapse(projectId));
	}
	
	@Test
	public void testOnCancelSetDefaults() {
		ProjectDisplayBundle result = new ProjectDisplayBundle(false, false, false, false, false, false);
		AsyncMockStubber.callSuccessWith(result).when(mockSynapseClient).getProjectDisplay(anyString(), any(AsyncCallback.class));
		modal.show();
		
		reset(mockView);
		// mock defaults
		when(mockView.getWiki()).thenReturn(true);
		when(mockView.getFiles()).thenReturn(true);
		when(mockView.getTables()).thenReturn(true);
		when(mockView.getChallenge()).thenReturn(false);
		when(mockView.getDiscussion()).thenReturn(true);
		when(mockView.getDocker()).thenReturn(false);
		
		modal.onCancel();
		// sets defaults on view, and saves
		verify(mockView).setWiki(true);
		verify(mockView).setFiles(true);
		verify(mockView).setTables(true);
		verify(mockView).setDiscussion(true);
		verify(mockStorage, times(4)).put(anyString(), anyString(), anyLong());
		
		verify(mockSynAlert).clear();
		verify(mockView).hide();
		verify(mockPlaceChanger).goTo(new Synapse(projectId));
	}

	
	@Test
	public void testOnSave() {
		ProjectDisplayBundle result = new ProjectDisplayBundle(false, false, false, false, false, false);
		AsyncMockStubber.callSuccessWith(result).when(mockSynapseClient).getProjectDisplay(anyString(), any(AsyncCallback.class));
		when(mockView.getWiki()).thenReturn(true);
		when(mockView.getFiles()).thenReturn(true);
		when(mockView.getTables()).thenReturn(false);
		when(mockView.getChallenge()).thenReturn(false);
		when(mockView.getDiscussion()).thenReturn(false);
		when(mockView.getDocker()).thenReturn(false);
		modal.show();
		modal.onSave();
		verify(mockSynAlert).clear();
		verify(mockStorage, times(2)).put(anyString(), anyString(), anyLong());
		verify(mockStorage, times(4)).remove(anyString());
		verify(mockPlaceChanger).goTo(new Synapse(projectId));
	}
	
	@Test
	public void testOnSaveIllegal() {
		ProjectDisplayBundle result = new ProjectDisplayBundle(true, false, false, false, false, false);
		AsyncMockStubber.callSuccessWith(result).when(mockSynapseClient).getProjectDisplay(anyString(), any(AsyncCallback.class));
		when(mockView.getWiki()).thenReturn(false);
		when(mockView.getFiles()).thenReturn(false);
		when(mockView.getTables()).thenReturn(false);
		when(mockView.getChallenge()).thenReturn(false);
		when(mockView.getDiscussion()).thenReturn(false);
		when(mockView.getDocker()).thenReturn(false);
		modal.show();
		modal.onSave();
		verify(mockSynAlert).showError(anyString());
		verify(mockView, never()).hide();
	}

}
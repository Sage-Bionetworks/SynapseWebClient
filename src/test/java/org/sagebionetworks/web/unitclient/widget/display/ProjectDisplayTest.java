package org.sagebionetworks.web.unitclient.widget.display;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.Callback;
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
	SessionStorage mockStorage;
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
		AsyncMockStubber.callSuccessWith(result).when(mockSynapseClient).getCountsForTabs(anyString(), any(AsyncCallback.class));
		modal.show();
		verify(mockView).setWiki(false);
		verify(mockView).setFiles(false);
		verify(mockView).setTables(false);
		verify(mockView).setChallenge(false);
		verify(mockView).setDiscussion(false);
		verify(mockView).setDocker(false);
	}
	
	@Test
	public void testShowDialogError() {
		ProjectDisplayBundle result = new ProjectDisplayBundle(false, false, false, false, false, false);
		AsyncMockStubber.callFailureWith(new Throwable("error")).when(mockSynapseClient).getCountsForTabs(anyString(), any(AsyncCallback.class));
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
	public void asWidgetTest() {
		modal.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void onClearTest() {
		modal.clear();
		verify(mockView).clear();
	}
	
	@Test
	public void cancelTest() {
		modal.cancel();
		verify(mockSynAlert).clear();
		verify(mockView).hide();
		verify(mockPlaceChanger).goTo(new Synapse(projectId));
	}
	
	@Test
	public void testOnSave() {
		ProjectDisplayBundle result = new ProjectDisplayBundle(false, false, false, false, false, false);
		AsyncMockStubber.callSuccessWith(result).when(mockSynapseClient).getCountsForTabs(anyString(), any(AsyncCallback.class));
		when(mockView.getWiki()).thenReturn(true);
		when(mockView.getFiles()).thenReturn(true);
		when(mockView.getTables()).thenReturn(false);
		when(mockView.getChallenge()).thenReturn(false);
		when(mockView.getDiscussion()).thenReturn(false);
		when(mockView.getDocker()).thenReturn(false);
		modal.show();
		modal.onSave();
		verify(mockSynAlert).clear();
		verify(mockStorage, times(2)).setItem(anyString(), anyString());
		verify(mockStorage, times(4)).removeItem(anyString());
		verify(mockPlaceChanger).goTo(new Synapse(projectId));
	}
	
	@Test
	public void testOnSaveIllegal() {
		ProjectDisplayBundle result = new ProjectDisplayBundle(true, false, false, false, false, false);
		AsyncMockStubber.callSuccessWith(result).when(mockSynapseClient).getCountsForTabs(anyString(), any(AsyncCallback.class));
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
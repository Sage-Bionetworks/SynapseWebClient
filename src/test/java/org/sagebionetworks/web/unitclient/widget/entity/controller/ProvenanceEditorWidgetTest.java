package org.sagebionetworks.web.unitclient.widget.entity.controller;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.provenance.Used;
import org.sagebionetworks.repo.model.provenance.UsedEntity;
import org.sagebionetworks.repo.model.provenance.UsedURL;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.controller.EntityRefProvEntryView;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceEditorWidget;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceEditorWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceEntry;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceListWidget;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceURLDialogWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.controller.URLProvEntryView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ProvenanceEditorWidgetTest {
	
	ProvenanceEditorWidgetView mockView;
	SynapseClientAsync mockSynClient;
	SynapseAlert mockSynAlert;
	PortalGinInjector mockInjector;
	ProvenanceListWidget mockProvenanceList;
	Activity mockActivity;
	EntityFinder mockEntityFinder;
	ProvenanceURLDialogWidget mockUrlDialog;
	EntityUpdatedHandler mockEntityUpdatedHandler;
	ProvenanceEditorWidget presenter;
	EntityBundle mockEntityBundle;
	Entity mockEntity;
	Reference mockRef;
	UsedURL mockUsedUrl;
	UsedEntity mockUsedEntity;
	EntityRefProvEntryView mockEntityProvEntry;
	URLProvEntryView mockUrlProvEntry;

	Set<Used> usedSet = new HashSet<Used>();
	String name = "testName";
	String url = "test.com";
	String entityId = "syn123";
	Long version = 1L;
	Exception caught = new Exception("this is an exception");
	
	@Before
	public void before() {
		mockView = mock(ProvenanceEditorWidgetView.class);
		mockSynClient = mock(SynapseClientAsync.class);
		mockSynAlert = mock(SynapseAlert.class);
		mockInjector = mock(PortalGinInjector.class);
		mockProvenanceList = mock(ProvenanceListWidget.class);
		mockActivity = mock(Activity.class);
		mockEntityFinder = mock(EntityFinder.class);
		mockUrlDialog = mock(ProvenanceURLDialogWidget.class);
		mockEntityUpdatedHandler = mock(EntityUpdatedHandler.class);
		mockEntityBundle = mock(EntityBundle.class);
		mockEntity = mock(Entity.class);
		mockRef = mock(Reference.class);
		mockUsedUrl = mock(UsedURL.class);
		mockUrlProvEntry = mock(URLProvEntryView.class);
		mockUsedEntity = mock(UsedEntity.class);
		mockEntityProvEntry = mock(EntityRefProvEntryView.class);
		when(mockInjector.getProvenanceListWidget()).thenReturn(mockProvenanceList);
		presenter = new ProvenanceEditorWidget(mockView, mockSynClient, mockSynAlert,
				mockInjector, mockEntityFinder, mockUrlDialog);
		
		when(mockInjector.getEntityRefEntry()).thenReturn(mockEntityProvEntry);
		when(mockInjector.getURLEntry()).thenReturn(mockUrlProvEntry);
		when(mockEntityBundle.getEntity()).thenReturn(mockEntity);
		UsedURL usedUrl = new UsedURL();
		usedUrl.setName(name);
		usedUrl.setUrl(url);
		usedUrl.setWasExecuted(true);
		UsedEntity usedEntity = new UsedEntity();
		when(mockRef.getTargetId()).thenReturn(entityId);
		when(mockRef.getTargetVersionNumber()).thenReturn(version);
		usedEntity.setReference(mockRef);
		usedEntity.setWasExecuted(false);
		usedSet.add(usedUrl);
		usedSet.add(usedEntity);
		when(mockActivity.getUsed()).thenReturn(usedSet);
		List<ProvenanceEntry> provList = new LinkedList<ProvenanceEntry>();
		provList.add(mockEntityProvEntry);
		provList.add(mockUrlProvEntry);
		when(mockProvenanceList.getEntries())
				.thenReturn(provList);
	}
	
	@Test
	public void testConstruction() {
		verify(mockInjector, times(2)).getProvenanceListWidget();
		verify(mockProvenanceList, times(2)).setEntityFinder(mockEntityFinder);
		verify(mockProvenanceList, times(2)).setURLDialog(mockUrlDialog);
		verify(mockView).setSynAlertWidget(mockSynAlert);
		verify(mockView).setUsedProvenanceList(mockProvenanceList);
		verify(mockView).setExecutedProvenanceList(mockProvenanceList);
		verify(mockView).setEntityFinder(mockEntityFinder);
		verify(mockView).setURLDialog(mockUrlDialog);
		verify(mockView).setPresenter(presenter);
	}
	
	@Test
	public void testConfigureSuccess() {
		AsyncMockStubber.callSuccessWith(mockActivity)
				.when(mockSynClient).getOrCreateActivityForEntityVersion
				(anyString(), anyLong(), any(AsyncCallback.class));
		presenter.configure(mockEntityBundle, mockEntityUpdatedHandler);
		verify(mockView).setName(mockActivity.getName());
		verify(mockView).setDescription(mockActivity.getDescription());
		verify(mockActivity).getUsed();
		verify(mockInjector).getEntityRefEntry();
		verify(mockInjector.getEntityRefEntry()).configure(mockRef.getTargetId(), mockRef.getTargetVersionNumber().toString());
		verify(mockInjector.getEntityRefEntry()).setAnchorTarget(anyString());
		verify(mockInjector).getURLEntry();
		verify(mockInjector.getURLEntry()).configure(name, url);
		verify(mockInjector.getURLEntry()).setAnchorTarget(anyString());
		verify(mockProvenanceList, times(2)).configure(anyList());
	}
	
	@Test
	public void testConfigureFailure() {
		AsyncMockStubber.callFailureWith(caught)
				.when(mockSynClient).getOrCreateActivityForEntityVersion
				(anyString(), anyLong(), any(AsyncCallback.class));
		presenter.configure(mockEntityBundle, mockEntityUpdatedHandler);
		verify(mockSynAlert).handleException(caught);
	}
	
	@Test
	public void onSaveSuccess() {
		AsyncMockStubber.callSuccessWith(null)
				.when(mockSynClient).putActivity(eq(mockActivity), any(AsyncCallback.class));
		presenter.setActivty(mockActivity);
		presenter.setEntityUpdatedHandler(mockEntityUpdatedHandler);
		presenter.onSave();
		verify(mockUrlProvEntry, times(2)).getURL();
		verify(mockUrlProvEntry, times(2)).getTitle();
		verify(mockEntityProvEntry, times(2)).getEntryId();
		verify(mockEntityProvEntry, times(2)).getEntryVersion();
		ArgumentCaptor<Set> captor = ArgumentCaptor.forClass(Set.class);
		verify(mockActivity).setUsed(captor.capture());
		Set usedSet = captor.getValue();
		assertTrue(usedSet.size() == 4);
		verify(mockSynClient).putActivity(eq(mockActivity), any(AsyncCallback.class));
		verify(mockView).hide();
		verify(mockEntityUpdatedHandler).onPersistSuccess(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void onSaveFailure() {
		AsyncMockStubber.callFailureWith(caught)
				.when(mockSynClient).putActivity(eq(mockActivity), any(AsyncCallback.class));
		presenter.setActivty(mockActivity);
		presenter.onSave();
		verify(mockSynClient).putActivity(eq(mockActivity), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(caught);
	}
	
}

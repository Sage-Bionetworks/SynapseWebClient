package org.sagebionetworks.web.unitclient.widget.entity.download;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.utils.FutureUtils.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseJavascriptFactory.OBJECT_TYPE;
import org.sagebionetworks.web.client.widget.entity.SharingAndDataUseConditionWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.download.AddFolderDialogWidget;
import org.sagebionetworks.web.client.widget.entity.download.AddFolderDialogWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public class AddFolderDialogWidgetTest {

	@Mock
	AddFolderDialogWidgetView mockView;
	@Mock
	SharingAndDataUseConditionWidget mockSharingAndDataUseWidget;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	GlobalApplicationState mockGlobalAppState;
	@Mock
	PopupUtilsView mockPopupUtils;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	Folder mockFolder;
	
	AddFolderDialogWidget w;
	
	public static final String PARENT_ENTITY_ID = "syn98208";
	public static final String NEW_FOLDER_ID = "syn98209";
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		w = new AddFolderDialogWidget(mockView, 
				mockSharingAndDataUseWidget, 
				mockSynapseClient, 
				mockSynapseJavascriptClient, 
				mockGlobalAppState, 
				mockPopupUtils, 
				mockSynAlert);
		when(mockFolder.getId()).thenReturn(NEW_FOLDER_ID);
		when(mockSynapseJavascriptClient.createEntity(any(Entity.class))).thenReturn(getDoneFuture(mockFolder));
	}

	@Test
	public void testConstructor() {
		verify(mockView).setSynAlert(mockSynAlert);;
		verify(mockView).setSharingAndDataUseWidget(any(IsWidget.class));
		verify(mockView).setPresenter(w);
	}

	@Test
	public void testAsWidget() {
		w.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testShow() {
		w.show(PARENT_ENTITY_ID);
		verify(mockSynapseJavascriptClient).createEntity(any(Folder.class));
		verify(mockView).show();
	}
	
	@Test
	public void testCreateFolderFail() {
		String error = "Too many files in one folder, Kenny!";
		Exception ex = new Exception(error);
		when(mockSynapseJavascriptClient.createEntity(any(Entity.class))).thenReturn(getFailedFuture(ex));
		w.show(PARENT_ENTITY_ID);
		verify(mockSynapseJavascriptClient).createEntity(any(Folder.class));
		verify(mockPopupUtils).showErrorMessage(AddFolderDialogWidget.FOLDER_CREATION_ERROR, error);
	}

	@Test
	public void testDeleteFolder() {
		boolean skipTrashCan = true;
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseJavascriptClient).deleteEntityById(anyString(), anyBoolean(), any(AsyncCallback.class));
		
		w.show(PARENT_ENTITY_ID);
		w.deleteFolder(skipTrashCan);
		
		verify(mockSynapseJavascriptClient).deleteEntityById(eq(NEW_FOLDER_ID), eq(skipTrashCan), any(AsyncCallback.class));
		verify(mockView).hide();
		// does not need to refresh (SWC-4089)
		verify(mockGlobalAppState, never()).refreshPage();
	}

	@Test
	public void testDeleteFolderFail() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).deleteEntityById(anyString(), anyBoolean(), any(AsyncCallback.class));

		w.show(PARENT_ENTITY_ID);
		w.deleteFolder(true);
		verify(mockSynapseJavascriptClient).deleteEntityById(anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testUpdateFolderNameFail() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).updateEntity(any(Entity.class), any(AsyncCallback.class));
		w.show(PARENT_ENTITY_ID);
		Folder f = new Folder();
		f.setName("raven");
		f.setId(NEW_FOLDER_ID);
		AsyncMockStubber.callSuccessWith(f).when(mockSynapseJavascriptClient).getEntity(anyString(), 
				any(OBJECT_TYPE.class), any(AsyncCallback.class));
		w.updateFolderName("newname");
		
		verify(mockSynapseClient).updateEntity(any(Entity.class), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}

}

package org.sagebionetworks.web.unitclient.widget.entity.download;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
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
		boolean isNewEntity = true;
		AsyncMockStubber.callSuccessWith(NEW_FOLDER_ID).when(mockSynapseClient).createOrUpdateEntity(any(Entity.class), 
				any(Annotations.class), eq(isNewEntity), any(AsyncCallback.class));
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
		verify(mockSynapseClient).createOrUpdateEntity(any(Entity.class), any(Annotations.class), eq(true), any(AsyncCallback.class));
		verify(mockView).show();
	}
	
	@Test
	public void testCreateFolderFail() {
		String error = "Too many files in one folder, Tom!";
		Exception ex = new Exception(error);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).createOrUpdateEntity(any(Entity.class), 
				any(Annotations.class), anyBoolean(), any(AsyncCallback.class));
		w.show(PARENT_ENTITY_ID);
		verify(mockSynapseClient).createOrUpdateEntity(any(Entity.class), any(Annotations.class), eq(true), any(AsyncCallback.class));
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
		verify(mockGlobalAppState).refreshPage();
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

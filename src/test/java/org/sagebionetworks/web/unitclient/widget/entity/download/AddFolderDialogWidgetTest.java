package org.sagebionetworks.web.unitclient.widget.entity.download;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
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
		w = new AddFolderDialogWidget(mockView, mockSharingAndDataUseWidget, mockSynapseJavascriptClient, mockGlobalAppState, mockPopupUtils, mockSynAlert);
		when(mockFolder.getId()).thenReturn(NEW_FOLDER_ID);
		AsyncMockStubber.callSuccessWith(mockFolder).when(mockSynapseJavascriptClient).createEntity(any(Entity.class), any(AsyncCallback.class));
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

		verify(mockSynapseJavascriptClient, never()).createEntity(any(Folder.class));
		verify(mockView).show();

		w.createFolder("test");

		verify(mockSynapseJavascriptClient).createEntity(any(Folder.class), any(AsyncCallback.class));
		verify(mockView).hide();
	}

	@Test
	public void testCreateFolderFail() {
		String error = "Too many files in one folder, Kenny!";
		Exception ex = new Exception(error);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).createEntity(any(Entity.class), any(AsyncCallback.class));

		w.show(PARENT_ENTITY_ID);
		w.createFolder("test");

		verify(mockSynapseJavascriptClient).createEntity(any(Folder.class), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}
}

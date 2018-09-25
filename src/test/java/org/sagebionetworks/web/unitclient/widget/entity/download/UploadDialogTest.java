package org.sagebionetworks.web.unitclient.widget.entity.download;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidget;
import org.sagebionetworks.web.client.widget.entity.download.UploadDialogWidgetView;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;

import com.google.gwt.user.client.ui.Widget;


public class UploadDialogTest {
	
	UploadDialogWidgetView view;
	Uploader mockUploader; 
	UploadDialogWidget widget;
	
	@Before
	public void before() throws Exception {
		view = mock(UploadDialogWidgetView.class);
		mockUploader = mock(Uploader.class);
		widget = new UploadDialogWidget(view, mockUploader);
	}
	
	@Test
	public void testConfigure() {
		String title = "dialog title";
		Entity entity = mock(Entity.class);
		String parentEntityId = "parent";
		EntityUpdatedHandler handler = mock(EntityUpdatedHandler.class);
		CallbackP<String> fileHandleIdCallback = mock(CallbackP.class);
		boolean isEntity = true;
		widget.configure(title, entity, parentEntityId, handler, fileHandleIdCallback, isEntity);
		
		verify(mockUploader).configure(entity, parentEntityId, fileHandleIdCallback, isEntity);
		verify(view).configureDialog(eq(title), any(Widget.class));
		
		verify(mockUploader).clearHandlers();
		verify(mockUploader, times(2)).addPersistSuccessHandler(any(EntityUpdatedHandler.class));
		verify(mockUploader).addCancelHandler(any(CancelHandler.class));
	}
	
	@Test
	public void testDisableMultipleFileUploads() {
		widget.disableMultipleFileUploads();
		verify(mockUploader).disableMultipleFileUploads();
	}
	
	@Test
	public void testShow() {
		widget.show();
		verify(view).showDialog();
	}

}

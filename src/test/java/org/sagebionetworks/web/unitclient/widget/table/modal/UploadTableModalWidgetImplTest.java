package org.sagebionetworks.web.unitclient.widget.table.modal;

import org.junit.Before;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.modal.UploadTableModalView;
import org.sagebionetworks.web.client.widget.table.modal.UploadTableModalWidgetImpl;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;

/**
 * 
 * @author jhill
 *
 */
public class UploadTableModalWidgetImplTest {
	
	FileInputWidget fileInputWidget;
	UploadTableModalView mockView;
	UploadTableModalWidgetImpl widget;
	
	@Before
	public void before(){
		mockView = Mockito.mock(UploadTableModalView.class);
		fileInputWidget = Mockito.mock(FileInputWidget.class);
		widget = new UploadTableModalWidgetImpl(mockView, fileInputWidget);
	}

}

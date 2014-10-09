package org.sagebionetworks.web.unitclient.widget.table.modal;

import org.junit.Before;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.TableCreatedHandler;
import org.sagebionetworks.web.client.widget.table.modal.UploadTableModalView;
import org.sagebionetworks.web.client.widget.table.modal.UploadTableModalWidgetImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFilePage;

/**
 * 
 * @author jhill
 *
 */
public class UploadTableModalWidgetImplTest {
	
	UploadCSVFilePage mockUploadCSVFileWidget;
	UploadTableModalView mockView;
	UploadTableModalWidgetImpl widget;
	String parentId;
	TableCreatedHandler mockHandler; 
	
	@Before
	public void before(){
		mockView = Mockito.mock(UploadTableModalView.class);
		mockHandler = Mockito.mock(TableCreatedHandler.class);
		parentId = "syn123";
		widget = new UploadTableModalWidgetImpl(mockView, mockUploadCSVFileWidget);
		widget.configure(parentId, mockHandler);
	}

}

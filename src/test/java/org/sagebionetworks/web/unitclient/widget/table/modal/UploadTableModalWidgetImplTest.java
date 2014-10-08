package org.sagebionetworks.web.unitclient.widget.table.modal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.TableCreatedHandler;
import org.sagebionetworks.web.client.widget.table.modal.UploadTableModalView;
import org.sagebionetworks.web.client.widget.table.modal.UploadTableModalWidgetImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVConfigurationWidget;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;

import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 
 * @author jhill
 *
 */
public class UploadTableModalWidgetImplTest {
	
	FileInputWidget mockFileInputWidget;
	UploadCSVConfigurationWidget mockUploadPreviewWidget;
	UploadTableModalView mockView;
	UploadTableModalWidgetImpl widget;
	String parentId;
	TableCreatedHandler mockHandler; 
	
	@Before
	public void before(){
		mockView = Mockito.mock(UploadTableModalView.class);
		mockFileInputWidget = Mockito.mock(FileInputWidget.class);
		mockUploadPreviewWidget = Mockito.mock(UploadCSVConfigurationWidget.class);
		mockHandler = Mockito.mock(TableCreatedHandler.class);
		parentId = "syn123";
		widget = new UploadTableModalWidgetImpl(mockView, mockFileInputWidget, mockUploadPreviewWidget);
		widget.configure(parentId, mockHandler);
	}

	@Test
	public void testShowModal(){
		widget.showModal();
		verify(mockView).setPrimaryEnabled(true);
		verify(mockView).setInstructionsMessage(UploadTableModalWidgetImpl.CHOOSE_A_CSV_OR_TSV_FILE);
		verify(mockView).showAlert(false);
		verify(mockFileInputWidget).configure(widget);
		verify(mockView).setBody(mockFileInputWidget);
		verify(mockView).showModal();

	}
}

package org.sagebionetworks.web.unitclient.widget.table.modal.upload;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;
import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsView;
import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsWidgetImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsWidget.ChangeHandler;

public class CSVOptionsWidgetImplTest {
	
	ChangeHandler mockHandler;
	CSVOptionsView mockView;
	CSVOptionsWidgetImpl widget;
	
	@Before
	public void before(){
		mockView = Mockito.mock(CSVOptionsView.class);
		mockHandler = Mockito.mock(ChangeHandler.class);
		widget = new CSVOptionsWidgetImpl(mockView);
	}

	@Test
	public void testOptionsRoundTripOther(){
		CSVOptionsViewStub stub = new CSVOptionsViewStub();
		widget = new CSVOptionsWidgetImpl(stub);
		UploadToTablePreviewRequest inRequest = new UploadToTablePreviewRequest();
		CsvTableDescriptor csvTableDescriptor = new CsvTableDescriptor();
		csvTableDescriptor.setSeparator("p");
		inRequest.setCsvTableDescriptor(csvTableDescriptor);
		widget.configure(inRequest, mockHandler);
		UploadToTableRequest outRequest = widget.getCurrentOptions();
		assertNotNull(outRequest);
		assertEquals(inRequest.getCsvTableDescriptor(), outRequest.getCsvTableDescriptor());
	}
	
	@Test
	public void testOptionsRoundTripComma(){
		CSVOptionsViewStub stub = new CSVOptionsViewStub();
		widget = new CSVOptionsWidgetImpl(stub);
		UploadToTablePreviewRequest inRequest = new UploadToTablePreviewRequest();
		CsvTableDescriptor csvTableDescriptor = new CsvTableDescriptor();
		csvTableDescriptor.setSeparator(",");
		inRequest.setCsvTableDescriptor(csvTableDescriptor);
		widget.configure(inRequest, mockHandler);
		UploadToTableRequest outRequest = widget.getCurrentOptions();
		assertNotNull(outRequest);
		assertEquals(inRequest.getCsvTableDescriptor(), outRequest.getCsvTableDescriptor());
	}
}

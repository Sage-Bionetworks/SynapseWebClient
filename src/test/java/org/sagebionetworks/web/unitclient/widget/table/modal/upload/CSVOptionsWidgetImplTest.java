package org.sagebionetworks.web.unitclient.widget.table.modal.upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsView;
import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsWidget.ChangeHandler;
import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsWidgetImpl;

public class CSVOptionsWidgetImplTest {
	
	ChangeHandler mockHandler;
	CSVOptionsView mockView;
	CSVOptionsWidgetImpl widget;
	
	String fileHandleId;
	
	@Before
	public void before(){
		mockView = Mockito.mock(CSVOptionsView.class);
		mockHandler = Mockito.mock(ChangeHandler.class);
		fileHandleId = "123456";
		widget = new CSVOptionsWidgetImpl(mockView);
	}

	@Test
	public void testOptionsRoundTripOther(){
		CSVOptionsViewStub stub = new CSVOptionsViewStub();
		widget = new CSVOptionsWidgetImpl(stub);
		UploadToTablePreviewRequest inRequest = new UploadToTablePreviewRequest();
		CsvTableDescriptor csvTableDescriptor = new CsvTableDescriptor();
		csvTableDescriptor.setSeparator("p");
		csvTableDescriptor.setIsFirstLineHeader(true);
		inRequest.setCsvTableDescriptor(csvTableDescriptor);
		inRequest.setUploadFileHandleId(fileHandleId);
		widget.configure(inRequest, mockHandler);
		UploadToTablePreviewRequest outRequest = widget.getCurrentOptions();
		assertNotNull(outRequest);
		assertEquals(inRequest, outRequest);
	}
	
	@Test
	public void testOptionsRoundTripComma(){
		CSVOptionsViewStub stub = new CSVOptionsViewStub();
		widget = new CSVOptionsWidgetImpl(stub);
		UploadToTablePreviewRequest inRequest = new UploadToTablePreviewRequest();
		CsvTableDescriptor csvTableDescriptor = new CsvTableDescriptor();
		csvTableDescriptor.setIsFirstLineHeader(false);
		csvTableDescriptor.setSeparator(",");
		inRequest.setCsvTableDescriptor(csvTableDescriptor);
		inRequest.setUploadFileHandleId(fileHandleId);
		widget.configure(inRequest, mockHandler);
		UploadToTablePreviewRequest outRequest = widget.getCurrentOptions();
		assertNotNull(outRequest);
		assertEquals(inRequest, outRequest);
	}
}

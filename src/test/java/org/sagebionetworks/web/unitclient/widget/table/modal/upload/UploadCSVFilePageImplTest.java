package org.sagebionetworks.web.unitclient.widget.table.modal.upload;


import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFilePageImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPage;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;

public class UploadCSVFilePageImplTest {
	
	FileHandleUploadWidget mockFileInputWidget;
	ModalPresenter mockPresenter;
	UploadCSVPreviewPage mockNextPage;
	UploadCSVFilePageImpl page;
	String parentId;
	String tableId;
	
	@Before
	public void before(){
		mockFileInputWidget = Mockito.mock(FileHandleUploadWidget.class);
		mockPresenter = Mockito.mock(ModalPresenter.class);
		mockNextPage = Mockito.mock(UploadCSVPreviewPage.class);
		page = new UploadCSVFilePageImpl(mockFileInputWidget, mockNextPage);
		parentId = "syn456";
		tableId = "syn987";
		page.configure(parentId, tableId);
	}

	@Test
	public void testSetModalPresenter(){
		page.setModalPresenter(mockPresenter);
		verify(mockPresenter).setPrimaryButtonText(UploadCSVFilePageImpl.NEXT);
		verify(mockPresenter).setInstructionMessage(UploadCSVFilePageImpl.CHOOSE_A_CSV_OR_TSV_FILE);
		verify(mockFileInputWidget).reset();
	}

	
//	@Test
//	public void testOnPrimaryFailed(){
//		// For this case we need a stub
//		FileInputWidgetStub stub = new FileInputWidgetStub();
//		stub.setErrorString("something went wrong");
//		stub.setMetadata(new FileMetadata[]{new FileMetadata("testing.csv", ContentTypeDelimiter.CSV.getContentType())});
//		page = new UploadCSVFilePageImpl(stub, mockNextPage);
//		page.setModalPresenter(mockPresenter);
//		page.onPrimary();
//		verify(mockPresenter).setErrorMessage(stub.getErrorString());
//		verify(mockPresenter, never()).setNextActivePage(any(ModalPage.class));
//	}
	
//	@Test
//	public void testOnPrimarySuccess(){
//		// For this case we need a stub
//		String fileName = "testing.csv";
//		String fileHandleId = "123";
//		FileInputWidgetStub stub = new FileInputWidgetStub();
//		stub.setFileHandle(fileHandleId);
//		stub.setMetadata(new FileMetadata[]{new FileMetadata(fileName, ContentTypeDelimiter.CSV.getContentType())});
//		page = new UploadCSVFilePageImpl(stub, mockNextPage);
//		page.configure(parentId, tableId);
//		page.setModalPresenter(mockPresenter);
//		page.onPrimary();
//		verify(mockPresenter, never()).setErrorMessage(anyString());
//		verify(mockNextPage).configure(ContentTypeDelimiter.CSV, fileName, parentId, fileHandleId, tableId);
//		verify(mockPresenter).setNextActivePage(mockNextPage);
//	}
}

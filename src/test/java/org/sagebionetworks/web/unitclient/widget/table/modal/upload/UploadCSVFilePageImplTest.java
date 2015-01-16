package org.sagebionetworks.web.unitclient.widget.table.modal.upload;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.modal.upload.ContentTypeDelimiter;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPage;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFilePageImpl;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;

public class UploadCSVFilePageImplTest {
	
	FileInputWidget mockFileInputWidget;
	ModalPresenter mockPresenter;
	UploadCSVPreviewPage mockNextPage;
	UploadCSVFilePageImpl page;
	String parentId;
	String tableId;
	
	@Before
	public void before(){
		mockFileInputWidget = Mockito.mock(FileInputWidget.class);
		mockPresenter = Mockito.mock(ModalPresenter.class);
		mockNextPage = Mockito.mock(UploadCSVPreviewPage.class);
		page = new UploadCSVFilePageImpl(mockFileInputWidget, mockNextPage);
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(new FileMetadata[]{new FileMetadata("testing.csv", ContentTypeDelimiter.CSV.getContentType())});
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
	
	@Test
	public void testValidateSelecedFileNulls(){
		page.setModalPresenter(mockPresenter);
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(null);
		boolean isValid = page.validateSelecedFile();
		assertFalse(isValid);
		verify(mockPresenter).setErrorMessage(UploadCSVFilePageImpl.PLEASE_SELECT_A_CSV_OR_TSV_FILE_TO_UPLOAD);
	}
	
	@Test
	public void testValidateSelecedFileEmpty(){
		page.setModalPresenter(mockPresenter);
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(new FileMetadata[0]);
		boolean isValid = page.validateSelecedFile();
		assertFalse(isValid);
		verify(mockPresenter).setErrorMessage(UploadCSVFilePageImpl.PLEASE_SELECT_A_CSV_OR_TSV_FILE_TO_UPLOAD);
	}
	
	@Test
	public void testValidateSelecedFileNotCSV(){
		page.setModalPresenter(mockPresenter);
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(new FileMetadata[]{new FileMetadata("a name", "application/binary")});
		assertTrue(page.validateSelecedFile());
	}
	
	@Test
	public void testValidateSelecedCSV(){
		page.setModalPresenter(mockPresenter);
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(new FileMetadata[]{new FileMetadata("testing.csv", ContentTypeDelimiter.CSV.getContentType())});
		boolean isValid = page.validateSelecedFile();
		assertTrue(isValid);
	}
	@Test
	public void testValidateSelecedTab(){
		page.setModalPresenter(mockPresenter);
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(new FileMetadata[]{new FileMetadata("testing.tab", ContentTypeDelimiter.TSV.getContentType())});
		boolean isValid = page.validateSelecedFile();
		assertTrue(isValid);
	}
	@Test
	public void testValidateSelecedPlainText(){
		page.setModalPresenter(mockPresenter);
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(new FileMetadata[]{new FileMetadata("testing.tab", ContentTypeDelimiter.TEXT.getContentType())});
		boolean isValid = page.validateSelecedFile();
		assertTrue(isValid);
	}
	
	@Test
	public void testOnPrimaryFailed(){
		// For this case we need a stub
		FileInputWidgetStub stub = new FileInputWidgetStub();
		stub.setErrorString("something went wrong");
		stub.setMetadata(new FileMetadata[]{new FileMetadata("testing.csv", ContentTypeDelimiter.CSV.getContentType())});
		page = new UploadCSVFilePageImpl(stub, mockNextPage);
		page.setModalPresenter(mockPresenter);
		page.onPrimary();
		verify(mockPresenter).setErrorMessage(stub.getErrorString());
		verify(mockPresenter, never()).setNextActivePage(any(ModalPage.class));
	}
	
	@Test
	public void testOnPrimarySuccess(){
		// For this case we need a stub
		String fileName = "testing.csv";
		String fileHandleId = "123";
		FileInputWidgetStub stub = new FileInputWidgetStub();
		stub.setFileHandle(fileHandleId);
		stub.setMetadata(new FileMetadata[]{new FileMetadata(fileName, ContentTypeDelimiter.CSV.getContentType())});
		page = new UploadCSVFilePageImpl(stub, mockNextPage);
		page.configure(parentId, tableId);
		page.setModalPresenter(mockPresenter);
		page.onPrimary();
		verify(mockPresenter, never()).setErrorMessage(anyString());
		verify(mockNextPage).configure(ContentTypeDelimiter.CSV, fileName, parentId, fileHandleId, tableId);
		verify(mockPresenter).setNextActivePage(mockNextPage);
	}
}

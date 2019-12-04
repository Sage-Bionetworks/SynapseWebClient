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
	public void before() {
		mockFileInputWidget = Mockito.mock(FileHandleUploadWidget.class);
		mockPresenter = Mockito.mock(ModalPresenter.class);
		mockNextPage = Mockito.mock(UploadCSVPreviewPage.class);
		page = new UploadCSVFilePageImpl(mockFileInputWidget, mockNextPage);
		parentId = "syn456";
		tableId = "syn987";
		page.configure(parentId, tableId);
	}

	@Test
	public void testSetModalPresenter() {
		page.setModalPresenter(mockPresenter);
		verify(mockPresenter).setPrimaryButtonText(UploadCSVFilePageImpl.NEXT);
		verify(mockPresenter).setInstructionMessage(UploadCSVFilePageImpl.CHOOSE_A_CSV_OR_TSV_FILE);
		verify(mockFileInputWidget).reset();
	}

}

package org.sagebionetworks.web.unitclient.widget.table.modal.upload;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.widget.table.TableCreatedHandler;
import org.sagebionetworks.web.client.widget.table.modal.upload.ModalPage;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFilePage;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadTableModalView;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadTableModalWidgetImpl;

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
		mockUploadCSVFileWidget = Mockito.mock(UploadCSVFilePage.class);
		parentId = "syn123";
		widget = new UploadTableModalWidgetImpl(mockView, mockUploadCSVFileWidget);
		widget.configure(parentId, mockHandler);
	}

	@Test
	public void testConfigure(){
		verify(mockUploadCSVFileWidget).configure(parentId);
	}
	
	@Test
	public void testShowModal(){
		widget.showModal();
		verify(mockUploadCSVFileWidget).setModalPresenter(widget);
		verify(mockView).showModal();
	}
	
	@Test
	public void testCancel(){
		widget.onCancel();
		verify(mockView).hideModal();
	}
	
	@Test
	public void testSetNextActive(){
		ModalPage mockPageTwo = Mockito.mock(ModalPage.class);
		// make a new page active
		widget.setNextActivePage(mockPageTwo);
		verify(mockPageTwo).setModalPresenter(widget);
		verify(mockView).setBody(mockPageTwo);
		verify(mockView).setLoading(false);
		verify(mockView).showAlert(false);
	}
	
	@Test
	public void testOnPrimary(){
		widget.showModal();
		widget.onPrimary();
		verify(mockUploadCSVFileWidget).onPrimary();
		// change active page
		ModalPage mockPageTwo = Mockito.mock(ModalPage.class);
		widget.setNextActivePage(mockPageTwo);
		widget.onPrimary();
		verify(mockPageTwo).onPrimary();
	}
	
	@Test
	public void testShowError(){
		String anError = "an error";
		widget.setErrorMessage(anError);
		verify(mockView).showAlert(true);
		verify(mockView).showErrorMessage(anError);
		verify(mockView).setLoading(false);
	}
	
	@Test
	public void testTableCreated(){
		TableEntity mockEntity = Mockito.mock(TableEntity.class);
		widget.onTableCreated(mockEntity);
		verify(mockHandler).tableCreated(mockEntity);
		verify(mockView).hideModal();
	}
}

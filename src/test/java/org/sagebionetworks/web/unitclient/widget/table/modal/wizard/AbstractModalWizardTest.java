package org.sagebionetworks.web.unitclient.widget.table.modal.wizard;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.widget.table.TableCreatedHandler;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFilePage;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardView;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;

/**
 * 
 * @author jhill
 *
 */
public class AbstractModalWizardTest {
	
	UploadCSVFilePage mockUploadCSVFileWidget;
	ModalWizardView mockView;
	WizardCallback mockWizardCallback;
	TestModalWizardWidget widget;
	String parentId;
	TableCreatedHandler mockHandler; 
	
	@Before
	public void before(){
		mockView = Mockito.mock(ModalWizardView.class);
		mockHandler = Mockito.mock(TableCreatedHandler.class);
		mockUploadCSVFileWidget = Mockito.mock(UploadCSVFilePage.class);
		mockWizardCallback = Mockito.mock(WizardCallback.class);
		parentId = "syn123";
		widget = new TestModalWizardWidget(mockView, mockUploadCSVFileWidget);
	}

	@Test
	public void testConfigure(){
		verify(mockUploadCSVFileWidget).configure(parentId);
	}
	
	@Test
	public void testShowModal(){
		widget.showModal(mockWizardCallback);
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
		widget.showModal(mockWizardCallback);
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
		widget.onFinished();
		verify(mockHandler).tableCreated();
		verify(mockView).hideModal();
	}
}

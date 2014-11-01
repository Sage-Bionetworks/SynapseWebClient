package org.sagebionetworks.web.unitclient.widget.table.modal.wizard;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFilePage;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardView;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidgetImpl;

/**
 * 
 * @author jhill
 *
 */
public class ModalWizardWidgetImplTest {
	
	UploadCSVFilePage mockUploadCSVFileWidget;
	ModalWizardView mockView;
	WizardCallback mockWizardCallback;
	ModalWizardWidgetImpl widget;

	@Before
	public void before(){
		mockView = Mockito.mock(ModalWizardView.class);
		mockUploadCSVFileWidget = Mockito.mock(UploadCSVFilePage.class);
		mockWizardCallback = Mockito.mock(WizardCallback.class);
		widget = new ModalWizardWidgetImpl(mockView);
		widget.configure(mockUploadCSVFileWidget);
	}
	
	@Test
	public void testShowModal(){
		widget.showModal(mockWizardCallback);
		verify(mockUploadCSVFileWidget).setModalPresenter(widget);
		verify(mockView).showModal();
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
	public void testFinished(){
		widget.showModal(mockWizardCallback);
		widget.onFinished();
		verify(mockWizardCallback).onFinished();
		verify(mockView).hideModal();
	}
	
	@Test
	public void testCancled(){
		widget.showModal(mockWizardCallback);
		widget.onCancel();
		verify(mockWizardCallback).onCanceled();
		verify(mockView).hideModal();
	}
}

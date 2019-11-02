package org.sagebionetworks.web.unitclient.widget.table.modal.wizard;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
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
@RunWith(MockitoJUnitRunner.class)
public class ModalWizardWidgetImplTest {

	@Mock
	UploadCSVFilePage mockUploadCSVFileWidget;
	@Mock
	ModalWizardView mockView;
	@Mock
	WizardCallback mockWizardCallback;
	@Mock
	SynapseAlert mockSynAlert;
	ModalWizardWidgetImpl widget;

	@Before
	public void before() {
		widget = new ModalWizardWidgetImpl(mockView, mockSynAlert);
		widget.configure(mockUploadCSVFileWidget);
	}

	@Test
	public void testShowModal() {
		widget.showModal(mockWizardCallback);
		verify(mockUploadCSVFileWidget).setModalPresenter(widget);
		verify(mockView).showModal();
		assertEquals(1, widget.getCallbacks().size());
		assertEquals(mockWizardCallback, widget.getCallbacks().get(0));

		// verify callbacks are cleared when showModal is called
		widget.showModal(mockWizardCallback);
		assertEquals(1, widget.getCallbacks().size());
		assertEquals(mockWizardCallback, widget.getCallbacks().get(0));
	}

	@Test
	public void testSetNextActive() {
		ModalPage mockPageTwo = Mockito.mock(ModalPage.class);
		// make a new page active
		widget.setNextActivePage(mockPageTwo);
		verify(mockPageTwo).setModalPresenter(widget);
		verify(mockView).setBody(mockPageTwo);
		verify(mockView).setLoading(false);
		verify(mockSynAlert).clear();
	}

	@Test
	public void testOnPrimary() {
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
	public void testShowError() {
		String anError = "an error";
		widget.setErrorMessage(anError);
		verify(mockSynAlert).showError(anError);
		verify(mockView).setLoading(false);
	}

	@Test
	public void testFinished() {
		widget.showModal(mockWizardCallback);
		widget.onFinished();
		verify(mockWizardCallback).onFinished();
		verify(mockView).hideModal();
	}

	@Test
	public void testCancled() {
		widget.showModal(mockWizardCallback);
		widget.onCancel();
		verify(mockWizardCallback).onCanceled();
		verify(mockView).hideModal();
	}
}

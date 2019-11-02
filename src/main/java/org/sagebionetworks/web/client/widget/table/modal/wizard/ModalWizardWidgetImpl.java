package org.sagebionetworks.web.client.widget.table.modal.wizard;

import java.util.ArrayList;
import java.util.List;
import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * The main modal dialog of a wizard.
 * 
 * @author jhill
 *
 */
public class ModalWizardWidgetImpl implements ModalWizardWidget, ModalWizardView.Presenter, IsWidget {

	List<WizardCallback> callbacks;
	ModalPage currentPage;
	ModalPage firstPage;
	ModalWizardView view;
	SynapseAlert synAlert;

	@Inject
	public ModalWizardWidgetImpl(ModalWizardView view, SynapseAlert synAlert) {
		this.view = view;
		this.synAlert = synAlert;
		this.view.setPresenter(this);
		view.setSynAlert(synAlert);
		callbacks = new ArrayList<WizardCallback>();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onPrimary() {
		// pass this to the current page
		this.currentPage.onPrimary();
	}

	@Override
	public void onCancel() {
		if (!callbacks.isEmpty()) {
			for (WizardCallback callback : callbacks) {
				callback.onCanceled();
			}
		}
		this.view.hideModal();
	}

	@Override
	public void setNextActivePage(ModalPage next) {
		this.currentPage = next;
		this.currentPage.setModalPresenter(this);
		// add the page to the dialog.
		this.view.setBody(this.currentPage);
		this.setLoading(false);
	}

	@Override
	public void setLoading(boolean loading) {
		synAlert.clear();
		view.setLoading(loading);
	}

	@Override
	public void setPrimaryButtonText(String text) {
		view.setPrimaryButtonText(text);
	}

	@Override
	public void setInstructionMessage(String message) {
		view.setInstructionsMessage(message);
	}

	@Override
	public void setError(Throwable error) {
		synAlert.handleException(error);
		view.setLoading(false);
	}

	@Override
	public void setErrorMessage(String message) {
		synAlert.showError(message);
		view.setLoading(false);
	}

	@Override
	public void clearErrors() {
		synAlert.clear();
	}

	@Override
	public void onFinished() {
		for (WizardCallback callback : callbacks) {
			callback.onFinished();
		}
		this.view.hideModal();
	}

	@Override
	public void setTitle(String title) {
		view.setTile(title);
	}

	@Override
	public void setModalSize(ModalSize size) {
		view.setSize(size);
	}

	@Override
	public void showModal(WizardCallback callback) {
		callbacks.clear();
		setNextActivePage(this.firstPage);
		addCallback(callback);
		this.view.showModal();
	}

	@Override
	public void addCallback(WizardCallback callback) {
		if (callback != null) {
			callbacks.add(callback);
		}
	}

	@Override
	public void configure(ModalPage firstPage) {
		this.firstPage = firstPage;
	}

	@Override
	public void setHelp(String helpMarkdown, String helpUrl) {
		view.setHelp(helpMarkdown, helpUrl);
	}

	public List<WizardCallback> getCallbacks() {
		return callbacks;
	}
}

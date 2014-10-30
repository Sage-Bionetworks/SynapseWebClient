package org.sagebionetworks.web.client.widget.sharing;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccessControlListModalWidgetViewImpl implements
		AccessControlListModalWidgetView {

	public interface Binder extends
			UiBinder<Modal, AccessControlListModalWidgetViewImpl> {
	}

	@UiField
	SimplePanel editorPanel;
	@UiField
	Button primaryButton;
	@UiField
	Button defaultButton;

	Modal modal;

	@Inject
	public AccessControlListModalWidgetViewImpl(Binder binder) {
		modal = binder.createAndBindUi(this);
	}

	@Override
	public void showDialog() {
		modal.show();
	}

	@Override
	public void setDefaultButtonText(String text) {
		defaultButton.setText(text);
	}

	@Override
	public void setPrimaryButtonVisible(boolean visible) {
		primaryButton.setVisible(visible);
	}

	@Override
	public void setPrimaryButtonEnabled(boolean enabled) {
		primaryButton.setEnabled(enabled);
	}

	@Override
	public Widget asWidget() {
		return modal;
	}

	@Override
	public void addEditor(IsWidget editor) {
		this.editorPanel.add(editor);
	}
}

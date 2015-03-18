package org.sagebionetworks.web.client.widget.entity.row;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EditAnnotationsDialogImpl extends Composite implements EditAnnotationsDialog{
	
	public interface Binder extends UiBinder<Widget, EditAnnotationsDialogImpl> {	}

	@UiField
	FlowPanel editorsPanel;
	@UiField
	Modal editModal;
	@UiField
	Button saveButton;
	@UiField
	Button cancelButton;
	@UiField
	Alert alert;
	Presenter presenter;
	
	@Inject
	public EditAnnotationsDialogImpl(final Binder uiBinder){
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSave();
			}
		});
	}

	@Override
	public void showEditor() {
		saveButton.state().reset();
		alert.setVisible(false);
		editModal.show();
	}

	@Override
	public void hideEditor() {
		editModal.hide();
	}

	@Override
	public void setLoading() {
		saveButton.state().loading();
	}
	
	@Override
	public void showError(String message) {
		alert.setText(message);
		alert.setVisible(true);
		// enable the save button after an error
		saveButton.state().reset();
	}

	@Override
	public void hideErrors() {
		alert.clear();
		alert.setVisible(false);
	}
	
	public void addEditor(AnnotationEditorView editor) {
		
	};
}

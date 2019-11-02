package org.sagebionetworks.web.client.widget.entity.annotation;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EditAnnotationsDialogViewImpl implements EditAnnotationsDialogView {

	public interface Binder extends UiBinder<Widget, EditAnnotationsDialogViewImpl> {
	}

	@UiField
	FlowPanel editorsPanel;
	@UiField
	Modal editModal;
	@UiField
	Button saveButton;
	@UiField
	Button cancelButton;
	@UiField
	Button addAnnotationButton;

	@UiField
	Alert alert;
	Presenter presenter;

	Widget widget;

	@Inject
	public EditAnnotationsDialogViewImpl(final Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
		saveButton.addClickHandler(event -> {
			presenter.onSave();
		});
		addAnnotationButton.addClickHandler(event -> {
			presenter.onAddNewAnnotation();
		});
		saveButton.addDomHandler(DisplayUtils.getPreventTabHandler(saveButton), KeyDownEvent.getType());
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
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

	@Override
	public void addAnnotationEditor(Widget editor) {
		editorsPanel.add(editor);
	}

	@Override
	public void removeAnnotationEditor(Widget editor) {
		editorsPanel.remove(editor);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void clearAnnotationEditors() {
		editorsPanel.clear();
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

}

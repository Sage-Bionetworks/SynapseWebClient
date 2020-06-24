package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.Modal;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;

public class EditJSONListModalViewImpl implements EditJSONListModalView {

	public interface Binder extends UiBinder<Widget, EditJSONListModalViewImpl> {
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
	Button addNewValueButton;
	@UiField
	Button pasteNewValuesButton;
	@UiField
	FlowPanel pasteNewValuesPanel;

	@UiField
	Alert alert;
	Presenter presenter;

	Widget widget;

	@Inject
	public EditJSONListModalViewImpl(final Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
		saveButton.addClickHandler(event -> {
			presenter.onSave();
		});
		pasteNewValuesButton.addClickHandler(clickEvent -> {
			presenter.onClickPasteNewValues();
		});
		saveButton.addDomHandler(DisplayUtils.getPreventTabHandler(saveButton), KeyDownEvent.getType());
		cancelButton.addClickHandler(clickEvent -> hideEditor());
		addNewValueButton.addClickHandler(clickEvent -> presenter.onAddNewEmptyValue());
	}


	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void showError(String message) {
		alert.setText(message);
		alert.setVisible(true);
		// enable the save button after an error
		saveButton.state().reset();
	}

	@Override
	public void clearEditors(){
		editorsPanel.clear();
	}

	@Override
	public void addCommaSeparatedValuesParser(Widget commaSeparatedValuesParser){
		pasteNewValuesPanel.add(commaSeparatedValuesParser);
	}

	@Override
	public void removeCommaSeparatedValuesParser(Widget commaSeparatedValuesParser){
		pasteNewValuesPanel.remove(commaSeparatedValuesParser);
	}

	@Override
	public void showEditor() {
		saveButton.state().reset();
		alert.setVisible(false);
		editModal.show();
	}

	@Override
	public void hideEditor(){
		editModal.hide();
	}

	@Override
	public void addNewEditor(final CellEditor editor) {
		InputGroup editorWithDeleteButton = CellFactory.appendDeleteButton(editor, presenter::onValueDeleted);
		editorsPanel.add(editorWithDeleteButton.asWidget());
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

}

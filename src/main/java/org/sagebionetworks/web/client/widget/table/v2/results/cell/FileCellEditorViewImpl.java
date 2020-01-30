package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * View with zero business logic.
 * 
 * @author jhill
 *
 */
public class FileCellEditorViewImpl implements FileCellEditorView {

	public interface Binder extends UiBinder<Widget, FileCellEditorViewImpl> {
	}

	@UiField
	FormGroup formGroup;
	@UiField
	HelpBlock helpBlock;
	@UiField
	TextBox idTextBox;
	@UiField
	Button showUploadModalButton;
	@UiField
	Collapse collapse;
	@UiField
	SimplePanel fileInputWidgetPanel;
	@UiField
	Alert uploadAlert;

	Widget widget;

	@Inject
	public FileCellEditorViewImpl(Binder binder) {
		this.widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setValue(String value) {
		idTextBox.setText(value);
	}

	@Override
	public String getValue() {
		return idTextBox.getText();
	}

	@Override
	public int getTabIndex() {
		return showUploadModalButton.getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		showUploadModalButton.setAccessKey(key);
	}

	@Override
	public void setFocus(boolean focused) {
		showUploadModalButton.setFocus(focused);
	}

	@Override
	public void setTabIndex(int index) {
		showUploadModalButton.setTabIndex(index);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		showUploadModalButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onToggleCollapse();
			}
		});
	}

	@Override
	public void showCollapse() {
		collapse.show();
	}

	@Override
	public void hideCollapse() {
		collapse.hide();
	}

	@Override
	public void addFileInputWidget(IsWidget fileInputWidget) {
		fileInputWidgetPanel.add(fileInputWidget);
	}

	@Override
	public void showErrorMessage(String message) {
		uploadAlert.setVisible(true);
		uploadAlert.setText(message);
	}

	@Override
	public void hideErrorMessage() {
		uploadAlert.setVisible(false);
	}

	@Override
	public void setValueError(String help) {
		this.formGroup.setValidationState(ValidationState.ERROR);
		this.helpBlock.setText(help);
		helpBlock.setVisible(help != null && !help.trim().isEmpty());
	}

	@Override
	public void clearValueError() {
		this.formGroup.setValidationState(ValidationState.NONE);
		this.helpBlock.setText("");
		helpBlock.setVisible(false);
	}

	@Override
	public void toggleCollapse() {
		this.collapse.toggle();
	}

}

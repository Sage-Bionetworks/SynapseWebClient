package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class NumberCellEditorViewImpl implements NumberCellEditorView {

	public interface Binder extends UiBinder<Widget, NumberCellEditorViewImpl> {
	}

	@UiField
	FormGroup formGroup;
	@UiField
	TextBox textBox;
	@UiField
	HelpBlock helpBlock;

	Widget widget;

	@Inject
	public NumberCellEditorViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		// users want us to select all on focus see SWC-2213
		textBox.addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						textBox.selectAll();
					}
				});
			}
		});
	}

	@Override
	public Widget asWidget() {
		return formGroup;
	}

	@Override
	public void setValue(String value) {
		textBox.setValue(value);
	}

	@Override
	public String getValue() {
		return textBox.getValue();
	}

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return textBox.addKeyDownHandler(handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		textBox.fireEvent(event);
	}

	@Override
	public int getTabIndex() {
		return textBox.getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		textBox.setAccessKey(key);
	}

	@Override
	public void setFocus(boolean focused) {
		textBox.setFocus(focused);
	}

	@Override
	public void setTabIndex(int index) {
		textBox.setTabIndex(index);
	}

	@Override
	public void setValidationState(ValidationState state) {
		this.formGroup.setValidationState(state);
	}

	@Override
	public void setHelpText(String help) {
		this.helpBlock.setText(help);
		helpBlock.setVisible(help != null && !help.trim().isEmpty());
	}

	@Override
	public void setPlaceholder(String placeholder) {
		textBox.setPlaceholder(placeholder);
	}

	@Override
	public String getPlaceholder() {
		return textBox.getPlaceholder();
	}

}

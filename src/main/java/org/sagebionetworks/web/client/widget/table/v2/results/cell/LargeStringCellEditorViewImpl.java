package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.TextArea;
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

/**
 * Large string text cell editor view
 *
 */
public class LargeStringCellEditorViewImpl implements LargeStringCellEditorView {

	public interface Binder extends UiBinder<Widget, LargeStringCellEditorViewImpl> {
	}

	@UiField
	FormGroup formGroup;
	@UiField
	TextArea textArea;
	@UiField
	HelpBlock helpBlock;

	Widget widget;

	@Inject
	public LargeStringCellEditorViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		// users want us to select all on focus see SWC-2213
		textArea.addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						textArea.selectAll();
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
		textArea.setValue(value);
	}

	@Override
	public String getValue() {
		return textArea.getValue();
	}

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return textArea.addKeyDownHandler(handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		textArea.fireEvent(event);
	}

	@Override
	public int getTabIndex() {
		return textArea.getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		textArea.setAccessKey(key);
	}

	@Override
	public void setFocus(boolean focused) {
		textArea.setFocus(focused);
	}

	@Override
	public void setTabIndex(int index) {
		textArea.setTabIndex(index);
	}

	@Override
	public void setValidationState(ValidationState state) {
		this.formGroup.setValidationState(state);
	}

	@Override
	public void setHelpText(String help) {
		helpBlock.setVisible(help != null && !help.trim().isEmpty());
		this.helpBlock.setText(help);
	}

	@Override
	public void setPlaceholder(String placeholder) {
		textArea.setPlaceholder(placeholder);
	}

	@Override
	public String getPlaceholder() {
		return textArea.getPlaceholder();
	}

}

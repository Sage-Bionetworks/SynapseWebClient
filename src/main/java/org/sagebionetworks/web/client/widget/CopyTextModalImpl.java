package org.sagebionetworks.web.client.widget;

import static org.sagebionetworks.web.client.DisplayUtils.TEXTBOX_SELECT_ALL_FIELD_CLICKHANDLER;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CopyTextModalImpl implements CopyTextModal {
	@UiField
	TextBox textBox;
	@UiField
	Button closeButton;
	@UiField
	Modal modal;
	Widget widget;

	public interface Binder extends UiBinder<Widget, CopyTextModalImpl> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@Inject
	public CopyTextModalImpl(final SynapseJSNIUtils jsniUtils) {
		widget = uiBinder.createAndBindUi(this);
		closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				modal.hide();
			}
		});
		textBox.addClickHandler(TEXTBOX_SELECT_ALL_FIELD_CLICKHANDLER);
	}

	public void setTitle(String title) {
		modal.setTitle(title);
	}

	public void setText(String text) {
		textBox.setText(text);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void show() {
		modal.show();
	}
}

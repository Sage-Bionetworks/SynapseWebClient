package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * View with zero business logic.
 * 
 * @author jhill
 *
 */
public class FileCellEditorViewImpl implements FileCellEditorView {
	
	public interface Binder extends UiBinder<Widget, FileCellEditorViewImpl> {}
	
	@UiField
	Text idText;
	@UiField
	Button uploadButton;
	@UiField
	Modal modal;
	
	Widget widget;
	
	@Inject
	public FileCellEditorViewImpl(Binder binder){
		this.widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setValue(String value) {
		idText.setText(value);
	}

	@Override
	public String getValue() {
		return idText.getText();
	}

	@Override
	public int getTabIndex() {
		return uploadButton.getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		uploadButton.setAccessKey(key);		
	}

	@Override
	public void setFocus(boolean focused) {
		uploadButton.setFocus(focused);
	}

	@Override
	public void setTabIndex(int index) {
		uploadButton.setTabIndex(index);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		uploadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onUpload();
			}
		});
	}

	@Override
	public void showModal() {
		modal.show();
	}

}

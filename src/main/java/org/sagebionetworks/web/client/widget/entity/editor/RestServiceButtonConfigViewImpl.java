package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RestServiceButtonConfigViewImpl implements RestServiceButtonConfigView {
	public interface RestServiceButtonConfigViewImplUiBinder extends UiBinder<Widget, RestServiceButtonConfigViewImpl> {}
	private Widget widget;
	private Presenter presenter;
	@UiField
	TextBox uriField;
	@UiField
	ListBox methodListBox;
	@UiField
	ListBox buttonTypeListBox;
	@UiField
	TextBox	buttonTextField;
	@UiField
	TextArea requestJsonField;
	
	@Inject
	public RestServiceButtonConfigViewImpl(RestServiceButtonConfigViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public void initView() {
		uriField.setValue("");
		buttonTextField.setValue("");
		requestJsonField.setValue("");
		methodListBox.setSelectedIndex(0);
		buttonTypeListBox.setSelectedIndex(0);
	}
	
	@Override
	public void checkParams() throws IllegalArgumentException {
	}
	
	
	@Override
	public Widget asWidget() {
		return widget;
	}	
	
	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
	}

	@Override
	public String getURI() {
		return uriField.getValue();
	}
	@Override
	public void setUri(String uri) {
		uriField.setValue(uri);
	}

	@Override
	public String getMethod() {
		return methodListBox.getSelectedItemText();
	}


	@Override
	public String getRequestJson() {
		return requestJsonField.getText();
	}
	@Override
	public void setRequestJson(String requestJson) {
		requestJsonField.setText(requestJson);
	}

	@Override
	public String getButtonText() {
		return buttonTextField.getText();
	}
	@Override
	public void setButtonText(String buttonText) {
		buttonTextField.setText(buttonText);
	}

	@Override
	public String getButtonType() {
		return buttonTypeListBox.getSelectedItemText();
	}

	
	/*
	 * Private Methods
	 */

}

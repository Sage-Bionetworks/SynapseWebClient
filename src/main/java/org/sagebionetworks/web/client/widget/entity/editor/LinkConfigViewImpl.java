package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.presenter.LoginPresenter;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LinkConfigViewImpl implements LinkConfigView {
	public interface LinkConfigViewImplUiBinder extends UiBinder<Widget, LinkConfigViewImpl> {}
	private Presenter presenter;
	@UiField
	TextBox urlField;
	@UiField
	TextBox nameField;
	
	private Widget widget;
	
	@Inject
	public LinkConfigViewImpl(LinkConfigViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public void initView() {
	}

	@Override
	public void checkParams() throws IllegalArgumentException {
		if (!LoginPresenter.isValidUrl(urlField.getValue(), false))
			throw new IllegalArgumentException("Invalid URL: " + urlField.getValue());
		if (!LoginPresenter.isValidWidgetName(nameField.getValue()))
			throw new IllegalArgumentException("Invalid name: " + nameField.getValue());
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
		if (nameField != null)
			nameField.setValue("");
		if (urlField != null)
			urlField.setValue("");
	}

	@Override
	public String getLinkUrl() {
		return urlField.getValue();
	}
	
	@Override
	public void setLinkUrl(String url) {
		urlField.setValue(url);
	}
	
	@Override
	public String getName() {
		return nameField.getValue();
	}
	@Override
	public void setName(String name) {
		nameField.setValue(name);
	}
	
	/*
	 * Private Methods
	 */

}

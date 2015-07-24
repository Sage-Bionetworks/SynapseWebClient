package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.ValidationUtils;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class IFrameConfigViewImpl implements IFrameConfigView {
	public interface IFrameConfigViewImplUiBinder extends UiBinder<Widget, IFrameConfigViewImpl> {}
	private Widget widget;
	@UiField
	TextBox urlField;
	
	@Inject
	public IFrameConfigViewImpl(IFrameConfigViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public void initView() {
		urlField.setValue("");
	}
	@Override
	public void checkParams() throws IllegalArgumentException {
		String url = getVideoUrl();
		if (!ValidationUtils.isValidUrl(url, false))
			throw new IllegalArgumentException("Invalid URL: " + url);
	}
	@Override
	public String getVideoUrl() {
		return urlField.getValue();
	}
	
	@Override
	public void setVideoUrl(String url) {
		urlField.setValue(url);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
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
	
	/*
	 * Private Methods
	 */

}

package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cache.ClientCache;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ExternalImageConfigViewImpl implements ExternalImageConfigView {
	public interface ExternalImageConfigViewImplUiBinder extends UiBinder<Widget, ExternalImageConfigViewImpl> {}
	private Widget widget;
	private Presenter presenter;
	ClientCache clientCache;
	SynapseJSNIUtils synapseJSNIUtils;

	@UiField
	TextBox nameField;
	@UiField
	TextBox urlField;

	@Inject
	public ExternalImageConfigViewImpl(
			ExternalImageConfigViewImplUiBinder binder,
			ClientCache clientCache, SynapseJSNIUtils synapseJSNIUtils
			) {
		widget = binder.createAndBindUi(this);
		this.clientCache = clientCache;
		this.synapseJSNIUtils = synapseJSNIUtils;
	}

	@Override
	public void initView() {
		urlField.setValue("");
		nameField.setValue("");
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
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public String getImageUrl() {
		return urlField.getValue();
	}

	@Override
	public String getAltText() {
		return nameField.getValue();
	}

	@Override
	public void setImageUrl(String url) {
		urlField.setValue(url);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void clear() {
	}
}

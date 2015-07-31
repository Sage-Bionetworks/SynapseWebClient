package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.ValidationUtils;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidget;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ShinySiteConfigViewImpl implements ShinySiteConfigView {
	public interface ShinySiteConfigViewImplUiBinder extends UiBinder<Widget, ShinySiteConfigViewImpl> {}
	
	private Presenter presenter;
	@UiField
	public TextBox urlField;
	@UiField
	public TextBox heightField;
	
	Widget widget;
	
	@Inject
	public ShinySiteConfigViewImpl(ShinySiteConfigViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public void initView() {
	}
	
	@Override
	public void checkParams() throws IllegalArgumentException {
		if (!ValidationUtils.isValidUrl(urlField.getValue(), false))
			throw new IllegalArgumentException(DisplayConstants.INVALID_URL_MESSAGE);
		if(!ShinySiteWidget.isValidShinySite(urlField.getValue()))
			throw new IllegalArgumentException(urlField.getValue() + DisplayConstants.INVALID_SHINY_SITE);
	}
	@Override
	public String getSiteUrl() {
		return urlField.getValue();
	}

	@Override
	public Integer getSiteHeight() {		
		String value = heightField.getValue();
		Integer height = null;
		if(value != null) {
			try {
				height = Integer.parseInt(value);
			} catch(NumberFormatException e) {
			}
		}		
		return height;
	}
	
	@Override
	public void configure(String url, int height) {
		urlField.setValue(url);
		heightField.setValue(String.valueOf(height));
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

	
	/*
	 * Private Methods
	 */

}

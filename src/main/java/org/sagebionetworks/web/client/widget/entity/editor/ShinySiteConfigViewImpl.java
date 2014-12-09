package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ShinySiteConfigViewImpl extends LayoutContainer implements ShinySiteConfigView {

	private Presenter presenter;
	private TextField<String> urlField;
	private TextField<String> heightField;
	
	@Inject
	public ShinySiteConfigViewImpl() {
	}
	
	@Override
	public void initView() {
		//build the view
		this.setLayout(new FlowLayout());
		final FormPanel panel = new FormPanel();
		panel.setHeaderVisible(false);
		panel.setFrame(false);
		panel.setBorders(false);
		panel.setShadow(false);
		panel.setLabelAlign(LabelAlign.RIGHT);
		panel.setBodyBorder(false);
		panel.setLabelWidth(104);
				
		FormData basicFormData = new FormData();
		basicFormData.setWidth(250);
		Margins margins = new Margins(10, 10, 0, 10);
		basicFormData.setMargins(margins);

		urlField = new TextField<String>();
		urlField.setAllowBlank(false);
		urlField.setRegex(WebConstants.VALID_URL_REGEX);
		urlField.getMessages().setRegexText(DisplayConstants.INVALID_URL_MESSAGE);
		urlField.setFieldLabel(DisplayConstants.SHINYSITE_SITE_LABEL);
		panel.add(urlField, basicFormData);
		
		heightField = new TextField<String>();
		heightField.setValue(null);
		heightField.setEmptyText(WidgetConstants.SHINYSITE_DEFAULT_HEIGHT_PX + " (" + DisplayConstants.DEFAULT + ")");
		heightField.setFieldLabel(DisplayConstants.DISPLAY_HEIGHT + " (px)");
		heightField.setRegex(WebConstants.VALID_POSITIVE_NUMBER_REGEX);
		heightField.getMessages().setRegexText(DisplayConstants.INVALID_NUMBER_MESSAGE);
		heightField.setAllowBlank(true);
	    panel.add(heightField, basicFormData);
		
		
		add(panel);
	}
	@Override
	public void checkParams() throws IllegalArgumentException {
		if (!urlField.isValid())
			throw new IllegalArgumentException(urlField.getErrorMessage());
		if(!ShinySiteWidget.isValidShinySite(urlField.getValue()))
			throw new IllegalArgumentException(urlField.getValue() + DisplayConstants.INVALID_SHINY_SITE);
		if(!heightField.isValid())
			throw new IllegalArgumentException(heightField.getErrorMessage());
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
		return this;
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

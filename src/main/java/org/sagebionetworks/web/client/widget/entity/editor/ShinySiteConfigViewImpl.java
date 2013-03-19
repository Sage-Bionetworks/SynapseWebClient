package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants.MarkdownWidthParam;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidget;
import org.sagebionetworks.web.shared.WebConstants;

import scala.actors.threadpool.Arrays;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ShinySiteConfigViewImpl extends LayoutContainer implements ShinySiteConfigView {

	private Presenter presenter;
	private TextField<String> urlField;
	private SimpleComboBox<String> widthCombo;
	private TextField<String> heightField;
	
	private final static String WIDE = "Wide";
	private final static String NARROW = "Narrow";
	
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
		
		widthCombo = new SimpleComboBox<String>();
		widthCombo.setFieldLabel(DisplayConstants.WIDTH);
		widthCombo.setTypeAhead(false);
		widthCombo.setEditable(false);
		widthCombo.setForceSelection(true);
		widthCombo.setTriggerAction(TriggerAction.ALL);
		widthCombo.add(WIDE);
		widthCombo.add(NARROW);
		widthCombo.setSimpleValue(WIDE);
		panel.add(widthCombo, basicFormData);
		
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
	public MarkdownWidthParam getSiteWidth() {		
		return MarkdownWidthParam.valueOf(widthCombo.getValue().getValue().toUpperCase());
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
	public void configure(String url, int width, int height) {
		urlField.setValue(url);
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
	public int getDisplayHeight() {
		return 90;
	}
	@Override
	public int getAdditionalWidth() {
		return 40;
	}
	@Override
	public void clear() {
	}

	
	/*
	 * Private Methods
	 */

}

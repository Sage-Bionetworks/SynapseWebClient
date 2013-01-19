package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseAPICallConfigViewImpl extends LayoutContainer implements SynapseAPICallConfigView {

	private Presenter presenter;
	private TextField<String> urlField, columnNamesField, friendlyColumnNamesField, renderersField;
	
	@Inject
	public SynapseAPICallConfigViewImpl() {
	}
	
	@Override
	public void initView() {
		//build the view
		HorizontalPanel hp = new HorizontalPanel();
		hp.setStyleAttribute("margin", "10px");
		hp.setVerticalAlign(VerticalAlignment.MIDDLE);
		urlField = getNewTextfield(DisplayConstants.SYNAPSE_API_CALL_URL_LABEL, hp);
		columnNamesField = getNewTextfield(DisplayConstants.SYNAPSE_API_CALL_COLUMNS_LABEL, hp);
		friendlyColumnNamesField = getNewTextfield(DisplayConstants.SYNAPSE_API_CALL_COLUMN_HEADERS_LABEL, hp);
		renderersField = getNewTextfield(DisplayConstants.SYNAPSE_API_CALL_RENDERERS_LABEL, hp);
		
		add(hp);
	}
	
	private TextField<String> getNewTextfield(String label, LayoutContainer container) {
		TextField textField = new TextField<String>();
		textField.setAllowBlank(false);
		Label labelField = new Label(label);
		labelField.setWidth(90);
		textField.setWidth(228);
		container.add(labelField);
		container.add(textField);
		return textField;
	}
	
	
	@Override
	public void checkParams() throws IllegalArgumentException {
		if (!urlField.isValid())
			throw new IllegalArgumentException(urlField.getErrorMessage());
	}
	@Override
	public String getApiUrl() {
		return urlField.getValue();
	}
	
	@Override
	public void setApiUrl(String url) {
		urlField.setValue(url);
	}
	
	@Override
	public String getFriendlyColumnNames() {
		return friendlyColumnNamesField.getValue();
	}
	@Override
	public String getColumnsToDisplay() {
		return columnNamesField.getValue();
	}
	@Override
	public String getRendererNames() {
		return renderersField.getValue();
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
		return 50;
	}
	@Override
	public int getAdditionalWidth() {
		return 0;
	}
	@Override
	public void clear() {
	}
	
	/*
	 * Private Methods
	 */

}

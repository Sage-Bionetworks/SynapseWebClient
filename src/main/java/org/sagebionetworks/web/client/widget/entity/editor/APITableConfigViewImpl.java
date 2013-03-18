package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class APITableConfigViewImpl extends LayoutContainer implements APITableConfigView {

	private Presenter presenter;
	private TextField<String> urlField, rowNumbersColumnNameField, pageSizeField, jsonResultsKeyNameField, cssStyleNameField, widthField, columnConfigsField;
	private CheckBox isPagingField, isRowVisibleField;
	private IconsImageBundle iconsImageBundle;
	
	@Inject
	public APITableConfigViewImpl(IconsImageBundle iconsImageBundle) {
		this.iconsImageBundle = iconsImageBundle;
	}
	
	@Override
	public void initView() {
		//build the view
		FlowPanel hp = new FlowPanel();
		urlField = new TextField<String>();
		isRowVisibleField = new CheckBox();
		rowNumbersColumnNameField = new TextField<String>();
		isPagingField = new CheckBox();
		pageSizeField = new TextField<String>();
		widthField = new TextField<String>();
		jsonResultsKeyNameField = new TextField<String>();
		cssStyleNameField = new TextField<String>();
		columnConfigsField = new TextField<String>();
		
		initNewField(DisplayConstants.SYNAPSE_API_CALL_URL_LABEL, urlField, hp);
		urlField.setAllowBlank(false);
		initNewField(DisplayConstants.SYNAPSE_API_CALL_SHOW_ROW_NUMBERS_COL, isRowVisibleField, hp);
		initNewField(DisplayConstants.SYNAPSE_API_CALL_ROW_NUMBERS_COL_NAME, rowNumbersColumnNameField, hp);
		
		initNewField(DisplayConstants.SYNAPSE_API_CALL_IS_PAGING, isPagingField, hp);
		initNewField(DisplayConstants.SYNAPSE_API_CALL_PAGE_SIZE, pageSizeField, hp);
		
		initNewField(DisplayConstants.SYNAPSE_API_CALL_WIDTH, widthField, hp);
		initNewField(DisplayConstants.SYNAPSE_API_CALL_JSON_REUSLTS_KEY_NAME, jsonResultsKeyNameField, hp);
		initNewField(DisplayConstants.SYNAPSE_API_CALL_CSS_STYLE_NAME, cssStyleNameField, hp);
		LayoutContainer container = initNewField(DisplayConstants.SYNAPSE_API_CALL_COL_CONFIGS_COL_NAME, columnConfigsField, hp);
		//and add the Add Column Config button
		Button addColConfigButton = new Button("Add", AbstractImagePrototype.create(iconsImageBundle.addSquare16()), new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				//pop up a new column config dialog to gather that info
			}
		});
		container.add(addColConfigButton);
		
		add(hp);
	}
	
	private LayoutContainer initNewField(String label, Field field, FlowPanel container) {
		HorizontalPanel hp= new HorizontalPanel();
		
		Label labelField = new Label(label);
		labelField.setWidth(140);
		field.setWidth(198);
		hp.add(labelField);
		hp.add(field);
		
		container.add(hp);
		return hp;
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
	public String getPageSize() {
		return pageSizeField.getValue();
	}
	
	@Override
	public String getRowNumberColumnName() {
		return rowNumbersColumnNameField.getValue();
	}
	
	@Override
	public String getTableWidth() {
		return widthField.getValue();
	}
	
	@Override
	public Boolean isPaging() {
		return isPagingField.getValue();
	}
	
	@Override
	public String getColumnConfigs() {
		return columnConfigsField.getValue();
	}
	
	@Override
	public Boolean isShowRowNumbers() {
		return isRowVisibleField.getValue();
	}
	
	@Override
	public String getJsonResultsKeyName() {
		return jsonResultsKeyNameField.getValue();
	}
	
	@Override
	public String getCssStyle() {
		return cssStyleNameField.getValue();
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
		return 230;
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

package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceConfigViewImpl extends LayoutContainer implements ProvenanceConfigView {
	private Presenter presenter;
	TextField<String> depthTextField;
	TextField<String> entityIdTextField;
	
	CheckBox showExpandCheckbox;
	@Inject
	public ProvenanceConfigViewImpl() {
	}
	
	@Override
	public void initView() {
		this.setLayout(new FlowLayout());
		final FormPanel panel = new FormPanel();
		panel.setHeaderVisible(false);
		panel.setFrame(false);
		panel.setBorders(false);
		panel.setShadow(false);
		panel.setLabelAlign(LabelAlign.RIGHT);
		panel.setBodyBorder(false);
		panel.setLabelWidth(60);
		
		FormData basicFormData = new FormData();
		basicFormData.setWidth(250);
		Margins margins = new Margins(10, 10, 0, 10);
		basicFormData.setMargins(margins);
		
		entityIdTextField = new TextField<String>();
		entityIdTextField.setEmptyText("syn12345 ");
		entityIdTextField.setFieldLabel(DisplayConstants.SYNAPSE_ID_LABEL);
		entityIdTextField.setRegex(WebConstants.VALID_ENTITY_ID_REGEX);
		entityIdTextField.getMessages().setRegexText(DisplayConstants.INVALID_SYNAPSE_ID_MESSAGE);
		entityIdTextField.setAllowBlank(false);
	    panel.add(entityIdTextField, basicFormData);
		
		depthTextField = new TextField<String>();
		depthTextField.setValue("1");
		depthTextField.setFieldLabel(DisplayConstants.DEPTH_LABEL);
//		depthTextField.setRegex(WebConstants.VALID_POSITIVE_NUMBER_REGEX);
//		depthTextField.getMessages().setRegexText(DisplayConstants.INVALID_NUMBER_MESSAGE);
//		depthTextField.setAllowBlank(false);
	    //panel.add(depthTextField, basicFormData);
		
	    showExpandCheckbox = new CheckBox();
	    showExpandCheckbox.setBoxLabel(DisplayConstants.SHOW_EXPANDED_LABEL);
	    SimplePanel wrapper = new SimplePanel(showExpandCheckbox);
	    wrapper.addStyleName("margin-left-10");
		this.add(panel);
		//this.add(wrapper);
	}
	@Override
	public void checkParams() throws IllegalArgumentException {
		if (!entityIdTextField.isValid())
			throw new IllegalArgumentException(entityIdTextField.getErrorMessage());
		if (!depthTextField.isValid())
			throw new IllegalArgumentException(depthTextField.getErrorMessage());
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
		return 70;
	}
	@Override
	public int getAdditionalWidth() {
		return 0;
	}
	@Override
	public void clear() {
	}
	@Override
	public Long getDepth() {
//		Long depth = null;
//		if (depthTextField.isValid()) {
//			depth = Long.parseLong(depthTextField.getValue());
//		}
//			
//		return depth;
		return 1l;
	}
	@Override
	public void setDepth(Long depth) {
//		depthTextField.setValue(depth.toString());
	}
	@Override
	public String getEntityId() {
		if (entityIdTextField.isValid())
			return entityIdTextField.getValue();
		else return null;
	}
	@Override
	public void setEntityId(String entityId) {
		entityIdTextField.setValue(entityId);
	}
	@Override
	public boolean isExpanded() {
//		return showExpandCheckbox.getValue();
		return true;
	}
	@Override
	public void setIsExpanded(boolean b) {
//		showExpandCheckbox.setValue(b);
	}
	
	/*
	 * Private Methods
	 */

}

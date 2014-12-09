package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceConfigViewImpl extends LayoutContainer implements ProvenanceConfigView {
	private Presenter presenter;
	TextField<String> depthTextField;
	TextField<String> entityListField;
	TextField<String> displayHeightField;
	IconsImageBundle iconsImageBundle;
	EntityFinder entityFinder;
	
	CheckBox showExpandCheckbox;
	@Inject
	public ProvenanceConfigViewImpl(IconsImageBundle iconsImageBundle, EntityFinder entityFinder) {
		this.iconsImageBundle = iconsImageBundle;
		this.entityFinder = entityFinder;
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
		panel.setLabelWidth(104);
				
		FormData basicFormData = new FormData();
		basicFormData.setWidth(250);
		Margins margins = new Margins(10, 10, 0, 10);
		basicFormData.setMargins(margins);
		
		entityListField = new TextField<String>(); 
		entityListField.setFieldLabel(DisplayConstants.ENTITY_LIST);
		entityListField.setAllowBlank(false);
		panel.add(entityListField, basicFormData);
		
		Button findEntitiesButton = new Button(DisplayConstants.FIND_ENTITIES, AbstractImagePrototype.create(iconsImageBundle.magnify16()));
		findEntitiesButton.addSelectionListener(new SelectionListener<ButtonEvent>() {			
			@Override
			public void componentSelected(ButtonEvent ce) {
				entityFinder.configure(true, new SelectedHandler<Reference>() {					
					@Override
					public void onSelected(Reference selected) {
						if(selected.getTargetId() != null) {					
							appendEntityListValue(selected);
							entityFinder.hide();
						} else {
							showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
						}
					}
				});
				entityFinder.show();
			}
		});
		AdapterField buttonField = new AdapterField(findEntitiesButton);
		buttonField.setLabelSeparator("");
		panel.add(buttonField, basicFormData);
		
		    		
		depthTextField = new TextField<String>();
		depthTextField.setValue("1");
		depthTextField.setFieldLabel(DisplayConstants.DEPTH_LABEL);
		depthTextField.setRegex(WebConstants.VALID_POSITIVE_NUMBER_REGEX);
		depthTextField.getMessages().setRegexText(DisplayConstants.INVALID_NUMBER_MESSAGE);
		depthTextField.setAllowBlank(false);
	    panel.add(depthTextField, basicFormData);
		
		displayHeightField = new TextField<String>();
		displayHeightField.setValue(null);
		displayHeightField.setEmptyText(WidgetConstants.PROV_WIDGET_HEIGHT_DEFAULT + " (" + DisplayConstants.DEFAULT + ")");
		displayHeightField.setFieldLabel(DisplayConstants.DISPLAY_HEIGHT + " (px)");
		displayHeightField.setRegex(WebConstants.VALID_POSITIVE_NUMBER_REGEX);
		displayHeightField.getMessages().setRegexText(DisplayConstants.INVALID_NUMBER_MESSAGE);
		displayHeightField.setAllowBlank(true);
	    panel.add(displayHeightField, basicFormData);
	    	    
	    showExpandCheckbox = new CheckBox();
	    showExpandCheckbox.setFieldLabel(DisplayConstants.SHOW_EXPAND);
	    showExpandCheckbox.setBoxLabel("");
	    panel.add(showExpandCheckbox, basicFormData);
	    
		this.add(panel);
	}

	@Override
	public void checkParams() throws IllegalArgumentException {
		if (entityListField.getValue() == null)
			throw new IllegalArgumentException(DisplayConstants.ERROR_ENTER_AT_LEAST_ONE_ENTITY);
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
	public void clear() {
	}
	@Override
	public Long getDepth() {
		Long depth = null;
		if (depthTextField.isValid()) {
			depth = Long.parseLong(depthTextField.getValue());
		}
			
		return depth;
	}
	@Override
	public void setDepth(Long depth) {
		depthTextField.setValue(depth.toString());
	}
	@Override
	public String getEntityList() {
		if (entityListField.getValue() != null)
			return entityListField.getValue();
		else 
			return null;
	}
	@Override
	public void setEntityList(String entityList) {
		entityListField.setValue(entityList);
	}
	@Override
	public boolean isExpanded() {
		return showExpandCheckbox.getValue();
	}
	@Override
	public void setIsExpanded(boolean b) {
		showExpandCheckbox.setValue(b);
	}

	@Override
	public void setProvDisplayHeight(int provDisplayHeight) {
		displayHeightField.setValue(Integer.toString(provDisplayHeight));
	}
	@Override
	public Integer getProvDisplayHeight() {
		return displayHeightField.getValue() != null ? Integer.parseInt(displayHeightField.getValue()) : null;		
	}
	
	
	/*
	 * Private Methods
	 */
	private void appendEntityListValue(Reference selected) {		
		String str = entityListField.getValue();
		if(str == null) str = "";
		if(!str.equals("")) 
			str += ",";
		str += DisplayUtils.createEntityVersionString(selected);
		entityListField.setValue(str);
	}

}

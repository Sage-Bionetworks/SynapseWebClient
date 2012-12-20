package org.sagebionetworks.web.client.widget.entity.dialog;

import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BaseEditWidgetDescriptorViewImpl extends Composite implements BaseEditWidgetDescriptorView {
	
	Dialog window;
	SimplePanel paramsPanel;
	SimplePanel baseContentPanel;
	String saveButtonText = "Save";
	TextField<String> nameField;
	
	private Presenter presenter;
	private WidgetEditorPresenter widgetDescriptorPresenter;
	private static final int STARTING_HEIGHT = 110;
	private static final int STARTING_WIDTH = 370;
	private WidgetRegistrar widgetRegistrar;
	
	@Inject
	public BaseEditWidgetDescriptorViewImpl(WidgetRegistrar widgetRegistrar) {
		this.widgetRegistrar = widgetRegistrar;
		
		paramsPanel = new SimplePanel();
		baseContentPanel = new SimplePanel();

		initializeBaseContentPanel();
	}
	
	private Dialog getNewDialog() {
		Dialog window = new Dialog();
		window.setMaximizable(false);
	    window.setPlain(true);  
	    window.setModal(true);  
	    window.setBlinkModal(true);  
	    //window.setLayout(new FitLayout());
	    window.okText = saveButtonText;
	    window.setButtons(Dialog.OKCANCEL);
	    window.setHideOnButtonClick(false);
	    
	    FlowPanel container = new FlowPanel();
		container.add(paramsPanel);
		container.add(new HTML("<hr style=\"margin: 0px\">"));
		container.add(baseContentPanel);
		
		window.add(container);
		
		Button saveButton = window.getButtonById(Dialog.OK);	    
	    saveButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				presenter.apply();
			}
	    });
	    Button cancelButton = window.getButtonById(Dialog.CANCEL);	    
	    cancelButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
	    });
	    
	    String width = Integer.toString(STARTING_WIDTH + widgetDescriptorPresenter.getAdditionalWidth()) + "px";
		String height = Integer.toString(STARTING_HEIGHT + widgetDescriptorPresenter.getDisplayHeight())+"px";
		container.setWidth(width);
		container.setHeight(height);
		window.setWidth(width);
		window.setHeight(height);
		
	    return window;
	}
	
	private void initializeBaseContentPanel() {
		HorizontalPanel hp = new HorizontalPanel();
		hp.setStyleAttribute("margin", "10px");
		hp.setVerticalAlign(VerticalAlignment.MIDDLE);
		nameField = new TextField<String>();
		nameField.setAllowBlank(false);
		nameField.setName("Name");
		nameField.setRegex(WebConstants.VALID_WIDGET_NAME_REGEX);
		nameField.getMessages().setRegexText(DisplayConstants.ERROR_WIDGET_NAME_PATTERN_MISMATCH);
		Label nameLabel = new Label("Name:");
		nameLabel.setWidth(50);
		nameField.setWidth(270);
		hp.add(nameLabel);
		hp.add(nameField);
		
		baseContentPanel.add(hp);
	}
	
	@Override
	public void show(String windowTitle) {
		window = getNewDialog();
		window.setHeading(windowTitle);
		window.show();
	}

	@Override
	public void hide() {
		window.hide();
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
	public void setWidgetDescriptor(String entityId, String contentTypeKey, WidgetDescriptor widgetDescriptor) {
		//clear out params panel.  Get the right params editor based on the descriptor (it's concrete class, and configure based on the parameters inside of it).
		paramsPanel.clear();
		widgetDescriptorPresenter = widgetRegistrar.getWidgetEditorForWidgetDescriptor(entityId, contentTypeKey, widgetDescriptor);
		if (widgetDescriptorPresenter != null) {
			Widget w = widgetDescriptorPresenter.asWidget();
			paramsPanel.add(w);
		}
	}
	
	@Override
	public String getTextToInsert(String name) {
		return widgetDescriptorPresenter.getTextToInsert(name);
	}
	
	@Override
	public String getName() {
		return nameField.getValue();
	}
	@Override
	public void setName(String name) {
		nameField.setValue(name);
	}
	
	@Override
	public void updateDescriptorFromView() {
		if (!nameField.isValid())
			throw new IllegalArgumentException(nameField.getErrorMessage());
		widgetDescriptorPresenter.updateDescriptorFromView();
	}
	
	
	@Override
	public void showBaseParams(boolean visible) {
		baseContentPanel.setVisible(visible);
	}
	
	@Override
	public void clear() {
		if (nameField != null)
			nameField.setValue("");
	}
	@Override
	public void setSaveButtonText(String text) {
		this.saveButtonText = text;
	}
}

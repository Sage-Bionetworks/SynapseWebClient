package org.sagebionetworks.web.client.widget.entity.dialog;

import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.WidgetDescriptorPresenter;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BaseEditWidgetDescriptorViewImpl extends Composite implements BaseEditWidgetDescriptorView {
	
	Dialog window;
	SimplePanel paramsPanel;
	SimplePanel baseContentPanel;
	
	TextField<String> nameField;
	
	private Presenter presenter;
	private WidgetDescriptorPresenter widgetDescriptorPresenter;
	private static final int STARTING_HEIGHT = 110;
	@Inject
	public BaseEditWidgetDescriptorViewImpl() {
		window = new Dialog();
		window.setMaximizable(false);
	    window.setPlain(true);  
	    window.setModal(true);  
	    window.setBlinkModal(true);  
	    //window.setLayout(new FitLayout());
	    // We want okay to say save
	    window.okText = "Save";
	    window.setButtons(Dialog.OKCANCEL);
	    window.setHideOnButtonClick(false);

		paramsPanel = new SimplePanel();
		baseContentPanel = new SimplePanel();
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.add(paramsPanel);
		verticalPanel.add(baseContentPanel);
		window.add(verticalPanel);
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
		
		initializeBaseContentPanel();
	}

	private void initializeBaseContentPanel() {
		HorizontalPanel hp = new HorizontalPanel();
		hp.setStyleAttribute("margin", "10px");
		hp.setVerticalAlign(VerticalAlignment.MIDDLE);
		nameField = new TextField<String>();
		nameField.setAllowBlank(false);
		nameField.setName("Name");
		nameField.setRegex(WebConstants.VALID_ENTITY_NAME_REGEX);
		nameField.getMessages().setRegexText(DisplayConstants.ERROR_NAME_PATTERN_MISMATCH);
		Label nameLabel = new Label("Name:");
		nameLabel.setWidth(50);
		nameField.setWidth(205);
		hp.add(nameLabel);
		hp.add(nameField);
		
		baseContentPanel.add(hp);
	}
	
	@Override
	public void show() {
		window.setHeight(STARTING_HEIGHT + widgetDescriptorPresenter.getDisplayHeight());
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
	public void setWidgetDescriptor(WidgetDescriptor widgetDescriptor) {
		//clear out params panel.  Get the right params editor based on the descriptor (it's concrete class, and configure based on the parameters inside of it).
		paramsPanel.clear();
		widgetDescriptorPresenter = WidgetDescriptorUtils.getWidgetEditorForWidgetDescriptor(widgetDescriptor);
		if (widgetDescriptorPresenter != null) {
			Widget w = widgetDescriptorPresenter.asWidget();
			paramsPanel.add(w);
			window.setHeading(WidgetDescriptorUtils.getFriendlyTypeName(widgetDescriptor.getEntityType()));
		}
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
}

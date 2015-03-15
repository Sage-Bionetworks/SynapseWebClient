package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.widget.entity.row.EntityFormModel;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.user.client.Element;
import com.google.inject.Inject;

public class EntityPropertyFormViewImpl extends FormPanel implements EntityPropertyFormView {
	Presenter presenter;
	TextField<String> nameField;
	List<Field<?>> propertyFields;
	FormFieldFactory formFactory;
	FormPanel formPanel;
	ContentPanel propPanel;
	VerticalPanel vp;
	IconsImageBundle iconsImageBundle;
	SageImageBundle sageImageBundle;
	SynapseJSNIUtils synapseJSNIUtils;
	Window loading;
	public static final int DIALOG_WIDTH = 850;
	
	@Inject
	public EntityPropertyFormViewImpl(FormFieldFactory formFactory, SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle, BaseEditWidgetDescriptorPresenter widgetDescriptorEditor,SynapseJSNIUtils synapseJSNIUtils) {
		this.formFactory = formFactory;
		this.iconsImageBundle = iconsImageBundle;
		this.sageImageBundle= sageImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
		loading = DisplayUtils.createLoadingWindow(sageImageBundle, "Updating...");
		this.setHeaderVisible(false);
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void showEditEntityDialog(String windowTitle) {
		final Dialog window = new Dialog();
		window.setMaximizable(false);
		boolean isWikiEntityEditor = DisplayUtils.isWikiSupportedType(presenter.getEntity());
		int height = isWikiEntityEditor ? 180 : 660;
		window.setSize(DIALOG_WIDTH, height);
	    window.setPlain(true);  
	    window.setModal(true);  
	    window.setHeading(windowTitle);
	    window.setLayout(new FitLayout());
	    // We want okay to say save
	    window.okText = "Save";
	    window.setButtons(Dialog.OKCANCEL);
	    window.setHideOnButtonClick(true);
	    // Create the property from
	    window.add(this.asWidget(), new FitData(0));
	    // List for the button selection
	    Button saveButton = window.getButtonById(Dialog.OK);	    
	    FormButtonBinding binding = new FormButtonBinding(this);  
	    binding.addButton(saveButton);
	    saveButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				presenter.saveButtonClicked();
			}
	    });
	    // show the window
	    window.show();	
	}
	
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		this.setLayout(new AnchorLayout());
		this.setScrollMode(Scroll.AUTO);
		this.vp = new VerticalPanel();
		this.add(vp);
		// This is the property panel
		propPanel = new ContentPanel();
		propPanel.setCollapsible(false);
		propPanel.setFrame(false);
		propPanel.setHeaderVisible(false);
		propPanel.setHeading("Properties");
		propPanel.setLayout(new AnchorLayout());
		propPanel.setWidth(DIALOG_WIDTH-30);
		// Add a place holder form panel
		formPanel = new FormPanel();
		propPanel.add(formPanel);
		
		vp.add(propPanel);
		rebuild();
	}

	@Override
	public boolean isComponentVisible(){
		return this.isVisible();
	}
	
	public void rebuild() {
		// Nothing to do if this is not being rendered.
		if (!this.isRendered())
			return;
		this.propPanel.remove(formPanel);
		
		// Build up a new form
		formPanel = createNewFormPanel();
		
		// formPanel.setSize("100%", "100%");
		// Basic form data
		Margins margins = new Margins(10, 10, 0, 10);
		FormData basicFormData = new FormData(); 
		basicFormData.setWidth(DIALOG_WIDTH-160);
		basicFormData.setMargins(margins);

		// Name is the first
		formPanel.add(nameField, basicFormData);
		
		// Add them to the form
		for (Field<?> formField : propertyFields) {
			// FormData thisData = new FormData("-100");
			formPanel.add(formField, basicFormData);
		}
		
		// Add both panels back.
		this.propPanel.add(formPanel);
		this.layout();
	}
	
	
	public void refresh() {
		//based on the presenters data, refresh the view configuration
		// The name field is just a text field that cannot be null
		EntityFormModel model = presenter.getFormModel();
		nameField = (TextField<String>) formFactory.createField(model.getName());
		nameField.setAllowBlank(false);
		nameField.setRegex(WebConstants.VALID_ENTITY_NAME_REGEX);
		nameField.getMessages().setRegexText(WebConstants.INVALID_ENTITY_NAME_MESSAGE);
		nameField.setToolTip((ToolTipConfig)null);
		
		// Create the list of fields
		propertyFields = formFactory.createFormFields(model.getProperties());
		
		rebuild(); 
	}
	
	@Override
	public void showLoading() {
		loading.show();
	}
	@Override
	public void hideLoading() {
		loading.hide();
	}
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		super.clear();
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	/**
	 * Build a new empty from panel
	 * @return
	 */
	private FormPanel createNewFormPanel(){
		FormPanel form = new FormPanel();
		form.setHeading("Simple Form");
		form.setHeaderVisible(false);
		form.setFrame(false);
		form.setBorders(false);
		form.setBodyStyleName("form-background"); 
		form.setLabelAlign(LabelAlign.RIGHT);
		return form;
	}
	
}

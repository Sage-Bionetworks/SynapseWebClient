package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.row.EntityFormModel;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
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
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.TextArea;
import com.google.inject.Inject;

public class EntityPropertyFormViewImpl extends FormPanel implements EntityPropertyFormView {
	Presenter presenter;
	TextField<String> nameField;
	TextField<String> descriptionField;
	TextArea markdownDescriptionField;
	List<Field<?>> propertyFields;
	List<Field<?>> annotationFields;
	FormFieldFactory formFactory;
	FormPanel formPanel;
	FormPanel annotationFormPanel;
	ContentPanel annoPanel;
	ContentPanel propPanel;
	VerticalPanel vp;
	IconsImageBundle iconsImageBundle;
	SageImageBundle sageImageBundle;
	SynapseJSNIUtils synapseJSNIUtils;
	Window loading;
	private MarkdownEditorWidget markdownEditorWidget;
	public static final int DIALOG_WIDTH = 850;
	
	@Inject
	public EntityPropertyFormViewImpl(FormFieldFactory formFactory, SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle, BaseEditWidgetDescriptorPresenter widgetDescriptorEditor,SynapseJSNIUtils synapseJSNIUtils, MarkdownEditorWidget markdownEditorWidget) {
		this.formFactory = formFactory;
		this.iconsImageBundle = iconsImageBundle;
		this.sageImageBundle= sageImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.markdownEditorWidget= markdownEditorWidget;
		loading = DisplayUtils.createLoadingWindow(sageImageBundle, "Updating...");
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
		int height = isWikiEntityEditor ? 400 : 660;
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
		propPanel.setCollapsible(true);
		propPanel.setFrame(false);
		propPanel.setHeading("Properties");
		propPanel.setLayout(new AnchorLayout());
		propPanel.setWidth(DIALOG_WIDTH);
		// Add a place holder form panel
		formPanel = new FormPanel();
		propPanel.add(formPanel);
		
		ToolBar toolBar = new ToolBar();
		Button addButton = new Button("Add Annotation");
		addButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.addSquare16()));
		toolBar.add(addButton);
		Button removeButton = new Button("Remove Annotation");
		removeButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.deleteButton16()));
		toolBar.add(removeButton);
		toolBar.setAlignment(HorizontalAlignment.CENTER);
		// The add button.
		addButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				presenter.addAnnotation();
			}
	    });
		// The remove annotation button.
		removeButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				presenter.removeAnnotation();
			}
	    });

		annoPanel = new ContentPanel();
		annoPanel.setCollapsible(true);
		annoPanel.setFrame(false);
		annoPanel.setHeading("Annotations");
		annoPanel.setLayout(new AnchorLayout());
		annoPanel.setWidth(DIALOG_WIDTH);
		annoPanel.setBottomComponent(toolBar);
		// Add a place holder form panel
		annotationFormPanel = new FormPanel();
		annoPanel.add(annotationFormPanel);
		
		vp.add(propPanel);
		vp.add(annoPanel);
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
		this.annoPanel.remove(annotationFormPanel);
		
		// Build up a new form
		formPanel = createNewFormPanel();
		annotationFormPanel = createNewFormPanel();
		
		// formPanel.setSize("100%", "100%");
		// Basic form data
		Margins margins = new Margins(10, 10, 0, 10);
		FormData basicFormData = new FormData(); 
		basicFormData.setWidth(DIALOG_WIDTH-160);
		basicFormData.setMargins(margins);

		// Name is the first
		formPanel.add(nameField, basicFormData);
		
		//markdown widget to be removed from entity property form
		//only reconfigure the md editor if the entity id is set
		if (DisplayUtils.isWikiSupportedType(presenter.getEntity())) {
			formPanel.add(descriptionField, basicFormData);
		}
		else {
			if (presenter.getEntity().getId() != null) {
				markdownEditorWidget.configure(new WikiPageKey(presenter.getEntity().getId(),  WidgetConstants.WIKI_OWNER_ID_ENTITY, null), markdownDescriptionField, formPanel, true, false, new WidgetDescriptorUpdatedHandler() {
					@Override
					public void onUpdate(WidgetDescriptorUpdatedEvent event) {
						presenter.refreshEntityAttachments();
					}
				}, null, null, 6);
			}
		}
		// Add them to the form
		for (Field<?> formField : propertyFields) {
			// FormData thisData = new FormData("-100");
			formPanel.add(formField, basicFormData);
		}
		
		// Add them to the form
		for (Field<?> formField : annotationFields) {
			// FormData thisData = new FormData("-100");
			annotationFormPanel.add(formField, basicFormData);
		}
		// Add both panels back.
		this.propPanel.add(formPanel);
		this.annoPanel.add(annotationFormPanel);
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
		
		if (DisplayUtils.isWikiSupportedType(presenter.getEntity())) {
			descriptionField = (TextField<String>) formFactory.createField(model.getDescription());
			descriptionField.setToolTip(DisplayConstants.ENTITY_DESCRIPTION_TOOLTIP);
		}
		else {
			markdownDescriptionField = formFactory.createTextAreaField(model.getDescription());
			markdownDescriptionField.setWidth((DIALOG_WIDTH-90)+"px");
			markdownDescriptionField.setHeight("300px");
		}

		// Create the list of fields
		propertyFields = formFactory.createFormFields(model.getProperties());
		annotationFields = formFactory.createFormFields(model.getAnnotations());
		
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

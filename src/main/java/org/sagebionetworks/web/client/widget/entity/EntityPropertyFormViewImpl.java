package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.TOOLTIP_POSITION;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.entity.row.EntityFormModel;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
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
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.inject.Inject;

public class EntityPropertyFormViewImpl extends FormPanel implements EntityPropertyFormView {
	Presenter presenter;
	TextField<String> nameField;
	TextArea descriptionField;
	List<Field<?>> propertyFields;
	List<Field<?>> annotationFields;
	FormFieldFactory formFactory;
	FormPanel formPanel;
	FormPanel annotationFormPanel;
	ContentPanel annoPanel;
	ContentPanel propPanel;
	VerticalPanel vp;
	IconsImageBundle iconsImageBundle;
	BaseEditWidgetDescriptorPresenter widgetDescriptorEditor;
	SageImageBundle sageImageBundle;
	SynapseJSNIUtils synapseJSNIUtils;
	HTML descriptionFormatInfo;
	VerticalPanel attachmentsContainer;
	Window loading;
	CookieProvider cookies;
	
	@Inject
	public EntityPropertyFormViewImpl(FormFieldFactory formFactory, SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle, BaseEditWidgetDescriptorPresenter widgetDescriptorEditor,SynapseJSNIUtils synapseJSNIUtils, Attachments attachmentsWidget, CookieProvider cookies) {
		this.formFactory = formFactory;
		this.iconsImageBundle = iconsImageBundle;
		this.sageImageBundle= sageImageBundle;
		this.widgetDescriptorEditor = widgetDescriptorEditor;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.cookies = cookies;
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
	    window.setSize(880, 660);
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
		int width = 850;
		// This is the property panel
		propPanel = new ContentPanel();
		propPanel.setCollapsible(true);
		propPanel.setFrame(false);
		propPanel.setHeading("Properties");
		propPanel.setLayout(new AnchorLayout());
		propPanel.setWidth(width);
		// Add a place holder form panel
		formPanel = new FormPanel();
		propPanel.add(formPanel);
		
		attachmentsContainer = new VerticalPanel();
		attachmentsContainer.setBorders(false);
		
		attachmentsContainer.setVisible(false);
		attachmentsContainer.setLayout(new VBoxLayout());
		attachmentsContainer.setScrollMode(Scroll.AUTOY);

		
		descriptionFormatInfo = new HTML(WebConstants.ENTITY_DESCRIPTION_FORMATTING_TIPS_HTML);
		
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
		annoPanel.setWidth(width);
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
	
	public void showFormattingGuideDialog() {
        final Dialog window = new Dialog();
        window.setMaximizable(false);
        window.setSize(550, 600);
        window.setPlain(true); 
        window.setModal(true); 

        window.setHeading(DisplayConstants.ENTITY_DESCRIPTION_TIPS_TEXT); 
        window.setButtons(Dialog.OK);
        window.setHideOnButtonClick(true);

        window.setLayout(new FitLayout());
        ScrollPanel wrapper = new ScrollPanel();
        wrapper.add(descriptionFormatInfo);
	    window.add(wrapper);
        // show the window
	    window.show();		
	}

	public void insertMarkdown(String md) {
		TextArea descriptionTextArea = descriptionField;
		String currentValue = descriptionTextArea.getValue();
		if (currentValue == null)
			currentValue = "";
		int cursorPos = descriptionTextArea.getCursorPos();
		if (cursorPos < 0)
			cursorPos = 0;
		else if (cursorPos > currentValue.length())
			cursorPos = currentValue.length();
		DisplayUtils.updateTextArea(descriptionField, currentValue.substring(0, cursorPos) + md + currentValue.substring(cursorPos));
	}
	
	@Override
	public BaseEditWidgetDescriptorPresenter getWidgetDescriptorEditor() {
		return widgetDescriptorEditor;
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
		FormData basicFormData = new FormData("-100");
		basicFormData.setMargins(margins);

		// Name is the first
		formPanel.add(nameField, basicFormData);
		
		FormData formatLinkFormData = new FormData("-5");
		
		//Toolbar
		HorizontalPanel mdCommands = new HorizontalPanel();
		mdCommands.setVerticalAlign(VerticalAlignment.MIDDLE);
		mdCommands.addStyleName("view header-inner-commands-container");
		Button insertButton = new Button("Insert", AbstractImagePrototype.create(iconsImageBundle.add16()));
		insertButton.setWidth(55);
		insertButton.setMenu(createWidgetMenu());
		FormData descriptionLabelFormData = new FormData();
		descriptionLabelFormData.setMargins(new Margins(0,15,0,17));
		formPanel.add(new Label("Description:"),descriptionLabelFormData);
		FormData mdCommandFormData = new FormData();
		mdCommandFormData.setMargins(new Margins(0,15,0,10));
		formPanel.add(mdCommands,mdCommandFormData);
		
		// followed by description.
		SimplePanel descriptionWrapper= new SimplePanel();
		descriptionWrapper.add(descriptionField);
		
		FormData descriptionData = new FormData("-5");
		//descriptionData.setHeight(310);
		descriptionData.setMargins(new Margins(0, 10, 0, 10));
        formPanel.add(descriptionWrapper, descriptionData);
		
		//Widgets Manager
		FormData widgetManagerFormData = new FormData("-5");
		widgetManagerFormData.setMargins(new Margins(10,10,0,10));
		widgetManagerFormData.setWidth(170);
		
		//Preview
		final com.google.gwt.user.client.ui.Button previewButton =  new com.google.gwt.user.client.ui.Button();
		previewButton.setText(DisplayConstants.ENTITY_DESCRIPTION_PREVIEW_BUTTON_TEXT);
		previewButton.addStyleName("btn");
		previewButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String baseUrl = GWT.getModuleBaseURL()+"attachment";
				presenter.showPreview(((TextArea)descriptionField).getValue(), baseUrl);
			}
		});
		FormData previewFormData = new FormData("-5");
		previewFormData.setMargins(new Margins(10,10,0,10));
		
		FlowPanel mdCommandsLower = new FlowPanel();
		formPanel.add(mdCommandsLower, previewFormData);
		SimplePanel previewButtonWrapper= new SimplePanel();
		previewButtonWrapper.add(previewButton);
		previewButtonWrapper.addStyleName("inline-block margin-left-725");
		mdCommandsLower.add(previewButtonWrapper);
		
		//Formatting Guide
		formatLinkFormData.setMargins(new Margins(10,10,0,10));
		
		final Button formatLink = new Button(DisplayConstants.ENTITY_DESCRIPTION_TIPS_TEXT);
		formatLink.setIcon(AbstractImagePrototype.create(iconsImageBundle.slideInfo16()));
		formatLink.setWidth(120);
		mdCommands.add(formatLink);
		mdCommands.add(insertButton);
		formatLink.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				//pop up format guide
				showFormattingGuideDialog();
			}
		});
		
		formPanel.add(attachmentsContainer, widgetManagerFormData);
		
		Image image = getNewCommand("Insert Image", iconsImageBundle.imagePlus16(),new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleInsertWidgetCommand(WidgetConstants.IMAGE_CONTENT_TYPE);
			}
		}); 
		mdCommands.add(image);
		
		Image link = getNewCommand("Insert Link", iconsImageBundle.link16(),new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleInsertWidgetCommand(WidgetConstants.LINK_CONTENT_TYPE);
			}
		}); 
		mdCommands.add(link);
		
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
	
	private Menu createWidgetMenu() {
	    Menu menu = new Menu();
	    menu.add(getNewCommand("Image", new SelectionListener<ComponentEvent>() {
	    	public void componentSelected(ComponentEvent ce) {
	    		handleInsertWidgetCommand(WidgetConstants.IMAGE_CONTENT_TYPE);
	    	};
		}));
	    menu.add(getNewCommand("Link", new SelectionListener<ComponentEvent>() {
	    	public void componentSelected(ComponentEvent ce) {
	    		handleInsertWidgetCommand(WidgetConstants.LINK_CONTENT_TYPE);
	    	};
		}));
	    menu.add(getNewCommand("YouTube Video", new SelectionListener<ComponentEvent>() {
	    	public void componentSelected(ComponentEvent ce) {
	    		handleInsertWidgetCommand(WidgetConstants.YOUTUBE_CONTENT_TYPE);	
	    	};
		}));
	    menu.add(getNewCommand("Provenance Graph", new SelectionListener<ComponentEvent>() {
	    	public void componentSelected(ComponentEvent ce) {
	    		handleInsertWidgetCommand(WidgetConstants.PROVENANCE_CONTENT_TYPE);
	    	};
		}));
	    if (DisplayUtils.isInTestWebsite(cookies)) {
		    menu.add(getNewCommand("Synapse API SuperTable", new SelectionListener<ComponentEvent>() {
		    	public void componentSelected(ComponentEvent ce) {
		    		handleInsertWidgetCommand(WidgetConstants.API_TABLE_CONTENT_TYPE);
		    	};
			}));
	    }

	    return menu;
	  }
	
	public Image getNewCommand(String tooltipText, ImageResource image, ClickHandler clickHandler){
		Image command = new Image(image);
		command.addStyleName("imageButton");
		command.addClickHandler(clickHandler);
		DisplayUtils.addTooltip(this.synapseJSNIUtils, command, tooltipText, TOOLTIP_POSITION.BOTTOM);
		return command;
	}
	public MenuItem getNewCommand(String text, SelectionListener selectionListener){
		MenuItem item = new MenuItem(text);
		item.addSelectionListener(selectionListener);
		return item;
	}
	
	public void refresh() {
		//based on the presenters data, refresh the view configuration
		// The name field is just a text field that cannot be null
		EntityFormModel model = presenter.getFormModel();
		nameField = (TextField<String>) formFactory.createField(model.getName());
		nameField.setAllowBlank(false);
		nameField.setRegex(WebConstants.VALID_ENTITY_NAME_REGEX);
		nameField.getMessages().setRegexText(WebConstants.INVALID_ENTITY_NAME_MESSAGE);
		descriptionField = formFactory.createTextAreaField(model.getDescription());
		descriptionField.setWidth("796px");
		descriptionField.setHeight("300px");

		// Create the list of fields
		propertyFields = formFactory.createFormFields(model.getProperties());
		annotationFields = formFactory.createFormFields(model.getAnnotations());
		
		rebuild();
	}
	
	public void handleInsertWidgetCommand(String contentTypeKey){
		presenter.insertWidget(contentTypeKey);
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
	public void showPreview(String result, EntityBundle bundle, WidgetRegistrar widgetRegistrar, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator, JSONObjectAdapter jsonObjectAdapter) throws JSONObjectAdapterException {
		final Dialog window = new Dialog();
		window.setMaximizable(false);
	    window.setSize(650, 500);
	    window.setPlain(true);  
	    window.setModal(true);  
	    window.setHeading("Preview Description");
	    window.setLayout(new FitLayout());
	    window.setButtons(Dialog.OK);
	    window.setHideOnButtonClick(true);

		HTMLPanel panel;
		if(result == null || "".equals(result)) {
	    	panel = new HTMLPanel(SafeHtmlUtils.fromSafeConstant("<div style=\"font-size: 80%\">" + DisplayConstants.LABEL_NO_DESCRIPTION + "</div>"));
		}
		else{
			panel = new HTMLPanel(result);
		}
		MarkdownWidget.loadWidgets(panel, bundle.getEntity().getId(), WidgetConstants.WIKI_OWNER_ID_ENTITY, widgetRegistrar, synapseClient, iconsImageBundle, true);
		FlowPanel f = new FlowPanel();
		f.setStyleName("entity-description-preview-wrapper");
		f.add(panel);
		window.add(new ScrollPanel(f));
		window.show();
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

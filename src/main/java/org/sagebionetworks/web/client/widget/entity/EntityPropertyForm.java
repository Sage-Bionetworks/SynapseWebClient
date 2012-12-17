package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.AttachmentSelectedEvent;
import org.sagebionetworks.web.client.events.AttachmentSelectedHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.TOOLTIP_POSITION;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAnnotationDialog;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAnnotationDialog.TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.DeleteAnnotationDialog;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.row.EntityFormModel;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowFactory;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.Style.Direction;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.inject.Inject;

/**
 * This is a form for editing entity properties.
 * 
 * @author jmhill
 * 
 */
public class EntityPropertyForm extends FormPanel {

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
	EventBus bus;
	
	JSONObjectAdapter adapter;
	ObjectSchema schema;
	Annotations annos;
	Set<String> filter;
	HTML descriptionFormatInfo;
	VerticalPanel descriptionFormatInfoContainer, attachmentsContainer;
	EntityBundle bundle;
	Attachments attachmentsWidget;
	Previewable previewGenerator;
	EntityUpdatedHandler entityUpdatedHandler;
	NodeModelCreator nodeModelCreator;
	SynapseClientAsync synapseClient;
	
	@Inject
	public EntityPropertyForm(FormFieldFactory formFactory, IconsImageBundle iconsImageBundle, SageImageBundle sageImageBundle, Previewable previewGenerator, EventBus bus, NodeModelCreator nodeModelCreator, SynapseClientAsync synapseClient, BaseEditWidgetDescriptorPresenter widgetDescriptorEditor, SynapseJSNIUtils synapseJSNIUtils) {
		this.formFactory = formFactory;
		this.iconsImageBundle = iconsImageBundle;
		this.sageImageBundle= sageImageBundle;
		this.previewGenerator = previewGenerator;
		this.bus = bus;
		this.nodeModelCreator = nodeModelCreator;
		this.synapseClient = synapseClient;
		this.widgetDescriptorEditor = widgetDescriptorEditor;
		this.synapseJSNIUtils = synapseJSNIUtils;
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
		//attachments widget init
		attachmentsWidget.configure(GWT.getModuleBaseURL()+"attachment", bundle.getEntity(), true);
		attachmentsWidget.setAttachmentColumnWidth(800);
		attachmentsWidget.clearHandlers();
		attachmentsWidget.addAttachmentSelectedHandler(new AttachmentSelectedHandler() {
			
			@Override
			public void onAttachmentSelected(AttachmentSelectedEvent event) {
				//insert widget ref into description
				if (event.getTokenId() != null)
					insertWidgetMarkdown(event.getName());
			}
		});
		attachmentsWidget.addAttachmentUpdatedHandler(new WidgetDescriptorUpdatedHandler() {
			@Override
			public void onUpdate(WidgetDescriptorUpdatedEvent event) {
				//only do something if the attachment name has changed
				String newName = event.getName();
				String oldName = event.getOldName();
				if (newName == null) {
					//remove all references to the attachment name
					removeAllOccurrences(DisplayUtils.getWidgetMD(oldName));
				} else if (!newName.equals(oldName)) {
					//renamed. point all references to the new name
					replaceAllOccurrences(DisplayUtils.getWidgetMD(oldName), DisplayUtils.getWidgetMD(newName));
				}
				refreshEntityAttachments();
			}
		});

		attachmentsContainer.add(attachmentsWidget.asWidget());
		attachmentsContainer.setVisible(false);
		
		descriptionFormatInfoContainer = new VerticalPanel();
		descriptionFormatInfoContainer.setBorders(true);
		descriptionFormatInfo = new HTML(DisplayConstants.ENTITY_DESCRIPTION_FORMATTING_TIPS_HTML);
		descriptionFormatInfoContainer.add(descriptionFormatInfo);
		descriptionFormatInfoContainer.setVisible(false);
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
				// Show a form for adding an Annotations
				AddAnnotationDialog.showAddAnnotation(new AddAnnotationDialog.Callback(){

					@Override
					public void addAnnotation(String name, TYPE type) {
						// Add a new annotation
						if(TYPE.STRING == type){
							annos.addAnnotation(name, "");
						}else if(TYPE.DOUBLE == type){
							annos.addAnnotation(name, 0.0);
						}else if(TYPE.LONG == type){
							annos.addAnnotation(name, 0l);
						}else if(TYPE.DATE == type){
							annos.addAnnotation(name, new Date());
						}else{
							throw new IllegalArgumentException("Unknown type: "+type);
						}
						// Rebuild the models
						rebuildModel();
					}
				});
			}
	    });
		// The remove annotation button.
		removeButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				// Show a form for adding an Annotations
				List<String> keys = new ArrayList<String>();
				keys.addAll(annos.keySet());
				DeleteAnnotationDialog.showDeleteAnnotationsDialog(keys, new DeleteAnnotationDialog.Callback() {
					@Override
					public void deletAnnotations(List<String> keysToDelete) {
						// Delete all of the selected annotations.
						for(String key: keysToDelete){
							annos.deleteAnnotation(key);
						}
						// Rebuild the models
						rebuildModel();
					}
				});
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
		
		//also, if attachments should change, the entity must be updated. we should update the attachments, and etag.  then let our version (which may have other modifications) update
		if (entityUpdatedHandler == null) {
			entityUpdatedHandler = new EntityUpdatedHandler() {
				@Override
				public void onPersistSuccess(EntityUpdatedEvent event) {
					//ask for the new entity, update our attachments and etag, and reconfigure the attachments widget
					refreshEntityAttachments();
				}
			};
			bus.addHandler(EntityUpdatedEvent.getType(), entityUpdatedHandler);	
		}
		
		
		rebuild();
	}
	
	private void refreshEntityAttachments(Entity newEntity){
		//check to see if only the attachments and etag (and potentially description) have changed
		Entity oldEntity = bundle.getEntity();
		boolean isSufficientlyEqual = DisplayUtils.isEqualDuringWidgetEditing(oldEntity, newEntity);
		//these must be equal, otherwise, other there have been modifications that we don't know how to sync
		if (isSufficientlyEqual) {
			//update entity (in bundle)
			oldEntity.setEtag(newEntity.getEtag());
			oldEntity.setAttachments(newEntity.getAttachments());
			oldEntity.setModifiedOn(newEntity.getModifiedOn());
			attachmentsWidget.configure(GWT.getModuleBaseURL()+"attachment", oldEntity, true);
			//and the adapter
			try{
				//the primary information hasn't changed, but let's cache some of the metadata
				//cache the name and description, in case they've changed
				String name = (String)adapter.get("name");
				boolean hasDescription = adapter.has("description");
				String description = null;
				if (hasDescription)
					description = (String)adapter.get("description");
				oldEntity.writeToJSONObject(adapter);
				adapter.put("name", name);
				if (hasDescription)
					adapter.put("description", description);
				
			} catch (JSONObjectAdapterException e) {
				throw new RuntimeException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
			}
		} else {
			DisplayUtils.showErrorMessage(DisplayConstants.ERROR_UNABLE_TO_UPDATE_ATTACHMENTS);
		}
	}
	
	
	private void refreshEntityAttachments() {
		// We need to refresh the entity, and update our entity bundle with the most current attachments and etag.
		int mask = ENTITY;
		AsyncCallback<EntityBundleTransport> callback = new AsyncCallback<EntityBundleTransport>() {
			@Override
			public void onSuccess(EntityBundleTransport transport) {
				EntityBundle newBundle = null;
				try {
					newBundle = nodeModelCreator.createEntityBundle(transport);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
				//if we ignore attachments and etag, are these the same objects?
				Entity newEntity = newBundle.getEntity();
				refreshEntityAttachments(newEntity);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				DisplayUtils.showErrorMessage(DisplayConstants.ERROR_UNABLE_TO_LOAD + caught.getMessage());
			}			
		};
		
		synapseClient.getEntityBundle(bundle.getEntity().getId(), mask, callback);
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

	/**
	 * Recalculates size for the description field based on it's contents
	 */
	private void resizeDescription() {
		int scrollheight = descriptionField.getElement().getScrollHeight();
		if (scrollheight == 0)
			scrollheight = 300;
		descriptionField.setHeight(scrollheight + "px");
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
		
		final com.google.gwt.user.client.ui.Button widgetsManagerButton =  new com.google.gwt.user.client.ui.Button();
		widgetsManagerButton.setText(DisplayConstants.ENTITY_DESCRIPTION_SHOW_WIDGETS_TEXT);
		widgetsManagerButton.addStyleName("btn btn-info");
		widgetsManagerButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (attachmentsContainer.isVisible()) {
					attachmentsContainer.el().slideOut(Direction.UP, FxConfig.NONE);
					widgetsManagerButton.setText(DisplayConstants.ENTITY_DESCRIPTION_SHOW_WIDGETS_TEXT);
				} else {
					attachmentsContainer.setVisible(true);
					attachmentsContainer.el().slideIn(Direction.DOWN, FxConfig.NONE);
					widgetsManagerButton.setText(DisplayConstants.ENTITY_DESCRIPTION_HIDE_WIDGETS_TEXT);
				}
			}
		});
		attachmentsContainer.setLayout(new VBoxLayout());
		attachmentsContainer.setScrollMode(Scroll.AUTOY);
		
		//Preview
		final com.google.gwt.user.client.ui.Button previewButton =  new com.google.gwt.user.client.ui.Button();
		previewButton.setText(DisplayConstants.ENTITY_DESCRIPTION_PREVIEW_BUTTON_TEXT);
		previewButton.addStyleName("btn");
		previewButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				previewGenerator.showPreview(((TextArea)descriptionField).getValue());
			}
		});
		FormData previewFormData = new FormData("-5");
		previewFormData.setMargins(new Margins(10,10,0,10));
		
		FlowPanel mdCommandsLower = new FlowPanel();
		formPanel.add(mdCommandsLower, previewFormData);
		mdCommandsLower.add(widgetsManagerButton);
		SimplePanel previewButtonWrapper= new SimplePanel();
		previewButtonWrapper.add(previewButton);
		previewButtonWrapper.addStyleName("inline-block margin-left-570");
		mdCommandsLower.add(previewButtonWrapper);
		
		//Formatting Guide
		formatLinkFormData.setMargins(new Margins(10,10,0,10));
		
		final Button formatLink = new Button(DisplayConstants.ENTITY_DESCRIPTION_SHOW_TIPS_TEXT);
		formatLink.setIcon(AbstractImagePrototype.create(iconsImageBundle.slideInfo16()));
		mdCommands.add(formatLink);
		mdCommands.add(insertButton);
		formatLink.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (descriptionFormatInfoContainer.isVisible()) {
					descriptionFormatInfoContainer.el().slideOut(Direction.UP, FxConfig.NONE);
					formatLink.setText(DisplayConstants.ENTITY_DESCRIPTION_SHOW_TIPS_TEXT);
				} else {
					descriptionFormatInfoContainer.setVisible(true);
					descriptionFormatInfoContainer.el().slideIn(Direction.DOWN, FxConfig.NONE);
					formatLink.setText(DisplayConstants.ENTITY_DESCRIPTION_HIDE_TIPS_TEXT);
				}
			}
		});
		
		descriptionFormatInfoContainer.setLayout(new VBoxLayout());
		descriptionFormatInfoContainer.setScrollMode(Scroll.AUTOY);
		
		formPanel.add(descriptionFormatInfoContainer, formatLinkFormData);
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
		resizeDescription();
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
	
	public void handleInsertWidgetCommand(String contentTypeKey){
		BaseEditWidgetDescriptorPresenter.editNewWidget(widgetDescriptorEditor, bundle.getEntity().getId(), contentTypeKey, bundle.getEntity().getAttachments(), new WidgetDescriptorUpdatedHandler() {
			@Override
		public void onUpdate(WidgetDescriptorUpdatedEvent event) {
			if (event.getInsertValue()!=null)
				insertMarkdown(event.getInsertValue());
			else {
				insertWidgetMarkdown(event.getName());
				try {
					//switch to the up-to-date entity version after adding the attachment, but save our local description and name (in case they've changed here)
					Entity updatedEntity = nodeModelCreator.createEntity(event.getEntityWrapper());
					refreshEntityAttachments(updatedEntity);
				} catch (JSONObjectAdapterException e) {
					throw new RuntimeException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
				}
			}
		}
	});
		
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
		descriptionTextArea.setValue(currentValue.substring(0, cursorPos) + md + currentValue.substring(cursorPos));
	}
	
	public void insertWidgetMarkdown(String attachmentName) {
		//insert the markdown into the description for the attachment (where the attachment points to the json used to describe the widget)
		String md = DisplayUtils.getWidgetMD(attachmentName);
		insertMarkdown(md);
	}
	
	/**
	 * replace all occurrences of oldMd with newMd
	 * @param oldMd
	 * @param newMd
	 */
	public void replaceAllOccurrences(String oldMd, String newMd) {
		if (oldMd != null & newMd != null) {
			TextArea descriptionTextArea = descriptionField;
			String currentValue = descriptionTextArea.getValue();
			if (currentValue != null) {
				descriptionTextArea.setValue(currentValue.replaceAll(DisplayUtils.quotePattern(oldMd), newMd));
			}
		}
	}
	
	/**
	 * remove all occurrences of oldMd
	 * @param oldMd
	 * @param newMd
	 */
	public void removeAllOccurrences(String oldMd) {
		if (oldMd != null) {
			TextArea descriptionTextArea = descriptionField;
			String currentValue = descriptionTextArea.getValue();
			if (currentValue != null) {
				descriptionTextArea.setValue(currentValue.replaceAll(DisplayUtils.quotePattern(oldMd), ""));
			}
		}
	}
	
	public static List<AttachmentData> getVisualAttachments(List<AttachmentData> attachments){
		List<AttachmentData> visualAttachments = new ArrayList<AttachmentData>();
		for (Iterator iterator = attachments.iterator(); iterator
				.hasNext();) {
			AttachmentData data = (AttachmentData) iterator.next();
			// Ignore all attachments without a preview.
			if(data.getPreviewId() != null) 
				visualAttachments.add(data);
		}
		return visualAttachments;
	}
	
	/**
	 * Pass editable copies of all objects.
	 * @param adapter
	 * @param schema
	 * @param annos
	 * @param filter
	 */
	public void setDataCopies(JSONObjectAdapter adapter, ObjectSchema schema, Annotations annos, Set<String> filter, EntityBundle bundle, Attachments attachmentsWidget){
		this.adapter = adapter;
		this.schema = schema;
		this.annos = annos;
		this.filter = filter;
		this.bundle = bundle;
		this.attachmentsWidget = attachmentsWidget;
		rebuildModel();
	}
	
	/**
	 * Rebuild the model
	 */
	@SuppressWarnings("unchecked")
	private void rebuildModel(){
		EntityFormModel model = EntityRowFactory.createEntityRowList(this.adapter, this.schema, this.annos, filter);
		// The name field is just a text field that cannot be null
		nameField = (TextField<String>) formFactory.createField(model.getName());
		nameField.setAllowBlank(false);
		nameField.setRegex(WebConstants.VALID_ENTITY_NAME_REGEX);
		nameField.getMessages().setRegexText(WebConstants.INVALID_ENTITY_NAME_MESSAGE);
		descriptionField = formFactory.createTextAreaField(model.getDescription());
		descriptionField.setWidth("796px");
		descriptionField.setHeight("200px");
		resizeDescription();
		//automatically change size based on content
		descriptionField.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
//				int keyCode = event.getNativeEvent().getKeyCode();
				resizeDescription();
			}
		});

		// Create the list of fields
		propertyFields = formFactory.createFormFields(model.getProperties());
		annotationFields = formFactory.createFormFields(model.getAnnotations());
		
		rebuild();
	}
}

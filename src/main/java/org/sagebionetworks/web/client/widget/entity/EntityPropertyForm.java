package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAnnotationDialog;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAnnotationDialog.TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.DeleteAnnotationDialog;
import org.sagebionetworks.web.client.widget.entity.dialog.SelectAttachmentDialog;
import org.sagebionetworks.web.client.widget.entity.row.EntityFormModel;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowFactory;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.Style.Direction;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.inject.Inject;

/**
 * This is a form for editing entity properties.
 * 
 * @author jmhill
 * 
 */
public class EntityPropertyForm extends FormPanel {

	TextField<String> nameField;
	Field<?> descriptionField;
	List<Field<?>> propertyFields;
	List<Field<?>> annotationFields;
	FormFieldFactory formFactory;
	FormPanel formPanel;
	FormPanel annotationFormPanel;
	ContentPanel annoPanel;
	ContentPanel propPanel;
	VerticalPanel vp;
	IconsImageBundle iconsImageBundle;
	
	JSONObjectAdapter adapter;
	ObjectSchema schema;
	Annotations annos;
	Set<String> filter;
	HTML descriptionFormatInfo;
	VerticalPanel descriptionFormatInfoContainer;
	String entityId;
	List<AttachmentData> attachments;
	SynapseClientAsync synapseClient;
	
	@Inject
	public EntityPropertyForm(FormFieldFactory formFactory, IconsImageBundle iconsImageBundle, SynapseClientAsync synapseClient) {
		this.formFactory = formFactory;
		this.iconsImageBundle = iconsImageBundle;
		this.synapseClient = synapseClient;
	}

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		this.setLayout(new AnchorLayout());
		this.setScrollMode(Scroll.AUTO);
		this.vp = new VerticalPanel();
		this.add(vp);
		int width = 700;
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
		
		rebuild();
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
		// followed by description.
		FormData descriptionData = new FormData("-20 120%");
        descriptionData.setMargins(margins);
		formPanel.add(descriptionField, descriptionData);
		final Anchor formatLink = new Anchor(DisplayConstants.ENTITY_DESCRIPTION_SHOW_TIPS_TEXT);
		formatLink.setStyleName("link");
		FormData formatLinkFormData = new FormData("-100");
		formatLinkFormData.setMargins(new Margins(10,10,0,90));
						
		formatLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
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
		
		formPanel.add(formatLink, formatLinkFormData);
		formPanel.add(descriptionFormatInfoContainer, formatLinkFormData);
		

		//and now the description toolbar
		Button previewButton = new Button(DisplayConstants.ENTITY_DESCRIPTION_PREVIEW_BUTTON_TEXT);
		Button addImageButton = new Button(DisplayConstants.ENTITY_DESCRIPTION_INSERT_IMAGE_BUTTON_TEXT);
		addImageButton.setEnabled(attachments.size() > 0);
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.setTableWidth("150px");
		hp.add(previewButton);
		hp.add(addImageButton);
		
		// The preview button.
		previewButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				showPreviewWindow(synapseClient, ((TextArea)descriptionField).getValue());
			}
	    });
		final String baseURl = GWT.getModuleBaseURL()+"attachment";
        
		// The add image button
		addImageButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				//pop up a list of attachments, and have the user pick one.
				SelectAttachmentDialog.showSelectAttachmentDialog(baseURl,  entityId, attachments, synapseClient, "Select Attachment", "Insert", new SelectAttachmentDialog.Callback() {
					
					@Override
					public void onSelectAttachment(AttachmentData data) {
						//insert the markdown into the description for the image attachment
						SafeHtml safeName = SafeHtmlUtils.fromString(data.getName());
						StringBuilder sb = new StringBuilder();
						sb.append("![");
						sb.append(safeName.asString());
						sb.append("](");
						sb.append(DisplayUtils.createAttachmentUrl(baseURl, entityId, data.getPreviewId(), null));
						sb.append(" \"");
						sb.append(safeName.asString());
						sb.append("\")");
						TextArea descriptionTextArea = (TextArea)descriptionField;
						String currentValue = descriptionTextArea.getValue();
						int cursorPos = descriptionTextArea.getCursorPos();
						if (cursorPos < 0)
							cursorPos = 0;
						else if (cursorPos > currentValue.length())
							cursorPos = currentValue.length();
						descriptionTextArea.setValue(currentValue.substring(0, cursorPos) + sb.toString() + currentValue.substring(cursorPos, currentValue.length()));
					}
				});
					
			}
	    });
		formPanel.add(hp,formatLinkFormData);
		
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
	
	/**
	 * Show the edit entity dialog.
	 * @param entity
	 * @param annos
	 * @param callback
	 */
	public static void showPreviewWindow(SynapseClientAsync synapseClient, String descriptionMarkdown){
		final Dialog window = new Dialog();
		window.setMaximizable(false);
	    window.setSize(650, 500);
	    window.setPlain(true);  
	    window.setModal(true);  
	    window.setBlinkModal(true);  
	    window.setHeading("Preview Description");
	    window.setLayout(new FitLayout());
	    window.setButtons(Dialog.OK);
	    window.setHideOnButtonClick(true);
	    
	    //get the html for the markdown
	    synapseClient.markdown2Html(descriptionMarkdown, new AsyncCallback<String>() {
	    	@Override
			public void onSuccess(String result) {
	    		HTMLPanel panel;
	    		if(result == null || "".equals(result)) {
	    	    	panel = new HTMLPanel(SafeHtmlUtils.fromSafeConstant("<div style=\"font-size: 80%\">" + DisplayConstants.LABEL_NO_DESCRIPTION + "</div>"));
	    		}
	    		else{
	    			panel = new HTMLPanel("<div style=\"margin: 20px\">" + DisplayUtils.fixEntityDescriptionHtml(result, DisplayConstants.ENTITY_DESCRIPTION_CSS_CLASSNAME) + "</div>");
	    		}
	    		
	    		window.add(new ScrollPanel(panel));
				window.show();
			}
			@Override
			public void onFailure(Throwable caught) {
				//preview failed
			}
		});
	}
	
	
	/**
	 * Pass editable copies of all objects.
	 * @param adapter
	 * @param schema
	 * @param annos
	 * @param filter
	 */
	public void setDataCopies(JSONObjectAdapter adapter, ObjectSchema schema, Annotations annos, Set<String> filter, String entityId, List<AttachmentData> attachments){
		this.adapter = adapter;
		this.schema = schema;
		this.annos = annos;
		this.filter = filter;
		this.entityId = entityId;
		this.attachments = attachments;
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
		
		// Create the list of fields
		propertyFields = formFactory.createFormFields(model.getProperties());
		annotationFields = formFactory.createFormFields(model.getAnnotations());
		
		rebuild();
	}
}

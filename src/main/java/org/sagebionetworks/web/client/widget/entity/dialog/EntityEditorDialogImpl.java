package org.sagebionetworks.web.client.widget.entity.dialog;

import java.util.Set;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;
import org.sagebionetworks.web.client.widget.entity.Attachments;
import org.sagebionetworks.web.client.widget.entity.EntityPageTop;
import org.sagebionetworks.web.client.widget.entity.EntityPropertyForm;
import org.sagebionetworks.web.client.widget.entity.FormFieldFactory;
import org.sagebionetworks.web.client.widget.entity.Previewable;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.inject.Inject;

/**
 * Dialog used to edit an entity.
 * 
 * @author John
 *
 */
public class EntityEditorDialogImpl implements EntityEditorDialog, Previewable, SynapseWidgetView{
	
	FormFieldFactory formFactory;
	IconsImageBundle icons;
	SageImageBundle sageImageBundle;
	SynapseClientAsync synapseClient;
	EventBus bus;
	NodeModelCreator nodeModelCreator;
	BaseEditWidgetDescriptorPresenter baseEditWidgetDescriptor;
	WidgetRegistrar widgetRegistrar;
	EntityBundle bundle;
	SynapseJSNIUtils synapseJSNIUtils;
	
	@Inject
	public EntityEditorDialogImpl(FormFieldFactory formFactory, IconsImageBundle icons, SageImageBundle sageImageBundle, SynapseClientAsync synapseClient, EventBus bus, NodeModelCreator nodeModelCreator, BaseEditWidgetDescriptorPresenter baseEditWidgetDescriptor, WidgetRegistrar widgetRegistrar, SynapseJSNIUtils synapseJSNIUtils){
		this.formFactory = formFactory;
		this.icons = icons;
		this.sageImageBundle = sageImageBundle;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.baseEditWidgetDescriptor = baseEditWidgetDescriptor;
		this.bus = bus;
		this.widgetRegistrar = widgetRegistrar;
		this.synapseJSNIUtils = synapseJSNIUtils;
	}

	@Override
	public void showPreview(Object ob) {
		//ob is the description markdown string in this case
		String descriptionMarkdown= (String)ob;
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
	    String baseUrl = GWT.getModuleBaseURL()+"attachment";
	    synapseClient.markdown2Html(descriptionMarkdown, baseUrl, true, new AsyncCallback<String>() {
	    	@Override
			public void onSuccess(String result) {
	    		HTMLPanel panel;
	    		if(result == null || "".equals(result)) {
	    	    	panel = new HTMLPanel(SafeHtmlUtils.fromSafeConstant("<div style=\"font-size: 80%\">" + DisplayConstants.LABEL_NO_DESCRIPTION + "</div>"));
	    		}
	    		else{
	    			
	    			panel = new HTMLPanel(result);
	    		}
	    		EntityPageTop.loadWidgets(panel, bundle, widgetRegistrar, synapseClient, nodeModelCreator, EntityEditorDialogImpl.this, true);
	    		FlowPanel f = new FlowPanel();
	    		f.setStyleName("entity-description-preview-wrapper");
	    		f.add(panel);
	    		window.add(new ScrollPanel(f));
				window.show();
			}
			@Override
			public void onFailure(Throwable caught) {
				//preview failed
				showErrorMessage(DisplayConstants.ENTITY_DESCRIPTION_PREVIEW_FAILED_TEXT + caught.getMessage());
			}
		});	
	}
	/**
	 * Show the edit entity dialog.
	 * @param entity
	 * @param annos
	 * @param callback
	 */
	public void showEditEntityDialog(final String windowTitle, final EntityBundle bundle, final Attachments attachmentsWidget, final JSONObjectAdapter newAdapter,
			ObjectSchema schema, final Annotations newAnnos, Set<String> filter, final Callback callback){
		final Dialog window = new Dialog();
		window.setMaximizable(false);
	    window.setSize(880, 660);
	    window.setPlain(true);  
	    window.setModal(true);  
	    window.setBlinkModal(true);  
	    window.setHeading(windowTitle);
	    window.setLayout(new FitLayout());
	    // We want okay to say save
	    window.okText = "Save";
	    window.setButtons(Dialog.OKCANCEL);
	    window.setHideOnButtonClick(true);
	    this.bundle = bundle;
	    // Create the property from
	    EntityPropertyForm editor = new EntityPropertyForm(formFactory, icons, sageImageBundle, this, bus, nodeModelCreator, synapseClient, baseEditWidgetDescriptor, synapseJSNIUtils);
	    editor.setDataCopies(newAdapter, schema, newAnnos, filter, bundle, attachmentsWidget);
	    window.add(editor, new FitData(0));
	    // List for the button selection
	    Button saveButton = window.getButtonById(Dialog.OK);	    
	    // Disable the button until the user enters a valid Entity name.
	    FormButtonBinding binding = new FormButtonBinding(editor);  
	    binding.addButton(saveButton);
	    saveButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				// Let the caller know about the save.
				callback.saveEntity(newAdapter, newAnnos);
			}
	    });
	    // show the window
	    window.show();
	}
	
	/**
	 * Show an error message.
	 * @param error
	 */
	public void showErrorMessage(String message){
		DisplayUtils.showErrorMessage(message);
	}
	@Override
	public void clear() {
	}
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	@Override
	public void showLoading() {
	}
}

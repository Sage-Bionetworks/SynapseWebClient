package org.sagebionetworks.web.client.widget.entity.dialog;

import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.entity.EntityPropertyForm;
import org.sagebionetworks.web.client.widget.entity.FormFieldFactory;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.inject.Inject;

/**
 * Dialog used to edit an entity.
 * 
 * @author John
 *
 */
public class EntityEditorDialogImpl implements EntityEditorDialog{
	
	FormFieldFactory formFactory;
	IconsImageBundle icons;
	private SynapseClientAsync synapseClient;

	@Inject
	public EntityEditorDialogImpl(FormFieldFactory formFactory, IconsImageBundle icons, SynapseClientAsync synapseClient){
		this.formFactory = formFactory;
		this.icons = icons;
		this.synapseClient = synapseClient;
	}

	/**
	 * Show the edit entity dialog.
	 * @param entity
	 * @param annos
	 * @param callback
	 */
	public void showEditEntityDialog(final String windowTitle, final String entityId, final List<AttachmentData> attachments, final JSONObjectAdapter newAdapter,
			ObjectSchema schema, final Annotations newAnnos, Set<String> filter, final Callback callback){
		final Dialog window = new Dialog();
		window.setMaximizable(false);
	    window.setSize(733, 660);
	    window.setPlain(true);  
	    window.setModal(true);  
	    window.setBlinkModal(true);  
	    window.setHeading(windowTitle);
	    window.setLayout(new FitLayout());
	    // We want okay to say save
	    window.okText = "Save";
	    window.setButtons(Dialog.OKCANCEL);
	    window.setHideOnButtonClick(true);
	    
	    // Create the property from
	    EntityPropertyForm editor = new EntityPropertyForm(formFactory, icons, synapseClient);
	    editor.setDataCopies(newAdapter, schema, newAnnos, filter, entityId, attachments);
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
}

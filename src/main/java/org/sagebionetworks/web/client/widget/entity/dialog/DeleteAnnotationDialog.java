package org.sagebionetworks.web.client.widget.entity.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class DeleteAnnotationDialog {

	/**
	 * Callback called when the user adds an annotation.
	 * @author John
	 *
	 */
	public interface Callback{
		/**
		 * Called when the user selects annotations to delete.
		 * @param keysToDelete - The list of keys to be deleted.
		 */
		public void deletAnnotations(List<String> keysToDelete);
	}
	
	/**
	 * Show the Add Annotation dialog.
	 * @param callback If the user chooses to add an annotation, this callback will be called.
	 */
	public static void showDeleteAnnotationsDialog(List<String> keys, final Callback callback){
		// Sort the keys
		Collections.sort(keys);
		// Show a form for adding an Annotations
		Dialog dialog = new Dialog();
		dialog.setMaximizable(false);
		int height = 135;
		if(keys.size() > 1){
			height = height + (keys.size()*18);
		}
		if(height > 400){
			height = 400;
		}
		dialog.setSize(250, height);
		dialog.setPlain(true);  
		dialog.setModal(true);  
		dialog.setBlinkModal(true);  
		dialog.setHeading("Remove Annotations");  
		dialog.setLayout(new FitLayout());
	    // We want okay to say save
		dialog.okText = "Remove Selected";
		dialog.setButtons(Dialog.OKCANCEL);
		dialog.setHideOnButtonClick(true);
		
		
		FormPanel form = createNewFormPanel();
		form.setHeaderVisible(false);
		form.setBorders(false);
		
		// Add them to the form
		FormData basicFormData = new FormData("100%");
		Margins margins = new Margins(10, 10, 0, 0);
		basicFormData.setMargins(margins);
		// Create a check box for each annotation.
	    final CheckBoxGroup checkGroup = new CheckBoxGroup();
	    checkGroup.setOrientation(Orientation.VERTICAL);
	    checkGroup.setFieldLabel("Names");  ;
		for(String key: keys){
		    CheckBox check = new CheckBox();  
		    check.setBoxLabel(key);  
		    check.setValue(false); 
		    checkGroup.add(check);
		}
	    form.add(checkGroup, basicFormData);
	    ContentPanel cp = new ContentPanel(new AnchorLayout());
	    cp.setScrollMode(Scroll.AUTOY);
	    cp.add(form);
	    cp.setHeaderVisible(false);
	    cp.setBorders(false);
		dialog.add(cp);
		dialog.show();
		// Listen to the button
		Button removeButton = dialog.getButtonById(Dialog.OK);
		// the binding will disable the button until the user fills in both fields.
	    FormButtonBinding binding = new FormButtonBinding(form);  
	    binding.addButton(removeButton);
	    removeButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				// Let the caller know.
				List<String> toDelete = new ArrayList<String>();
				for(CheckBox check: checkGroup.getValues()){
					if(check.getValue()){
						toDelete.add(check.getBoxLabel());
					}
				}
				// Let the callback know.
				callback.deletAnnotations(toDelete);
			}
	    });
		
	}
	
	/**
	 * Build a new empty from panel
	 * @return
	 */
	private static FormPanel createNewFormPanel(){
		FormPanel form = new FormPanel();
		form.setHeading("Simple Form");
		form.setHeaderVisible(false);
		form.setFrame(false);
		form.setBorders(false);
		form.setShadow(false);
		form.setLabelAlign(LabelAlign.RIGHT);
		form.setBodyStyleName("form-background"); 
		return form;
	}
}

package org.sagebionetworks.web.client.widget.entity.dialog;

import org.sagebionetworks.web.client.widget.entity.ComboValue;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;

/**
 * Dialog for adding an annotation.
 * 
 * @author John
 *
 */
public class AddAnnotationDialog {
	
	/**
	 * The annotation types.
	 * @author John
	 *
	 */
	public enum TYPE {
		STRING("Text"),
		LONG("Integer"),
		DOUBLE("Floating Point"),
		DATE("Date");
		
		private String dispalyText;
		TYPE(String dispalyText){
			this.dispalyText = dispalyText;
		}
		
		/**
		 * This display text for this option.
		 * @return
		 */
		public String getDispalyText(){
			return dispalyText;
		}
		
		public static TYPE getTypeForDisplay(String dispaly){
			for(TYPE type: values()){
				if(type.dispalyText.equals(dispaly)) return type;
			}
			throw new IllegalArgumentException("Cannot find type for display: "+dispaly);
		}
	}
	/**
	 * Callback called when the user adds an annotation.
	 * @author John
	 *
	 */
	public interface Callback{
		public void addAnnotation(String name, TYPE type);
	}
	
	/**
	 * Show the Add Annotation dialog.
	 * @param callback If the user chooses to add an annotation, this callback will be called.
	 */
	public static void showAddAnnotation(final Callback callback){
		// Show a form for adding an Annotations
		Dialog dialog = new Dialog();
		dialog.setMaximizable(false);
		dialog.setSize(325, 175);
		dialog.setPlain(true);  
		dialog.setModal(true);  
		dialog.setBlinkModal(true);  
		dialog.setHeading("Add New Annotation");  
		dialog.setLayout(new FitLayout());
	    // We want okay to say save
		dialog.okText = "Add";
		dialog.setButtons(Dialog.OKCANCEL);
		dialog.setHideOnButtonClick(true);
		FormPanel form = createNewFormPanel();
		final TextField<String> nameField = new TextField<String>();
		nameField.setAllowBlank(false);
		nameField.setEmptyText("Set the annotation name...");
		nameField.setFieldLabel("Name");
		ListStore<ComboValue> store = new ListStore<ComboValue>();
		// Add each type
		for(TYPE type: TYPE.values()){
			ComboValue comboValue = new ComboValue(type.getDispalyText());
			store.add(comboValue);
		}
		final ComboBox<ComboValue> combo = new ComboBox<ComboValue>();
		combo.setEmptyText("Select a data type...");
		combo.setDisplayField(ComboValue.VALUE_KEY);
		combo.setStore(store);
		combo.setFieldLabel("Type");
		combo.setEditable(false);
		combo.setTriggerAction(TriggerAction.ALL);
		combo.setAllowBlank(false);
		// Add them to the form
		FormData basicFormData = new FormData("-20");
		Margins margins = new Margins(10, 10, 0, 0);
		basicFormData.setMargins(margins);
		form.add(nameField, basicFormData);
		form.add(combo, basicFormData);
		dialog.add(form);
		dialog.show();
		// Listen to the button
		Button addButton = dialog.getButtonById(Dialog.OK);
		// the binding will disable the button until the user fills in both fields.
	    FormButtonBinding binding = new FormButtonBinding(form);  
	    binding.addButton(addButton);
	    addButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				// Let the caller know.
				callback.addAnnotation(nameField.getValue(), TYPE.getTypeForDisplay(combo.getValue().getValue()) );
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
		form.setBodyStyleName("form-background"); 
		form.setLabelAlign(LabelAlign.RIGHT);
		return form;
	}


}

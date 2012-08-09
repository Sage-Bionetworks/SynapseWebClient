package org.sagebionetworks.web.client.widget.entity.dialog;

import org.sagebionetworks.web.client.DisplayConstants;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class NameAndDescriptionEditorDialog {
	
	public interface Callback {
		/**
		 * When the user selects save this will be called.
		 */
		public void onSave(String name, String description);		
	}

	public static void showNameAndDescriptionDialog(Callback callback) {
		showNameAndDescriptionDialog(null, null, true, true, null, null, callback);
	}
	
	public static void showNameAndDescriptionDialog(String nameLabel, String descLabel, Callback callback) {
		showNameAndDescriptionDialog(null, null, true, true, nameLabel, descLabel, callback);
	}
	
	public static void showNameAndDescriptionDialog(String name, String description, String nameLabel, String descLabel, Callback callback) {
		showNameAndDescriptionDialog(name, description, true, true, nameLabel, descLabel, callback);
	}
	
	public static void showNameDialog(String nameLabel, Callback callback) {
		showNameAndDescriptionDialog(null, null, true, false, nameLabel, null, callback);
	}
	
	public static void showNameDialog(String name, String nameLabel, Callback callback) {
		showNameAndDescriptionDialog(name, null, true, false, nameLabel, null, callback);
	}
	
	public static void showTextAreaDialog(String descLabel, Callback callback) {
		showNameAndDescriptionDialog(null, null, false, true, null, descLabel, callback);
	}
	
	public static void showTextAreaDialog(String value, String descLabel, Callback callback) {
		showNameAndDescriptionDialog(null, value, false, true, null, descLabel, callback);
	}
	
	/**
	 * Show the name/description dialog
	 * @param name
	 * @param description
	 * @param showName
	 * @param showDescription
	 * @param callback
	 */
	private static void showNameAndDescriptionDialog(final String name,
			final String description, final boolean showName,
			final boolean showDescription,
			final String nameLabel, final String descLabel,
			final Callback callback) {
		// Show a form for adding an Annotations
		final Dialog dialog = new Dialog();
		dialog.setMaximizable(false);
		dialog.setSize(325, 175);
		dialog.setPlain(true);
		dialog.setModal(true);
		dialog.setBlinkModal(true);
		dialog.setHideOnButtonClick(true);
		dialog.setLayout(new FitLayout());
		dialog.setBorders(false);
		dialog.setButtons(Dialog.OKCANCEL);

		final FormPanel panel = new FormPanel();
		panel.setHeaderVisible(false);
		panel.setFrame(false);
		panel.setBorders(false);
		panel.setShadow(false);
		panel.setButtonAlign(HorizontalAlignment.CENTER);
		panel.setLabelAlign(LabelAlign.LEFT);
		panel.setBodyBorder(false);

		final TextField<String> nameField = new TextField<String>();
		if(showName) {			
			if(nameLabel == null) {
				nameField.setFieldLabel(DisplayConstants.LABEL_NAME);
			} else {
				nameField.setFieldLabel(nameLabel);
			}
			if(name != null) nameField.setValue(name);
			panel.add(nameField);			
		}
		
		final TextArea descField = new TextArea();
		if(showDescription) {
			if(descLabel == null) {
				descField.setFieldLabel(DisplayConstants.DESCRIPTION);
			} else {
				descField.setFieldLabel(descLabel);
			}
			if(description != null) descField.setValue(description);
			panel.add(descField);
		}
		
		dialog.getButtonBar().removeAll();
		dialog.addButton(new Button(DisplayConstants.OK, new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				dialog.hide();
				String nameVal = showName ? nameVal = nameField.getValue() : null;
				String descVal = showDescription ? descField.getValue() : null;
				nameField.clear();
				descField.clear();
				callback.onSave(nameVal, descVal);
			}
		}));
		dialog.addButton(new Button(DisplayConstants.BUTTON_CANCEL, new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				nameField.clear();
				descField.clear();
				dialog.hide();
			}
		}));
		
		dialog.add(panel);
		dialog.show();

	}	
}

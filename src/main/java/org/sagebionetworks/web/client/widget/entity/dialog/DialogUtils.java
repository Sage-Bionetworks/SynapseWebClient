package org.sagebionetworks.web.client.widget.entity.dialog;

import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;

public class DialogUtils {
	/**
	 * Build a new empty from panel
	 * @return
	 */
	public static FormPanel createNewFormPanel(){
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

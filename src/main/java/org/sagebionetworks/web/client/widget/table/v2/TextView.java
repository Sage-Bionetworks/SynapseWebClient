package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.FormControlStatic;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Abstraction for wrapping either an editor or static control
 * @author John
 *
 */
public class TextView implements IsWidget, HasText {
	
	HasText hasText;
	IsWidget widget;
	
	/**
	 * New text view for text.
	 * @param text
	 * @param isEditable
	 */
	public TextView(String text, boolean isEditable){
		if(isEditable){
			TextBox textBox = new TextBox();
			textBox.setText(text);
			this.hasText = textBox;
			this.widget = textBox;
		}else{
			FormControlStatic fs = new FormControlStatic();
			fs.setText(text);
			this.hasText = fs;
			this.widget = fs;
		}
	}

	@Override
	public String getText() {
		return hasText.getText();
	}

	@Override
	public void setText(String text) {
		hasText.setText(text);
	}

	@Override
	public Widget asWidget() {
		return widget.asWidget();
	}

}

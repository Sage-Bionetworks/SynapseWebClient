package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.FormControlStatic;

/**
 * A non editable renderer for a string.
 * 
 * @author John
 *
 */
public class StringRendererCellImpl extends FormControlStatic implements StringRendererCell {
	
	public StringRendererCellImpl(){
		super();
	}

	@Override
	public void setValue(String value) {
		this.setText(value);
	}

	@Override
	public String getValue() {
		return this.getText();
	}

}

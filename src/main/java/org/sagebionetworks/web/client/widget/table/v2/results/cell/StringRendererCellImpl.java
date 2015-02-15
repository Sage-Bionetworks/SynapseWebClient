package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.html.Text;

/**
 * A non editable renderer for a string.
 * 
 * @author John
 *
 */
public class StringRendererCellImpl extends Text implements StringRendererCell {
	
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

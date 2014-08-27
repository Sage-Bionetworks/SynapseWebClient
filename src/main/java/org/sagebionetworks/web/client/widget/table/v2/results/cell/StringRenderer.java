package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.FormControlStatic;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * A non editable renderer for a string.
 * 
 * @author John
 *
 */
public class StringRenderer extends FormControlStatic implements Cell {
	
	public StringRenderer(){
		super();
	}

	@Override
	public void setValue(String value) {
		this.setText(SafeHtmlUtils.fromString(value).toString());
	}

	@Override
	public String getValue() {
		return this.getText();
	}

}

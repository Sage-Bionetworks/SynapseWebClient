package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * An editor for a string.
 * 
 * @author John
 *
 */
public class StringEditorCellImpl extends TextBox implements  StringEditorCell {

	public StringEditorCellImpl(){
		super();
	}

	@Override
	public void setValue(String value) {
		super.setValue(SafeHtmlUtils.htmlEscape(value));
	}
	
}

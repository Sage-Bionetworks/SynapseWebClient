package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;

/**
 * An editor for a string.
 * 
 * @author John
 *
 */
public class StringEditorCellImpl extends TextBox implements  StringEditorCell {

	public StringEditorCellImpl(){
		super();
		DOM.setStyleAttribute(getElement(), "minWidth", "75px");
	}

	@Override
	public void setValue(String value) {
		super.setValue(SafeHtmlUtils.htmlEscape(value));
	}
	
}

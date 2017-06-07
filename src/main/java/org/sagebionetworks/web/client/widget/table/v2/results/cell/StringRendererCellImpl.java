package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.html.Paragraph;

import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * A non editable renderer for a string.
 * 
 * @author John
 *
 */
public class StringRendererCellImpl extends Paragraph implements StringRendererCell {
	HandlerRegistration registration;
	
	public StringRendererCellImpl(){
		super();
		addStyleName("max-height-100 overflowHidden");
		registration = addDomHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				removeStyleName("max-height-100 overflowHidden");
				registration.removeHandler();
			}
		}, MouseOverEvent.getType());
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

package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.html.Paragraph;

import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;

/**
 * A non editable renderer for a string.
 * 
 * @author John
 *
 */
public class StringRendererCellImpl extends Paragraph implements StringRendererCell {
	public static final MouseOverHandler STRING_RENDERER_MOUSE_OVER_HANDLER = event -> {
		StringRendererCellImpl src = (StringRendererCellImpl)event.getSource();
		src.removeStyleName("max-height-100 overflowHidden");
	};
	public StringRendererCellImpl(){
		super();
		addStyleName("max-height-100 overflowHidden");
		addDomHandler(STRING_RENDERER_MOUSE_OVER_HANDLER, MouseOverEvent.getType());
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

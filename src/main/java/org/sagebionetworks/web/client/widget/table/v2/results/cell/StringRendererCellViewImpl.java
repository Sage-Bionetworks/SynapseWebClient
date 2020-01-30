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
public class StringRendererCellViewImpl extends Paragraph implements StringRendererCellView {
	public static final MouseOverHandler STRING_RENDERER_MOUSE_OVER_HANDLER = event -> {
		StringRendererCellViewImpl src = (StringRendererCellViewImpl) event.getSource();
		src.removeStyleName("max-height-100 overflowHidden");
	};

	public StringRendererCellViewImpl() {
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

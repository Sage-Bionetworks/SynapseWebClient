package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.Anchor;
import org.sagebionetworks.web.client.place.Synapse;

import com.google.gwt.user.client.ui.Widget;

public class EntityIdCellRendererImpl extends Anchor implements EntityIdCellRenderer{

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void setValue(String value) {
		this.setHref(Synapse.getHrefForDotVersion(value));
		this.setText(value);
		this.setTarget(BLANK);
	}

	@Override
	public String getValue() {
		return this.getText();
	}

}

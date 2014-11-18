package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.Anchor;

import com.google.gwt.user.client.ui.Widget;

public class EntityCellRendererImpl extends Anchor implements EntityCellRenderer{

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void setValue(String value) {
		this.setHref("#!Synapse:"+value);
		this.setText(value);
	}

	@Override
	public String getValue() {
		return this.getText();
	}

}

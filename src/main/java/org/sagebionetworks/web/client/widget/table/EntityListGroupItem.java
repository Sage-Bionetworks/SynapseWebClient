package org.sagebionetworks.web.client.widget.table;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.LinkedGroupItemText;
import org.gwtbootstrap3.client.ui.ListGroupItem;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

/**
 * Simple list item for an entity.
 * 
 * @author jmhill
 *
 */
public class EntityListGroupItem extends ListGroupItem {

	private static final String CREATED_ON = "Created On: ";

	static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
	
	EntityListGroupItem(HeadingSize size, EntityQueryResult header, ClickHandler clickHandler){
		Heading heading = new Heading(size);
		Anchor anchor = new Anchor();
		anchor.setText(header.getName());
		anchor.addClickHandler(clickHandler);
		heading.add(anchor);
		heading.addStyleName("displayInline");
		LinkedGroupItemText text = new LinkedGroupItemText();
		text.setText(CREATED_ON+DATE_FORMAT.format(header.getCreatedOn()));
		anchor = new Anchor("#!Synapse:"+header.getId());
		anchor.setTarget("_blank");
		anchor.setIcon(IconType.EXTERNAL_LINK);
		anchor.addStyleName("margin-left-10 moveup-5");
		Div div = new Div();
		div.add(heading);
		div.add(anchor);
		this.add(div); 
		this.add(text);
	}
	
}

package org.sagebionetworks.web.client.widget.table;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.LinkedGroupItem;
import org.gwtbootstrap3.client.ui.LinkedGroupItemText;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

/**
 * Simple linked list item for an entity.
 * 
 * @author jmhill
 *
 */
public class EntityLinkedGroupItem extends LinkedGroupItem {

	private static final String CREATED_ON = "Created On: ";

	static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
	
	private static final String ENTITY_LINK_PREFIX = "#!Synapse:";
	Heading heading;
	LinkedGroupItemText text;
	
	EntityLinkedGroupItem(HeadingSize size, EntityQueryResult header){
		this.heading = new Heading(size);
		this.heading.setText(header.getName());
		this.text = new LinkedGroupItemText();
		this.text.setText(CREATED_ON+DATE_FORMAT.format(header.getCreatedOn()));
		this.add(this.heading);
		this.add(this.text);
		this.setHref(ENTITY_LINK_PREFIX+header.getId());
	}
	
}

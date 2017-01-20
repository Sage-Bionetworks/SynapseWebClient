package org.sagebionetworks.web.client.widget.table;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.LinkedGroupItemText;
import org.gwtbootstrap3.client.ui.ListGroupItem;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.html.Br;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

/**
 * Simple list item for an entity.
 * 
 * @author jmhill
 *
 */
public class TableEntityListGroupItem extends ListGroupItem {

	private static final String CREATED_ON = "Created on ";
	private static final String MODIFIED_ON = "Modified on ";
	private static final String BY = " by ";

	static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
	
	TableEntityListGroupItem(HeadingSize size, EntityQueryResult header, UserBadge createdByUserBadge, UserBadge modifiedByUserBadge, ClickHandler clickHandler){
		addStyleName("padding-10");
		Heading iconHeading = new Heading(HeadingSize.H3);
		iconHeading.setPull(Pull.LEFT);
		Icon icon = new Icon(EntityTypeUtils.getIconTypeForEntityType(header.getEntityType()));
		icon.addStyleName("lightGreyText margin-right-10 moveup-10");
		iconHeading.add(icon);
		
		Heading heading = new Heading(size);
		Anchor anchor = new Anchor();
		anchor.setText(header.getName());
		anchor.addClickHandler(clickHandler);
		heading.add(anchor);
		heading.addStyleName("displayInline");
		
		LinkedGroupItemText createdOnDiv = new LinkedGroupItemText();
		createdOnDiv.add(new Text(CREATED_ON+DATE_FORMAT.format(header.getCreatedOn())));
		Span hiddenOnXs = new Span();
		hiddenOnXs.addStyleName("hidden-xs");
		createdOnDiv.add(hiddenOnXs);
		hiddenOnXs.add(new Text(BY));
		hiddenOnXs.add(createdByUserBadge);
		createdByUserBadge.asWidget().addStyleName("movedown-9 margin-right-10");
		
		hiddenOnXs.add(new Text(MODIFIED_ON+DATE_FORMAT.format(header.getModifiedOn())+BY));
		hiddenOnXs.add(modifiedByUserBadge);
		modifiedByUserBadge.asWidget().addStyleName("movedown-9");
		
		anchor = new Anchor("#!Synapse:"+header.getId());
		anchor.setTarget("_blank");
		anchor.setIcon(IconType.EXTERNAL_LINK);
		anchor.addStyleName("margin-left-10 moveup-2");
		
		Div div = new Div();
		div.add(iconHeading);
		div.add(heading);
		div.add(anchor);
		div.add(createdOnDiv);
		this.add(div); 
	}
	
}

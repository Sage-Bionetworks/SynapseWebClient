package org.sagebionetworks.web.client.widget.table;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.ListGroupItem;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.html.ClearFix;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.EntityTypeUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.TextBox;

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
	
	TableEntityListGroupItem(HeadingSize size, EntityHeader header, final ClickHandler clickHandler){
		addStyleName("padding-10");
		Heading iconHeading = new Heading(HeadingSize.H3);
		iconHeading.setPull(Pull.LEFT);
		Icon icon = new Icon(EntityTypeUtils.getIconTypeForEntityClassName(header.getType()));
		icon.addStyleName("lightGreyText margin-right-10 moveup-10");
		iconHeading.add(icon);
		
		Heading heading = new Heading(size);
		Anchor anchor = new Anchor();
		anchor.setHref("#!Synapse:"+header.getId());
		anchor.setText(header.getName());
		anchor.addClickHandler(event -> {
			event.preventDefault();
			clickHandler.onClick(event);
		});
		heading.add(anchor);
		heading.addStyleName("displayInline");
		
//		LinkedGroupItemText createdOnDiv = new LinkedGroupItemText();
//		createdOnDiv.add(new Text(CREATED_ON+DATE_FORMAT.format(header.getCreatedOn())));
//		Span hiddenOnXs = new Span();
//		hiddenOnXs.addStyleName("hidden-xs");
//		createdOnDiv.add(hiddenOnXs);
//		hiddenOnXs.add(new Text(BY));
//		hiddenOnXs.add(createdByUserBadge);
//		createdByUserBadge.asWidget().addStyleName("margin-right-10");
		
		// Uncomment when PLFM-3054/PLFM-4220 have been fixed.
//		hiddenOnXs.add(new Text(MODIFIED_ON+DATE_FORMAT.format(header.getModifiedOn())+BY));
//		hiddenOnXs.add(modifiedByUserBadge);
//		
		
		final TextBox synIdTextBox = new TextBox();
		synIdTextBox.addStyleName("hidden-xs right border-none noBackground margin-right-15");
		synIdTextBox.setReadOnly(true);
		synIdTextBox.setWidth("130px");
		synIdTextBox.setValue(header.getId());
		synIdTextBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				synIdTextBox.selectAll();
			}
		});
		
		Div div = new Div();
		div.add(new ClearFix());
		div.add(iconHeading);
		div.add(heading);
		div.add(synIdTextBox);
//		div.add(createdOnDiv);
		this.add(div); 
	}
	
}

package org.sagebionetworks.web.client.widget.docker;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.LinkedGroupItemText;
import org.gwtbootstrap3.client.ui.ListGroupItem;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.CallbackP;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

public class DockerRepoListGroupItem extends ListGroupItem {

	private static final String LAST_UPDATED = "Last Updated: ";
	static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
	Anchor anchor = new Anchor();

	public DockerRepoListGroupItem(HeadingSize size, EntityHeader entityHeader, CallbackP<String> entityClickedHandler) {
		addStyleName("padding-10");
		Heading iconHeading = new Heading(HeadingSize.H3);
		iconHeading.setPull(Pull.LEFT);
		Icon icon = new Icon(IconType.ARCHIVE);
		icon.addStyleName("lightGreyText margin-right-10 moveup-10");
		iconHeading.add(icon);

		Heading heading = new Heading(size);
		anchor.setText("Loading...");
		anchor.setHref("#!Synapse:" + entityHeader.getId());
		anchor.addClickHandler(event -> {
			if (!DisplayUtils.isAnyModifierKeyDown(event)) {
				event.preventDefault();
				entityClickedHandler.invoke(entityHeader.getId());
			}
		});
		heading.add(anchor);
		heading.addStyleName("displayInline");

		LinkedGroupItemText text = new LinkedGroupItemText();
		text.setText(LAST_UPDATED + DATE_FORMAT.format(entityHeader.getModifiedOn()));
		Anchor anchor = new Anchor("#!Synapse:" + entityHeader.getId());
		anchor.setTarget("_blank");
		anchor.setIcon(IconType.ANGLE_RIGHT);
		anchor.addStyleName("margin-right-10 moveup-2 pull-right h3");

		Div div = new Div();
		div.add(iconHeading);
		div.add(heading);
		div.add(anchor);
		div.add(text);
		this.add(div);
	}

	public void setDockerRepositoryName(String name) {
		anchor.setText(name);
	}
}

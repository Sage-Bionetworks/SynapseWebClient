package org.sagebionetworks.web.client.help;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Popover;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Trigger;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Text;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Context-sensitive help button/modal combination.
 * https://sagebionetworks.jira.com/wiki/display/SWC/Context-sensitive+help
 * 
 * 
 * Import: xmlns:h="urn:import:org.sagebionetworks.web.client.help"
 * 
 * Use: <h:HelpButton href="https://docs.synapse.org/discussionForum" placement="RIGHT">
 * <bh:Text><ui:text from='{help.discussionForum}'/></bh:Text> </h:HelpButton>
 * 
 */
public class HelpButton extends Div implements HasHTML {

	private final Icon helpButton = new Icon(IconType.QUESTION_CIRCLE);
	private final Popover popover = new Popover(helpButton);

	public HelpButton() {
		addStyleName("displayInline margin-left-5 margin-right-5");
		popover.setTrigger(Trigger.CLICK);
		popover.setIsHtml(true);
		helpButton.addStyleName("imageButton synapse-blue");
		add(helpButton);
	}

	public void setHref(String href) {
		popover.setContent("<a href=\"" + href + "\" target=\"_blank\"><button class=\"btn btn-primary right\">Learn More</button></a>");
	}

	public void setPlacement(Placement placement) {
		popover.setPlacement(placement);
	}

	@Override
	public void add(final Widget w) {
		if (w instanceof Text) {
			popover.setTitle(((Text) w).getText());
		} else {
			super.add(w);
		}
	}

	@Override
	public String getHTML() {
		return popover.getTitle();
	}

	@Override
	public String getText() {
		return popover.getTitle();
	}

	@Override
	public void setHTML(String html) {
		popover.setTitle(html);
	}

	@Override
	public void setText(String text) {
		popover.setTitle(text);
	}
}

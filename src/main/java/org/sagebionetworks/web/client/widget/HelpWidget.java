package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Popover;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.MarkdownIt;
import org.sagebionetworks.web.client.MarkdownItImpl;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * View only widget used to show a help icon (and help text).  When clicked, a popover is shown that contains basic help, and a More Info button.  
 * When the More Info button is clicked, the browser will open a new tab to the full help documentation (typically to the docs.synapse.org site).
 * 
 * ## Usage
 * 
 * In your ui.xml, add the help widget.
 * ```
 * xmlns:w="urn:import:org.sagebionetworks.web.client.widget"
 * <w:HelpWidget text="Optional help link text" help="This contains concise but basic help." href="http://link/to/more/help" />
 * ```
 * 
 * That's it!
 * You can set visibility and placement today, and we can easily extend it to have additional options in the future.
 * 
 * @author jayhodgson
 *
 */
public class HelpWidget implements IsWidget {
	// 10 second delay, then auto hide
	public static final int POPOVER_DELAY = 10000;
	
	@UiField
	SpanElement moreInfoText;
	@UiField
	Popover helpPopover;
	@UiField
	SpanElement icon;
	@UiField
	Anchor anchor;
	
	Widget widget;
	private static MarkdownIt markdownIt = new MarkdownItImpl();
	public interface Binder extends UiBinder<Widget, HelpWidget> {}
	private static Binder uiBinder = GWT.create(Binder.class);
	String text="", basicHelpText="", moreHelpHTML="", iconStyles="lightGreyText";
	Timer popoverHider;
	public HelpWidget() {
		widget = uiBinder.createAndBindUi(this);
		anchor.getElement().setAttribute("tabindex", "0");
		popoverHider = new Timer() { 
		    public void run() { 
		    	helpPopover.hide();
		    } 
		};
		anchor.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				helpPopover.toggle();
				popoverHider.cancel();
				popoverHider.schedule(POPOVER_DELAY);
			}
		});
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public void setIconStyles(String iconStyles) {
		this.iconStyles = iconStyles;
	}
	
	public void setHelpMarkdown(String md) {
		this.basicHelpText = markdownIt.markdown2Html(md, "");
	}
	
	public void setHref(String fullHelpHref) {
		if (DisplayUtils.isDefined(fullHelpHref)) {
			this.moreHelpHTML = "<div><a class=\"btn btn-primary btn-xs right\" target=\"_blank\" href=\"" + SafeHtmlUtils.htmlEscape(fullHelpHref) + "\" role=\"button\">More info</a></div>";
		}
	}
	
	@Override
	public Widget asWidget() {
		if (DisplayUtils.isDefined(iconStyles))
			icon.setClassName(iconStyles);
		moreInfoText.setInnerText(text);
		helpPopover.setContent(basicHelpText + moreHelpHTML);
		return widget;
	}
	
	public void setVisible(boolean visible) {
		widget.setVisible(visible);
	}
	
	public void setPlacement(final Placement placement) {
		helpPopover.setPlacement(placement);		
	}
	
	public void setAddStyleNames(String styleNames) {
		widget.addStyleName(styleNames);
	}
	
	public void setPull(Pull pull) {
		widget.addStyleName(pull.getCssName());
	}
}

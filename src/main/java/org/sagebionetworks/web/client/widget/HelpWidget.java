package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Popover;
import org.gwtbootstrap3.client.ui.constants.Placement;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
 * <w:HelpWidget ui:field="helpWidget" />
 * ```
 * 
 * In your code, bind it and configure.
 * ```
 * @UiField
 * HelpWidget helpWidget;
 * ...
 * helpWidget.configure(helpLinkText, basicHelp, fullHelpHref);
 * ```
 * 
 * That's it!
 * You can set visibility and placement today, and we can easily extend it to have additional options in the future.
 * 
 * @author jayhodgson
 *
 */
public class HelpWidget implements IsWidget {
	@UiField
	Anchor moreInfoLink;
	@UiField
	SpanElement moreInfoText;
	@UiField
	Popover helpPopover;
	Widget widget;
	public interface Binder extends UiBinder<Widget, HelpWidget> {}
	private static Binder uiBinder = GWT.create(Binder.class);
	String text, basicHelpText, fullHelpHref;
	public HelpWidget() {
		widget = uiBinder.createAndBindUi(this);
	}

	public void configure(String text, String basicHelpText, String fullHelpHref) {
		this.text = text;
		this.basicHelpText = basicHelpText;
		this.fullHelpHref = fullHelpHref;
	}
	
	public void setText(String text) {
		moreInfoText.setInnerText(text);
	}
	
	public void setHelp(String basicHelpText) {
		this.basicHelpText = basicHelpText;
	}
	
	public void setHref(String fullHelpHref) {
		this.fullHelpHref = fullHelpHref;
	}
	
	@Override
	public Widget asWidget() {
		if (text != null) {
			moreInfoText.setInnerText(text);
		}
		helpPopover.setContent(SafeHtmlUtils.htmlEscape(basicHelpText) + "<div><a class=\"btn btn-primary btn-xs right\" target=\"_blank\" href=\"" + SafeHtmlUtils.htmlEscape(fullHelpHref) + "\" role=\"button\">More info</a></div>");
		return widget;
	}
	
	public void setVisible(boolean visible) {
		widget.setVisible(visible);
	}
	
	public void setPlacement(final Placement placement) {
		helpPopover.setPlacement(placement);		
	}
}

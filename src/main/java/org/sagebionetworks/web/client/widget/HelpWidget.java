package org.sagebionetworks.web.client.widget;

import java.util.HashSet;
import java.util.Set;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Popover;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.MarkdownIt;
import org.sagebionetworks.web.client.MarkdownItImpl;
import org.sagebionetworks.web.client.SynapseJSNIUtilsImpl;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * View only widget used to show a help icon (and help text). When clicked, a popover is shown that
 * contains basic help, and a More Info button. When the More Info button is clicked, the browser
 * will open a new tab to the full help documentation (typically to the docs.synapse.org site).
 * 
 * ## Usage
 * 
 * In your ui.xml, add the help widget. ```
 * xmlns:w="urn:import:org.sagebionetworks.web.client.widget"
 * <w:HelpWidget text="Optional help link text" help="This contains concise but basic help." href=
 * "http://link/to/more/help" /> ```
 * 
 * That's it! You can set visibility and placement today, and we can easily extend it to have
 * additional options in the future.
 * 
 * @author jayhodgson
 *
 */
public class HelpWidget implements IsWidget {
	public static final Set<HelpWidget> POPOVERS = new HashSet<HelpWidget>();
	@UiField
	SpanElement moreInfoText;
	@UiField
	Popover helpPopover;
	@UiField
	SpanElement icon;
	@UiField
	Anchor anchor;
	@UiField
	SpanElement innerIconSpan;

	Widget widget;
	private String popoverElementId;
	private String closePopoverJs;
	private IconType iconType = IconType.QUESTION_CIRCLE;

	private static MarkdownIt markdownIt = new MarkdownItImpl(GWT.create(SynapseJSNIUtilsImpl.class));

	public interface Binder extends UiBinder<Widget, HelpWidget> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);
	String text = "", basicHelpText = "", moreHelpHTML = "", iconStyles = "lightGreyText", closeHTML = "";

	public HelpWidget() {
		widget = uiBinder.createAndBindUi(this);
		anchor.getElement().setAttribute("tabindex", "0");
		popoverElementId = HTMLPanel.createUniqueId();
		helpPopover.getWidget().getElement().setId(popoverElementId);
		closePopoverJs = "window.jQuery('#" + popoverElementId + "').popover('hide')";
		closeHTML = "<button class=\"btn btn-default btn-xs right margin-right-5\" onClick=\"" + closePopoverJs + "\">Close</button>";
		POPOVERS.add(this);
		anchor.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hideOtherPopovers(HelpWidget.this);
				helpPopover.show();
			}
		});
		helpPopover.asWidget().addDomHandler(DisplayUtils.getESCKeyDownHandler(event -> {
			hidePopover();
		}), KeyDownEvent.getType());
	}

	public void setText(String text) {
		this.text = text;
		updateContent();
	}

	public void setIconType(IconType newType) {
		iconType = newType;
		updateContent();
	}

	public void setIconStyles(String iconStyles) {
		this.iconStyles = iconStyles;
		updateContent();
	}

	public void setHelpMarkdown(String md) {
		this.basicHelpText = markdownIt.markdown2Html(md, "");
		updateContent();
	}

	public void setHref(String fullHelpHref) {
		if (DisplayUtils.isDefined(fullHelpHref)) {
			this.moreHelpHTML = "<button class=\"btn btn-primary btn-xs right\" onClick=\"window.open('" + SafeHtmlUtils.htmlEscape(fullHelpHref) + "');" + closePopoverJs + "\">More info</button>";
		}
	}

	public void hidePopover() {
		helpPopover.hide();
	}

	@Override
	public Widget asWidget() {
		updateContent();
		return widget;
	}

	public void updateContent() {
		innerIconSpan.setClassName("fa " + iconType.getCssName());
		if (DisplayUtils.isDefined(iconStyles))
			icon.setClassName(iconStyles);
		moreInfoText.setInnerText(text);
		helpPopover.setContent(basicHelpText + moreHelpHTML + closeHTML);
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

	public void setAddMoreInfoStyleNames(String styleNames) {
		moreInfoText.addClassName(styleNames);
	}

	public void setAddAnchorStyleNames(String styleNames) {
		anchor.addStyleName(styleNames);
	}

	public void setPull(Pull pull) {
		widget.addStyleName(pull.getCssName());
	}

	public static void hideOtherPopovers(HelpWidget showingHelpWidget) {
		for (HelpWidget h : POPOVERS) {
			if (!showingHelpWidget.equals(h)) {
				h.getHelpPopover().hide();
			}
		}
	}

	public Popover getHelpPopover() {
		return helpPopover;
	}

	public void focus() {
		anchor.setFocus(true);
	}

	// SWC-4292: On static initialization of class, add a listener for the bootstrap modal close event.
	// If the event fires, make sure all popovers are hidden.
	static {
		_addModalHideListener();
	}

	private static native void _addModalHideListener() /*-{
		try {
			$wnd
					.jQuery($doc.body)
					.on(
							'hidden.bs.modal',
							function() {
								//call static method when modal is hidden
								@org.sagebionetworks.web.client.widget.HelpWidget::hideAllPopovers()();
							});
		} catch (err) {
			console.error(err);
		}
	}-*/;

	public static void hideAllPopovers() {
		for (HelpWidget h : POPOVERS) {
			h.getHelpPopover().hide();
		}
	}
}

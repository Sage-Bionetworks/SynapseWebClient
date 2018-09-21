package org.sagebionetworks.web.client.widget;

import java.util.HashSet;
import java.util.Set;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Icon;
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
import com.google.gwt.user.client.ui.TextBox;
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
public class TextBoxWithCopyToClipboardWidget implements IsWidget {
	public static final String SUCCESSFULLY_COPIED_TO_CLIPBOARD = "Successfully copied to clipboard";
	@UiField
	TextBox textBox;
	@UiField
	Icon icon;

	Widget widget;

	public interface Binder extends UiBinder<Widget, TextBoxWithCopyToClipboardWidget> {}
	private static Binder uiBinder = GWT.create(Binder.class);
	String text = "", width = "";
	IconType iconType = IconType.CLIPBOARD;

	public TextBoxWithCopyToClipboardWidget() {
		widget = uiBinder.createAndBindUi(this);
		icon.addClickHandler(event -> copyContentsToClipboard());
	}

	public void setText(String text) {
		this.text = text;
		textBox.setText(this.text);
	}

	public void setWidth(String width) {
		this.width = width;
		textBox.setWidth(width);
	}
	
	public void setIcon(IconType iconType) {
		this.iconType = iconType;
		icon.setType(iconType);
	}

	@Override
	public Widget asWidget() {
		textBox.setText(this.text);
		icon.setType(this.iconType);
		return widget;
	}
	
	public void setVisible(boolean visible) {
		widget.setVisible(visible);
	}

	public void setAddStyleNames(String styleNames) {
		widget.addStyleName(styleNames);
	}

	public void setPull(Pull pull) {
		widget.addStyleName(pull.getCssName());
	}

	/**
	 * See https://stackoverflow.com/questions/1317052/how-to-copy-to-clipboard-with-gwt
	 */
	private void copyContentsToClipboard() {
		textBox.setFocus(true);
		textBox.selectAll();
		boolean success = copyToClipboard();
		if (success) {
			DisplayUtils.showInfo(SUCCESSFULLY_COPIED_TO_CLIPBOARD);
		}
	}

	private static native boolean copyToClipboard() /*-{
        return $doc.execCommand('copy');
    }-*/;
}

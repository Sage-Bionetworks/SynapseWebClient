package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * View only widget used to show a text box and a 'copy to clipboard' icon. When the icon is
 * clicked, the text in the textbox is copied to the clipboard and a popup confirming the copy
 * appears ## Usage
 * 
 * In your ui.xml, add the TextBoxWithCopyToClipboardWidget. ```
 * xmlns:w="urn:import:org.sagebionetworks.web.client.widget"
 * <w:TextBoxWithCopyToClipboardWidget ui:field="copyToClipboardWidget" icon="CLIPBOARD" width=
 * "170px"/> ```
 * 
 * That's it! You can set visibility and placement today, and we can easily extend it to have
 * additional options in the future.
 * 
 * @author Nick Grosenbacher
 *
 */
public class TextBoxWithCopyToClipboardWidget implements IsWidget {
	public static final String SUCCESSFULLY_COPIED_TO_CLIPBOARD = "Successfully copied to clipboard";
	@UiField
	Anchor textAnchor;
	Widget widget;

	public interface Binder extends UiBinder<Widget, TextBoxWithCopyToClipboardWidget> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	public TextBoxWithCopyToClipboardWidget() {
		widget = uiBinder.createAndBindUi(this);
		textAnchor.addClickHandler(event -> copyContentsToClipboard());
	}

	public void setText(String text) {
		textAnchor.setText(text);
	}

	@Override
	public Widget asWidget() {
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
		TextBox textBox = new TextBox();
		((Span) widget).add(textBox);
		textBox.setFocus(true);
		textBox.setText(textAnchor.getText());
		textBox.selectAll();
		boolean success = copyToClipboard();
		textBox.removeFromParent();
		if (success) {
			DisplayUtils.showInfo(SUCCESSFULLY_COPIED_TO_CLIPBOARD);
		}
	}

	private static native boolean copyToClipboard() /*-{
		return $doc.execCommand('copy');
	}-*/;
}

package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.ValidationUtils;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ButtonLinkConfigViewImpl implements ButtonLinkConfigView {
	public interface ButtonLinkConfigViewImplUiBinder extends UiBinder<Widget, ButtonLinkConfigViewImpl> {}
	@UiField
	TextBox urlField;
	@UiField
	TextBox nameField;
	@UiField
	Radio infoButtonStyle;
	@UiField
	Radio defaultButtonStyle;
	
	@UiField
	Button previewButton;
	Widget widget;
	
	@Inject
	public ButtonLinkConfigViewImpl(ButtonLinkConfigViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
		
		ClickHandler updatePreviewClickHandler = event -> {
			updatePreviewButton();
		}; 
		infoButtonStyle.addClickHandler(updatePreviewClickHandler);
		defaultButtonStyle.addClickHandler(updatePreviewClickHandler);
		
		nameField.addKeyUpHandler(event -> {
			updatePreviewButton();
		});
	}
	
	private void updatePreviewButton() {
		ButtonType type = isHighlightButtonStyle() ? ButtonType.INFO : ButtonType.DEFAULT;
		previewButton.setType(type);
		previewButton.setText(getName());
	}
	
	@Override
	public void initView() {
		urlField.setValue("");
		nameField.setValue("");
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> descriptor) {
		String text = descriptor.get(WidgetConstants.TEXT_KEY);
		if (text != null)
			nameField.setValue(text);
		String url = descriptor.get(WidgetConstants.LINK_URL_KEY);
		if (url != null)
			urlField.setValue(url);
		boolean isHighlight = false;
		if (descriptor.containsKey(WebConstants.HIGHLIGHT_KEY)){
			isHighlight = Boolean.parseBoolean(descriptor.get(WebConstants.HIGHLIGHT_KEY));
		}
		setIsHighlightButtonStyle(isHighlight);
		
		updatePreviewButton();
	}

	@Override
	public void checkParams() throws IllegalArgumentException {
		if (!ValidationUtils.isValidUrl(urlField.getValue(), false))
			throw new IllegalArgumentException("Invalid URL: " + urlField.getValue());
		if (!ValidationUtils.isValidWidgetName(nameField.getValue()))
			throw new IllegalArgumentException("Invalid name: " + nameField.getValue());
	}

	@Override
	public Widget asWidget() {
		return widget;
	}	
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {
		if (nameField != null)
			nameField.setValue("");
		if (urlField != null)
			urlField.setValue("");
	}

	@Override
	public String getLinkUrl() {
		return urlField.getValue();
	}
	
	@Override
	public String getName() {
		return nameField.getValue();
	}
	
	@Override
	public boolean isHighlightButtonStyle() {
		return infoButtonStyle.getValue();
	}
	
	@Override
	public void setIsHighlightButtonStyle(boolean isHighlight) {
		infoButtonStyle.setValue(isHighlight, true);
		defaultButtonStyle.setValue(!isHighlight, true);
	}
}

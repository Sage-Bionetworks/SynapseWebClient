package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ButtonLinkConfigViewImpl extends LayoutContainer implements ButtonLinkConfigView {
	private Presenter presenter;
	private TextField<String> urlField;
	private TextField<String> nameField;
	
	@Inject
	public ButtonLinkConfigViewImpl() {
	}
	
	@Override
	public void initView() {
		VerticalPanel vp = new VerticalPanel();
		HorizontalPanel hp = new HorizontalPanel();
		hp.setVerticalAlign(VerticalAlignment.MIDDLE);
		urlField = new TextField<String>();
		urlField.setAllowBlank(false);
		urlField.setRegex(WebConstants.VALID_URL_REGEX);
		urlField.getMessages().setRegexText(DisplayConstants.IMAGE_CONFIG_INVALID_URL_MESSAGE);
		Label urlLabel = new Label(DisplayConstants.URL_LABEL);
		urlLabel.setWidth(60);
		urlField.setWidth(270);
		hp.add(urlLabel);
		hp.add(urlField);
		hp.addStyleName("margin-top-left-10");
		vp.add(hp);
		
		hp = new HorizontalPanel();
		hp.setVerticalAlign(VerticalAlignment.MIDDLE);
		nameField = new TextField<String>();
		nameField.setAllowBlank(false);
		nameField.setName("Text");
		nameField.setRegex(WebConstants.VALID_WIDGET_NAME_REGEX);
		nameField.getMessages().setRegexText(DisplayConstants.ERROR_WIDGET_NAME_PATTERN_MISMATCH);
		Label nameLabel = new Label("Button Text:");
		nameLabel.setWidth(60);
		nameField.setWidth(270);
		hp.add(nameLabel);
		hp.add(nameField);
		hp.addStyleName("margin-top-left-10");
		vp.add(hp);
		
		add(vp);
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> descriptor) {
		String text = descriptor.get(WidgetConstants.TEXT_KEY);
		if (text != null)
			nameField.setValue(text);
		String url = descriptor.get(WidgetConstants.LINK_URL_KEY);
		if (url != null)
			urlField.setValue(url);
	}

	@Override
	public void checkParams() throws IllegalArgumentException {
		if (!urlField.isValid())
			throw new IllegalArgumentException(urlField.getErrorMessage());
		if (!nameField.isValid())
			throw new IllegalArgumentException(nameField.getErrorMessage());
	}

	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
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
	public void setLinkUrl(String url) {
		urlField.setValue(url);
	}
	
	@Override
	public String getName() {
		return nameField.getValue();
	}
	@Override
	public void setName(String name) {
		nameField.setValue(name);
	}
	
	/*
	 * Private Methods
	 */

}

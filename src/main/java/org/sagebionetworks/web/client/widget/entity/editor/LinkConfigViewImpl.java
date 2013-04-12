package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.utils.AlertUtils;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LinkConfigViewImpl extends LayoutContainer implements LinkConfigView {
	private Presenter presenter;
	private TextField<String> urlField;
	private TextField<String> nameField;
	
	@Inject
	public LinkConfigViewImpl() {
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
		Label nameLabel = new Label("Link Text:");
		nameLabel.setWidth(60);
		nameField.setWidth(270);
		hp.add(nameLabel);
		hp.add(nameField);
		hp.addStyleName("margin-top-left-10");
		vp.add(hp);
		
		add(vp);
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
		AlertUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		AlertUtils.showInfo(title, message);
	}

	@Override
	public int getDisplayHeight() {
		return 60;
	}
	@Override
	public int getAdditionalWidth() {
		return 0;
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

package org.sagebionetworks.web.client.widget.entity.dialog.editors;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class YouTubeConfigViewImpl extends LayoutContainer implements YouTubeConfigView {

	private Presenter presenter;
	private TextField<String> urlField;
	
	@Inject
	public YouTubeConfigViewImpl() {
	}
	
	@Override
	public void initView() {
		//build the view
		HorizontalPanel hp = new HorizontalPanel();
		hp.setStyleAttribute("margin", "10px");
		hp.setVerticalAlign(VerticalAlignment.MIDDLE);
		urlField = new TextField<String>();
		urlField.setAllowBlank(false);
		urlField.setRegex(WebConstants.VALID_URL_REGEX);
		urlField.getMessages().setRegexText("Enter a valid URL");
		Label urlLabel = new Label("Video URL:");
		urlLabel.setWidth(70);
		urlField.setWidth(185);
		hp.add(urlLabel);
		hp.add(urlField);
		add(hp);
	}
	@Override
	public void checkParams() throws IllegalArgumentException {
		if (!urlField.isValid())
			throw new IllegalArgumentException(urlField.getErrorMessage());
	}
	@Override
	public String getVideoUrl() {
		return urlField.getValue();
	}
	
	@Override
	public void setVideoUrl(String url) {
		urlField.setValue(url);
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
	public int getDisplayHeight() {
		return 50;
	}
	
	@Override
	public void clear() {
	}
	
	/*
	 * Private Methods
	 */

}

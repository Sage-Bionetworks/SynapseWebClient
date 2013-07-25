package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;

public class BookmarkConfigViewImpl extends LayoutContainer implements BookmarkConfigView {
	private Presenter presenter;
	private TextField<String> linkTextField;
	private TextField<String> bookmarkIdField;
	
	@Inject
	public BookmarkConfigViewImpl() {
	}
	
	@Override
	public void initView() {
		VerticalPanel vp = new VerticalPanel();
		HorizontalPanel hp = new HorizontalPanel();
		hp.setVerticalAlign(VerticalAlignment.MIDDLE);
		linkTextField = new TextField<String>();
		linkTextField.setAllowBlank(false);
		Label linkTextLabel = new Label(DisplayConstants.LINK_TEXT_LABEL);
		linkTextLabel.setWidth(70);
		linkTextField.setWidth(270);
		hp.add(linkTextLabel);
		hp.add(linkTextField);
		hp.addStyleName("margin-top-left-10");
		vp.add(hp);
		
		hp = new HorizontalPanel();
		hp.setVerticalAlign(VerticalAlignment.MIDDLE);
		bookmarkIdField = new TextField<String>();
		bookmarkIdField.setAllowBlank(false);
		bookmarkIdField.setRegex(WebConstants.VALID_BOOKMARK_ID_REGEX);
		bookmarkIdField.getMessages().setRegexText(DisplayConstants.ERROR_BOOKMARK_ID);
		Label bookmarkIdLabel = new Label("Bookmark ID:");
		bookmarkIdLabel.setWidth(70);
		bookmarkIdField.setWidth(270);
		hp.add(bookmarkIdLabel);
		hp.add(bookmarkIdField);
		hp.addStyleName("margin-top-left-10");
		vp.add(hp);
		
		add(vp);
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
	public void checkParams() throws IllegalArgumentException {
		if (!linkTextField.isValid()) {
			throw new IllegalArgumentException(linkTextField.getErrorMessage());
		} 
		if (!bookmarkIdField.isValid()) {
			throw new IllegalArgumentException(bookmarkIdField.getErrorMessage());
		}
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
		if (linkTextField != null) {
			linkTextField.setValue("");
		}
		if (bookmarkIdField != null) {
			bookmarkIdField.setValue("");
		}
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setLinkText(String linkText) {
		linkTextField.setValue(linkText);
	}

	@Override
	public String getLinkText() {
		return linkTextField.getValue();
	}

	@Override
	public void setTargetId(String targetId) {
		bookmarkIdField.setValue(targetId);
	}

	@Override
	public String getTargetId() {
		return bookmarkIdField.getValue();
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

}

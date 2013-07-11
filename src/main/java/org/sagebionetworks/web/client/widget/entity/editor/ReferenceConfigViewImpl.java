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
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ReferenceConfigViewImpl extends LayoutContainer implements ReferenceConfigView {
	private Presenter presenter;
	private TextField<String> authorField;
	private TextField<String> titleField;
	private TextField<String> dateField;
	
	@Inject
	public ReferenceConfigViewImpl() {
	}

	@Override
	public void initView() {
		VerticalPanel vp = new VerticalPanel();
		HorizontalPanel hp = new HorizontalPanel();
		hp.setVerticalAlign(VerticalAlignment.MIDDLE);
		authorField = new TextField<String>();
		authorField.setAllowBlank(false);
		authorField.setRegex(WebConstants.VALID_WIDGET_NAME_REGEX);
		authorField.getMessages().setRegexText(DisplayConstants.ERROR_WIDGET_NAME_PATTERN_MISMATCH);
		Label authorLabel = new Label(DisplayConstants.REFERENCE_AUTHOR_LABEL);
		authorLabel.setWidth(60);
		authorField.setWidth(270);
		hp.add(authorLabel);
		hp.add(authorField);
		hp.addStyleName("margin-top-left-10");
		vp.add(hp);
		
		hp = new HorizontalPanel();
		hp.setVerticalAlign(VerticalAlignment.MIDDLE);
		titleField = new TextField<String>();
		titleField.setAllowBlank(false);
		titleField.setRegex(WebConstants.VALID_WIDGET_NAME_REGEX);
		titleField.getMessages().setRegexText(DisplayConstants.ERROR_WIDGET_NAME_PATTERN_MISMATCH);
		Label titleLabel = new Label(DisplayConstants.REFERENCE_TITLE_LABEL);
		titleLabel.setWidth(60);
		titleField.setWidth(270);
		hp.add(titleLabel);
		hp.add(titleField);
		hp.addStyleName("margin-top-left-10");
		vp.add(hp);
		
		hp = new HorizontalPanel();
		hp.setVerticalAlign(VerticalAlignment.MIDDLE);
		dateField = new TextField<String>();
		dateField.setAllowBlank(false);
		dateField.setRegex(WebConstants.VALID_REFERENCE_DATE_REGEX);
		dateField.getMessages().setRegexText(DisplayConstants.REFERENCE_INVALID_DATE);
		Label dateLabel = new Label(DisplayConstants.REFERENCE_DATE_LABEL);
		dateLabel.setWidth(60);
		dateField.setWidth(270);
		hp.add(dateLabel);
		hp.add(dateField);
		hp.addStyleName("margin-top-left-10");
		vp.add(hp);
		
		add(vp);
	}
	
	@Override
	public Widget asWidget() {
		return this;
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
		if(!authorField.isValid()) {
			throw new IllegalArgumentException(authorField.getErrorMessage());
		}
		if(!titleField.isValid()) {
			throw new IllegalArgumentException(titleField.getErrorMessage());
		}
		if(!dateField.isValid()) {
			throw new IllegalArgumentException(dateField.getErrorMessage());
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
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void clear() {
		if(authorField != null) {
			authorField.setValue("");
		}
		if(titleField != null) {
			titleField.setValue("");
		}
		if(dateField != null) {
			dateField.setValue("");
		}
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setAuthor(String author) {
		authorField.setValue(author);
	}

	@Override
	public String getAuthor() {
		return authorField.getValue();
	}

	@Override
	public void setTitle(String title) {
		titleField.setValue(title);
	}

	@Override
	public String getTitle() {
		return titleField.getValue();
	}

	@Override
	public void setDate(String date) {
		dateField.setValue(date);
	}

	@Override
	public String getDate() {
		return dateField.getValue();
	}

}

package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Date;
import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

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
	private String bookmarkId;
	
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
		add(vp);
		
		Date time = new Date();
		bookmarkId = String.valueOf(time.getTime());
	}

	@Override
	public void configure(WikiPageKey wikiKey,
			Map<String, String> widgetDescriptor) {
		String text = widgetDescriptor.get(WidgetConstants.TEXT_KEY);
		if (text != null) {
			linkTextField.setValue(text);
		}
		String id = widgetDescriptor.get(WidgetConstants.BOOKMARK_KEY);
		if(id != null) {
			bookmarkId = id;
		}
	}
	
	@Override
	public void checkParams() throws IllegalArgumentException {
		if (!linkTextField.isValid()) {
			throw new IllegalArgumentException(linkTextField.getErrorMessage());
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
	public void setBookmarkId(String targetId) {
		bookmarkId = targetId;
	}

	@Override
	public String getBookmarkId() {
		return bookmarkId;
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

}

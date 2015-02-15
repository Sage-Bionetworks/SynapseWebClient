package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Date;
import java.util.Map;

import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BookmarkConfigViewImpl implements BookmarkConfigView {
	private Presenter presenter;
	private String bookmarkId;
	
	public interface BookmarkConfigViewImplUiBinder extends UiBinder<Widget, BookmarkConfigViewImpl> {}
	
	private Widget widget;
	@UiField
	TextBox linkTextField;

	
	@Inject
	public BookmarkConfigViewImpl(BookmarkConfigViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public void initView() {
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
		if (!DisplayUtils.isDefined(linkTextField.getValue())) {
			throw new IllegalArgumentException("Please fill in the link text");
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

	@Override
	public Widget asWidget() {
		return widget;
	}

}

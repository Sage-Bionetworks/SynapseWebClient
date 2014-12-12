package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BookmarkConfigEditor implements BookmarkConfigView.Presenter, WidgetEditorPresenter{
	private BookmarkConfigView view;
	private Map<String, String> descriptor;
	
	@Inject
	public BookmarkConfigEditor(BookmarkConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void configure(WikiPageKey wikiKey,
			Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;
		view.configure(wikiKey, descriptor);
	}

	@Override
	public void updateDescriptorFromView() throws IllegalArgumentException {
		view.checkParams();
		descriptor.put(WidgetConstants.TEXT_KEY, view.getLinkText());
		descriptor.put(WidgetConstants.BOOKMARK_KEY, view.getBookmarkId());
	}

	@Override
	public String getTextToInsert() {
		return "[" + view.getLinkText() + "](" + WidgetConstants.BOOKMARK_LINK_IDENTIFIER + ":" + view.getBookmarkId() + ")\n${" + WidgetConstants.BOOKMARK_TARGET_CONTENT_TYPE + "?" + WidgetConstants.BOOKMARK_KEY + "=" + view.getBookmarkId() + "}";
	}
	
	@Override
	public List<String> getNewFileHandleIds() {
		return null;
	}

}

package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BookmarkConfigEditor implements BookmarkConfigView.Presenter, WidgetEditorPresenter{
	private BookmarkConfigView view;
	
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
			Map<String, String> widgetDescriptor) {
	}

	@Override
	public void updateDescriptorFromView() throws IllegalArgumentException {
		view.checkParams();
	}

	@Override
	public int getDisplayHeight() {
		return view.getDisplayHeight();
	}

	@Override
	public int getAdditionalWidth() {
		return view.getAdditionalWidth();
	}

	@Override
	public String getTextToInsert() {
		return "[" + view.getLinkText() + "](" + WidgetConstants.BOOKMARK_LINK_IDENTIFIER + ":" + view.getTargetId() + ")\n${" + WidgetConstants.BOOKMARK_TARGET_CONTENT_TYPE + "?" + WidgetConstants.BOOKMARK_KEY + "=" + view.getTargetId() + "}";
	}

}

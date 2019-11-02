package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ButtonLinkConfigEditor implements WidgetEditorPresenter {
	private Map<String, String> descriptor;
	private ButtonLinkConfigView view;

	@Inject
	public ButtonLinkConfigEditor(ButtonLinkConfigView view) {
		this.view = view;
		view.initView();
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;
		view.configure(wikiKey, widgetDescriptor);
	}

	public void clearState() {
		view.clear();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void updateDescriptorFromView() {
		// update widget descriptor from the view
		view.checkParams();
		descriptor.clear();
		descriptor.put(WidgetConstants.TEXT_KEY, view.getName());
		descriptor.put(WidgetConstants.LINK_URL_KEY, view.getLinkUrl());
		if (view.isHighlightButtonStyle()) {
			descriptor.put(WebConstants.HIGHLIGHT_KEY, Boolean.toString(true));
		}
		descriptor.put(WidgetConstants.ALIGNMENT_KEY, view.getAlignment());
	}

	@Override
	public String getTextToInsert() {
		return null;
	}

	@Override
	public List<String> getNewFileHandleIds() {
		return null;
	}

	@Override
	public List<String> getDeletedFileHandleIds() {
		return null;
	}
}

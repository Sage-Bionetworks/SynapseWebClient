package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidget;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ShinySiteConfigEditor implements WidgetEditorPresenter {

	private ShinySiteConfigView view;
	private Map<String, String> descriptor;

	@Inject
	public ShinySiteConfigEditor(ShinySiteConfigView view) {
		this.view = view;
		view.initView();
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;
		String siteUrl = descriptor.get(WidgetConstants.SHINYSITE_SITE_KEY);
		int height = ShinySiteWidget.getHeightFromDescriptor(descriptor);
		boolean isIncludePrincipalId = ShinySiteWidget.isIncludePrincipalId(descriptor);
		if (siteUrl != null)
			view.configure(siteUrl, height, isIncludePrincipalId);
	}

	@SuppressWarnings("unchecked")
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
		try {
			descriptor.put(WidgetConstants.SHINYSITE_SITE_KEY, view.getSiteUrl());
			descriptor.put(WidgetConstants.INCLUDE_PRINCIPAL_ID_KEY, view.isIncludePrincipalId().toString());
			if (view.getSiteHeight() != null)
				descriptor.put(WidgetConstants.HEIGHT_KEY, String.valueOf(view.getSiteHeight()));
		} catch (IllegalArgumentException e) {
			view.showErrorMessage(e.getMessage());
		}
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
	/*
	 * Private Methods
	 */
}

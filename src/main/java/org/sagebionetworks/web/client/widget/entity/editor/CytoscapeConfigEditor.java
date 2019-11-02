package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CytoscapeConfigEditor implements CytoscapeConfigView.Presenter, WidgetEditorPresenter {

	private CytoscapeConfigView view;
	private Map<String, String> descriptor;

	@Inject
	public CytoscapeConfigEditor(CytoscapeConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;
		if (descriptor.get(WidgetConstants.SYNAPSE_ID_KEY) != null)
			view.setEntity(descriptor.get(WidgetConstants.SYNAPSE_ID_KEY));

		if (descriptor.get(WidgetConstants.STYLE_SYNAPSE_ID_KEY) != null) {
			view.setStyleEntity(descriptor.get(WidgetConstants.STYLE_SYNAPSE_ID_KEY));
		}
		if (descriptor.get(WidgetConstants.HEIGHT_KEY) != null) {
			view.setHeight(descriptor.get(WidgetConstants.HEIGHT_KEY));
		}
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
		if (!"".equals(view.getEntity()))
			descriptor.put(WidgetConstants.SYNAPSE_ID_KEY, view.getEntity());
		if (!"".equals(view.getStyleEntity()))
			descriptor.put(WidgetConstants.STYLE_SYNAPSE_ID_KEY, view.getStyleEntity());
		if (!"".equals(view.getHeight()))
			descriptor.put(WidgetConstants.HEIGHT_KEY, view.getHeight());
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

package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageConfigEditor implements ImageConfigView.Presenter, WidgetEditorPresenter {
	
	private ImageConfigView view;
	private Map<String, String> descriptor;
	private List<String> fileHandleIds;
	
	@Inject
	public ImageConfigEditor(ImageConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;
		fileHandleIds = new ArrayList<String>();
		view.configure(wikiKey, dialogCallback);
		//and try to prepopulate with values from the map.  if it fails, ignore
		try {
			if (descriptor.containsKey(WidgetConstants.IMAGE_WIDGET_SYNAPSE_ID_KEY) || descriptor.containsKey(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY)) {
				if (descriptor.containsKey(WidgetConstants.IMAGE_WIDGET_SYNAPSE_ID_KEY)){
					view.setSynapseId(descriptor.get(WidgetConstants.IMAGE_WIDGET_SYNAPSE_ID_KEY));
				} else if (descriptor.containsKey(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY)){
					view.setUploadedFileHandleName(descriptor.get(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY));
					dialogCallback.setPrimaryEnabled(true);
				}
				view.setAlignment(descriptor.get(WidgetConstants.IMAGE_WIDGET_ALIGNMENT_KEY));
				view.setScale(descriptor.get(WidgetConstants.IMAGE_WIDGET_SCALE_KEY));
			}
		} catch (Exception e) {}
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
		view.checkParams();
		if (!view.isExternal()) {
			if (view.isSynapseEntity())
				descriptor.put(WidgetConstants.IMAGE_WIDGET_SYNAPSE_ID_KEY, view.getSynapseId());
			else
				descriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, view.getUploadedFileHandleName());
			descriptor.put(WidgetConstants.IMAGE_WIDGET_ALIGNMENT_KEY, view.getAlignment());
			descriptor.put(WidgetConstants.IMAGE_WIDGET_SCALE_KEY, view.getScale());
		}
	}
	
	@Override
	public String getTextToInsert() {
		if (view.isExternal())
			return "!["+view.getAltText()+"]("+view.getImageUrl()+")";
		else return null;
	}
	
	@Override
	public void addFileHandleId(String fileHandleId) {
		fileHandleIds.add(fileHandleId);
	}

	@Override
	public List<String> getNewFileHandleIds() {
		return fileHandleIds;
	}
	/*
	 * Private Methods
	 */
}

package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class RestServiceButtonConfigEditor implements RestServiceButtonConfigView.Presenter, WidgetEditorPresenter {
	
	private RestServiceButtonConfigView view;
	private Map<String, String> descriptor;
	
	@Inject
	public RestServiceButtonConfigEditor(RestServiceButtonConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;

		if(widgetDescriptor.containsKey(WidgetConstants.URI_KEY)) {
			view.setUri(widgetDescriptor.get(WidgetConstants.URI_KEY));
		}
		if(widgetDescriptor.containsKey(WidgetConstants.REQUEST_JSON_KEY)) {
			view.setRequestJson(widgetDescriptor.get(WidgetConstants.REQUEST_JSON_KEY));
		}
		if(widgetDescriptor.containsKey(WidgetConstants.TEXT_KEY)) {
			view.setButtonText(widgetDescriptor.get(WidgetConstants.TEXT_KEY));
		}
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
		//update widget descriptor from the view
		view.checkParams();
		
		descriptor.put(WidgetConstants.URI_KEY, view.getURI());
		descriptor.put(WidgetConstants.METHOD_KEY, view.getMethod());
		descriptor.put(WidgetConstants.REQUEST_JSON_KEY, view.getRequestJson());
		descriptor.put(WidgetConstants.TEXT_KEY, view.getButtonText());
		descriptor.put(WidgetConstants.BUTTON_TYPE_KEY, view.getButtonType());
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

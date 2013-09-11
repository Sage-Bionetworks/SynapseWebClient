package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.extjs.gxt.ui.client.widget.Dialog;
public class ReferenceConfigEditor implements ReferenceConfigView.Presenter, WidgetEditorPresenter {
	private Map<String, String> descriptor;
	private ReferenceConfigView view;
	
	@Inject
	public ReferenceConfigEditor(ReferenceConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Dialog window) {
		descriptor = widgetDescriptor;
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void updateDescriptorFromView() {
		view.checkParams();
		
		//Add the inline parameter to make the widget render inline
		descriptor.put(WidgetConstants.INLINE_WIDGET_KEY, "true");
		descriptor.put(WidgetConstants.TEXT_KEY, view.getReference());
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
		return null;
	}
}

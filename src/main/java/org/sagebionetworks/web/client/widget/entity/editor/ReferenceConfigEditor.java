package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

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
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor) {
		descriptor = widgetDescriptor;
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void updateDescriptorFromView() {
		view.checkParams();
		descriptor.put(WidgetConstants.REFERENCE_TEXT_KEY, view.getReference());
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

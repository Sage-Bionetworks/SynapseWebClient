package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ReferenceConfigEditor implements ReferenceConfigView.Presenter, WidgetEditorPresenter {
	private ReferenceConfigView view;
	
	@Inject
	public ReferenceConfigEditor(ReferenceConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor) {
		//no way to edit an existing link
	}
	
	@Override
	public void updateDescriptorFromView() {
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
		return "^[" + view.getAuthor() + ". " + view.getTitle() + ". " + view.getDate()+ "]";
	}
}

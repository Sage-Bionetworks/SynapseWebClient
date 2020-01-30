package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageLinkConfigEditor implements WidgetEditorPresenter {

	private ImageConfigEditor imageConfigEditor;

	@Inject
	public ImageLinkConfigEditor(ImageConfigEditor imageConfigEditor) {
		this.imageConfigEditor = imageConfigEditor;
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, final DialogCallback dialogCallback) {
		imageConfigEditor.configureWithoutUpload(wikiKey, widgetDescriptor, dialogCallback);
	}

	@Override
	public Widget asWidget() {
		return imageConfigEditor.asWidget();
	}

	@Override
	public void updateDescriptorFromView() {
		imageConfigEditor.updateDescriptorFromView();
	}

	@Override
	public String getTextToInsert() {
		return imageConfigEditor.getTextToInsert();
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

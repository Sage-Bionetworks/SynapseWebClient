package org.sagebionetworks.web.client.presenter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.widget.entity.dialog.BaseEditWidgetDescriptorView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrarImpl;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.inject.Inject;

public class BaseEditWidgetDescriptorPresenter implements BaseEditWidgetDescriptorView.Presenter {

	private BaseEditWidgetDescriptorView view;
	String contentTypeKey, attachmentName;
	// contains all of the widget specific parameters
	Map<String, String> widgetDescriptor;
	private WidgetRegistrar widgetRegistrar;
	WidgetDescriptorUpdatedHandler handler;

	@Inject
	public BaseEditWidgetDescriptorPresenter(BaseEditWidgetDescriptorView view, WidgetRegistrar widgetRegistrar) {
		this.widgetRegistrar = widgetRegistrar;
		this.view = view;
		this.view.setPresenter(this);
	}

	@Override
	public void apply() {
		view.clearErrors();
		// widgetDescriptor should have all of the updated parameter info. But we do need to ask for the
		// widget name from the view.
		// ask for the new file handles that the editor added (if any)
		List<String> newFileHandleIds = view.getNewFileHandleIds();
		List<String> deletedFileHandleIds = view.getDeletedFileHandleIds();
		try {
			view.updateDescriptorFromView();
			String textToInsert = view.getTextToInsert();
			if (textToInsert != null) {
				// this is it!
				fireUpdatedEvent(textToInsert, newFileHandleIds, deletedFileHandleIds);
				view.hide();
				return;
			}
		} catch (IllegalArgumentException e) {
			// invalid param, just show a message and return
			view.showErrorMessage(e.getMessage());
			return;
		}
		try {
			fireUpdatedEvent(WidgetRegistrarImpl.getWidgetMarkdown(contentTypeKey, widgetDescriptor, widgetRegistrar), newFileHandleIds, deletedFileHandleIds);
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(e.getMessage());
		}
		view.hide();
	}

	public void fireUpdatedEvent(String valueToInsert, List<String> newFileHandleIds, List<String> deletedFileHandleIds) {
		// fire event that contains a value to insert into the description
		WidgetDescriptorUpdatedEvent event = new WidgetDescriptorUpdatedEvent();
		event.setNewFileHandleIds(newFileHandleIds);
		event.setDeletedFileHandleIds(deletedFileHandleIds);
		event.setInsertValue(valueToInsert);
		fireUpdatedEvent(event);
	}

	public void fireUpdatedEvent(WidgetDescriptorUpdatedEvent event) {
		handler.onUpdate(event);
	}

	@Override
	public void addWidgetDescriptorUpdatedHandler(WidgetDescriptorUpdatedHandler handler) {
		this.handler = handler;
	}

	@Override
	public void editNew(WikiPageKey wikiKey, String contentTypeKey) {
		editExisting(wikiKey, contentTypeKey, new HashMap<String, String>());
	}

	@Override
	public void editExisting(WikiPageKey wikiKey, String contentTypeKey, Map<String, String> descriptor) {
		if (contentTypeKey == null)
			throw new IllegalArgumentException("content type key cannot be null");
		cleanInit();
		this.contentTypeKey = contentTypeKey;

		// initialize the view with a new widget descriptor definition of the correct type and show
		widgetDescriptor = descriptor;
		view.setWidgetDescriptor(wikiKey, contentTypeKey, widgetDescriptor);
		view.show();
	}

	private void cleanInit() {
		contentTypeKey = null;
		attachmentName = null;
		view.clear();
	}

}

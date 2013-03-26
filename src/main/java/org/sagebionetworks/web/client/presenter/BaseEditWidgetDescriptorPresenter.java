package org.sagebionetworks.web.client.presenter;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.widget.entity.dialog.BaseEditWidgetDescriptorView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrarImpl;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.shared.HandlerManager;
import com.google.inject.Inject;

public class BaseEditWidgetDescriptorPresenter implements BaseEditWidgetDescriptorView.Presenter {
	
	private HandlerManager handlerManager;
	private BaseEditWidgetDescriptorView view;
	String contentTypeKey, attachmentName;
	//contains all of the widget specific parameters
	Map<String, String> widgetDescriptor;
	private WidgetRegistrar widgetRegistrar;
	private WikiPageKey wikiKey;
	
	@Inject
	public BaseEditWidgetDescriptorPresenter(BaseEditWidgetDescriptorView view, WidgetRegistrar widgetRegistrar){
		this.widgetRegistrar = widgetRegistrar;
		this.view = view;
		this.view.setPresenter(this);
		handlerManager = new HandlerManager(this);
	}

	/**
	 * Pop up an editor to create a new widget of the given class type (class that implements WidgetDescriptor).  Add the given handler, which will be notified when the widget descriptor has been updated.
	 * @param entityId
	 * @param attachmentName
	 * @param handler
	 */
	public static void editNewWidget(BaseEditWidgetDescriptorPresenter presenter, WikiPageKey wikiKey, String contentTypeKey, WidgetDescriptorUpdatedHandler handler, boolean isWiki) {
		presenter.addWidgetDescriptorUpdatedHandler(handler);
		presenter.editNew(wikiKey, contentTypeKey, isWiki);
	}
	
	@Override
	public void apply() {
		//widgetDescriptor should have all of the updated parameter info.  But we do need to ask for the widget name from the view.
		try {
			view.updateDescriptorFromView();
			String textToInsert = view.getTextToInsert();
			if (textToInsert != null) {
				//this is it!
				fireUpdatedEvent(textToInsert);
				view.hide();
				return;
			}
		} catch (IllegalArgumentException e) {
			//invalid param, just show a message and return
			view.showErrorMessage(e.getMessage());
			return;
		}
		try {
			fireUpdatedEvent(WidgetRegistrarImpl.getWidgetMarkdown(contentTypeKey, widgetDescriptor, widgetRegistrar));
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(e.getMessage());
		}
		view.hide();
	}
	
	public void fireUpdatedEvent(String valueToInsert) {
		//fire event that contains a value to insert into the description
		WidgetDescriptorUpdatedEvent event = new WidgetDescriptorUpdatedEvent();
		event.setInsertValue(valueToInsert);
		fireUpdatedEvent(event);
	}
	
	public void fireUpdatedEvent(WidgetDescriptorUpdatedEvent event) {
		handlerManager.fireEvent(event);
	}
	
	@Override
	public void addWidgetDescriptorUpdatedHandler(WidgetDescriptorUpdatedHandler handler) {
		clearHandlers();
		handlerManager.addHandler(WidgetDescriptorUpdatedEvent.getType(), handler);
	}
	
	private void clearHandlers() {
		while(handlerManager.getHandlerCount(WidgetDescriptorUpdatedEvent.getType()) > 0) {
			handlerManager.removeHandler(WidgetDescriptorUpdatedEvent.getType(), handlerManager.getHandler(WidgetDescriptorUpdatedEvent.getType(), 0));
		}
	}
	
	@Override
	public void editNew(WikiPageKey wikiKey, String contentTypeKey, boolean isWiki) {
		if(wikiKey == null) throw new IllegalArgumentException("wiki key cannot be null");
		if(wikiKey.getOwnerObjectId() == null) throw new IllegalArgumentException("ownerObjectId cannot be null");
		if(wikiKey.getOwnerObjectType() == null) throw new IllegalArgumentException("ownerObjectType cannot be null");
		if(contentTypeKey == null) throw new IllegalArgumentException("content type key cannot be null");
		cleanInit();
		this.wikiKey = wikiKey;
		this.contentTypeKey = contentTypeKey;
		
		//initialize the view with a new widget descriptor definition of the correct type and show
		widgetDescriptor = new HashMap<String, String>();
		view.setWidgetDescriptor(wikiKey, contentTypeKey, widgetDescriptor, isWiki);
		//prepopulate with a unique attachment name of the correct type
		String friendlyName = widgetRegistrar.getFriendlyTypeName(contentTypeKey);
		view.show(friendlyName);
		view.setSaveButtonText(DisplayConstants.INSERT_BUTTON_LABEL);
	}
	
	private void cleanInit() {
		wikiKey = null;
		contentTypeKey = null;
		attachmentName = null;
		view.clear();
	}

}

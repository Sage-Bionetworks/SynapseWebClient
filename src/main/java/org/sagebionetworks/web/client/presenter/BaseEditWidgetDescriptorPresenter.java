package org.sagebionetworks.web.client.presenter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.dialog.BaseEditWidgetDescriptorView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class BaseEditWidgetDescriptorPresenter implements BaseEditWidgetDescriptorView.Presenter {
	
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private HandlerManager handlerManager;
	private BaseEditWidgetDescriptorView view;
	String contentTypeKey, entityId, attachmentName;
	JSONObjectAdapter jsonObjectAdapter;
	//contains all of the widget specific parameters
	WidgetDescriptor widgetDescriptor;
	private List<AttachmentData> attachments;
	private WidgetRegistrar widgetRegistrar;
	
	@Inject
	public BaseEditWidgetDescriptorPresenter(BaseEditWidgetDescriptorView view, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator, JSONObjectAdapter jsonObjectAdapter, WidgetRegistrar widgetRegistrar){
		this.widgetRegistrar = widgetRegistrar;
		this.view = view;
		this.view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
		handlerManager = new HandlerManager(this);
	}

	
	/**
	 * Pop up an editor for the existing widget.  Add the given handler, which will be notified if the widget descriptor has been updated.
	 * @param entityId
	 * @param attachmentName
	 * @param handler
	 */
	public static void editExistingWidget(BaseEditWidgetDescriptorPresenter presenter, String entityId, String attachmentName, List<AttachmentData> attachments, WidgetDescriptorUpdatedHandler handler) {
		presenter.addWidgetDescriptorUpdatedHandler(handler);
		presenter.editExisting(entityId, attachmentName, attachments);
	}
	
	/**
	 * Pop up an editor to create a new widget of the given class type (class that implements WidgetDescriptor).  Add the given handler, which will be notified when the widget descriptor has been updated.
	 * @param entityId
	 * @param attachmentName
	 * @param handler
	 */
	public static void editNewWidget(BaseEditWidgetDescriptorPresenter presenter, String entityId, String contentTypeKey, List<AttachmentData> attachments, WidgetDescriptorUpdatedHandler handler) {
		presenter.addWidgetDescriptorUpdatedHandler(handler);
		presenter.editNew(entityId, contentTypeKey, attachments);
	}
	
	public static String getUniqueAttachmentName(List<AttachmentData> attachments, String attachmentName) {
		if (attachments == null || attachments.size() == 0)
			return attachmentName;
		//throw all the names in a hashset, and see if the value is in there
		HashSet<String> names = new HashSet<String>();
		for (Iterator iterator = attachments.iterator(); iterator.hasNext();) {
			names.add(((AttachmentData) iterator.next()).getName());
		}
		//do attachments contain this name?
		if (!names.contains(attachmentName))
			return attachmentName;
		//yes they do. find a unique name then
		boolean unique = false;
		int i = 1;
		String newAttachmentName = null;
		while(!unique){
			newAttachmentName = attachmentName+" ("+i + ")";
			if (!names.contains(newAttachmentName))
				unique = true;
			i++;
		}
		return newAttachmentName;
	}
	
	@Override
	public void apply() {
		//widgetDescriptor should have all of the updated parameter info.  But we do need to ask for the widget name from the view.
		final String userProvidedWidgetName = SafeHtmlUtils.htmlEscape(view.getName());
		try {
			String textToInsert = view.getTextToInsert(userProvidedWidgetName);
			if (textToInsert != null) {
				//this is it!
				fireUpdatedEvent(textToInsert);
				view.hide();
				return;
			} else 
				view.updateDescriptorFromView();
		} catch (IllegalArgumentException e) {
			//invalid param, just show a message and return
			view.showErrorMessage(e.getMessage());
			return;
		}
		
		//if this is a new attachment, then make it unique.  If it isn't new, then write over the old one
		if (attachmentName == null && attachments != null) {
			updateWidget(getUniqueAttachmentName(attachments, userProvidedWidgetName));
		} else
			updateWidget(userProvidedWidgetName);
	}
	
	private void updateWidget(final String name) {
		try {
			JSONObjectAdapter widgetDescriptorJson = widgetDescriptor.writeToJSONObject(jsonObjectAdapter.createNew());
			synapseClient.addWidgetDescriptorToEntity(widgetDescriptorJson.toJSONString(), entityId, name, contentTypeKey, new AsyncCallback<EntityWrapper>() {
				
				@Override
				public void onSuccess(EntityWrapper result) {
					if (!name.equals(attachmentName)) {
						//it's been renamed, remove the old attachment and include the old name in the update event
						try {
							synapseClient.removeAttachmentFromEntity(entityId,  attachmentName, new AsyncCallback<EntityWrapper>() {
								@Override
								public void onSuccess(EntityWrapper result) {
									fireUpdatedEvent(name, result, widgetDescriptor, attachmentName);
									view.hide();
								}
								@Override
								public void onFailure(Throwable caught) {
									view.showErrorMessage(caught.getMessage());
								}
							});
						} catch (RestServiceException e) {
							view.showErrorMessage(e.getMessage());
						}
					}
					else {
						fireUpdatedEvent(name, result, widgetDescriptor, attachmentName);
						view.hide();
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
			});
		} catch (RestServiceException e) {
			view.showErrorMessage(e.getMessage());
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(e.getMessage());
		}
	}
	
	public void fireUpdatedEvent(String attachmentName, EntityWrapper updatedEntity, WidgetDescriptor descriptor, String oldName) {
		//fire event that contains name and the updated widgetdescriptor
		WidgetDescriptorUpdatedEvent event = new WidgetDescriptorUpdatedEvent();
		event.setName(attachmentName);
		event.setOldName(oldName);
		event.setEntityWrapper(updatedEntity);
		event.setWidgetDescriptor(widgetDescriptor);
		fireUpdatedEvent(event);
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
	public void editExisting(final String entityId, String attachmentName, List<AttachmentData> attachments) {
		if(entityId == null) throw new IllegalArgumentException("entityId cannot be null");
		if(attachmentName == null) throw new IllegalArgumentException("attachmentName type cannot be null");
		cleanInit();
		this.entityId= entityId;
		this.attachmentName = attachmentName;
		this.attachments = attachments;
		this.contentTypeKey = getContentType(attachmentName, attachments);
		view.setName(attachmentName);
		view.setSaveButtonText(DisplayConstants.SAVE_BUTTON_LABEL);
		//initialize view with the correct widget descriptor definition and show
		try {
			synapseClient.getWidgetDescriptorJson(entityId, attachmentName, new AsyncCallback<String>() {
				
				@Override
				public void onSuccess(String widgetDescriptorJson) {
					try {
						widgetDescriptor = nodeModelCreator.createJSONEntity(widgetDescriptorJson, widgetRegistrar.getWidgetClass(contentTypeKey));
						view.setWidgetDescriptor(entityId, contentTypeKey, widgetDescriptor);
						view.show(widgetRegistrar.getFriendlyTypeName(contentTypeKey));
					} catch (JSONObjectAdapterException e) {
						onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
			});
		} catch (RestServiceException e) {
			view.showErrorMessage(e.getMessage());
		}
	}
	
	@Override
	public void editNew(String entityId, String contentTypeKey, List<AttachmentData> attachments) {
		if(entityId == null) throw new IllegalArgumentException("entityId cannot be null");
		if(contentTypeKey == null) throw new IllegalArgumentException("content type key cannot be null");
		cleanInit();
		this.entityId = entityId;
		this.attachments = attachments;
		this.contentTypeKey = contentTypeKey;
		
		//initialize the view with a new widget descriptor definition of the correct type and show
		String widgetClassName = widgetRegistrar.getWidgetClass(contentTypeKey);
		if (widgetClassName != null)
			widgetDescriptor = (WidgetDescriptor)nodeModelCreator.newInstance(widgetClassName);
		view.setWidgetDescriptor(entityId, contentTypeKey, widgetDescriptor);
		//prepopulate with a unique attachment name of the correct type
		String friendlyName = widgetRegistrar.getFriendlyTypeName(contentTypeKey);
		view.setName(getUniqueAttachmentName(attachments, friendlyName));
		view.show(friendlyName);
		view.setSaveButtonText(DisplayConstants.INSERT_BUTTON_LABEL);
	}
	
	private String getContentType(String attachmentName, List<AttachmentData> attachments) {
		for (Iterator iterator = attachments.iterator(); iterator.hasNext();) {
			AttachmentData attachmentData = (AttachmentData) iterator.next();
			if (attachmentName.equals(attachmentData.getName()))
					return attachmentData.getContentType();
		}
		return null;
	}
	
	private void cleanInit() {
		entityId = null;
		contentTypeKey = null;
		attachmentName = null;
		view.clear();
	}

}

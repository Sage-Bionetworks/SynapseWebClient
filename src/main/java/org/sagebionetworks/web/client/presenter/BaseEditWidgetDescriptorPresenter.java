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
import org.sagebionetworks.web.client.widget.entity.dialog.WidgetDescriptorUtils;
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
	String widgetClassName, entityId, attachmentName;
	JSONObjectAdapter jsonObjectAdapter;
	//contains all of the widget specific parameters
	WidgetDescriptor widgetDescriptor;
	private List<AttachmentData> attachments;
	
	@Inject
	public BaseEditWidgetDescriptorPresenter(BaseEditWidgetDescriptorView view, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator, JSONObjectAdapter jsonObjectAdapter){
		this.view = view;
		this.view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
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
	public static void editNewWidget(BaseEditWidgetDescriptorPresenter presenter, String entityId, String widgetClassName, List<AttachmentData> attachments, WidgetDescriptorUpdatedHandler handler) {
		presenter.addWidgetDescriptorUpdatedHandler(handler);
		presenter.editNew(entityId, widgetClassName, attachments);
	}
	
	public static String getUniqueAttachmentName(List<AttachmentData> attachments, String attachmentName) {
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
		try {
			view.updateDescriptorFromView();
		} catch (IllegalArgumentException e) {
			//invalid param, just show a message and return
			view.showErrorMessage(e.getMessage());
			return;
		}
		//widgetDescriptor should have all of the updated parameter info.  But we do need to ask for the widget name from the view.
		String newWidgetName = SafeHtmlUtils.htmlEscape(view.getName());
		//if this is a new attachment, then make it unique.  If it isn't new, then write over the old one
		if (attachmentName == null && attachments != null) {
			newWidgetName = getUniqueAttachmentName(attachments, newWidgetName);
		}
		final String name = newWidgetName;
		try {
			JSONObjectAdapter widgetDescriptorJson = widgetDescriptor.writeToJSONObject(jsonObjectAdapter.createNew());
			synapseClient.addWidgetDescriptorToEntity(widgetDescriptorJson.toJSONString(), entityId, name, new AsyncCallback<EntityWrapper>() {
				
				@Override
				public void onSuccess(EntityWrapper result) {

					// Hide the dialog
					view.hide();
					
					//throw event that contains name and the updated widgetdescriptor
					WidgetDescriptorUpdatedEvent event = new WidgetDescriptorUpdatedEvent();
					event.setName(name);
					event.setEntityWrapper(result);
					event.setWidgetDescriptor(widgetDescriptor);
					fireUpdatedEvent(event);
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
	
	public void fireUpdatedEvent(WidgetDescriptorUpdatedEvent event) {
		handlerManager.fireEvent(event);
	}
	
	@Override
	public void addWidgetDescriptorUpdatedHandler(WidgetDescriptorUpdatedHandler handler) {
		handlerManager = new HandlerManager(this);
		handlerManager.addHandler(WidgetDescriptorUpdatedEvent.getType(), handler);
	}

	@Override
	public void editExisting(String entityId, String attachmentName, List<AttachmentData> attachments) {
		if(entityId == null) throw new IllegalArgumentException("entityId cannot be null");
		if(attachmentName == null) throw new IllegalArgumentException("attachmentName type cannot be null");
		cleanInit();
		this.entityId= entityId;
		this.attachmentName = attachmentName;
		this.attachments = attachments;
		
		//initialize view with the correct widget descriptor definition and show
		try {
			synapseClient.getWidgetDescriptorJson(entityId, attachmentName, new AsyncCallback<String>() {
				
				@Override
				public void onSuccess(String widgetDescriptorJson) {
					try {
						widgetDescriptor = nodeModelCreator.createWidget(widgetDescriptorJson);
						view.setWidgetDescriptor(widgetDescriptor);
						view.show();
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
	public void editNew(String entityId, String widgetClassName, List<AttachmentData> attachments) {
		if(entityId == null) throw new IllegalArgumentException("entityId cannot be null");
		if(widgetClassName == null) throw new IllegalArgumentException("widget class name cannot be null");
		cleanInit();
		this.entityId = entityId;
		this.widgetClassName = widgetClassName;
		this.attachments = attachments;
		
		//initialize the view with a new widget descriptor definition of the correct type and show
		widgetDescriptor = (WidgetDescriptor)nodeModelCreator.newInstance(widgetClassName);
		widgetDescriptor.setEntityType(widgetClassName);
		view.setWidgetDescriptor(widgetDescriptor);
		//prepopulate with a unique attachment name of the correct type
		if (attachments != null) {
			view.setName(getUniqueAttachmentName(attachments, WidgetDescriptorUtils.getFriendlyTypeName(widgetClassName)));
		}
				
		view.show();
		
	}
	
	private void cleanInit() {
		entityId = null;
		widgetClassName = null;
		attachmentName = null;
		view.clear();
	}

}

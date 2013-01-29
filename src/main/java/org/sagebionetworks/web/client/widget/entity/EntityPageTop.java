package org.sagebionetworks.web.client.widget.entity;

import java.util.Map;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.IconSize;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class EntityPageTop implements EntityPageTopView.Presenter, SynapseWidgetPresenter  {

	private EntityPageTopView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private EntitySchemaCache schemaCache;
	private EntityTypeProvider entityTypeProvider;
	private IconsImageBundle iconsImageBundle;
	private WidgetRegistrar widgetRegistrar;
	
	private EntityBundle bundle;
	private boolean readOnly;
	private String entityTypeDisplay;
	private EventBus bus;
	private JSONObjectAdapter jsonObjectAdapter;
	
	@Inject
	public EntityPageTop(EntityPageTopView view, 
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			EntitySchemaCache schemaCache,
			EntityTypeProvider entityTypeProvider,
			IconsImageBundle iconsImageBundle,
			WidgetRegistrar widgetRegistrar,
			EventBus bus, JSONObjectAdapter jsonObjectAdapter) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.schemaCache = schemaCache;
		this.entityTypeProvider = entityTypeProvider;
		this.iconsImageBundle = iconsImageBundle;
		this.widgetRegistrar = widgetRegistrar;
		this.bus = bus;
		this.jsonObjectAdapter = jsonObjectAdapter;
		view.setPresenter(this);
	}

    /**
     * Update the bundle attached to this EntityPageTop. Consider calling refresh()
     * to notify an attached view.
     *
     * @param bundle
     */
    public void setBundle(EntityBundle bundle, boolean readOnly) {
    	this.bundle = bundle;
    	this.readOnly = readOnly;
	}

	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
		// remove handlers
		this.bundle = null;
	}

	@Override
	public Widget asWidget() {
		if(bundle != null) {
			return asWidget(bundle);
		}
		return null;
	}

	public Widget asWidget(EntityBundle bundle) {
		view.setPresenter(this);
		return view.asWidget();
	}

	@Override
	public void refresh() {
		sendDetailsToView(bundle.getPermissions().getCanChangePermissions(), bundle.getPermissions().getCanEdit());
	}

	@Override
	public void fireEntityUpdatedEvent() {
		bus.fireEvent(new EntityUpdatedEvent());
	}

	public HandlerRegistration addEntityUpdatedHandler(EntityUpdatedHandler handler) {
		return bus.addHandler(EntityUpdatedEvent.getType(), handler);
	}
	
	@Override
	public boolean isLocationable() {
		if(bundle.getEntity() instanceof Locationable) {
			return true;
		}
		return false;
	}

	@Override 

	public boolean isLoggedIn() {
		return authenticationController.getLoggedInUser() != null;
	}

	@Override
	public String createEntityLink(String id, String version, String display) {
		return DisplayUtils.createEntityLink(id, version, display);
	}

	@Override
	public ImageResource getIconForType(String typeString) {
		EntityType type = entityTypeProvider.getEntityTypeForString(typeString);
		// try class name as some references are short names, some class names
		if(type == null)
			type = entityTypeProvider.getEntityTypeForClassName(typeString);
		if(type == null) {
			return DisplayUtils.getSynapseIconForEntity(null, IconSize.PX16, iconsImageBundle);
		}
		return DisplayUtils.getSynapseIconForEntityType(type, IconSize.PX16, iconsImageBundle);
	}

	@Override
	public void loadShortcuts(int offset, int limit, final AsyncCallback<PaginatedResults<EntityHeader>> callback) {
		if(offset == 0) {
			 callback.onSuccess(bundle.getReferencedBy());
		} else {
			synapseClient.getEntityReferencedBy(bundle.getEntity().getId(), new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					PaginatedResults<EntityHeader> paginatedResults;
					try {
						paginatedResults = nodeModelCreator.createPaginatedResults(result, EntityHeader.class);
						callback.onSuccess(paginatedResults);
					} catch (JSONObjectAdapterException e) {
						onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));						
					}
				}
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}
	}

	@Override
	public void getHtmlFromMarkdown(String markdown, String attachmentBaseUrl, final AsyncCallback<String> asyncCallback) {
		synapseClient.markdown2Html(markdown, attachmentBaseUrl, false, asyncCallback);
			}
	@Override
	public void loadWidgets(final HTMLPanel panel) {
		try {
			loadWidgets(panel, bundle, widgetRegistrar, synapseClient, nodeModelCreator, view, jsonObjectAdapter, iconsImageBundle, false);
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(e.getMessage());
		}
	}
	
	/**
	 * Shared method for loading the widgets into the html returned by the service (used to render the entity page, and to generate a preview of the description)
	 * @param panel
	 * @param bundle
	 * @param widgetRegistrar
	 * @param synapseClient
	 * @param nodeModelCreator
	 * @param view
	 * @throws JSONObjectAdapterException 
	 */
	public static void loadWidgets(final HTMLPanel panel, final EntityBundle bundle, final WidgetRegistrar widgetRegistrar, SynapseClientAsync synapseClient, final NodeModelCreator nodeModelCreator, final SynapseWidgetView view, final JSONObjectAdapter jsonObjectAdapter, IconsImageBundle iconsImageBundle, Boolean isPreview) throws JSONObjectAdapterException {
		final String entityId = bundle.getEntity().getId();
		final String suffix = isPreview ? DisplayConstants.DIV_ID_PREVIEW_SUFFIX : "";
		//look for every element that has the right format
		int i = 0;
		String currentWidgetDiv = DisplayConstants.DIV_ID_WIDGET_PREFIX + i + suffix;
		Element el = panel.getElementById(currentWidgetDiv);
		while (el != null) {
				//based on the contents of the element, create the correct widget descriptor and renderer
				String innerText = el.getAttribute("widgetParams");
				if (innerText != null) {
					try {
						innerText = innerText.trim();
						String contentType = widgetRegistrar.getWidgetContentType(innerText);
						Map<String, String> widgetDescriptor = widgetRegistrar.getWidgetDescriptor(innerText);
						WidgetRendererPresenter presenter = widgetRegistrar.getWidgetRendererForWidgetDescriptor(entityId, WidgetConstants.WIKI_OWNER_ID_ENTITY, contentType, widgetDescriptor);
						if (presenter == null)
							throw new IllegalArgumentException("unable to render widget from the specified markdown:" + innerText);
						panel.add(presenter.asWidget(), currentWidgetDiv);
					}catch(IllegalArgumentException e) {
						//try our best to load all of the widgets. if one fails to load, then fail quietly.
						e.printStackTrace();
						panel.add(new HTMLPanel(DisplayUtils.getIconHtml(iconsImageBundle.error16()) + innerText), currentWidgetDiv);
					}
				}
			
			i++;
			currentWidgetDiv = DisplayConstants.DIV_ID_WIDGET_PREFIX + i + suffix;
			el = panel.getElementById(currentWidgetDiv);
		}
	}
	
	/*
	 * Private Methods
	 */
	private void sendDetailsToView(boolean isAdmin, boolean canEdit) {
		ObjectSchema schema = schemaCache.getSchemaEntity(bundle.getEntity());
		entityTypeDisplay = DisplayUtils.getEntityTypeDisplay(schema);
		view.setEntityBundle(bundle, getUserProfile(), entityTypeDisplay, isAdmin, canEdit, readOnly);
	}
	
	private UserProfile getUserProfile() {
		UserSessionData sessionData = authenticationController.getLoggedInUser();
		return (sessionData==null ? null : sessionData.getProfile());
		
	}
}

package org.sagebionetworks.web.client.widget.entity;

import java.util.Comparator;
import java.util.TreeMap;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
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
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityPageTop implements EntityPageTopView.Presenter, SynapseWidgetPresenter  {

	private EntityPageTopView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private EntitySchemaCache schemaCache;
	private JSONObjectAdapter jsonObjectAdapter;
	private EntityTypeProvider entityTypeProvider;
	private IconsImageBundle iconsImageBundle;

	private EntityBundle bundle;
	private boolean readOnly;
	private String entityTypeDisplay;
	private EventBus bus;
	private String rStudioUrl;

	@Inject
	public EntityPageTop(EntityPageTopView view, 
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			EntitySchemaCache schemaCache,
			JSONObjectAdapter jsonObjectAdapter,
			EntityTypeProvider entityTypeProvider,
			IconsImageBundle iconsImageBundle,
			EventBus bus) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.schemaCache = schemaCache;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.entityTypeProvider = entityTypeProvider;
		this.iconsImageBundle = iconsImageBundle;
		this.bus = bus;
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
		sendVersionInfoToView();
	}

	@Override
	public void fireEntityUpdatedEvent() {
		bus.fireEvent(new EntityUpdatedEvent());
	}

	public void addEntityUpdatedHandler(EntityUpdatedHandler handler) {
		bus.addHandler(EntityUpdatedEvent.getType(), handler);
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
		PaginatedResults<EntityHeader> references = null;
		if(offset == 0) {
			 callback.onSuccess(bundle.getReferencedBy());
		} else {
			synapseClient.getEntityReferencedBy(bundle.getEntity().getId(), new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					PaginatedResults<EntityHeader> paginatedResults = nodeModelCreator.createPaginatedResults(result, EntityHeader.class);
					callback.onSuccess(paginatedResults);
				}
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}
	}

	@Override
	public void getHtmlFromMarkdown(String markdown, final AsyncCallback<String> asyncCallback) {
		synapseClient.markdown2Html(markdown, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				asyncCallback.onSuccess(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				asyncCallback.onFailure(caught);
			}
		});
	}
	
	/*
	 * Private Methods
	 */
	private void sendDetailsToView(boolean isAdmin, boolean canEdit) {
		ObjectSchema schema = schemaCache.getSchemaEntity(bundle.getEntity());
		entityTypeDisplay = DisplayUtils.getEntityTypeDisplay(schema);
		view.setEntityBundle(bundle, entityTypeDisplay, isAdmin, canEdit, readOnly);
	}

	private void sendVersionInfoToView() {
		final Entity entity = bundle.getEntity();
		if (entity instanceof Versionable) {
			synapseClient.getEntityVersions(entity.getId(),
					new AsyncCallback<String>() {

						@Override
						public void onFailure(Throwable caught) {
							view.setEntityVersions((Versionable)entity, null);
						}

						@Override
						public void onSuccess(String result) {
							setEntityVersions(result);
						}

					});
		}
	}

	private void setEntityVersions(String jsonVersions) {
		TreeMap<Long, String> versions = new TreeMap<Long, String>(new ReverseLong());
		JSONObjectAdapter joa;
		try {
			joa = this.jsonObjectAdapter.createNew(jsonVersions);
			int numResults = joa.getInt("totalNumberOfResults");

			JSONArrayAdapter jsonArray = joa.getJSONArray("results");
			for (int i = 0; i < numResults; i++) {
				JSONObjectAdapter jsonObject = jsonArray.getJSONObject(i);
				long number = jsonObject.getInt("versionNumber");
				String label = jsonObject.getString("versionLabel");
				versions.put(number, label);
			}
		} catch (JSONObjectAdapterException e) {
		}
		Versionable vb = (Versionable)bundle.getEntity();

		view.setEntityVersions(vb, versions);
	}

	static class ReverseLong implements Comparator<Long> {

		@Override
		public int compare(Long o1, Long o2) {
			return o1 > o2 ? -1 :
					o1 < o2 ? +1 : 0;
		}
    }
}

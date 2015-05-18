package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.PERMISSIONS;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiOrderHint;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesWidget implements WikiSubpagesView.Presenter {
	
	private WikiSubpagesView view;
	private SynapseClientAsync synapseClient;
	private WikiPageKey wikiKey; 
	private String ownerObjectName;
	private Place ownerObjectLink;
	private FlowPanel wikiSubpagesContainer;
	private FlowPanel wikiPageContainer;
	private V2WikiOrderHint subpageOrderHint;
	private AuthenticationController authenticationController;
	private boolean canEdit;
	
	//true if wiki is embedded in it's owner page.  false if it should be shown as a stand-alone wiki 
	private boolean isEmbeddedInOwnerPage;
	private CallbackP<WikiPageKey> reloadWikiPageCallback;
	
	@Inject
	public WikiSubpagesWidget(WikiSubpagesView view, SynapseClientAsync synapseClient,
							AuthenticationController authenticationController) {
		this.view = view;		
		this.synapseClient = synapseClient;
		this.authenticationController = authenticationController;
		
		view.setPresenter(this);
	}

	public void configure(final WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, FlowPanel wikiSubpagesContainer, FlowPanel wikiPageContainer, boolean embeddedInOwnerPage, CallbackP<WikiPageKey> reloadWikiPageCallback) {
		canEdit = false;
		this.reloadWikiPageCallback = reloadWikiPageCallback;
		this.wikiPageContainer = wikiPageContainer;
		this.wikiSubpagesContainer = wikiSubpagesContainer;
		this.wikiKey = wikiKey;
		this.isEmbeddedInOwnerPage = embeddedInOwnerPage;
		view.clear();
		//figure out owner object name/link
		if (wikiKey.getOwnerObjectType().equalsIgnoreCase(ObjectType.ENTITY.toString())) {
			//lookup the entity name based on the id
			int mask = ENTITY | PERMISSIONS ;
			synapseClient.getEntityBundle(wikiKey.getOwnerObjectId(), mask, new AsyncCallback<EntityBundle>() {
				@Override
				public void onSuccess(EntityBundle bundle) {
					ownerObjectName = bundle.getEntity().getName();
					ownerObjectLink = getLinkPlace(bundle.getEntity().getId(), wikiKey.getVersion(), null, isEmbeddedInOwnerPage);
					canEdit = bundle.getPermissions().getCanEdit();
					refreshTableOfContents();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
			});
		}
	}
	
	public static Place getLinkPlace(String entityId, Long entityVersion, String wikiId, boolean isEmbeddedInOwnerPage) {
		if (isEmbeddedInOwnerPage)
			return new Synapse(entityId, entityVersion, Synapse.EntityArea.WIKI, wikiId);
		else
			return new Wiki(entityId, ObjectType.ENTITY.toString(), wikiId);
	}
	
	public void clearState() {
		view.clear();
	}

	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
	
	public void refreshTableOfContents() {
		view.clear();
		
		synapseClient.getV2WikiHeaderTree(wikiKey.getOwnerObjectId(), wikiKey.getOwnerObjectType(), new AsyncCallback<PaginatedResults<V2WikiHeader>>() {
			@Override
			public void onSuccess(PaginatedResults<V2WikiHeader> results) {
				final PaginatedResults<V2WikiHeader> wikiHeaders = results;
				
				synapseClient.getV2WikiOrderHint(wikiKey, new AsyncCallback<V2WikiOrderHint>() {
					@Override
					public void onSuccess(V2WikiOrderHint result) {
						// "Sort" stuff'
						subpageOrderHint = result;
						WikiOrderHintUtils.sortHeadersByOrderHint(wikiHeaders.getResults(), subpageOrderHint);
						
						view.configure(wikiHeaders.getResults(), wikiSubpagesContainer, wikiPageContainer, ownerObjectName,
										ownerObjectLink, wikiKey, isEmbeddedInOwnerPage, getUpdateOrderHintCallback());
						view.setEditOrderButtonVisible(canEdit);
					}
					@Override
					public void onFailure(Throwable caught) {
						// Failed to get order hint. Just ignore it.
						view.configure(wikiHeaders.getResults(), wikiSubpagesContainer, wikiPageContainer, ownerObjectName,
								ownerObjectLink, wikiKey, isEmbeddedInOwnerPage, getUpdateOrderHintCallback());
						view.setEditOrderButtonVisible(canEdit);
					}
				});
			}
			
			@Override
			public void onFailure(Throwable caught) {
				//if this is because the wiki header root was not found, and the parent wiki id is null,
				if (caught instanceof NotFoundException) {
					//do nothing, show nothing
					view.clear();
				} else {
					view.showErrorMessage(caught.getMessage());
				}
			}
		});
	}

	private UpdateOrderHintCallback getUpdateOrderHintCallback() {
		return new UpdateOrderHintCallback() {
			@Override
			public void updateOrderHint(List<String> newOrderHintIdList) {
					subpageOrderHint.setIdList(newOrderHintIdList);
					synapseClient.updateV2WikiOrderHint(subpageOrderHint, new AsyncCallback<V2WikiOrderHint>() {
						@Override
						public void onSuccess(V2WikiOrderHint result) {
							refreshTableOfContents();
						}
						@Override
						public void onFailure(Throwable caught) {
							view.showErrorMessage(caught.getMessage());
						}
					});
				}
		};
	}

	public interface UpdateOrderHintCallback {
		void updateOrderHint(List<String> newOrderHintIdList);
	}

	@Override
	public CallbackP<WikiPageKey> getReloadWikiPageCallback() {
		return this.reloadWikiPageCallback;
	}
}

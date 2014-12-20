package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiOrderHint;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesWidget implements WikiSubpagesView.Presenter, WidgetRendererPresenter {
	
	private WikiSubpagesView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private AdapterFactory adapterFactory;
	private WikiPageKey wikiKey; 
	private String ownerObjectName;
	private Place ownerObjectLink;
	private FlowPanel wikiSubpagesContainer;
	private FlowPanel wikiPageContainer;
	private V2WikiOrderHint subpageOrderHint;
	
	//true if wiki is embedded in it's owner page.  false if it should be shown as a stand-alone wiki 
	private boolean isEmbeddedInOwnerPage;
	
	@Inject
	public WikiSubpagesWidget(WikiSubpagesView view, SynapseClientAsync synapseClient,
							NodeModelCreator nodeModelCreator, AdapterFactory adapterFactory) {
		this.view = view;		
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.adapterFactory = adapterFactory;
		
		view.setPresenter(this);
	}	
	@Override
	public void configure(final WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		configure(wikiKey, widgetDescriptor, widgetRefreshRequired, null, null, true);
	}

	public void configure(final WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, FlowPanel wikiSubpagesContainer, FlowPanel wikiPageContainer, boolean embeddedInOwnerPage) {
		this.wikiPageContainer = wikiPageContainer;
		this.wikiSubpagesContainer = wikiSubpagesContainer;
		this.wikiKey = wikiKey;
		this.isEmbeddedInOwnerPage = embeddedInOwnerPage;
		view.clear();
		//figure out owner object name/link
		if (wikiKey.getOwnerObjectType().equalsIgnoreCase(ObjectType.ENTITY.toString())) {
			//lookup the entity name based on the id
			Reference ref = new Reference();
			ref.setTargetId(wikiKey.getOwnerObjectId());
			List<Reference> allRefs = new ArrayList<Reference>();
			allRefs.add(ref);
			ReferenceList list = new ReferenceList();
			list.setReferences(allRefs);	
			try {
				synapseClient.getEntityHeaderBatch(list.writeToJSONObject(adapterFactory.createNew()).toJSONString(), new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {					
						BatchResults<EntityHeader> headers;
						try {
							headers = nodeModelCreator.createBatchResults(result, EntityHeader.class);
							if (headers.getTotalNumberOfResults() == 1) {
								EntityHeader theHeader = headers.getResults().get(0);
								ownerObjectName = theHeader.getName();
								ownerObjectLink = getLinkPlace(theHeader.getId(), wikiKey.getVersion(), null, isEmbeddedInOwnerPage);
								refreshTableOfContents();
							}	
						} catch (JSONObjectAdapterException e) {
							onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {					
						view.showErrorMessage(caught.getMessage());
					}
				});
			} catch (JSONObjectAdapterException e) {
				view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
			}
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
	
	@Override
	public Widget asWidget() {
		view.setPresenter(this);		
		return view.asWidget();
	}
	
	public void refreshTableOfContents() {
		view.clear();
		synapseClient.getV2WikiHeaderTree(wikiKey.getOwnerObjectId(), wikiKey.getOwnerObjectType(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String results) {
				try {
					final PaginatedResults<JSONEntity> wikiHeaders = nodeModelCreator.createPaginatedResults(results, V2WikiHeader.class);
					
					synapseClient.getV2WikiOrderHint(wikiKey, new AsyncCallback<V2WikiOrderHint>() {
						@Override
						public void onSuccess(V2WikiOrderHint result) {
							// "Sort" stuff'
							subpageOrderHint = result;
							WikiOrderHintUtils.sortHeadersByOrderHint(wikiHeaders, subpageOrderHint);
							
							view.configure(wikiHeaders.getResults(), wikiSubpagesContainer, wikiPageContainer, ownerObjectName,
											ownerObjectLink, wikiKey, isEmbeddedInOwnerPage, getUpdateOrderHintCallback());
						}
						@Override
						public void onFailure(Throwable caught) {
							// Failed to get order hint. Just ignore it.
							view.configure(wikiHeaders.getResults(), wikiSubpagesContainer, wikiPageContainer, ownerObjectName,
									ownerObjectLink, wikiKey, isEmbeddedInOwnerPage, getUpdateOrderHintCallback());
						}
					});
					
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
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
	
}

package org.sagebionetworks.web.client.widget.entity;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.search.Hit;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBoxSuggestOracle.EntitySearchBoxSuggestion;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.SearchQueryUtils;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This widget is a Synapse entity Id search box
 * 
 * @author Dburdick
 *
 */
public class EntitySearchBox implements EntitySearchBoxView.Presenter, IsWidget {
	
	public static final int DELAY = 750;	// milliseconds
	public static final long PAGE_SIZE = 10;
	private EntitySearchBoxView view;
	private EntitySelectedHandler handler;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private EntitySearchBoxSuggestOracle oracle;
	private boolean retrieveVersions = false;
	private EntitySearchBoxSuggestion selectedSuggestion;
	private long offset;
	
	/**
	 * 
	 * @param factory
	 * @param cache
	 * @param propertyView
	 */
	@Inject
	public EntitySearchBox(AdapterFactory factory, EntitySearchBoxView view,
			SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController) {
		super();		
		this.view = view;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		oracle = view.getOracle();
		view.setPresenter(this);
	}

	/**
	 * Get widget with text box of specified width
	 * @param width the width of the input box
	 * @return
	 */	
	public Widget asWidget(int width) {
		view.setDisplayWidth(width);
		return this.view.asWidget();
	}

	/**
	 * Default size
	 * @param DIALOG_WIDTH
	 * @return
	 */
	@Override
	public Widget asWidget() {
		return asWidget(300);
	}
	
	public void setEntitySelectedHandler(EntitySelectedHandler handler, boolean retrieveVersions) {
		this.handler = handler;		
		this.retrieveVersions = retrieveVersions;		
	}

	@Override
	public void setSelectedSuggestion(EntitySearchBoxSuggestion suggestion) {
		selectedSuggestion = suggestion;
		entitySelected(selectedSuggestion.getHit().getId(), selectedSuggestion.getHit().getName());
	}
	
	public void entitySelected(final String entityId, final String name) {
		if(handler != null) {
			List<VersionInfo> versions = null;
			if(retrieveVersions) {
				synapseClient.getEntityVersions(entityId, 1, 20, new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						PaginatedResults<VersionInfo> versions;
						try {
							versions = nodeModelCreator.createPaginatedResults(result, VersionInfo.class);
							handler.onSelected(entityId, name, versions.getResults());
						} catch (JSONObjectAdapterException e) {
							onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
						}
					}
					@Override
					public void onFailure(Throwable caught) {
						view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
					}
				});
			} else {				
				handler.onSelected(entityId, name, versions);
			}
		}
	}

	public interface EntitySelectedHandler {
		public void onSelected(String entityId, String name, List<VersionInfo> versions);
	}

	
	public void getSuggestions(final SuggestOracle.Request request, final SuggestOracle.Callback callback) {
		view.showLoading();
		
		final String prefix = request.getQuery();
		SearchQuery query = SearchQueryUtils.getDefaultSearchQuery();
		query.setStart(offset);
		query.setSize(PAGE_SIZE);
		query.setQueryTerm(Arrays.asList(prefix.split(" ")));
		
		final List<Suggestion> suggestions = new LinkedList<Suggestion>();
		synapseClient.search(query, new AsyncCallback<SearchResults>() {
			@Override
			public void onSuccess(SearchResults result) {
				// Update view fields.
				view.updateFieldStateForSuggestions(result, offset);
				
				// Load suggestions.
				for (Hit hit : result.getHits()) {
					suggestions.add(oracle.makeEntitySuggestion(hit, prefix));
				}

				// Set up response
				SuggestOracle.Response response = new SuggestOracle.Response(suggestions);
				callback.onSuggestionsReady(request, response);
				
				view.hideLoading();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}

		});
	}
	
	@Override
	public EntitySearchBoxSuggestion getSelectedSuggestion() {
		return selectedSuggestion;
	}
	
	
	/**
	 * Clears out the state of the searchbox
	 */
	public void clearSelection() {
		this.view.clear();
	}
	
	public String getText() {
		return view.getText();
	}
	
	@Override
	public void getPrevSuggestions() {
		offset -= PAGE_SIZE;
		getSuggestions(oracle.getRequest(), oracle.getCallback());
	}

	@Override
	public void getNextSuggestions() {
		offset += PAGE_SIZE;
		getSuggestions(oracle.getRequest(), oracle.getCallback());
	}
	
	public void setOffset(long offset) {
		this.offset = offset;
	}

}

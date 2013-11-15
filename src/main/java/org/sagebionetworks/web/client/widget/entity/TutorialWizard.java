package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.wiki.WikiHeader;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TutorialWizard implements TutorialWizardView.Presenter, WidgetRendererPresenter {

	private TutorialWizardView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private Callback callback;
	private String entityId, tutorialButtonText;
	
	/**
	 * Callback called when the user skips the tutorial, or presses ok.
	 *
	 */
	public interface Callback{
		public void tutorialSkipped();
		public void tutorialFinished();
	}

	@Inject
	public TutorialWizard(TutorialWizardView view, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		view.setPresenter(this);
	}
	
	/**
	 * Synapse Widget configuration entry point
	 */
	@Override
	public void configure(WikiPageKey wikiKey,Map<String, String> widgetDescriptor, org.sagebionetworks.web.client.utils.Callback widgetRefreshRequired) {
		entityId = widgetDescriptor.get(WidgetConstants.WIDGET_ENTITY_ID_KEY);
		tutorialButtonText = widgetDescriptor.get(WidgetConstants.TEXT_KEY);
	}
	
	@Override
	public Widget asWidget() {
		return view.getTutorialLink(tutorialButtonText);
	}
	
	@Override
	public void userClickedTutorialButton() {
		configure(entityId, null);
	}
	
	/**
	 * Give it a base entity Id and it will discover all subpages (and show their content in a tutorial wizard)
	 * @param entityId
	 */
	public void configure(final String entityId, Callback callback) {
		this.callback = callback;
		synapseClient.getWikiHeaderTree(entityId, ObjectType.ENTITY.toString(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String results) {
				try {
					PaginatedResults<JSONEntity> wikiHeaders = nodeModelCreator.createPaginatedResults(results, WikiHeader.class);
					
					//sort them so that they are always in a predictable order for the wizard.
					List<WikiHeader> sortedHeaders= new ArrayList<WikiHeader>();
					for (JSONEntity headerEntity : wikiHeaders.getResults()) {
						WikiHeader header = (WikiHeader) headerEntity;
						//ignore root page to simplify setup
						if (header.getParentId() != null)
							sortedHeaders.add(header);
					}
					if (sortedHeaders.size() == 0) {
						onFailure(new NotFoundException("Wiki subpages not found for tutorial: " + entityId));
						return;
					}
					Collections.sort(sortedHeaders, new Comparator<WikiHeader>() {
					    @Override
				        public int compare(WikiHeader o1, WikiHeader o2) {
				        	return o1.getTitle().compareTo(o2.getTitle());
				        }
					});
					view.showWizard(entityId, sortedHeaders);
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	@Override
	public void userFinishedTutorial() {
		if (callback != null) {
			callback.tutorialFinished();
		}
	}
	@Override
	public void userSkippedTutorial() {
		if (callback != null) {
			callback.tutorialSkipped();
		}
	}
}

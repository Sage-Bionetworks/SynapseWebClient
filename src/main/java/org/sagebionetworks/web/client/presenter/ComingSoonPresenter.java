package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.ComingSoon;
import org.sagebionetworks.web.client.services.LayoutServiceAsync;
import org.sagebionetworks.web.client.transform.JsoProvider;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.ComingSoonView;
import org.sagebionetworks.web.client.widget.provenance.nchart.LayoutResult;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayer;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayerNode;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayersArray;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartUtil;
import org.sagebionetworks.web.client.widget.provenance.nchart.XYPoint;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.provenance.ActivityGraphNode;
import org.sagebionetworks.web.shared.provenance.ActivityType;
import org.sagebionetworks.web.shared.provenance.EntityGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraph;
import org.sagebionetworks.web.shared.provenance.ProvGraphEdge;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class ComingSoonPresenter extends AbstractActivity implements ComingSoonView.Presenter, Presenter<ComingSoon> {
		
	private ComingSoon place;
	private ComingSoonView view;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	LayoutServiceAsync layoutService;
	JsoProvider jsoProvider;
	SynapseJSNIUtils jsniUtils;
	
	@Inject
	public ComingSoonPresenter(ComingSoonView view,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			LayoutServiceAsync layoutService,
			JsoProvider jsoProvider,
			SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.layoutService = layoutService;
		this.jsoProvider = jsoProvider;
		this.jsniUtils = jsniUtils;
		
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(ComingSoon place) {
		this.place = place;
		this.view.setPresenter(this);
		final String token = place.toToken();
		view.showErrorMessage("Testing error message dialog look and feel.");
		synapseClient.getEntity(token, new AsyncCallback<EntityWrapper>() {			
			@Override
			public void onSuccess(EntityWrapper result) {
				try {
					Entity entity = nodeModelCreator.createEntity(result);
					view.setEntity(entity);
				} catch (JSONObjectAdapterException e) {
					view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
				}
			}			
			@Override
			public void onFailure(Throwable caught) {
				view.showInfo("Error", "error getting: " + token);
			}
		});
	}

	@Override
    public String mayStop() {
        view.clear();
        return null;
        
    }
	
}

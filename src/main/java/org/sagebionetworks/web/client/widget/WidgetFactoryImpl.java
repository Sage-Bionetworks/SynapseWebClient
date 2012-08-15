package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.LayoutServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetView;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetViewImpl;

import com.google.inject.Inject;

public class WidgetFactoryImpl implements WidgetFactory {

	SynapseClientAsync synapseClient;
	NodeModelCreator nodeModelCreator;
	AuthenticationController authenticationController;
	LayoutServiceAsync layoutService;
	AdapterFactory adapterFactory;
	SynapseJSNIUtils synapseJSNIUtils;
	SageImageBundle sageImageBundle;
	IconsImageBundle iconsImageBundle;
	
	@Inject
	public WidgetFactoryImpl(SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			LayoutServiceAsync layoutService, AdapterFactory adapterFactory,
			SynapseJSNIUtils synapseJSNIUtils, SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle) {
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.layoutService = layoutService;
		this.adapterFactory = adapterFactory;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
	}


	@Override
	public ProvenanceWidget createProvenanceWidget() {
		ProvenanceWidgetView view = new ProvenanceWidgetViewImpl(sageImageBundle, iconsImageBundle, synapseJSNIUtils);
	    return new ProvenanceWidget(view, synapseClient, nodeModelCreator, authenticationController, layoutService, adapterFactory);
	}

	
}

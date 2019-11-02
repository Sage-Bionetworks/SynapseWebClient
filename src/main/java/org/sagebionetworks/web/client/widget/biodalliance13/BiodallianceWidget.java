package org.sagebionetworks.web.client.widget.biodalliance13;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.gwtvisualizationwrappers.client.biodalliance13.BiodallianceConfigInterface;
import org.gwtvisualizationwrappers.client.biodalliance13.BiodallianceSource;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BiodallianceWidget implements BiodallianceWidgetView.Presenter, IsWidget, WidgetRendererPresenter {

	public static final String PORTAL_URL_PREFIX = "/Portal/";
	public static final String FILE_RESOLVER_URL = "Portal/" + WebConstants.FILE_ENTITY_RESOLVER_SERVLET + "?";

	public enum Species {
		HUMAN, MOUSE
	}

	public static final Species DEFAULT_SPECIES = Species.HUMAN;
	public static final String DEFAULT_CHR = "1";
	public static final int DEFAULT_VIEW_START = 3025001;
	public static final int DEFAULT_VIEW_END = 3525001;

	BiodallianceWidgetView view;
	AuthenticationController authenticationController;
	GlobalApplicationState globalApplicationState;
	HumanBiodallianceConfig humanConfig;
	MouseBiodallianceConfig mouseConfig;
	SynapseAlert synAlert;
	private boolean isConfigured;
	BiodallianceConfigInterface currentConfig;
	String initChr;
	int initViewStart, initViewEnd;
	List<BiodallianceSource> sources;

	@Inject
	public BiodallianceWidget(BiodallianceWidgetView view, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState, HumanBiodallianceConfig humanConfig, MouseBiodallianceConfig mouseConfig, SynapseAlert synAlert) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.humanConfig = humanConfig;
		this.mouseConfig = mouseConfig;
		this.synAlert = synAlert;
		isConfigured = false;
		view.setPresenter(this);
		view.setSynAlert(synAlert.asWidget());
	}

	public void configure(Species species, String initChr, int initViewStart, int initViewEnd, List<BiodallianceSource> sources) {
		synAlert.clear();
		if (!authenticationController.isLoggedIn()) {
			synAlert.showLogin();
		} else {
			this.initChr = initChr;
			this.initViewStart = initViewStart;
			this.initViewEnd = initViewEnd;
			this.sources = sources;
			if (Species.HUMAN.equals(species)) {
				currentConfig = humanConfig;
			} else if (Species.MOUSE.equals(species)) {
				currentConfig = mouseConfig;
			}

			isConfigured = true;
			// if view is already attached, then show the browser
			if (view.isAttached()) {
				showBiodallianceBrowser();
			}
		}
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> descriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		// get values from descriptor (params map) and pass to other configure.
		Species species = DEFAULT_SPECIES;

		if (descriptor.containsKey(WidgetConstants.BIODALLIANCE_SPECIES_KEY)) {
			species = Species.valueOf(descriptor.get(WidgetConstants.BIODALLIANCE_SPECIES_KEY).toUpperCase());
		}

		String chr = DEFAULT_CHR;
		if (descriptor.containsKey(WidgetConstants.BIODALLIANCE_CHR_KEY)) {
			chr = descriptor.get(WidgetConstants.BIODALLIANCE_CHR_KEY);
		}

		int viewStart = DEFAULT_VIEW_START;
		if (descriptor.containsKey(WidgetConstants.BIODALLIANCE_VIEW_START_KEY)) {
			viewStart = Integer.parseInt(descriptor.get(WidgetConstants.BIODALLIANCE_VIEW_START_KEY));
		}

		int viewEnd = DEFAULT_VIEW_END;
		if (descriptor.containsKey(WidgetConstants.BIODALLIANCE_VIEW_END_KEY)) {
			viewEnd = Integer.parseInt(descriptor.get(WidgetConstants.BIODALLIANCE_VIEW_END_KEY));
		}

		List<BiodallianceSource> sources = new ArrayList<BiodallianceSource>();
		if (descriptor.containsKey(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX + 0)) {
			// discover all sources
			sources.addAll(getSources(descriptor));
		}
		configure(species, chr, viewStart, viewEnd, sources);
	}

	public List<BiodallianceSource> getSources(Map<String, String> descriptor) {
		// reconstruct biodalliance sources (if there are any)
		List<BiodallianceSource> sources = new ArrayList<BiodallianceSource>();
		int i = 0;
		while (descriptor.containsKey(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX + i)) {
			String sourceJsonString = descriptor.get(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX + i);
			BiodallianceSource newSource = new BiodallianceSource(sourceJsonString);
			updateSourceURIs(newSource);
			sources.add(newSource);
			i++;
		}
		return sources;
	}

	public static void updateSourceURIs(BiodallianceSource source) {
		source.setSourceURI(BiodallianceWidget.getFileResolverURL(source.getEntityId(), source.getVersion()));
		source.setIndexSourceURI(BiodallianceWidget.getFileResolverURL(source.getIndexEntityId(), source.getIndexVersion()));
	}

	public static String getFileResolverURL(String entityIdAndVersion) {
		if (entityIdAndVersion != null) {
			String[] tokens = entityIdAndVersion.split("\\.");
			if (tokens.length == 2) {
				return getFileResolverURL(tokens[0], Long.parseLong(tokens[1]));
			}
		}
		return null;
	}

	public static String getFileResolverURL(String entityId, Long version) {
		StringBuilder sb = new StringBuilder();
		if (entityId != null) {
			sb.append(FILE_RESOLVER_URL);
			sb.append("entityId=");
			sb.append(entityId);
			if (version != null) {
				sb.append("&version=");
				sb.append(version);
			}
		}
		return sb.toString();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void showBiodallianceBrowser() {
		long uniqueId = new Date().getTime();
		String containerId = "biodallianceContainerId" + uniqueId;
		view.setContainerId(containerId);
		try {
			view.showBiodallianceBrowser(PORTAL_URL_PREFIX, containerId, initChr, initViewStart, initViewEnd, currentConfig, sources);
		} catch (JavaScriptException jse) {
			synAlert.handleException(jse);
		} ;
	}

	@Override
	public void viewAttached() {
		if (isConfigured) {
			// ready to show
			showBiodallianceBrowser();
		}
	}

	public boolean isConfigured() {
		return isConfigured;
	}
}

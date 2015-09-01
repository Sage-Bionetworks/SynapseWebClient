package org.sagebionetworks.web.client.widget.biodalliance13.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.constants.InputType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceSource;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceSourceViewImpl;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidget;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidget.Species;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class BiodallianceEditor implements BiodallianceEditorView.Presenter, WidgetEditorPresenter {
	
	private BiodallianceEditorView view;
	private PortalGinInjector ginInjector;
	private Map<String, String> descriptor;
	private List<BiodallianceSource> sources;
	@Inject
	public BiodallianceEditor(BiodallianceEditorView view, PortalGinInjector ginInjector) {
		this.view = view;
		this.ginInjector = ginInjector;
		sources = new ArrayList<BiodallianceSource>();
		view.initView();
	}
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;
		
		Species species = BiodallianceWidget.DEFAULT_SPECIES;
		if (descriptor.containsKey(WidgetConstants.BIODALLIANCE_SPECIES_KEY)){
			species = Species.valueOf(descriptor.get(WidgetConstants.BIODALLIANCE_SPECIES_KEY));
		}
		
		String chr = BiodallianceWidget.DEFAULT_CHR;
		if (descriptor.containsKey(WidgetConstants.BIODALLIANCE_CHR_KEY)){
			chr = descriptor.get(WidgetConstants.BIODALLIANCE_CHR_KEY);
		}
		
		int viewStart = BiodallianceWidget.DEFAULT_VIEW_START;
		if (descriptor.containsKey(WidgetConstants.BIODALLIANCE_VIEW_START_KEY)){
			viewStart = Integer.parseInt(descriptor.get(WidgetConstants.BIODALLIANCE_VIEW_START_KEY));
		}
		
		int viewEnd = BiodallianceWidget.DEFAULT_VIEW_END;
		if (descriptor.containsKey(WidgetConstants.BIODALLIANCE_VIEW_END_KEY)){
			viewEnd = Integer.parseInt(descriptor.get(WidgetConstants.BIODALLIANCE_VIEW_END_KEY));
		}
		
		sources = new ArrayList<BiodallianceSource>();
		if (descriptor.containsKey(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX + 0)){
			//discover all sources
			sources.addAll(BiodallianceWidget.getSources(descriptor));
		}
		
		view.setChr(chr);
		view.setViewStart(Integer.toString(viewStart));
		view.setViewEnd(Integer.toString(viewEnd));
		if (Species.HUMAN.equals(species)) {
			view.setHuman();
		} else {
			view.setMouse();
		}
		
		for (BiodallianceSource source : sources) {
			view.addTrack(source.asWidget());
		}
	}
	
	@Override
	public void addTrackClicked() {
		BiodallianceSource source = ginInjector.getBiodallianceSource();
		sources.add(source);
		view.addTrack(source.asWidget());
	}
	
	public void clearState() {
		sources.clear();
		view.clear();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void updateDescriptorFromView() {
		//update widget descriptor from the view
		view.checkParams();
		descriptor.put(WidgetConstants.BIODALLIANCE_CHR_KEY, view.getChr());
		Species species = Species.HUMAN;
		if (view.isMouse()) {
			species = Species.MOUSE;
		}
		descriptor.put(WidgetConstants.BIODALLIANCE_SPECIES_KEY, species.name());
		descriptor.put(WidgetConstants.BIODALLIANCE_VIEW_START_KEY, view.getViewStart());
		descriptor.put(WidgetConstants.BIODALLIANCE_VIEW_END_KEY, view.getViewEnd());
		
		//and add the sources to the map
		for (int j = 0; j < sources.size(); j++) {
			descriptor.put(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX+j, sources.get(j).toJsonObject().toString());
		}
	}
	
	@Override
	public String getTextToInsert() {
		return null;
	}
	
	@Override
	public List<String> getNewFileHandleIds() {
		return null;
	}
	@Override
	public List<String> getDeletedFileHandleIds() {
		return null;
	}

}

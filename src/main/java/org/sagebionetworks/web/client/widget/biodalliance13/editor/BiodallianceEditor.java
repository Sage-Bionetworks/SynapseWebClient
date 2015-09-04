package org.sagebionetworks.web.client.widget.biodalliance13.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gwtvisualizationwrappers.client.biodalliance.BiodallianceSource;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidget;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidget.Species;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class BiodallianceEditor implements BiodallianceEditorView.Presenter, WidgetEditorPresenter, BiodallianceSourceActionHandler {
	
	private BiodallianceEditorView view;
	private PortalGinInjector ginInjector;
	private Map<String, String> descriptor;
	private List<BiodallianceSourceEditor> sourceEditors;
	
	@Inject
	public BiodallianceEditor(BiodallianceEditorView view, PortalGinInjector ginInjector) {
		this.view = view;
		view.setPresenter(this);
		this.ginInjector = ginInjector;
		sourceEditors = new ArrayList<BiodallianceSourceEditor>();
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
		
		sourceEditors = new ArrayList<BiodallianceSourceEditor>();
		if (descriptor.containsKey(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX + 0)){
			//discover all sources
			sourceEditors.addAll(getSourceEditors(descriptor));
		}
		
		view.setChr(chr);
		view.setViewStart(Integer.toString(viewStart));
		view.setViewEnd(Integer.toString(viewEnd));
		if (Species.HUMAN.equals(species)) {
			view.setHuman();
		} else {
			view.setMouse();
		}
		
		refreshTracks();
	}
	
	public List<BiodallianceSourceEditor> getSourceEditors(Map<String, String> descriptor) {
		//reconstruct biodalliance sources (if there are any)
		List<BiodallianceSourceEditor> sources = new ArrayList<BiodallianceSourceEditor>();
		int i = 0;
		while (descriptor.containsKey(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX + i)) {
			String sourceJsonString = descriptor.get(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX+i);
			BiodallianceSourceEditor editor = ginInjector.getBiodallianceSourceEditor();
			editor.configure(new BiodallianceSource(sourceJsonString), this);
			sources.add(editor);
			i++;
		}
		return sources;
	}
	
	@Override
	public void addTrackClicked() {
		BiodallianceSourceEditor editor = ginInjector.getBiodallianceSourceEditor();
		editor.configure(new BiodallianceSource(), this);
		sourceEditors.add(editor);
		refreshTracks();
	}
	
	public void clearState() {
		sourceEditors.clear();
		view.clear();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void updateDescriptorFromView() {
		//update widget descriptor from the view
		checkParams();
		for (BiodallianceSourceEditor biodallianceSourceEditor : sourceEditors) {
			biodallianceSourceEditor.checkParams();
		}
		descriptor.clear();
		descriptor.put(WidgetConstants.BIODALLIANCE_CHR_KEY, view.getChr());
		Species species = Species.HUMAN;
		if (view.isMouse()) {
			species = Species.MOUSE;
		}
		descriptor.put(WidgetConstants.BIODALLIANCE_SPECIES_KEY, species.name());
		descriptor.put(WidgetConstants.BIODALLIANCE_VIEW_START_KEY, view.getViewStart());
		descriptor.put(WidgetConstants.BIODALLIANCE_VIEW_END_KEY, view.getViewEnd());
		
		//and add the sources to the map
		for (int j = 0; j < sourceEditors.size(); j++) {
			descriptor.put(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX+j, sourceEditors.get(j).toJsonObject().toString());
		}
	}
	
	public void checkParams() throws IllegalArgumentException{
		if ("".equals(view.getChr())){
			throw new IllegalArgumentException("chr is a required parameter.");
		} else if ("".equals(view.getViewStart())){
			throw new IllegalArgumentException("View start is a required parameter.");
		} else if ("".equals(view.getViewEnd())){
			throw new IllegalArgumentException("View end is a required parameter.");
		}
		//try to parse
		try {
			int chr = Integer.parseInt(view.getChr());
			if (chr < 1) {
				throw new IllegalArgumentException("chr must be greater than or equal to 1.");
			}
			int viewStart = Integer.parseInt(view.getViewStart());
			int viewEnd = Integer.parseInt(view.getViewEnd());
			if (viewStart < 0) {
				throw new IllegalArgumentException("View start must be a positive integer.");	
			}
			if (viewEnd < viewStart) {
				throw new IllegalArgumentException("View start must less than view end.");
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Chr, view start, and view end must be integers.");
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
	
	private void refreshTracks() {
		view.clearTracks();
		for (BiodallianceSourceEditor sourceEditor : sourceEditors) {
			view.addTrack(sourceEditor.asWidget());
			sourceEditor.setMoveDownEnabled(true);
			sourceEditor.setMoveUpEnabled(true);
		}
		if (sourceEditors.size() > 0) {
			sourceEditors.get(0).setMoveUpEnabled(false);
			sourceEditors.get(sourceEditors.size() - 1).setMoveDownEnabled(false);
		}
	}
	@Override
	public void moveUp(BiodallianceSourceEditor sourceEditor) {
		int index = sourceEditors.indexOf(sourceEditor);
		sourceEditors.remove(index);
		sourceEditors.add(index-1, sourceEditor);
		refreshTracks();
	}
	
	@Override
	public void moveDown(BiodallianceSourceEditor sourceEditor) {
		int index = sourceEditors.indexOf(sourceEditor);
		sourceEditors.remove(index);
		sourceEditors.add(index+1, sourceEditor);
		refreshTracks();
	}
	
	@Override
	public void delete(BiodallianceSourceEditor sourceEditor) {
		sourceEditors.remove(sourceEditor);
		refreshTracks();
	}
}

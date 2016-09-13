package org.sagebionetworks.web.client.widget.biodalliance13.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SelectableListItem;
import org.sagebionetworks.web.client.widget.SelectableListView;
import org.sagebionetworks.web.client.widget.SelectionToolbarPresenter;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidget;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidget.Species;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class BiodallianceEditor extends SelectionToolbarPresenter implements BiodallianceEditorView.Presenter, WidgetEditorPresenter {
	
	private BiodallianceEditorView view;
	private PortalGinInjector ginInjector;
	private Map<String, String> descriptor;
	private Callback selectionChangedCallback;
	
	@Inject
	public BiodallianceEditor(BiodallianceEditorView view, PortalGinInjector ginInjector) {
		this.view = view;
		view.setPresenter(this);
		this.ginInjector = ginInjector;
		selectionChangedCallback = new Callback() {
			@Override
			public void invoke() {
				checkSelectionState();
			}
		};
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
		
		items = new ArrayList<SelectableListItem>();
		if (descriptor.containsKey(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX + 0)){
			//discover all sources
			items.addAll(getSourceEditors(descriptor));
		}
		
		view.setChr(chr);
		view.setViewStart(Integer.toString(viewStart));
		view.setViewEnd(Integer.toString(viewEnd));
		if (Species.HUMAN.equals(species)) {
			view.setHuman();
		} else {
			view.setMouse();
		}
		
		refresh();
	}
	
	public List<BiodallianceSourceEditor> getSourceEditors(Map<String, String> descriptor) {
		//reconstruct biodalliance sources (if there are any)
		List<BiodallianceSourceEditor> sources = new ArrayList<BiodallianceSourceEditor>();
		int i = 0;
		while (descriptor.containsKey(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX + i)) {
			String sourceJsonString = descriptor.get(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX+i);
			BiodallianceSourceEditor editor = ginInjector.getBiodallianceSourceEditor();
			editor.setSourceJson(sourceJsonString);
			editor.setSelectionChangedCallback(selectionChangedCallback);
			sources.add(editor);
			i++;
		}
		return sources;
	}
	
	@Override
	public void addTrackClicked() {
		BiodallianceSourceEditor editor = ginInjector.getBiodallianceSourceEditor();
		editor.setSelectionChangedCallback(selectionChangedCallback);
		items.add(editor);
		refresh();
	}
	
	public void clearState() {
		items.clear();
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
		for (SelectableListItem biodallianceSourceEditor : items) {
			((BiodallianceSourceEditor)biodallianceSourceEditor).checkParams();
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
		for (int j = 0; j < items.size(); j++) {
			BiodallianceSourceEditor sourceEditor = (BiodallianceSourceEditor)items.get(j);
			descriptor.put(WidgetConstants.BIODALLIANCE_SOURCE_PREFIX+j, sourceEditor.toJsonObject().toString());
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
	
	@Override
	public void refresh() {
		view.clearTracks();
		for (SelectableListItem sourceEditor : items) {
			view.addTrack(((BiodallianceSourceEditor)sourceEditor).asWidget());
		}
		boolean sourceTracksVisible = items.size() > 0;
		view.setButtonToolbarVisible(sourceTracksVisible);
		view.setTrackHeaderColumnsVisible(sourceTracksVisible);
		checkSelectionState();
	}
	
		
	//for tests
	public List<SelectableListItem> getSourceEditors() {
		return items;
	}
	
	@Override
	public SelectableListView getView() {
		return view;
	}
	
}

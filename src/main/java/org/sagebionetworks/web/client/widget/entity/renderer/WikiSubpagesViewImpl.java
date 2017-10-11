package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesViewImpl extends FlowPanel implements WikiSubpagesView {
	private WikiSubpagesOrderEditor orderEditor;
	private static final String WELL="well";
	private static final String COL_MD_3="col-md-3";
	private static final String COL_MD_9="col-md-9";
	private static final String COL_XS_12="col-xs-12";
	
	private Button showHideButton;
	private Button editOrderButton;
	private FlowPanel navTreeContainer;
	private FlowPanel wikiSubpagesContainer;
	private FlowPanel wikiPageContainer;
	boolean isShowingSubpages;
	
	private WikiSubpageNavigationTree navTree;
	private GlobalApplicationState globalAppState;
	
	@Inject
	public WikiSubpagesViewImpl(WikiSubpagesOrderEditor orderEditor,
								WikiSubpageNavigationTree navTree,
								GlobalApplicationState globalAppState) {
		this.orderEditor = orderEditor;
		this.navTree = navTree;
		this.globalAppState = globalAppState;
		addStyleName("wikiSubpages");
	}
	
	@Override
	public void clear() {
		super.clear();
		if (wikiSubpagesContainer != null) {
			wikiSubpagesContainer.setStyleName("");
		}
		if (wikiPageContainer != null) {
			wikiPageContainer.setStyleName("");
		}
			
	}
	
	@Override
	public void configure(final List<V2WikiHeader> wikiHeaders,
						final FlowPanel wikiSubpagesContainer, FlowPanel wikiPageContainer,
						final String ownerObjectName, Place ownerObjectLink,
						final WikiPageKey curWikiKey, boolean isEmbeddedInOwnerPage,
						CallbackP<WikiPageKey> wikiPageCallback) {
		clear();
		
		this.wikiSubpagesContainer = wikiSubpagesContainer;
		this.wikiPageContainer = wikiPageContainer;
		navTreeContainer = new FlowPanel();
		navTreeContainer.addStyleName("margin-bottom-10");

		//this widget shows nothing if it doesn't have any pages!
		if (wikiHeaders.size() <=1 )
			return;
		
		//only show the tree if the root has children
		navTree.configure(wikiHeaders, ownerObjectName, ownerObjectLink, curWikiKey, isEmbeddedInOwnerPage, wikiPageCallback);
		
		showHideButton = DisplayUtils.createButton("");
		editOrderButton = DisplayUtils.createButton("Edit Order");
		editOrderButton.addStyleName("btn btn-default btn-xs left");

		editOrderButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				wikiSubpagesContainer.clear();
				orderEditor.configure(curWikiKey, ownerObjectName);
				wikiSubpagesContainer.add(orderEditor.asWidget());
				Button finishEditingOrderButton = DisplayUtils.createButton("Done");
				finishEditingOrderButton.addStyleName("btn btn-default margin-top-10 right");
				wikiSubpagesContainer.add(finishEditingOrderButton);
				finishEditingOrderButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						globalAppState.refreshPage();
					}
				});
				DisplayUtils.scrollToTop();
			}
		});
		
		showHideButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (isShowingSubpages)
					hideSubpages();
				else
					showSubpages();
			}
		});
		
		navTreeContainer.add(navTree.asWidget());
		
		add(navTreeContainer);
		add(editOrderButton);
		add(showHideButton);
		showSubpages();
		clearWidths();
	}
	
	/**
	 * Work around the Chrome bug.  See DisplayUtils.clearElementWidth() for more info.
	 */
	private void clearWidths() {
		DisplayUtils.clearElementWidth(getElement());
		if (wikiSubpagesContainer != null) 
			DisplayUtils.clearElementWidth(wikiSubpagesContainer.getElement());
		if (navTreeContainer != null)
			DisplayUtils.clearElementWidth(navTreeContainer.getElement());
		if (wikiPageContainer != null)
			DisplayUtils.clearElementWidth(wikiPageContainer.getElement());
	}
	
	@Override
	public void hideSubpages() {
		isShowingSubpages = false;
		// This call to layout is necessary to force the scroll bar to appear on page-load
		if (wikiSubpagesContainer != null){
			wikiSubpagesContainer.setStyleName(COL_XS_12);
		}
		
		if (editOrderButton != null) {
			editOrderButton.setVisible(false);
		}
				
		if (showHideButton != null) {
			showHideButton.setText("Show Pages " + DisplayConstants.RIGHT_ARROWS);
			showHideButton.removeStyleName("right");
			showHideButton.addStyleName("btn btn-default btn-xs margin-right-40 left");
		}
		
		if (navTreeContainer != null)
			DisplayUtils.hide(navTreeContainer);
		
		if (wikiPageContainer != null) {
			wikiPageContainer.setStyleName("");
			wikiPageContainer.addStyleName(COL_XS_12);
			wikiPageContainer.setVisible(true);
		}
	}
	
	@Override
	public void showSubpages() {
		isShowingSubpages = true;
		if (wikiSubpagesContainer != null) {
			wikiSubpagesContainer.setStyleName("");
			wikiSubpagesContainer.addStyleName(COL_XS_12);
			wikiSubpagesContainer.addStyleName(WELL);
			wikiSubpagesContainer.addStyleName(COL_MD_3);
		}
		
		if (editOrderButton != null) {
			editOrderButton.setVisible(true);
		}
			
		if (wikiPageContainer != null) {
			wikiPageContainer.setStyleName("");
			wikiPageContainer.addStyleName(COL_XS_12);
			wikiPageContainer.addStyleName(COL_MD_9);
		}
				
		if (showHideButton != null) {
			showHideButton.setText(DisplayConstants.LEFT_ARROWS);
			showHideButton.removeStyleName("left");
			showHideButton.addStyleName("btn btn-default btn-xs right");		
		}
		
		if (navTreeContainer != null)
			DisplayUtils.show(navTreeContainer);
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	public interface GetOrderHintCallback {
		public List<String> getCurrentOrderHint();
	}

	@Override
	public void setEditOrderButtonVisible(boolean visible) {
		if (editOrderButton != null) {
			editOrderButton.setVisible(visible);
		}
	}
}

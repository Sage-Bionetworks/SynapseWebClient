package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtilsImpl;
import org.sagebionetworks.web.client.events.WikiSubpagesCollapseEvent;
import org.sagebionetworks.web.client.events.WikiSubpagesExpandEvent;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesViewImpl extends FlowPanel implements WikiSubpagesView {
	private WikiSubpagesOrderEditor orderEditor;
	private Button showHideButton;
	private Button editOrderButton;
	private FlowPanel navTreeContainer;
	boolean isShowingSubpages;
	
	private WikiSubpageNavigationTree navTree;
	private EventBus eventBus;
	private Presenter presenter;
	
	@Inject
	public WikiSubpagesViewImpl(WikiSubpagesOrderEditor orderEditor,
								WikiSubpageNavigationTree navTree,
								EventBus eventBus) {
		this.orderEditor = orderEditor;
		this.navTree = navTree;
		this.eventBus = eventBus;
		addStyleName("wikiSubpages");
	}
	
	@Override
	public void setPresenter(Presenter p) {
		this.presenter = p;
	}
	
	@Override
	public void clear() {
		super.clear();
	}
	
	@Override
	public void configure(final List<V2WikiHeader> wikiHeaders,
						final String ownerObjectName, Place ownerObjectLink,
						final WikiPageKey curWikiKey, boolean isEmbeddedInOwnerPage,
						CallbackP<WikiPageKey> wikiPageCallback,
						ActionMenuWidget actionMenu) {
		clear();
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
		final ClickHandler editOrderClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				clear();
				orderEditor.configure(curWikiKey, ownerObjectName);
				add(orderEditor.asWidget());
				Button finishEditingOrderButton = DisplayUtils.createButton("Done");
				finishEditingOrderButton.addStyleName("btn btn-default margin-top-10 right");
				add(finishEditingOrderButton);
				finishEditingOrderButton.addClickHandler(e -> {
					clear();
					presenter.clearCachedHeaderTree();
					presenter.refreshWikiHeaderTree();
					SynapseJSNIUtilsImpl._scrollIntoView(getElement());
				});
				DisplayUtils.scrollToTop();
			}
		};
		editOrderButton.addClickHandler(editOrderClickHandler);
		if (actionMenu != null) {
			actionMenu.setActionListener(Action.REORDER_WIKI_SUBPAGES, new ActionMenuWidget.ActionListener() {
				@Override
				public void onAction(Action action) {
					editOrderClickHandler.onClick(null);
				}
			});
		}
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
		if (navTreeContainer != null)
			DisplayUtils.clearElementWidth(navTreeContainer.getElement());
	}
	
	@Override
	public void hideSubpages() {
		isShowingSubpages = false;
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
		
		eventBus.fireEvent(new WikiSubpagesCollapseEvent());
	}
	
	@Override
	public void showSubpages() {
		isShowingSubpages = true;
		
		if (editOrderButton != null) {
			editOrderButton.setVisible(true);
		}
				
		if (showHideButton != null) {
			showHideButton.setText(DisplayConstants.LEFT_ARROWS);
			showHideButton.removeStyleName("left");
			showHideButton.addStyleName("btn btn-default btn-xs right");		
		}
		
		if (navTreeContainer != null)
			DisplayUtils.show(navTreeContainer);
		
		eventBus.fireEvent(new WikiSubpagesExpandEvent());
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
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
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
	
	@Override
	public boolean contains(String wikiPageKey) {
		return navTree.contains(wikiPageKey);
	}
	
	@Override
	public void setPage(String wikiPageKey) {
		navTree.setPage(wikiPageKey);
	}
}

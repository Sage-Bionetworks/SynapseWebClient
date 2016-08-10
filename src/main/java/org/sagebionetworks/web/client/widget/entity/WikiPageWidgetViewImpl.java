package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Italic;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.MessagePopup;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Lightweight widget used to show a wiki page (has a markdown widget and pagebrowser)
 * 
 * @author Jay
 *
 */
public class WikiPageWidgetViewImpl extends FlowPanel implements WikiPageWidgetView {

	public interface Binder extends UiBinder<Widget, WikiPageWidgetViewImpl> {}
	
	WikiPageWidgetView.Presenter presenter;

	@UiField
	FlowPanel mainPanel;
	
	@UiField
	Italic noWikiCanEditMessage;
	
	@UiField
	Italic noWikiCannotEditMessage;
	
	@UiField
	FlowPanel wikiPagePanel;
	
	@UiField
	FlowPanel createdModifiedHistoryPanel;
	
	@UiField
	Alert diffVersionAlert;
	
	@UiField
	SimplePanel modifiedCreatedByPanel;
	
	@UiField
	Button wikiHistoryButton;
	
	@UiField
	Button restoreButton;
	
	@UiField
	FlowPanel wikiHistoryPanel;
	
	@UiField
	FlowPanel wikiSubpagesPanel;
	
	@UiField
	SimplePanel synAlertPanel;
	
	@UiField
	HTMLPanel loadingPanel;
	
	@UiField
	SimplePanel breadcrumbPanel;
	
	@UiField
	SimplePanel markdownPanel;
	
	@UiField
	Anchor anchorToCurrentVersion;
	
	@UiField
	Collapse historyCollapse;
	
	@UiField
	Heading wikiHeading;
	
	Widget widget;

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Inject
	public WikiPageWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		restoreButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//state held in presenter
				presenter.restoreClicked();
			}
		});
		anchorToCurrentVersion.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.reloadWikiPage();
				setDiffVersionAlertVisible(false);
			}
		});
		wikiHistoryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (historyCollapse.isShown()) {
					wikiHistoryButton.setIcon(IconType.CARET_SQUARE_O_RIGHT);
					historyCollapse.hide();
				} else {
					wikiHistoryButton.setIcon(IconType.CARET_SQUARE_O_DOWN);
					historyCollapse.show();
				}
			}
		});
		historyCollapse.hide();
		wikiHistoryButton.setIcon(IconType.CARET_SQUARE_O_RIGHT);
	}
	
	@Override
	public void setWikiHeadingText(String title) {
		wikiHeading.setText(title);
	}
	
	@Override
	public void scrollWikiHeadingIntoView() {
		wikiHeading.getElement().scrollIntoView();
	}
	
	@Override
	public void clear() {
		historyCollapse.hide();
		wikiHistoryButton.setIcon(IconType.CARET_SQUARE_O_RIGHT);
		loadingPanel.setVisible(false);
		diffVersionAlert.setVisible(false);
		noWikiCanEditMessage.setVisible(false);
		noWikiCannotEditMessage.setVisible(false);
	}
	
	@Override
	public void showPopup(String title, String message, MessagePopup popupType, Callback okCallback, Callback cancelCallback) {
		DisplayUtils.showPopup(title, message, popupType, okCallback, cancelCallback); 
	}
	
	@Override
	public void setWikiSubpagesWidget(IsWidget wikiSubpages) {
		wikiSubpagesPanel.clear();
		wikiSubpagesPanel.add(wikiSubpages);
	}

	@Override
	public void setWikiSubpagesWidgetVisible(boolean isVisible) {
		wikiSubpagesPanel.setVisible(isVisible);
	}
	
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	@Override
	public void addStyleName(String style) {
		widget.addStyleName(style);
	}


	/*
	 *     +------+
	 *     | Wiki |
	 * +---------------------------------------------------------+
	 * | +---------+ +-----------------------------------------+ |
	 * | | 2       | | +-------------------------------------+ | |
	 * | |         | | | Notice                              | | |
	 * | |         | | +-------------------------------------+ | |
	 * | +---------+ |  Wiki content                           | |
	 * |             |                                         | |
	 * |             |                                         | |
	 * |             |                                         | |
	 * |             |                                         | |
	 * |             | Modified/Created by                     | |
	 * |             | Show Wiki History                     3 | |
	 * | 1           +-----------------------------------------+ |
	 * +---------------------------------------------------------+
	 * 
	 * 1 - this widget
	 * 2 - WikiSubpagesWidget widget
	 * 3 - wikiPagePanel
	 */
	
	@Override
	public void setWikiSubpagesContainers(WikiSubpagesWidget wikiSubpages) {
		wikiSubpages.setContainers(wikiSubpagesPanel, wikiPagePanel);
	}
	
	@Override
	public void setWikiHistoryWidget(IsWidget historyWidget) {
		wikiHistoryPanel.clear();
		wikiHistoryPanel.add(historyWidget);
	}

	@Override
	public void setSynapseAlertWidget(IsWidget synapseAlert) {
		synAlertPanel.setWidget(synapseAlert);
	}

	@Override
	public void setMarkdownWidget(IsWidget markdownWidget) {
		markdownPanel.setWidget(markdownWidget);
	}

	@Override
	public void setBreadcrumbWidget(IsWidget breadcrumb) {
		breadcrumbPanel.setWidget(breadcrumb);
	}

	@Override
	public void setRestoreButtonVisible(boolean isVisible) {
		restoreButton.setVisible(isVisible);
	}

	@Override
	public void setDiffVersionAlertVisible(boolean isVisible) {
		diffVersionAlert.setVisible(isVisible);
	}

	@Override
	public void setBreadcrumbsVisible(boolean isVisible) {
		breadcrumbPanel.setVisible(isVisible);
	}

	@Override
	public void setModifiedCreatedByHistoryPanelVisible(boolean isVisible) {
		createdModifiedHistoryPanel.setVisible(isVisible);
	}

	@Override
	public void setNoWikiCannotEditMessageVisible(boolean isVisible) {
		noWikiCannotEditMessage.setVisible(isVisible);
	}
	
	@Override
	public void setNoWikiCanEditMessageVisible(boolean isVisible) {
		noWikiCanEditMessage.setVisible(isVisible);
		
	}

	@Override
	public void setMarkdownVisible(boolean isVisible) {
		markdownPanel.setVisible(isVisible);
	}

	@Override
	public void setMainPanelVisible(boolean isVisible) {
		mainPanel.setVisible(isVisible);
	}
	
	@Override
	public void setLoadingVisible(boolean isVisible) {
		loadingPanel.setVisible(isVisible);
	}

	@Override
	public void setWikiHistoryVisible(boolean isVisible) {
		if (isVisible) {
			historyCollapse.show();
		} else {
			historyCollapse.hide();
		}
	}

	@Override
	public void setModifiedCreatedBy(IsWidget modifiedCreatedBy) {
		modifiedCreatedByPanel.setWidget(modifiedCreatedBy);
	}
}

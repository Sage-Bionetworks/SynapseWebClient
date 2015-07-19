package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Italic;
import org.gwtbootstrap3.client.ui.html.Span;
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
	Span modifiedOnField;
	
	@UiField
	Span createdOnField;
	
	@UiField
	SimplePanel	modifiedByBadgePanel;
	
	@UiField
	SimplePanel	createdByBadgePanel;
	
	@UiField
	Button wikiHistoryButton;
	
	@UiField
	Button restoreButton;
	
	@UiField
	SimplePanel wikiHistoryPanel;
	
	@UiField
	FlowPanel wikiSubpagesPanel;
	
	@UiField
	SimplePanel synAlertPanel;
	
	@UiField
	HTMLPanel loadingPanel;
	
	@UiField
	HTMLPanel modifiedByPanel;
	
	@UiField
	HTMLPanel createdByPanel;
	
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

	public interface OwnerObjectNameCallback{
		public void ownerObjectNameInitialized();
	}

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
				hideDiffVersionAlert();
			}
		});
		wikiHistoryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!historyCollapse.isCollapsing()) {
					if (historyCollapse.isShown()) {
						wikiHistoryButton.setIcon(IconType.CARET_SQUARE_O_DOWN);
					} else {
						wikiHistoryButton.setIcon(IconType.CARET_SQUARE_O_RIGHT);
					}
				}
			}
		});
	}
	
	@Override
	public void setWikiHeadingText(String title) {
		wikiHeading.setText(title);
	}
	
	@Override
	public void clear() {
		loadingPanel.setVisible(false);
		diffVersionAlert.setVisible(false);
		noWikiCanEditMessage.setVisible(false);
		noWikiCannotEditMessage.setVisible(false);
	}
	
	@Override
	public void showNoWikiCanEditMessage() {
		noWikiCanEditMessage.setVisible(true);
	}
	
	@Override
	public void showNoWikiCannotEditMessage() {
		noWikiCannotEditMessage.setVisible(true);
	}
	
	@Override
	public void showDiffVersionAlert() {
		diffVersionAlert.setVisible(true);
	}
	
	@Override
	public void hideDiffVersionAlert() {
		diffVersionAlert.setVisible(false);
	}
	
	@Override
	public void showPopup(String title, String message, MessagePopup popupType, Callback okCallback, Callback cancelCallback) {
		DisplayUtils.showPopup(title, message, 
				popupType, okCallback, cancelCallback);
	}
	
	@Override
	public void setWikiSubpagesWidget(IsWidget wikiSubpages) {
		wikiSubpagesPanel.clear();
		wikiSubpagesPanel.add(wikiSubpages);
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
	public void showCreatedBy(boolean isVisible) {
		createdByPanel.setVisible(isVisible);
	}

	@Override
	public void showModifiedBy(boolean isVisible) {
		modifiedByPanel.setVisible(isVisible);
	}

	@Override
	public void showWikiHistory(boolean isVisible) {
		wikiHistoryPanel.setVisible(isVisible);
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
	public void setModifiedByBadge(IsWidget modifiedByUserBadge) {
		modifiedByBadgePanel.setWidget(modifiedByUserBadge);
	}
	
	@Override
	public void setModifiedByText(String modifiedByText) {
		modifiedOnField.setText(modifiedByText);
	}
	
	@Override
	public void setCreatedByBadge(IsWidget createdByUserBadge) {
		createdByBadgePanel.setWidget(createdByUserBadge);
	}
	
	@Override
	public void setCreatedByText(String createdByText) {
		createdOnField.setText(createdByText);
	}

	
	@Override
	public void setWikiHistoryWidget(IsWidget historyWidget) {
		wikiHistoryPanel.setWidget(historyWidget);
	}
	
	@Override
	public void showRestoreButton() {
		restoreButton.setVisible(true);
	}

	@Override
	public void setSynapseAlertWidget(IsWidget synapseAlert) {
		synAlertPanel.setWidget(synapseAlert);
	}
	
	@Override
	public void showLoading() {
		loadingPanel.setVisible(true);
	}
	
	@Override
	public void hideLoading() {
		loadingPanel.setVisible(false);
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
	public void showBreadcrumbs() {
		breadcrumbPanel.setVisible(true);
	}

	@Override
	public void hideBreadcrumbs() {
		breadcrumbPanel.setVisible(false);
	}
	
	@Override
	public void hideHistory() {
		wikiHistoryPanel.setVisible(false);
	}
	
	@Override
	public void showHistory() {
		wikiHistoryPanel.setVisible(true);
	}
	
	@Override
	public void hideCreatedModified() {
		createdModifiedHistoryPanel.setVisible(false);
	}
	
	@Override
	public void showCreatedModified() {
		createdModifiedHistoryPanel.setVisible(true);
	}

	@Override
	public void hideMarkdown() {
		markdownPanel.setVisible(false);
	}

	@Override
	public void showMarkdown() {
		markdownPanel.setVisible(true);		
	}

	@Override
	public void showMainPanel() {
		mainPanel.setVisible(true);
	}

	@Override
	public void hideMainPanel() {
		mainPanel.setVisible(false);
	}
	
	@Override
	public void showHistoryCollapse() {
		historyCollapse.show();
	}
	
	@Override
	public void hideHistoryCollapse() {
		historyCollapse.hide();
	}
}

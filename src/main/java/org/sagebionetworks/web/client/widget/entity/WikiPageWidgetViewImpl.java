package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Italic;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.MessagePopup;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.WikiDiff;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.binder.EventBinder;

/**
 * Lightweight widget used to show a wiki page (has a markdown widget and pagebrowser)
 * 
 * @author Jay
 *
 */
public class WikiPageWidgetViewImpl extends FlowPanel implements WikiPageWidgetView {
	private static final String WELL = "well";
	private static final String COL_MD_3 = "col-md-3";
	private static final String COL_MD_9 = "col-md-9";
	private static final String COL_XS_12 = "col-xs-12";

	public interface Binder extends UiBinder<Widget, WikiPageWidgetViewImpl> {
	}

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
	Italic createdOnText;
	@UiField
	Italic modifiedOnText;

	@UiField
	Button wikiHistoryButton;
	@UiField
	Button wikiCompareButton;

	@UiField
	Button restoreButton;

	@UiField
	FlowPanel wikiHistoryPanel;

	@UiField
	FlowPanel wikiSubpagesPanel;

	@UiField
	SimplePanel synAlertPanel;

	@UiField
	LoadingSpinner loadingPanel;

	@UiField
	SimplePanel markdownPanel;

	@UiField
	Anchor anchorToCurrentVersion;

	@UiField
	Collapse historyCollapse;

	@UiField
	Div wikiHeadingContainer;
	Heading wikiHeading;
	Widget widget;
	WikiPageKey key;

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	SynapseJSNIUtils jsniUtils;

	@Inject
	public WikiPageWidgetViewImpl(Binder binder, SynapseJSNIUtils jsniUtils) {
		widget = binder.createAndBindUi(this);
		this.jsniUtils = jsniUtils;
		restoreButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// state held in presenter
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
					presenter.showWikiHistory();
				}
			}
		});
		historyCollapse.hide();
		wikiHistoryButton.setIcon(IconType.CARET_SQUARE_O_RIGHT);
		wikiCompareButton.addClickHandler(event -> {
			WikiDiff place = new WikiDiff(key);
			DisplayUtils.newWindow("#!WikiDiff:" + place.toToken(), "_blank", "");
		});
	}

	@Override
	public void setWikiHeadingText(String title) {
		wikiHeadingContainer.clear();
		if (DisplayUtils.isDefined(title)) {
			wikiHeading = new Heading(HeadingSize.H2);
			wikiHeading.setText(title);
			wikiHeadingContainer.add(wikiHeading);
		}
	}

	@Override
	public void scrollWikiHeadingIntoView() {
		if (wikiHeading != null && !DisplayUtils.isInViewport(wikiHeading)) {
			jsniUtils.scrollIntoView(wikiHeading.getElement());
		}
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
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
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
	 * +------+ | Wiki | +---------------------------------------------------------+ | +---------+
	 * +-----------------------------------------+ | | | 2 | | +-------------------------------------+ |
	 * | | | | | | Notice | | | | | | | +-------------------------------------+ | | | +---------+ | Wiki
	 * content | | | | | | | | | | | | | | | | | | | | Modified/Created by | | | | Show Wiki History 3 |
	 * | | 1 +-----------------------------------------+ |
	 * +---------------------------------------------------------+
	 * 
	 * 1 - this widget 2 - WikiSubpagesWidget widget 3 - wikiPagePanel
	 */

	@Override
	public void expandWikiSubpages() {
		wikiPagePanel.setStyleName("");
		wikiPagePanel.addStyleName(COL_XS_12);
		wikiPagePanel.addStyleName(COL_MD_9);

		wikiSubpagesPanel.setStyleName("");
		wikiSubpagesPanel.addStyleName(COL_XS_12);
		wikiSubpagesPanel.addStyleName(WELL);
		wikiSubpagesPanel.addStyleName(COL_MD_3);
	}

	@Override
	public void collapseWikiSubpages() {
		wikiPagePanel.setStyleName("");
		wikiPagePanel.addStyleName(COL_XS_12);

		wikiSubpagesPanel.setStyleName("");
		wikiSubpagesPanel.addStyleName(COL_XS_12);
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
	public void setRestoreButtonVisible(boolean isVisible) {
		restoreButton.setVisible(isVisible);
	}

	@Override
	public void setDiffVersionAlertVisible(boolean isVisible) {
		diffVersionAlert.setVisible(isVisible);
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
			wikiHistoryButton.setIcon(IconType.CARET_SQUARE_O_DOWN);
		} else {
			historyCollapse.hide();
			wikiHistoryButton.setIcon(IconType.CARET_SQUARE_O_RIGHT);
		}
	}

	@Override
	public void setCreatedOn(String date) {
		createdOnText.setText(date);
	}

	@Override
	public void setModifiedOn(String date) {
		modifiedOnText.setText(date);
	}

	@Override
	public void setWikiHistoryDiffToolButtonVisible(boolean visible, WikiPageKey key) {
		this.key = key;
		wikiCompareButton.setVisible(visible);
	}

	/** Event binder code **/
	interface EBinder extends EventBinder<WikiPageWidget> {
	};

	private final EBinder eventBinder = GWT.create(EBinder.class);

	@Override
	public EventBinder<WikiPageWidget> getEventBinder() {
		return eventBinder;
	}
}

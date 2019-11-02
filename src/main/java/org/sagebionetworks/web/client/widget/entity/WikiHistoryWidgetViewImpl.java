package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.MessagePopup;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.THead;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableHeader;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.entity.WikiHistoryWidget.ActionHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiHistoryWidgetViewImpl extends FlowPanel implements WikiHistoryWidgetView {
	Button loadMoreHistoryButton;
	HTMLPanel inlineErrorMessagePanel;
	FlowPanel historyPanel;
	private boolean canEdit;
	private List<V2WikiHistorySnapshot> historyList;
	private List<HistoryEntry> historyEntries;
	private String currentVersion;
	WikiHistoryWidgetView.Presenter presenter;
	private ActionHandler actionHandler;
	private boolean isFirstGetHistory;
	private int offset;
	private int resultSize;
	DateTimeUtils dateTimeUtils;
	Div loadingUI;
	Div synAlertContainer = new Div();

	@Inject
	public WikiHistoryWidgetViewImpl(DateTimeUtils dateTimeUtils) {
		this.dateTimeUtils = dateTimeUtils;
		addStyleName("min-height-200");
		loadingUI = new Div();
		loadingUI.add(DisplayUtils.getLoadingWidget("Loading"));
		add(synAlertContainer);
	}

	private static class HistoryEntry {
		private final String version;
		private final Date modifiedOn;
		private final String user;

		public HistoryEntry(String version, String user, Date modifiedOn) {
			this.user = user;
			this.version = version;
			this.modifiedOn = modifiedOn;
		}
	}

	@Override
	public void configure(boolean canEdit, ActionHandler actionHandler) {
		this.canEdit = canEdit;
		this.actionHandler = actionHandler;
		this.isFirstGetHistory = true;
		this.offset = 0;
		// Reset history
		hideHistoryWidget();
		add(loadingUI);
		historyList = new ArrayList<V2WikiHistorySnapshot>();
		historyEntries = new ArrayList<HistoryEntry>();
		presenter.configureNextPage(new Long(offset), new Long(10));
	}

	@Override
	public void updateHistoryList(List<V2WikiHistorySnapshot> historyResults) {
		for (int i = 0; i < historyResults.size(); i++) {
			historyList.add(historyResults.get(i));
		}
		resultSize = historyResults.size();
		if (isFirstGetHistory) {
			currentVersion = historyResults.get(0).getVersion();
		}
	}

	@Override
	public void buildHistoryWidget() {
		// We have all the data to create entries for the table
		createHistoryEntries();
		// Create or build upon the history widget
		isFirstGetHistory = false;
		createHistoryWidget();
	}

	private void createHistoryEntries() {
		if (historyList != null) {
			for (int i = offset; i < historyList.size(); i++) {
				V2WikiHistorySnapshot snapshot = historyList.get(i);
				// Create an entry
				String userId = snapshot.getModifiedBy();
				String modifiedByName = presenter.getNameForUserId(userId);
				if (modifiedByName == null) {
					modifiedByName = userId;
				}
				HistoryEntry entry = new HistoryEntry(snapshot.getVersion(), modifiedByName, snapshot.getModifiedOn());
				historyEntries.add(entry);
			}
		}
	}

	private void createHistoryWidget() {
		// Remove any old table or inline error message first
		if (historyPanel != null || inlineErrorMessagePanel != null) {
			hideHistoryWidget();
		}

		Table historyTable = new Table();
		historyTable.setWidth("100%");

		loadMoreHistoryButton = new Button("Load more history");
		loadMoreHistoryButton.addStyleName("margin-top-10");
		loadMoreHistoryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				offset += resultSize;
				presenter.configureNextPage(new Long(offset), new Long(10));
			}
		});

		// create table header
		THead thead = new THead();
		TableRow headerRow = new TableRow();
		headerRow.setHeight("30px");
		headerRow.addStyleName("border-bottom-1");
		if (canEdit) {
			TableHeader th = new TableHeader();
			th.setText("Restore");
			headerRow.add(th);
		}

		TableHeader th = new TableHeader();
		th.setText("Preview");
		headerRow.add(th);

		th = new TableHeader();
		th.setText("Version");
		headerRow.add(th);

		th = new TableHeader();
		th.setText("Modified By");
		headerRow.add(th);

		th = new TableHeader();
		th.setText("Modified On");
		headerRow.add(th);

		thead.add(headerRow);
		historyTable.add(thead);

		TBody tBody = new TBody();
		for (final HistoryEntry entry : historyEntries) {
			TableRow row = new TableRow();
			row.setHeight("30px");
			row.addStyleName("border-bottom-1");
			tBody.add(row);
			TableData td;

			// Restore if edit permissions granted
			if (canEdit) {
				td = new TableData();
				td.setWidth("100px");
				if (entry.version.equals(currentVersion)) {
					td.add(new Span("(Current Wiki)"));
				} else {
					Button button = new Button("Restore");
					button.setSize(ButtonSize.EXTRA_SMALL);
					button.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							showRestorationWarning(new Long(entry.version));
						}
					});
					td.add(button);
				}
				row.add(td);
			}

			// Preview
			td = new TableData();
			td.setWidth("100px");
			Button button = new Button("Preview");
			button.setSize(ButtonSize.EXTRA_SMALL);
			button.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					actionHandler.previewClicked(new Long(entry.version), new Long(currentVersion));
				}
			});
			td.add(button);
			row.add(td);

			// Version
			td = new TableData();
			td.add(new Text(entry.version));
			row.add(td);

			// Modified by
			td = new TableData();
			td.add(new Text(entry.user));
			row.add(td);

			// Modified on
			td = new TableData();
			td.add(new Text(dateTimeUtils.getDateTimeString(entry.modifiedOn)));
			row.add(td);
		}
		historyTable.add(tBody);
		historyTable.setVisible(true);

		historyPanel = new FlowPanel();
		historyPanel.add(wrapWidget(historyTable, "margin-top-5"));
		historyPanel.add(loadMoreHistoryButton);
		add(historyPanel);
		loadingUI.removeFromParent();
	}

	@Override
	public void hideHistoryWidget() {
		if (historyPanel != null) {
			historyPanel.removeFromParent();
		}
		if (inlineErrorMessagePanel != null) {
			inlineErrorMessagePanel.removeFromParent();
		}
	}

	@Override
	public void showHistoryWidget() {
		if (historyPanel != null) {
			historyPanel.setVisible(true);
		}
	}

	@Override
	public void hideLoadMoreButton() {
		loadMoreHistoryButton.setVisible(false);
	}

	public void showRestorationWarning(final Long wikiVersion) {
		org.sagebionetworks.web.client.utils.Callback okCallback = () -> {
			actionHandler.restoreClicked(wikiVersion);
		};
		org.sagebionetworks.web.client.utils.Callback cancelCallback = () -> {
		};
		DisplayUtils.showPopup(DisplayConstants.RESTORING_WIKI_VERSION_WARNING_TITLE, DisplayConstants.RESTORING_WIKI_VERSION_WARNING_MESSAGE, MessagePopup.WARNING, okCallback, cancelCallback);
	}

	private SimplePanel wrapWidget(Widget widget, String styleNames) {
		SimplePanel widgetWrapper = new SimplePanel();
		widgetWrapper.addStyleName(styleNames);
		widgetWrapper.setWidget(widget);
		return widgetWrapper;
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void showLoading() {}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showErrorMessage(String message) {
		// Show an inline error message
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		builder.appendHtmlConstant(message);
		inlineErrorMessagePanel = new HTMLPanel(builder.toSafeHtml());
		add(inlineErrorMessagePanel);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
}

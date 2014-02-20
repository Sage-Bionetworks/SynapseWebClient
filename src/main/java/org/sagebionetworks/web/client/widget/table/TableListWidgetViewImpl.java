package org.sagebionetworks.web.client.widget.table;

import java.util.List;

import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

public class TableListWidgetViewImpl extends FlowPanel implements TableListWidgetView {
	
	private Presenter presenter;
	FlowPanel listContainer;
	
	
	final static String ROWS = "Rows";
	final static String STORAGE = "Storage";
	final static String LAST_UPDATED = "Last Updated";
	static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
	
	@Inject
	public TableListWidgetViewImpl() {
		listContainer = new FlowPanel();
		listContainer.addStyleName("list-group");
		add(listContainer);
	}

	@Override
	public void configure(List<TableEntity> tables, boolean canEdit) {
		for(final TableEntity table : tables) {
			String nRows = String.valueOf(Random.nextInt(10000));
			String storageSize = String.valueOf(Random.nextInt(999)) + " Kb";
			String modifiedOn = DATE_FORMAT.format(table.getModifiedOn());
			String entryHtml = "<h4 class=\"list-group-item-heading\">"
								+ SafeHtmlUtils.fromString(table.getName()).asString() 
								+"</h4>"
								+ "<p class=\"list-group-item-text\">" + ROWS + ": " + nRows 
								+ " | " + STORAGE + ": " + storageSize
								+ " | " + LAST_UPDATED + ": " + modifiedOn
								+ "</p>";
			SafeHtml safe = SafeHtmlUtils.fromTrustedString(entryHtml);
			Anchor entry = new Anchor();
			entry.addStyleName("list-group-item");
			entry.setHTML(entryHtml);
			entry.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					showInfo("clicked", "view table: " + table.getName());
				}
			});
			listContainer.add(entry);
		}
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

}

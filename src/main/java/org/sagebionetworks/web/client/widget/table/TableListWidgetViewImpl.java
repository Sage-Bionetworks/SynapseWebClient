package org.sagebionetworks.web.client.widget.table;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.DisplayUtils.IconSize;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.widget.entity.dialog.NameAndDescriptionEditorDialog;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

public class TableListWidgetViewImpl extends FlowPanel implements TableListWidgetView {
	
	final static String ROWS = "Rows";
	final static String STORAGE = "Storage";
	final static String LAST_UPDATED = "Last Updated";
	static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);

	private Presenter presenter;
	FlowPanel panel;
	GlobalApplicationState globalApplicationState;
	ImageResource tableEntityIcon;
	
	@Inject
	public TableListWidgetViewImpl(GlobalApplicationState globalApplicationState, IconsImageBundle iconsImageBundle) {
		this.globalApplicationState = globalApplicationState;
		panel = new FlowPanel();		
		add(panel);
		
		tableEntityIcon = DisplayUtils.getSynapseIconForEntityClassName(TableEntity.class.getName(), IconSize.PX24, iconsImageBundle);		
		
	}

	@Override
	public void configure(List<EntityHeader> tables, boolean canEdit, boolean showAddTable) {
		panel.clear();
		if(showAddTable) {
			Button addTable = DisplayUtils.createIconButton(DisplayConstants.ADD_TABLE, ButtonType.DEFAULT, "glyphicon-plus");
			addTable.addClickHandler(new ClickHandler() {			
				@Override
				public void onClick(ClickEvent event) {
					NameAndDescriptionEditorDialog.showNameDialog(DisplayConstants.TABLE_NAME, new NameAndDescriptionEditorDialog.Callback() {					
						@Override
						public void onSave(String name, String description) {
							presenter.createTableEntity(name);
						}
					});
				}
			});
			panel.add(addTable);				
		}
		
		FlowPanel tableListContainer = new FlowPanel();
		tableListContainer.addStyleName("list-group margin-top-15");
		for(final EntityHeader table : tables) {
			String nRows = String.valueOf(Random.nextInt(10000));
			String storageSize = String.valueOf(Random.nextInt(999)) + " Kb";
			
			//String modifiedOn = DATE_FORMAT.format(table.getModifiedOn());
			String modifiedOn = "date";
			String entryHtml = "<h4 class=\"list-group-item-heading\">"
							    + AbstractImagePrototype.create(tableEntityIcon).getHTML() 
								+ "<span class=\"movedown-2\"> " + SafeHtmlUtils.fromString(table.getName()).asString() + "</span>" 
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
					globalApplicationState.getPlaceChanger().goTo(new Synapse(table.getId(), null, EntityArea.TABLES, null));
				}
			});
			tableListContainer.add(entry);
		}
		panel.add(tableListContainer);
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

	@Override
	public void showLoadingError() {
		showErrorMessage("temp message");
		
	}

}

package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.BootstrapTable;
import org.sagebionetworks.web.client.widget.WidgetMenu;
import org.sagebionetworks.web.client.widget.entity.EntityGroupRecordDisplay;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;


public class EntityListRenderer extends SimplePanel {		
	
	private static final String HEADER_NAME = "Name";
	private static final String HEADER_DOWNLOAD = " ";
	private static final String HEADER_VERSION = "Version";
	private static final String HEADER_DESC = "Description";
	private static final String HEADER_DATE = "Date";
	private static final String HEADER_CREATEDBY = "Created By";
	private static final String HEADER_NOTE = "Note";
	private static final String HEADER_EDIT_MENU = " ";
	
	private static final int HEADER_NAME_IDX = 0;
	private static final int HEADER_DOWNLOAD_IDX = 1;
	private static final int HEADER_VERSION_IDX = 2;
	private static final int HEADER_DESC_IDX = 3;
	private static final int HEADER_DATE_IDX = 4;
	private static final int HEADER_CREATEDBY_IDX = 5;
	private static final int HEADER_NOTE_IDX = 6;
	private static final int HEADER_EDIT_MENU_IDX = 7;

	
	private IconsImageBundle iconsImageBundle;		
	SynapseJSNIUtils synapseJSNIUtils;
	PortalGinInjector ginInjector;
	
	BootstrapTable table;
	int uniqueId;
		
	public int getRowIndexForEvent(ClickEvent event) {
		return table.getCellForEvent(event).getRowIndex();
	}
			
	public EntityListRenderer(IconsImageBundle iconsImageBundle,
			SynapseJSNIUtils synapseJSNIUtils, PortalGinInjector ginInjector, boolean canEdit) {
		this.iconsImageBundle = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.ginInjector = ginInjector;
		
		setWidget(initTable(canEdit));
				
		uniqueId = Random.nextInt();
	}

	public void setRow(final int rowIndex,
			final EntityGroupRecordDisplay display, boolean isLoggedIn) {
		// convert EntityGroupRecordDisplay to a row entry
		
		// create name link		
		Widget name;
		if(display.getNameLinkUrl() != null && !"".equals(display.getNameLinkUrl())) {
			name = new Hyperlink(display.getName(), display.getNameLinkUrl());			
			name.setStyleName("link");
		} else {
			name = new HTML(display.getName());
		}
		name.addStyleName(ClientProperties.STYLE_BREAK_WORD);
		
		// create download link
		Widget downloadLink;
		if(display.getDownloadUrl() != null && !"".equals(display.getDownloadUrl())) {
			Anchor link = new Anchor();
			link.setHref(display.getDownloadUrl());
			link.setHTML(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(iconsImageBundle.NavigateDown16())));
			link.setStyleName("link");
			if(isLoggedIn) {
				// logged in users want to stay on the entity list page when downloading
				link.setTarget("_new");
			} 
			downloadLink = link;
		} else {
			downloadLink = new HTML("");
		}
		
		// wrap description
		HTML description = new HTML(display.getDescription());
		description.addStyleName(ClientProperties.STYLE_BREAK_WORD);		
		
		// set row in table
		setRow(rowIndex, name, downloadLink,
				display.getVersion(), description,
				display.getModifienOn(), display.getCreatedByPrincipalId(),
				display.getNote());
	}

	
	private void setRow(int rowIndex, Widget nameLink, Widget downloadLink,
			SafeHtml version, HTML description, Date date,
			String createdByPrincipalId, SafeHtml note) {			
		table.setWidget(rowIndex, HEADER_NAME_IDX, nameLink);
		table.setWidget(rowIndex, HEADER_DOWNLOAD_IDX, downloadLink);			
		if(version != null) table.setHTML(rowIndex, HEADER_VERSION_IDX, version);			
		table.setWidget(rowIndex, HEADER_DESC_IDX, description);
		table.setHTML(rowIndex, HEADER_DATE_IDX, date == null ? "" : synapseJSNIUtils.convertDateToSmallString(date) + "</br>&nbsp;");
		//set userbadge widget
		UserBadge createdByBadge = ginInjector.getUserBadgeWidget();
		createdByBadge.configure(createdByPrincipalId);
		table.setWidget(rowIndex, HEADER_CREATEDBY_IDX, createdByBadge.asWidget());
		updateRowNote(rowIndex, note);
	}
	
	public void updateRowNote(int rowIndex, SafeHtml note) {
		HTML noteDiv = new HTML(note);
		noteDiv.addStyleName(ClientProperties.STYLE_BREAK_WORD);			
		table.setWidget(rowIndex, HEADER_NOTE_IDX, noteDiv);
	}
	
	public void removeRow(int rowIndex) {
		table.removeRow(rowIndex);
	}
	
	public void setRowEditor(int rowIndex, ClickHandler editRow,
			ClickHandler deleteRow) {
		WidgetMenu menu = new WidgetMenu(iconsImageBundle);
		if(editRow != null) {
			menu.showEdit(editRow);
		}
		if(deleteRow != null) {
			menu.showDelete(deleteRow);
		}
		table.setWidget(rowIndex, HEADER_EDIT_MENU_IDX, menu.asWidget());
	}
	
	/*
	 * Private Methods
	 */
	private Widget initTable(boolean canEdit) {
		table = new BootstrapTable();
		table.addStyleName("table-striped table-bordered table-condensed");
		List<String> headerRow = new ArrayList<String>();
		headerRow.add(HEADER_NAME_IDX, HEADER_NAME);
		headerRow.add(HEADER_DOWNLOAD_IDX, HEADER_DOWNLOAD);
		headerRow.add(HEADER_VERSION_IDX, HEADER_VERSION);
		headerRow.add(HEADER_DESC_IDX, HEADER_DESC);
		headerRow.add(HEADER_DATE_IDX, HEADER_DATE);
		headerRow.add(HEADER_CREATEDBY_IDX, HEADER_CREATEDBY);
		headerRow.add(HEADER_NOTE_IDX, HEADER_NOTE);	
		if(canEdit) {
			headerRow.add(HEADER_EDIT_MENU_IDX, HEADER_EDIT_MENU);
		}
		List<List<String>> tableHeaderRows = new ArrayList<List<String>>();
		tableHeaderRows.add(headerRow);
		table.setHeaders(tableHeaderRows);			

		table.setWidth("100%");		
		if(!canEdit) {
			table.getColumnFormatter().setWidth(0, "23%");
			table.getColumnFormatter().setWidth(1, "7%");
			table.getColumnFormatter().setWidth(2, "7%");
			table.getColumnFormatter().setWidth(3, "23%");
			table.getColumnFormatter().setWidth(4, "10%");
			table.getColumnFormatter().setWidth(5, "10%");
			table.getColumnFormatter().setWidth(6, "20%");
		} else {				
			table.getColumnFormatter().setWidth(0, "20%");
			table.getColumnFormatter().setWidth(1, "7%");
			table.getColumnFormatter().setWidth(2, "7%");
			table.getColumnFormatter().setWidth(3, "20%");
			table.getColumnFormatter().setWidth(4, "10%");
			table.getColumnFormatter().setWidth(5, "10%");
			table.getColumnFormatter().setWidth(6, "20%");				
			table.getColumnFormatter().setWidth(7, "6%"); // edit column
		}
		
		return table;
	}

}
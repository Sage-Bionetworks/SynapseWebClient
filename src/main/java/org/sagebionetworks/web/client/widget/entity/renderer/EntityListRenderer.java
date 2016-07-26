package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.ClientProperties;
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
import com.google.gwt.user.client.Random;
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
	
	private int headerNameIndex;
	private int headerDownloadIndex;
	private int headerVersionIndex;
	private int headerDescriptionIndex;
	private int headerDateIndex;
	private int headerCreatedByIndex;
	private int headerNoteIndex;
	private int headerEditMenuIndex;

	
	private IconsImageBundle iconsImageBundle;		
	SynapseJSNIUtils synapseJSNIUtils;
	PortalGinInjector ginInjector;
	
	BootstrapTable table;
	int uniqueId;
		
	public int getRowIndexForEvent(ClickEvent event) {
		return table.getCellForEvent(event).getRowIndex();
	}
			
	public EntityListRenderer(IconsImageBundle iconsImageBundle,
			SynapseJSNIUtils synapseJSNIUtils, PortalGinInjector ginInjector, boolean canEdit, boolean showDescription) {
		this.iconsImageBundle = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.ginInjector = ginInjector;
		
		setWidget(initTable(canEdit, showDescription));
				
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
			link.setIcon(IconType.DOWNLOAD);
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
		table.setWidget(rowIndex, headerNameIndex, nameLink);
		table.setWidget(rowIndex, headerDownloadIndex, downloadLink);			
		if(version != null) table.setHTML(rowIndex, headerVersionIndex, version);
		if (headerDescriptionIndex > -1) {
			table.setWidget(rowIndex, headerDescriptionIndex, description);	
		}
		table.setHTML(rowIndex, headerDateIndex, date == null ? "" : synapseJSNIUtils.convertDateToSmallString(date) + "</br>&nbsp;");
		//set userbadge widget
		UserBadge createdByBadge = ginInjector.getUserBadgeWidget();
		createdByBadge.configure(createdByPrincipalId);
		table.setWidget(rowIndex, headerCreatedByIndex, createdByBadge.asWidget());
		updateRowNote(rowIndex, note);
	}
	
	public void updateRowNote(int rowIndex, SafeHtml note) {
		HTML noteDiv = new HTML(note);
		noteDiv.addStyleName(ClientProperties.STYLE_BREAK_WORD);			
		table.setWidget(rowIndex, headerNoteIndex, noteDiv);
	}
	
	public void removeRow(int rowIndex) {
		table.removeRow(rowIndex);
	}
	
	public void setRowEditor(int rowIndex, ClickHandler editRow,
			ClickHandler deleteRow) {
		WidgetMenu menu = new WidgetMenu();
		if(editRow != null) {
			menu.showEdit(editRow);
		}
		if(deleteRow != null) {
			menu.showDelete(deleteRow);
		}
		if (headerEditMenuIndex > -1) {
			table.setWidget(rowIndex, headerEditMenuIndex, menu.asWidget());	
		}
	}
	
	/*
	 * Private Methods
	 */
	private Widget initTable(boolean canEdit, boolean showDescription) {
		table = new BootstrapTable();
		table.addStyleName("table-striped table-bordered table-condensed");
		int colIndex = 0;
		List<String> headerRow = new ArrayList<String>();
		headerNameIndex = colIndex++;
		headerRow.add(headerNameIndex, HEADER_NAME);
		headerDownloadIndex = colIndex++;
		headerRow.add(headerDownloadIndex, HEADER_DOWNLOAD);
		headerVersionIndex = colIndex++;
		headerRow.add(headerVersionIndex, HEADER_VERSION);
		headerDescriptionIndex = -1;
		if (showDescription) {
			headerDescriptionIndex = colIndex++;
			headerRow.add(headerDescriptionIndex, HEADER_DESC);	
		}
		headerDateIndex = colIndex++;
		headerRow.add(headerDateIndex, HEADER_DATE);
		
		headerCreatedByIndex = colIndex++;
		headerRow.add(headerCreatedByIndex, HEADER_CREATEDBY);
		
		headerNoteIndex = colIndex++;
		headerRow.add(headerNoteIndex, HEADER_NOTE);
		headerEditMenuIndex = -1;
		if(canEdit) {
			headerEditMenuIndex = colIndex++;
			headerRow.add(headerEditMenuIndex, HEADER_EDIT_MENU);
		}
		List<List<String>> tableHeaderRows = new ArrayList<List<String>>();
		tableHeaderRows.add(headerRow);
		table.setHeaders(tableHeaderRows);			

		table.setWidth("100%");
		// leave name column width unset (to take the extra, which ranges from 20%-46%)
		table.getColumnFormatter().setWidth(headerDownloadIndex, "28px");
		table.getColumnFormatter().setWidth(headerVersionIndex, "7%");
		if (showDescription) {
			table.getColumnFormatter().setWidth(headerDescriptionIndex, "20%");
		}
		table.getColumnFormatter().setWidth(headerDateIndex, "10%");
		table.getColumnFormatter().setWidth(headerCreatedByIndex, "10%");
		table.getColumnFormatter().setWidth(headerNoteIndex, "20%");
		if (canEdit) {
			table.getColumnFormatter().setWidth(headerEditMenuIndex, "6%");
		}
		return table;
	}

}
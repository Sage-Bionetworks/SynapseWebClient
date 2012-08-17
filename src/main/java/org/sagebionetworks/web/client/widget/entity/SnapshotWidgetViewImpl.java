package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Snapshot;
import org.sagebionetworks.repo.model.SnapshotGroup;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.utils.BootstrapTable;
import org.sagebionetworks.web.client.widget.WidgetMenu;
import org.sagebionetworks.web.client.widget.entity.EntitySearchBox.EntitySelectedHandler;
import org.sagebionetworks.web.client.widget.entity.dialog.DeleteConfirmDialog;
import org.sagebionetworks.web.client.widget.entity.dialog.NameAndDescriptionEditorDialog;
import org.sagebionetworks.web.client.widget.entity.dialog.NameAndDescriptionEditorDialog.Callback;

import com.extjs.gxt.ui.client.Style.Direction;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A widget that renders the Snapshot
 * 
 * @author dburdick
 *
 */
public class SnapshotWidgetViewImpl extends LayoutContainer implements SnapshotWidgetView, IsWidget {

	private List<SnapshotGroupDisplay> groupDisplays; 
	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private boolean canEdit = false;
	private boolean readOnly = false;
	private boolean showEdit = false;
	private EntitySearchBox entitySearchBox;
	private AddEntityToGroupWidget addEntityToGroupWidget; 
	private LayoutContainer addEditor;
	private FlexTable groupsTable;
	
	@Inject
	public SnapshotWidgetViewImpl(IconsImageBundle iconsImageBundle, EntitySearchBox entitySearchBox) {
		this.iconsImageBundle = iconsImageBundle;
		this.entitySearchBox = entitySearchBox;
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setSnapshot(Snapshot entity, boolean canEdit, boolean readOnly, boolean showEdit) {
		this.canEdit = canEdit;
		this.readOnly = readOnly;
		this.showEdit = showEdit;
		this.removeAll();
		
		// create & display groups		
		List<SnapshotGroup> groups = entity.getGroups();
		groupDisplays = createListOfSize(groups.size());
		for(int groupIndex=0; groupIndex<groups.size(); groupIndex++) {						
			groupDisplays.set(groupIndex, createSnapshotGroupDisplay(groups.get(groupIndex), showEdit));			
		}
		
		// add editor to self for rendering
		if(canEdit && !readOnly) {
			// show edit button
			this.add(getHideShowListEditorButton(!showEdit), new MarginData(0,0,10,0));
			if(showEdit) {
				this.add(createEditor());
			} 			
		} 
		
		
		groupsTable = new FlexTable();
		groupsTable.setWidth("100%");		
		groupsTable.getColumnFormatter().setWidth(0, "100%");
		for(int i=0; i<groupDisplays.size(); i++) {			
			groupsTable.setWidget(i, 0, groupDisplays.get(i));
		}
		LayoutContainer groupsTableContainer = new LayoutContainer();
		groupsTableContainer.setStyleName("span-24 notopmargin last");
		groupsTableContainer.add(groupsTable);
		this.add(groupsTableContainer);
		
		this.layout(true);

		presenter.loadRowDetails();
	}

	private Button getHideShowListEditorButton(final boolean show) {
		String title = show ? DisplayConstants.SHOW_LIST_EDITOR : DisplayConstants.HIDE_LIST_EDITOR; 
		return new Button(title, AbstractImagePrototype.create(iconsImageBundle.cog16()),
				new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						presenter.setShowEditor(show);
					}
				});
	}

	private SnapshotGroupDisplay createSnapshotGroupDisplay(SnapshotGroup group, boolean canEdit) {
		// create display widget for the group
		SnapshotGroupDisplay groupDisplay = new SnapshotGroupDisplay();
		groupDisplay.initialize(SafeHtmlUtils.fromString(group.getName() == null ? "" : group.getName()),
				SafeHtmlUtils.fromString(group.getDescription() == null ? "" : group.getDescription()),
				iconsImageBundle, 
				createEditMenu(groupDisplay, iconsImageBundle),
				(canEdit && !readOnly));
		return groupDisplay;
	}

	private int getGroupIndexForEvent(ClickEvent event) {
		return groupsTable.getCellForEvent(event).getRowIndex();
	}

	private void addGroup(SnapshotGroup group) {
		int newGroupIndex = groupDisplays.size();
		SnapshotGroupDisplay groupDisplay = createSnapshotGroupDisplay(group, canEdit);		
		groupDisplays.add(groupDisplay);
		groupsTable.setWidget(newGroupIndex, 0, groupDisplay);
	}
	
	private void removeGroup(int groupIndex) {
		groupDisplays.remove(groupIndex);
		groupsTable.removeRow(groupIndex);
	}
	
	private WidgetMenu createEditMenu(final SnapshotGroupDisplay groupDisplay, IconsImageBundle iconsImageBundle) {
		WidgetMenu widgetMenu = new WidgetMenu(iconsImageBundle);
		widgetMenu.showEdit(new ClickHandler() {				
			@Override
			public void onClick(ClickEvent event) {
				// determine which group this is
				final int groupIdx = getGroupIndexForEvent(event);				
				NameAndDescriptionEditorDialog.showNameAndDescriptionDialog(groupDisplay.getName().asString(), groupDisplay.getDescription().asString(), null, null, new Callback() {						
					@Override
					public void onSave(String nameStr, String descriptionStr) {
						if(nameStr == null || "".equals(nameStr)) {
							showErrorMessage(DisplayConstants.ERROR_NAME_MUST_BE_DEFINED);
							return;
						}
						groupDisplay.updateName(SafeHtmlUtils.fromString(nameStr));
						SafeHtml descriptionSafe = descriptionStr == null ? SafeHtmlUtils.EMPTY_SAFE_HTML : new SafeHtmlBuilder().appendEscapedLines(descriptionStr).toSafeHtml();
						groupDisplay.updateDescription(descriptionSafe);
						presenter.updateGroup(groupIdx, nameStr, descriptionStr);
					}
				});
			}
		});			
		widgetMenu.showDelete(new ClickHandler() {				
			@Override
			public void onClick(final ClickEvent event) {
				final int groupIdx = getGroupIndexForEvent(event);
				DeleteConfirmDialog.showDialog(new DeleteConfirmDialog.Callback() {					
					@Override
					public void onAccept() {
						removeGroup(groupIdx);
						presenter.removeGroup(groupIdx);						
					}
				});
			}
		});
		return widgetMenu;
	}

	
	private List<SnapshotGroupDisplay> createListOfSize(int size) {
		List<SnapshotGroupDisplay> list = new ArrayList<SnapshotGroupDisplay>();
		for(int i=0; i<size; i++) {
			list.add(null);
		}
		return list;
	}

	@Override
	public void setSnapshotGroupRecordDisplay(final int groupIndex, final int rowIndex,
			final SnapshotGroupRecordDisplay display) {
		final SnapshotGroupDisplay groupDisplay = groupDisplays.get(groupIndex);

		// convert SnapshotGroupRecordDisplay to a row entry
		
		// create name link		
		Widget name;
		if(display.getNameLinkUrl() != null && !"".equals(display.getNameLinkUrl())) {
			name = new Hyperlink(display.getName(), display.getNameLinkUrl());
			name.setStyleName("link");
		} else {
			name = new HTML(display.getName());
		}
		
		// create download link
		Widget downloadLink;
		if(display.getDownloadUrl() != null && !"".equals(display.getDownloadUrl())) {
			downloadLink = new Anchor(DisplayConstants.BUTTON_DOWNLOAD, display.getDownloadUrl());
			downloadLink.setStyleName("link");
			((Anchor)downloadLink).setTarget("_new");
		} else {
			downloadLink = new HTML("");
		}
		
		// set row in table
		groupDisplay.setRow(rowIndex, name, downloadLink,
				display.getVersion(), display.getDescription(),
				display.getModifienOn(), display.getContact(),
				display.getNote());
		if(canEdit && showEdit && !readOnly) {
			ClickHandler editRow = new ClickHandler() {				
				@Override
				public void onClick(ClickEvent event) {
					final int row = groupDisplay.getRowIndexForEvent(event);
					final int group = getGroupIndexForEvent(event);
					NameAndDescriptionEditorDialog.showTextAreaDialog(display.getNote().asString(), DisplayConstants.NOTE, new Callback() {						
						@Override
						public void onSave(String name, String description) {
							if(description == null) description = "";
							display.setNote(new SafeHtmlBuilder().appendEscapedLines(description).toSafeHtml());
							groupDisplay.updateRowNote(row, display.getNote());
							presenter.updateGroupRecord(group, row, description);
						}
					});
				}
			};
			ClickHandler deleteRow = new ClickHandler() {				
				@Override
				public void onClick(final ClickEvent event) {
					final int row = groupDisplay.getRowIndexForEvent(event);
					final int group = getGroupIndexForEvent(event);
					DeleteConfirmDialog.showDialog(new DeleteConfirmDialog.Callback() {					
						@Override
						public void onAccept() {									
							groupDisplay.removeRow(row);
							presenter.removeGroupRecord(group, row);
						}
					});
				}
			};			
			groupDisplay.setRowEditor(rowIndex, editRow, deleteRow);
		}
	}
	
	@Override
	public void showLoading() {
	}

	@Override
	public void clear() {
		this.removeAll(true);
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	
	/*
	 * Private Methods
	 */
	
	private LayoutContainer createEditor() {		
		if(addEditor == null){
			addEditor = new LayoutContainer();
		} else {
			addEditor.removeAll();
		}
		if(canEdit && !readOnly) {
			addEditor.setStyleName("span-24 slider-area-inner");
			LayoutContainer left = new LayoutContainer();
			left.setStyleName("span-4 notopmargin");			
			final LayoutContainer right = new LayoutContainer();
			right.setStyleName("span-19 notopmargin last");
			
			SafeHtml editTitle = SafeHtmlUtils.fromSafeConstant("<h3 class=\"colored\">" + DisplayConstants.LABEL_ADD_TO_SNAPSHOT + "</h3>");
			left.add(new HTML(editTitle));
			
			ClickHandler closeButtonHandler = new ClickHandler() {				
				@Override
				public void onClick(ClickEvent event) {
					if(isVisible()) {
						addEntityToGroupWidget.el().slideOut(Direction.UP, FxConfig.NONE);
						entitySearchBox.clearSelection();
					}
				}
			};
			addEntityToGroupWidget = new AddEntityToGroupWidget(closeButtonHandler); 

			// Entity Search Box
			right.add(entitySearchBox.asWidget(725));
			EntitySelectedHandler entitySelectedHandler = new EntitySelectedHandler() {				
				@Override
				public void onSelected(String entityId, String name, List<EntityHeader> versions) {
					addEntityToGroupWidget.clear();
					final EntityHeader eh = new EntityHeader();
					eh.setId(entityId);
					eh.setName(name);
					ClickHandler addEntityClickHandler = new ClickHandler() {				
						@Override
						public void onClick(ClickEvent event) {
							SnapshotGroupDisplay group = addEntityToGroupWidget.getGroup();
							String version = addEntityToGroupWidget.getVersion();
							int groupIndex = -1;
							for(int i=0; i<groupDisplays.size(); i++) {
								if(group.equals(groupDisplays.get(i))) {
									groupIndex = i;
								}						
							}
							
							presenter.addGroupRecord(groupIndex, eh.getId(), version, addEntityToGroupWidget.getNote());
							addEntityToGroupWidget.el().slideOut(Direction.UP, FxConfig.NONE);
							entitySearchBox.clearSelection();
						}
					};
					addEntityToGroupWidget.configureForm(groupDisplays, eh, versions, addEntityClickHandler);									
					addEntityToGroupWidget.setVisible(true);
					addEntityToGroupWidget.el().slideIn(Direction.DOWN, FxConfig.NONE);
				}
			};
			entitySearchBox.setEntitySelectedHandler(entitySelectedHandler, true);

			// add entity Box
			addEntityToGroupWidget.setVisible(false);
			right.add(addEntityToGroupWidget, new MarginData(0, 0, 10, 0));			

			// add group button
			Button addGroupBtn = new Button(DisplayConstants.LABEL_ADD_GROUP, AbstractImagePrototype.create(iconsImageBundle.addSquareGrey16()));
			addGroupBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					NameAndDescriptionEditorDialog.showNameAndDescriptionDialog(new Callback() {						
						@Override
						public void onSave(String name, String description) {
							if(name == null || "".equals(name)) {
								showErrorMessage(DisplayConstants.ERROR_NAME_MUST_BE_DEFINED);
								return;
							}

							SnapshotGroup group = presenter.addGroup(name, description);
							addGroup(group);
						}						
					});
				}
			});		
			right.add(addGroupBtn, new MarginData(10, 0, 0, 0));
			
			addEditor.add(left);
			addEditor.add(right);		
			addEditor.layout(true);
		}
		return addEditor;
	}

	/* =======================================================================================================================================
	 * AddEntityEditor
	 */
	private class AddEntityEditor extends LayoutContainer {
		
//		public updateGroupsList(List)
	}
	
	
	/* =======================================================================================================================================
	 * SnapshotGroupDisplay
	 */
	
	private class SnapshotGroupDisplay extends LayoutContainer {		
		private static final String HEADER_NAME = "Name";
		private static final String HEADER_DOWNLOAD = "Download";
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
		SafeHtml name;
		SafeHtml description;
		BootstrapTable table;
		LayoutContainer nameContainer;
		LayoutContainer descriptionContainer;
		int uniqueId;
			
		/**
		 * Overrided for SimpleComboBox view	
		 */
		@Override
		public String toString() {
			return name.asString();
		}

		public int getRowIndexForEvent(ClickEvent event) {
			return table.getCellForEvent(event).getRowIndex();
		}
				
		public SnapshotGroupDisplay() {
			
		}
		
		public void initialize(SafeHtml name, SafeHtml description, IconsImageBundle iconsImageBundle, WidgetMenu editMenu, boolean canEdit) {
			this.iconsImageBundle = iconsImageBundle;
			this.name = name;
			this.description = description;
			this.nameContainer = new LayoutContainer();
			this.descriptionContainer = new LayoutContainer();			
			
			LayoutContainer tableContainer = initTable(canEdit);						
			LayoutContainer topBar = initTopBar(iconsImageBundle, editMenu, canEdit);
			
			this.add(topBar);
			this.add(tableContainer);
			
			updateName(name);
			updateDescription(description);		
			
			uniqueId = Random.nextInt();
		}

		private LayoutContainer initTopBar(IconsImageBundle iconsImageBundle, WidgetMenu editMenu, boolean canEdit) {
			LayoutContainer topBar = new LayoutContainer();
			topBar.setStyleName("span-24 last");
			LayoutContainer left = new LayoutContainer();
			left.setStyleName("span-22 notopmargin");
			LayoutContainer right = new LayoutContainer();
			right.setStyleName("span-2 notopmargin last");
			topBar.add(left);
			topBar.add(right);
			
			// Name
			left.add(nameContainer);

			// Modifier controls
			if(editMenu != null && canEdit) {				
				right.add(editMenu.asWidget());
			}

			// Description			
			topBar.add(descriptionContainer);
			return topBar;
		}

		private LayoutContainer initTable(boolean canEdit) {

			table = new BootstrapTable();			
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

			LayoutContainer tbl = new LayoutContainer();
			tbl.setStyleName("span-24 last notopmargin");
			tbl.add(table);
			return tbl;
		}

		public void setRow(int rowIndex, Widget nameLink, Widget downloadLink,
				SafeHtml version, SafeHtml description, Date date,
				SafeHtml createdBy, SafeHtml note) {			
			table.setWidget(rowIndex, HEADER_NAME_IDX, nameLink);
			table.setWidget(rowIndex, HEADER_DOWNLOAD_IDX, downloadLink);			
			if(version != null) table.setHTML(rowIndex, HEADER_VERSION_IDX, version);
			table.setHTML(rowIndex, HEADER_DESC_IDX, description);
			table.setHTML(rowIndex, HEADER_DATE_IDX, date == null ? "" : String.valueOf(date));
			table.setHTML(rowIndex, HEADER_CREATEDBY_IDX, createdBy);
			updateRowNote(rowIndex, note);
		}
		
		public void updateRowNote(int rowIndex, SafeHtml note) {
			table.setHTML(rowIndex, HEADER_NOTE_IDX, note);
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
		
		public SafeHtml getName() {
			return name;
		}

		public SafeHtml getDescription() {
			return description;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((description == null) ? 0 : description.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + uniqueId;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SnapshotGroupDisplay other = (SnapshotGroupDisplay) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (description == null) {
				if (other.description != null)
					return false;
			} else if (!description.equals(other.description))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (uniqueId != other.uniqueId)
				return false;
			return true;
		}

		/*
		 * Private Methods
		 */
		private void updateName(SafeHtml name) {
			this.name = name;
			nameContainer.removeAll();
			nameContainer.add(new HTML(new SafeHtmlBuilder()
					.appendHtmlConstant("<h3>").append(name)
					.appendHtmlConstant("</h3>").toSafeHtml()));
			nameContainer.layout();
		}		

		private void updateDescription(SafeHtml description) {
			this.description = description;
			
			descriptionContainer.removeAll();
			if(description != null && !description.equals("")) {
				descriptionContainer.add(new HTML(new SafeHtmlBuilder()
						.appendHtmlConstant("<p>").append(description)
						.appendHtmlConstant("</p>").toSafeHtml()));
				descriptionContainer.setStyleName("span-24 notopmargin");
			} else {
				descriptionContainer.setStyleName("");
			}
			
			descriptionContainer.layout();
		}

		private SnapshotWidgetViewImpl getOuterType() {
			return SnapshotWidgetViewImpl.this;
		}

	}
	
	/*
	 * ==============================================================================================================================
	 * AddEntityToGroupWidget
	 */
	
	private class AddEntityToGroupWidget extends LayoutContainer {
		private final String CONTAINER_STYLE = "span-19 notopmargin last";
		
		FormPanel form;
		SimpleComboBox<SnapshotGroupDisplay> groupBox;
		LabelField entityIdField;
		LabelField entityNameField;
		SimpleComboBox<String> versionBox;
		TextArea notesField;
		Button addEntityBtn;
		Anchor hideBtn;

		private AddEntityToGroupWidget(ClickHandler closeButtonHandler) {
			this.addStyleName(CONTAINER_STYLE);
			hideBtn = new Anchor();
			hideBtn.addStyleName("right");
			hideBtn.setHTML(AbstractImagePrototype.create(iconsImageBundle.delete16()).getHTML());
			hideBtn.addClickHandler(closeButtonHandler);
			this.add(hideBtn, new MarginData(5));
			
			form = new FormPanel();
			form.setStyleName(CONTAINER_STYLE);
			form.setFrame(false);
			form.setHeaderVisible(false); 
			form.setLabelAlign(LabelAlign.LEFT);
			form.setWidth(750);
					
			
			// Setup columns
			LayoutContainer main = new LayoutContainer();
			main.setLayout(new ColumnLayout());			
			LayoutContainer left = new LayoutContainer();
			FormLayout layout = new FormLayout();
			layout.setLabelAlign(LabelAlign.LEFT);
			left.setLayout(layout);			
			LayoutContainer right = new LayoutContainer();
			layout = new FormLayout();
			layout.setLabelAlign(LabelAlign.LEFT);
			right.setLayout(layout);			
			main.add(left, new ColumnData(.5));
		    main.add(right, new ColumnData(.5));			
			form.add(main, new FormData("100%"));

			// fill columns with form fields		     
			entityIdField = new LabelField();
			entityIdField.setFieldLabel(DisplayConstants.SYNAPSE_ID);				
			right.add(entityIdField);
			
			entityNameField = new LabelField();
			entityNameField.setFieldLabel(DisplayConstants.LABEL_NAME);
			right.add(entityNameField);		
			
			// versions dropdown
			versionBox = new SimpleComboBox<String>();			
			versionBox.setFieldLabel("Version");
			versionBox.setTypeAhead(false);
			versionBox.setEditable(false);
			versionBox.setForceSelection(true);
			versionBox.setTriggerAction(TriggerAction.ALL);
			right.add(versionBox);
			
			notesField = new TextArea();
			notesField.setFieldLabel(DisplayConstants.NOTE);
			right.add(notesField);
			
			// version dropdown
			// TODO : add version dd
			
			groupBox = new SimpleComboBox<SnapshotGroupDisplay>();
			groupBox.setFieldLabel("Group");
			groupBox.setTypeAhead(false);
			groupBox.setEditable(false);
			groupBox.setForceSelection(true);
			groupBox.setTriggerAction(TriggerAction.ALL);
			left.add(groupBox);		
			
			addEntityBtn = new Button(DisplayConstants.BUTTON_ADD_ENTITY_TO_GROUP, AbstractImagePrototype.create(iconsImageBundle.addSquareGrey16()));
			//addEntityBtn.setHeight(25);
			addEntityBtn.addStyleName("right");
			right.add(addEntityBtn, new MarginData(10, 0, 0, 0));
			
			this.add(form);	
			this.layout(true);			
		}
		
		private void configureForm(List<SnapshotGroupDisplay> groups, EntityHeader entityHeader, List<EntityHeader> versions, final ClickHandler addButtonHandler) {
			this.clear();
			
			// fill columns with form fields		     
			if(entityHeader != null) {
				entityIdField.setValue(entityHeader.getId());				
				entityNameField.setValue(entityHeader == null ? "" : entityHeader.getName());
			}
			
			if(versions != null) {				
				List<String> versionStrs = new ArrayList<String>();
				for(EntityHeader header : versions) {
					versionStrs.add(header.getVersionNumber().toString());
				}
				versionBox.add(versionStrs);
				versionBox.setSimpleValue(versionBox.getStore().getAt(0).getValue()); // select first
			}			
			
			if(groups != null && groups.size() >= 1) {
				groupBox.add(groupDisplays); // load it up
				groupBox.setSimpleValue(groupBox.getStore().getAt(0).getValue()); // select first
			}
			
			addEntityBtn.removeAllListeners();
			addEntityBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {				
				@Override
				public void componentSelected(ButtonEvent ce) {
					addButtonHandler.onClick(null);
				}
			});
			
			form.layout(true);			
		}
		
		public String getNote() {
			if(notesField != null) {
				return notesField.getValue();
			}
			return null;
		}
		
		public String getVersion() {
			if(versionBox != null && versionBox.getValue() != null)
				return versionBox.getValue().getValue();			
			return null;
		}
		
		public SnapshotGroupDisplay getGroup() {
			if(groupBox != null && groupBox.getValue() != null) {
				return groupBox.getValue().getValue();
			}
			return null;
		}
		
		public void clear() {
			groupBox.removeAll();
			entityIdField.clear();
			entityNameField.clear();
			versionBox.removeAll();
			notesField.clear();
			addEntityBtn.removeAllListeners();
		}
	}

}

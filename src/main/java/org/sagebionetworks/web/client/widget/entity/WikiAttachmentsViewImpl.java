package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiAttachmentsViewImpl extends LayoutContainer implements WikiAttachmentsView {
	
	private static final int ATTACHMENT_COLUMN_WIDTH_PX = 380;
	private static final String LINK_KEY = "link";
	private static final String ATTACHMENT_FILE_HANDLE_ID_KEY = "fileHandleIdKey";
	private static final String TOOLTIP_TEXT_KEY = "tooltip";
	private static final String ATTACHMENT_NAME_KEY = "fileNameKey";

	Grid<BaseModelData> grid;
	ListStore<BaseModelData> gridStore;
	ColumnModel columnModel;
	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private int attachmentColumnWidth;
	private SynapseJSNIUtils synapseJsniUtils;
	
	@Inject
	public WikiAttachmentsViewImpl(IconsImageBundle iconsImageBundle, SynapseJSNIUtils synapseJsniUtils) {
		this.iconsImageBundle = iconsImageBundle;
		this.synapseJsniUtils = synapseJsniUtils;
		gridStore = new ListStore<BaseModelData>();
		setAttachmentColumnWidth(ATTACHMENT_COLUMN_WIDTH_PX);
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		this.setBorders(true);

	    List<ColumnConfig> configs = new ArrayList<ColumnConfig>();  
	    GridCellRenderer<BaseModelData> valueRenderer = createValueRenderer();
	    
	    ColumnConfig column = new ColumnConfig();  
	    column.setId(LINK_KEY);  
	    column.setHeader("Attachment");  	    	   
	    column.setRowHeader(false);
	    column.setWidth(attachmentColumnWidth);
		column.setRenderer(valueRenderer);
	    configs.add(column);  
	  	  	     	 
	    columnModel = new ColumnModel(configs);  
	  
	    grid = new Grid<BaseModelData>(gridStore, columnModel);  
	    grid.setStyleAttribute("borderTop", "none");  
	    grid.setAutoExpandColumn(LINK_KEY);  
		grid.setAutoExpandMin(100);
		// This is important, the grid must resize to fit its height.
		grid.setAutoHeight(true);
		grid.setAutoWidth(false);
		grid.setBorders(true);
		grid.setStripeRows(false);
		grid.setColumnLines(false);
		grid.setColumnReordering(false);
		grid.setHideHeaders(true);
		grid.setTrackMouseOver(false);
		grid.setShadow(false);		
		
		this.add(grid);
	}
	
	@Override
	public void setAttachmentColumnWidth(int width) {
		attachmentColumnWidth = width;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, List<FileHandle> list) {		
		gridStore.removeAll();
		if(list == null || list.size() == 0){
			addNoAttachmentRow();
		} else {
			populateStore(wikiKey, list);			
		}

		
		if(isRendered())
			grid.reconfigure(gridStore, columnModel);
		this.layout(true);		
	}

	private void addNoAttachmentRow() {
		BaseModelData model = new BaseModelData();
		model.set(LINK_KEY, DisplayConstants.TEXT_NO_ATTACHMENTS);
		model.set(TOOLTIP_TEXT_KEY, null);
		gridStore.add(model);		
	}
	
	private void populateStore(WikiPageKey wikiKey,
			List<FileHandle> attachments) {		
		 
		for(FileHandle data: attachments){
			SafeHtml dataName = SafeHtmlUtils.fromString(data.getFileName());
			SafeHtmlBuilder builder = new SafeHtmlBuilder();
			SafeHtml iconNumber = SafeHtmlUtils.fromSafeConstant(DisplayUtils.getAttachmentIcon(data.getFileName()));
			builder.appendHtmlConstant("<div class=\"icon-white-small icon"+iconNumber.asString()+"-white\">");
			builder.appendHtmlConstant("<div style=\"margin-left:20px\">");
			builder.appendEscaped(data.getFileName());
			builder.appendHtmlConstant("</div>");
			builder.appendHtmlConstant("</div>");
			Html listItem = new Html(builder.toSafeHtml().asString());
			SafeHtml previewToolip = SafeHtmlUtils.fromSafeConstant("<div class=\"preview-image-loading\" >"
			    		+ ClientProperties.IMAGE_CENTERING_TABLE_START
			    		+ "<img style=\"background: white; margin:auto; display:block;\" src=\"" 
			    		+ DisplayUtils.createWikiAttachmentUrl(synapseJsniUtils.getBaseFileHandleUrl(), wikiKey, data.getFileName(),true) + "\"/>"
			    		+ ClientProperties.IMAGE_CENTERING_TABLE_END
			    		+ "</div>");  
		    
		    BaseModelData model = new BaseModelData();
			model.set(LINK_KEY, listItem.getHtml());
			model.set(ATTACHMENT_FILE_HANDLE_ID_KEY, data.getId());
			model.set(ATTACHMENT_NAME_KEY, SafeHtmlUtils.fromString(data.getFileName()).asString());
			model.set(TOOLTIP_TEXT_KEY, previewToolip.asString());
			
			gridStore.add(model);
		}
	}

	public GridCellRenderer<BaseModelData> createValueRenderer() {
		GridCellRenderer<BaseModelData> valueRenderer = new GridCellRenderer<BaseModelData>() {

			@Override
			public Object render(BaseModelData model, String property,
					ColumnData config, final int rowIndex, int colIndex,
					ListStore<BaseModelData> store, Grid<BaseModelData> grid) {
				String value = model.get(property);
				if (value == null) {
					value = "";
				}
				//from the value, create an anchor (with click handler)
				SimplePanel div = new SimplePanel();
				div.addStyleName("attachments-widget-row");
				
				Anchor attachmentLink = new Anchor(value, true);
				attachmentLink.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						//by default this pops up the attachment image.
						showAttachmentAt(rowIndex);
					}
				});
				div.add(attachmentLink);
										
				// TODO : determine if value should be SafeHtml escaped
				String tooltip = (String)model.get(TOOLTIP_TEXT_KEY);
				
				if (model.get(TOOLTIP_TEXT_KEY) == null) {
					return div;
				}

				HorizontalPanel panel = new HorizontalPanel();
				LayoutContainer wrap = new LayoutContainer();
				wrap.add(div);
				wrap.setWidth(attachmentColumnWidth-50);
				if(tooltip != null)
					wrap.setToolTip(tooltip);
				
				panel.add(wrap);

				AbstractImagePrototype img = AbstractImagePrototype.create(iconsImageBundle.deleteButtonGrey16());
				Anchor button = DisplayUtils.createIconLink(img, new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						deleteAttachmentAt(rowIndex);
					}
				});

				TableData td = new TableData();
				td.setHorizontalAlign(HorizontalAlignment.RIGHT);
				td.setVerticalAlign(VerticalAlignment.MIDDLE);
				
				panel.add(button, td);
				panel.setAutoWidth(true);
				return panel;
			}

		};
		return valueRenderer;
	}

	@Override
	public Widget asWidget() {
		if(isRendered()) {
			grid.reconfigure(gridStore, columnModel);
			this.layout(true);
		}
		return this;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void showLoading() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
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
	public void deleteAttachmentAt(int rowIndex) {
		final BaseModelData model = grid.getStore().getAt(rowIndex);
		if (model != null) {
			DisplayUtils.showConfirmDialog(
					DisplayConstants.LABEL_DELETE + " "
							+ model.get(ATTACHMENT_NAME_KEY),
					DisplayConstants.PROMPT_SURE_DELETE + " "
							+ model.get(ATTACHMENT_NAME_KEY) + "?",
					new Callback() {
						@Override
						public void invoke() {
							presenter.deleteAttachment((String) model
									.get(ATTACHMENT_NAME_KEY));							
						}
					});
		}
	}
	public void showAttachmentAt(int rowIndex) {
		final BaseModelData model = grid.getStore().getAt(rowIndex);
		if (model != null) {
			String name = (String) model.get(ATTACHMENT_NAME_KEY);
			presenter.attachmentClicked(name);
//			String tokenId = (String) model.get(ATTACHMENT_FILE_HANDLE_ID_KEY);
		}
	}

	@Override
	public void attachmentDeleted(String fileHandleId) {
		String deletedName="";
		if(isRendered() && fileHandleId != null && grid != null && gridStore != null) {
			//BaseModelData
			Integer foundIdx = null;			
			for(int i=0; i<gridStore.getCount(); i++) {
				BaseModelData model = gridStore.getAt(i);
				if(fileHandleId.equals(model.get(ATTACHMENT_FILE_HANDLE_ID_KEY))){
					foundIdx = i;
					deletedName = model.get(ATTACHMENT_NAME_KEY);
					break;
				}
			}
			if(foundIdx != null) {
				gridStore.remove(foundIdx);
				if(gridStore.getCount() == 0) {
					addNoAttachmentRow();
				}
				grid.reconfigure(gridStore, columnModel);
			}
		}
		showInfo(deletedName + " " + DisplayConstants.LABEL_DELETED + ".", "");
	}
	
}

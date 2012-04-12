package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AttachmentsViewImpl extends LayoutContainer implements AttachmentsView {
	
	private static final String LINK_KEY = "link";
	private static final String ATTACHMENT_DATA_TOKEN_KEY = "attachmentDataKey";
	private static final String TOOLTIP_TEXT_KEY = "tooltip";
	private static final String ATTACHMENT_DATA_NAME_KEY = "attachmentDataName";
	Grid<BaseModelData> grid;
	ListStore<BaseModelData> gridStore;
	ColumnModel columnModel;
	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	
	@Inject
	public AttachmentsViewImpl(IconsImageBundle iconsImageBundle) {
		this.iconsImageBundle = iconsImageBundle;
		
		gridStore = new ListStore<BaseModelData>();
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
	    column.setWidth(259);
		column.setRenderer(valueRenderer);
	    configs.add(column);  
	  	  	     	 
	    columnModel = new ColumnModel(configs);  
	  
	    grid = new Grid<BaseModelData>(gridStore, columnModel);  
	    grid.setStyleAttribute("borderTop", "none");  
	    grid.setAutoExpandColumn(LINK_KEY);  
		grid.setAutoExpandMin(100);
		grid.setAutoExpandMax(300);
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
		
		configureContextMenu();
		this.add(grid);
	}
	
	@Override
	public void configure(String baseUrl, String entityId,
			List<AttachmentData> attachments) {		
		gridStore.removeAll();
		if(attachments == null){
			addNoAttachmentRow();
		} else {
			populateStore(baseUrl, entityId, attachments);			
		}
		configureContextMenu();
		
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

	
	private void populateStore(String baseUrl, String entityId,
			List<AttachmentData> attachments) {		
		 
		for(AttachmentData data: attachments){
			StringBuilder builder = new StringBuilder();
			builder.append("<a href=\"");
			builder.append(DisplayUtils.createAttachmentUrl(baseUrl, entityId, data.getTokenId(), data.getTokenId()));
			builder.append("\" target=\"_blank\" name=\"");
			builder.append(data.getName());
			builder.append("\"");
			builder.append(">");
			String iconNumber = DisplayUtils.getAttachmentIcon(data.getName());
			builder.append("<div class=\"icon-white-small icon"+iconNumber+"-white\">");
			builder.append("<div style=\"margin-left:20px\">");
			builder.append(DisplayUtils.replaceWhiteSpace(data.getName()));
			builder.append("</div>");
			builder.append("</div>");
			builder.append("</a>");
			Html listItem = new Html(builder.toString());
		    // If we have a preview then show it as a tooltip.
			String tooltip;
		    if(data.getPreviewId() != null){
		    	StringBuilder imageBuilder = new StringBuilder();
		    	imageBuilder.append("<div class=\"preview-image-loading\" >");
		    	imageBuilder.append(DisplayUtils.IMAGE_CENTERING_TABLE_START);
		    	imageBuilder.append("<img style=\"margin:auto; display:block;\" src=\"");
		    	imageBuilder.append(DisplayUtils.createAttachmentUrl(baseUrl, entityId, data.getPreviewId(), null));
		    	imageBuilder.append("\"");
		    	imageBuilder.append("/>");
		    	imageBuilder.append(DisplayUtils.IMAGE_CENTERING_TABLE_END);
		    	imageBuilder.append("</div>");
		    	tooltip = imageBuilder.toString();  
		    }else{
			    tooltip = data.getName();			    
		    }		    
		    //listItem.setToolTip(config); 
			
			BaseModelData model = new BaseModelData();
			model.set(LINK_KEY, listItem.getHtml());
			model.set(ATTACHMENT_DATA_TOKEN_KEY, data.getTokenId());
			model.set(ATTACHMENT_DATA_NAME_KEY, data.getName());
			model.set(TOOLTIP_TEXT_KEY, tooltip);
			gridStore.add(model);
		}
	}

	public GridCellRenderer<BaseModelData> createValueRenderer() {
		GridCellRenderer<BaseModelData> valueRenderer = new GridCellRenderer<BaseModelData>() {

			@Override
			public Object render(BaseModelData model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BaseModelData> store, Grid<BaseModelData> grid) {
				String value = model.get(property);
				if (value == null) {
					value = "";
				}
				StringBuilder builder = new StringBuilder();
				builder.append("<div style='font-weight: normal;color:black; overflow:hidden; text-overflow:ellipsis; width:auto;'>");
				builder.append(value);
				builder.append("</div>");
				Html html = new Html(builder.toString());
//				html.setWidth(50);
				String tooltip = (String)model.get(TOOLTIP_TEXT_KEY);
				if(tooltip != null) {
				    ToolTipConfig tipsConfig = new ToolTipConfig();  			     
				    tipsConfig.setText(tooltip);
				    tipsConfig.setMouseOffset(new int[] {0, 0});  
				    tipsConfig.setAnchor("left");  
				    tipsConfig.setDismissDelay(0);
				    tipsConfig.setShowDelay(100);
				    ToolTip tip = new ToolTip(html, tipsConfig);
				}
				return html;
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
	private void configureContextMenu() {
		if(grid == null) return;
		
		if(gridStore.getCount() > 0) {
			Menu contextMenu = new Menu();
	
			MenuItem remove = new MenuItem();
			remove.setText(DisplayConstants.LABEL_DELETE);
			remove.setIcon(AbstractImagePrototype.create(iconsImageBundle.deleteButton16()));
			remove.addSelectionListener(new SelectionListener<MenuEvent>() {
				public void componentSelected(MenuEvent ce) {
					final BaseModelData model = grid.getSelectionModel().getSelectedItem();
					if (model != null) {
						MessageBox.confirm(DisplayConstants.LABEL_DELETE +" " + model.get(ATTACHMENT_DATA_NAME_KEY), DisplayConstants.PROMPT_SURE_DELETE + " " + model.get(ATTACHMENT_DATA_NAME_KEY) +"?", new Listener<MessageBoxEvent>() {					
							@Override
							public void handleEvent(MessageBoxEvent be) { 												
								Button btn = be.getButtonClicked();
								if(Dialog.YES.equals(btn.getItemId())) {
									presenter.deleteAttachment((String)model.get(ATTACHMENT_DATA_TOKEN_KEY));								
								}
							}
						});					
					}
				}
			});
			contextMenu.add(remove);
	
			grid.setContextMenu(contextMenu);
		} else {
			grid.setContextMenu(null);
		}
	}

	@Override
	public void attachmentDeleted(String tokenId, String deletedName) {
		if(isRendered() && tokenId != null && grid != null && gridStore != null) {
			//BaseModelData
			Integer foundIdx = null;			
			for(int i=0; i<gridStore.getCount(); i++) {
				BaseModelData model = gridStore.getAt(i);
				if(tokenId.equals(model.get(ATTACHMENT_DATA_TOKEN_KEY)))
					foundIdx = i;
			}
			if(foundIdx != null) {
				gridStore.remove(foundIdx);
				if(gridStore.getCount() == 0) {
					configureContextMenu();
					addNoAttachmentRow();
				}
				grid.reconfigure(gridStore, columnModel);
			}
		}
		showInfo(deletedName + " " + DisplayConstants.LABEL_DELETED + ".", "");
	}
	
}

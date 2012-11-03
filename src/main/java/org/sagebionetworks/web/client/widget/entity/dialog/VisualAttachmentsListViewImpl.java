package org.sagebionetworks.web.client.widget.entity.dialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class VisualAttachmentsListViewImpl extends LayoutContainer implements VisualAttachmentsListView {
	
	private static final String ATTACHMENT_DATA_TOKEN_KEY = "attachmentDataKey";
	private static final String TOOLTIP_TEXT_KEY = "tooltip";
	private static final String ATTACHMENT_DATA_NAME_KEY = "attachmentDataName";
	Grid<BaseModelData> grid;
	ListStore<BaseModelData> gridStore;
	ColumnModel columnModel;
	
	@Inject
	public VisualAttachmentsListViewImpl() {
		gridStore = new ListStore<BaseModelData>();
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		this.setBorders(true);

	    List<ColumnConfig> configs = new ArrayList<ColumnConfig>();  
	    GridCellRenderer<BaseModelData> valueRenderer = createValueRenderer();
	    
	    ColumnConfig column = new ColumnConfig();  
	    column.setId(ATTACHMENT_DATA_NAME_KEY);  
	    column.setHeader("Attachment");  	    	   
	    column.setRowHeader(false);
	    column.setWidth(384);
		column.setRenderer(valueRenderer);
	    configs.add(column);  
	  	  	     	 
	    columnModel = new ColumnModel(configs);  
	  
	    grid = new Grid<BaseModelData>(gridStore, columnModel);  
	    grid.setStyleAttribute("borderTop", "none");  
	    grid.setAutoExpandColumn(ATTACHMENT_DATA_NAME_KEY);  
		grid.setAutoHeight(true);
		grid.setAutoWidth(true);
		grid.setBorders(true);
		grid.setStripeRows(true);
		grid.setColumnLines(false);
		grid.setColumnReordering(false);
		grid.setHideHeaders(true);
		grid.setTrackMouseOver(true);
		grid.setShadow(false);
		grid.setSelectionModel(new GridSelectionModel<BaseModelData>());
		this.add(grid);
	}
	@Override
	public String getSelectedAttachmentTokenId() {
		BaseModelData baseModelData = grid.getSelectionModel().getSelectedItem();
		if (baseModelData == null)
			return null;
		else
			return baseModelData.get(ATTACHMENT_DATA_TOKEN_KEY);
	}
	
	@Override
	public void configure(String baseUrl, String entityId,
			List<AttachmentData> attachments) {		
		gridStore.removeAll();
		List<AttachmentData> visualAttachments = getVisualAttachments(attachments);
		if(visualAttachments == null || visualAttachments.size() == 0){
			addNoAttachmentRow();
		} else {
			populateStore(baseUrl, entityId, visualAttachments);			
		}
		
		if(isRendered())
			grid.reconfigure(gridStore, columnModel);
		this.layout(true);		
	}
	public static List<AttachmentData> getVisualAttachments(List<AttachmentData> attachments){
		List<AttachmentData> visualAttachments = new ArrayList<AttachmentData>();
		for (Iterator iterator = attachments.iterator(); iterator
				.hasNext();) {
			AttachmentData data = (AttachmentData) iterator.next();
			// Ignore all attachments without a preview.
			if(data.getPreviewId() != null) 
				visualAttachments.add(data);
		}
		return visualAttachments;
	}
	
	private void addNoAttachmentRow() {
		BaseModelData model = new BaseModelData();
		model.set(ATTACHMENT_DATA_NAME_KEY, DisplayConstants.TEXT_NO_ATTACHMENTS);
		model.set(TOOLTIP_TEXT_KEY, null);
		gridStore.add(model);		
	}
	
	private void populateStore(String baseUrl, String entityId,
			List<AttachmentData> attachments) {		
		 
		for(AttachmentData data: attachments){
			SafeHtml dataName = SafeHtmlUtils.fromString(data.getName());
		    // If we have a preview then show it as a tooltip.
			SafeHtml previewToolip;
		    if(data.getPreviewId() != null){
		    	previewToolip = SafeHtmlUtils.fromSafeConstant("<div class=\"preview-image-loading\" >"
			    		+ DisplayUtils.IMAGE_CENTERING_TABLE_START
			    		+ "<img style=\"margin:auto; display:block;\" src=\"" 
			    		+ DisplayUtils.createAttachmentUrl(baseUrl, entityId, data.getPreviewId(), null) + "\"/>"
			    		+ DisplayUtils.IMAGE_CENTERING_TABLE_END
			    		+ "</div>");  
		    }else{
			    previewToolip = dataName;			    
		    }		    

		    BaseModelData model = new BaseModelData();
			//model.set(LINK_KEY, listItem.getHtml());
			model.set(ATTACHMENT_DATA_TOKEN_KEY, data.getTokenId());
			model.set(ATTACHMENT_DATA_NAME_KEY, dataName.asString());
			model.set(TOOLTIP_TEXT_KEY, previewToolip.asString());
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
				String tooltip = (String)model.get(TOOLTIP_TEXT_KEY);
				if(tooltip != null) {
				    html.setToolTip(tooltip);
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
}

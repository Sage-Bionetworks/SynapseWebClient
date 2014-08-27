package org.sagebionetworks.web.client.widget.table;

import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;

import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;


public class FileHandleCell extends AbstractCell<TableCellFileHandle> {

	boolean canEdit = false;
	SynapseJSNIUtils synapseJSNIUtils;
	PortalGinInjector ginInjector;
	Uploader uploader;
	
    /**
     * The HTML templates used to render the cell.
     */
    interface Templates extends SafeHtmlTemplates {
    	@SafeHtmlTemplates.Template("<a href=\"{0}\" target=\"_blank\"><img class=\"margin-top-5\" src=\"{1}\"  style=\"max-height:150px; \"/></a><br/><a class=\"btn btn-default btn-sm margin-top-5 margin-right-5\" href=\"{0}\" target=\"_blank\">{2}</a>")
        SafeHtml previewImageLink(SafeUri fullUri, SafeUri previewUri, SafeHtml download); 
    }

    interface Template extends SafeHtmlTemplates { 
    } 
    
    
    /**
     * Create a singleton instance of the templates used to render the cell.
     */
    private static Templates templates = GWT.create(Templates.class);

	public FileHandleCell(boolean canEdit, SynapseJSNIUtils synapseJSNIUtils, PortalGinInjector ginInjector) {
      /*
       * Sink the click and keydown events. We handle click events in this
       * class. AbstractCell will handle the keydown event and call
       * onEnterKeyDown() if the user presses the enter key while the cell is
       * selected.
       */
      super("click", "keydown");
      this.canEdit = canEdit;
      this.synapseJSNIUtils = synapseJSNIUtils;
      this.ginInjector = ginInjector;
    }

    /**
     * Called when an event occurs in a rendered instance of this Cell. The
     * parent element refers to the element that contains the rendered cell, NOT
     * to the outermost element that the Cell rendered.
     */
    @Override
    public void onBrowserEvent(Context context, Element parent, TableCellFileHandle value, NativeEvent event,
        ValueUpdater<TableCellFileHandle> valueUpdater) {
      // Let AbstractCell handle the keydown event.
      super.onBrowserEvent(context, parent, value, event, valueUpdater);

      // Handle the click event.
      if ("click".equals(event.getType())) {
        // Ignore clicks that occur outside of the outermost element.
        EventTarget eventTarget = event.getEventTarget();
        if(Element.as(eventTarget).hasAttribute("uploadLink"))
        	showUploadForm(value, valueUpdater);
      }
    }
    
    /**
     * NOTE: this method can not make async calls. Later changes to sb are not rendered
     */
    @Override
    public void render(final Context context, final TableCellFileHandle value, final SafeHtmlBuilder sb) {
    	if(value == null) return;
    	SafeUri previewUri = UriUtils.fromString(DisplayUtils.createTableCellFileEntityUrl(synapseJSNIUtils.getBaseFileHandleUrl(), value, true, true));
    	SafeUri fullUri = UriUtils.fromString(DisplayUtils.createTableCellFileEntityUrl(synapseJSNIUtils.getBaseFileHandleUrl(), value, false, true));
    	SafeHtml filename = SafeHtmlUtils.fromSafeConstant(DisplayConstants.DOWNLOAD);
    	final SafeHtml preview; 
    	if(value.getFileHandleId() != null) {
    		SafeHtml download = value.getFileHandleId() == null ? SafeHtmlUtils.EMPTY_SAFE_HTML : SafeHtmlUtils.fromSafeConstant(filename.asString());
    		preview = templates.previewImageLink(fullUri, previewUri, download);	    				
    	} else {
    		preview = SafeHtmlUtils.EMPTY_SAFE_HTML;
    	}
    	String uploadLabel = value.getFileHandleId() == null ? DisplayConstants.UPLOAD_FILE : DisplayConstants.UPDATE_FILE;
    	final String uploadLink = "<a class=\"btn btn-default btn-sm margin-top-5\" uploadLink=\"true\">"+ uploadLabel +"</a>";
    	
    	sb.append(preview);
    	if(canEdit) sb.appendHtmlConstant(uploadLink);
    }

    /*
     * Private Methods
     */
    private void showUploadForm(final TableCellFileHandle value, final ValueUpdater<TableCellFileHandle> valueUpdater) {
		if (uploader == null)
			uploader = ginInjector.getUploaderWidget();
		uploader.clearHandlers();
		final com.extjs.gxt.ui.client.widget.Window window = new com.extjs.gxt.ui.client.widget.Window();
		
		// add handlers for closing the window
		uploader.addPersistSuccessHandler(new EntityUpdatedHandler() {
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				window.hide();
			}
		});
		uploader.addCancelHandler(new CancelHandler() {
			@Override
			public void onCancel(CancelEvent event) {
				window.hide();
			}
		});					
		window.removeAll();
		window.setPlain(true);
		window.setModal(true);		
		window.setHeading(DisplayConstants.UPLOAD_FILE);
		window.setLayout(new FitLayout());
		
		Widget widget = uploader.asWidget(null, null,
				new CallbackP<String>() {
					@Override
					public void invoke(String fileHandleId) {
						// update cell with new filehandleid
						if (valueUpdater != null)
							valueUpdater.update(new TableCellFileHandle(value.getTableId(), value.getColumnId(), value.getRowId(), value.getVersionNumber(), fileHandleId));
					}
				}, false);
		
		window.add(widget, new MarginData(5));
		window.show();
		window.setSize(uploader.getDisplayWidth(), uploader.getDisplayHeight());
    }

  }

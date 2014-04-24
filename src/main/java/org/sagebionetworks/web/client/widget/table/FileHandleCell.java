package org.sagebionetworks.web.client.widget.table;

import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;

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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;


public class FileHandleCell extends AbstractCell<TableCellFileHandle> {

	boolean canEdit = false;
	SynapseJSNIUtils synapseJSNIUtils;
	
    /**
     * The HTML templates used to render the cell.
     */
    interface Templates extends SafeHtmlTemplates {
    	@SafeHtmlTemplates.Template("<a href=\"{0}\" target=\"_blank\"><img class=\"margin-top-5\" src=\"{1}\"  height=\"100%\" width=\"100%\" /><br/>{2}</a>") 
        SafeHtml previewImageLink(SafeUri fullUri, SafeUri previewUri, SafeHtml download); 

    	@SafeHtmlTemplates.Template("<a href=\"{0}\" target=\"_blank\">{1}</a>") 
        SafeHtml fileLink(SafeUri fullUri, SafeHtml download);     
    }

    interface Template extends SafeHtmlTemplates { 
    } 
    
    
    /**
     * Create a singleton instance of the templates used to render the cell.
     */
    private static Templates templates = GWT.create(Templates.class);

    public FileHandleCell(boolean canEdit, SynapseJSNIUtils synapseJSNIUtils) {
      /*
       * Sink the click and keydown events. We handle click events in this
       * class. AbstractCell will handle the keydown event and call
       * onEnterKeyDown() if the user presses the enter key while the cell is
       * selected.
       */
      super("click", "keydown");
      this.canEdit = canEdit;
      this.synapseJSNIUtils = synapseJSNIUtils;
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
        Element image = parent.getFirstChildElement();
        Element uploadLink = image.getNextSiblingElement();
        if (image.equals(Element.as(eventTarget))) {
        	// do nothing at the moment
        } else if(uploadLink.isOrHasChild(Element.as(eventTarget))) {
        	showUploadForm(value, valueUpdater);
        }
      }
    }

    @Override
    public void render(final Context context, final TableCellFileHandle value, final SafeHtmlBuilder sb) {
    	if(value == null) return;
    	boolean hasPreview = false; // TODO : how to determine this?
    	
    	// TODO : also how to determine 
    	SafeUri previewUri = UriUtils.fromString(DisplayUtils.createTableCellFileEntityUrl(synapseJSNIUtils.getBaseFileHandleUrl(), value, true, true));
    	SafeUri fullUri = UriUtils.fromString(DisplayUtils.createTableCellFileEntityUrl(synapseJSNIUtils.getBaseFileHandleUrl(), value, true, true));
    	SafeHtml filename = SafeHtmlUtils.fromSafeConstant(DisplayConstants.DOWNLOAD_FILE_LOCAL);
    	SafeHtml preview; 
    	if(fullUri != null) {
    		SafeHtml download = value.getFileHandleId() == null ? SafeHtmlUtils.EMPTY_SAFE_HTML : SafeHtmlUtils.fromSafeConstant("<span class=\"margin-top-5\" style=\"color:#000; font-size:16px;\"> "+ filename.asString() +"</span>");
    		if(hasPreview) {    			
	    		preview = templates.previewImageLink(fullUri, previewUri, download);	    				
	    	} else {
	    		preview = templates.fileLink(fullUri, download);
	    	}
    	} else {
    		preview = SafeHtmlUtils.EMPTY_SAFE_HTML;
    	}
    	
		sb.append(preview);
		String uploadLabel = value.getFileHandleId() == null ? "Upload File" : "Update File";
		if(canEdit) sb.appendHtmlConstant("<div class=\"margin-top-5\"><a class=\"btn btn-default btn-sm\" id='"+ context.getKey() +"'>"+ uploadLabel +"</a></div>");      		
    }
    

    private void showUploadForm(TableCellFileHandle value, ValueUpdater<TableCellFileHandle> valueUpdater) {
    	// TODO : show file uploader
      Window.alert("show Upload form: " + value);

      if(valueUpdater != null) valueUpdater.update(value);
    }
  }

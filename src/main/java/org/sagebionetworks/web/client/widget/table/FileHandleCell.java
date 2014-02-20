package org.sagebionetworks.web.client.widget.table;

import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.web.client.DisplayUtils;

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


public class FileHandleCell extends AbstractCell<String> {

	
	
	boolean canEdit = false;
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

    public FileHandleCell(boolean canEdit) {
      /*
       * Sink the click and keydown events. We handle click events in this
       * class. AbstractCell will handle the keydown event and call
       * onEnterKeyDown() if the user presses the enter key while the cell is
       * selected.
       */
      super("click", "keydown");
      this.canEdit = canEdit;
    }

    /**
     * Called when an event occurs in a rendered instance of this Cell. The
     * parent element refers to the element that contains the rendered cell, NOT
     * to the outermost element that the Cell rendered.
     */
    @Override
    public void onBrowserEvent(Context context, Element parent, String value, NativeEvent event,
        ValueUpdater<String> valueUpdater) {
      // Let AbstractCell handle the keydown event.
      super.onBrowserEvent(context, parent, value, event, valueUpdater);

      // Handle the click event.
      if ("click".equals(event.getType())) {
        // Ignore clicks that occur outside of the outermost element.
        EventTarget eventTarget = event.getEventTarget();
        Element image = parent.getFirstChildElement();
        Element uploadLink = image.getNextSiblingElement();
        if (image.equals(Element.as(eventTarget))) {
        	//doAction(value, valueUpdater);
        	//Window.alert("image clicked for "+ context.getKey());
        } else if(uploadLink.isOrHasChild(Element.as(eventTarget))) {
        	Window.alert("link clicked for " + context.getKey() + ", col:" + context.getColumn());
        }
      }
    }

    @Override
    public void render(final Context context, final String value, final SafeHtmlBuilder sb) {
    	if(value == null) return;
    	 
    	// TODO: replace fake getFileHandle method with call to synapse client
    	getFileHandle(value, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				// TODO : actually properly create file handles
				S3FileHandle fileHandle = new S3FileHandle();
				//fileHandle.setFileName("logo11w.png");
				PreviewFileHandle previewFileHandle = new PreviewFileHandle();
				previewFileHandle.setContentType("image/png"); 
				renderFileHandle(previewFileHandle, fileHandle, context, sb);
			}
			@Override
			public void onFailure(Throwable caught) {
				renderFileHandle(null, null, context, sb);
			}
		});
    	
    }

	private void renderFileHandle(PreviewFileHandle previewFileHandle,
			S3FileHandle fileHandle, Context context, SafeHtmlBuilder sb) {
    	boolean hasPreview = (previewFileHandle != null 
    			&& previewFileHandle.getContentType() != null 
    			&& DisplayUtils.isRecognizedImageContentType(previewFileHandle.getContentType())) ? true : false;
    	 
    	// TODO : get real URLs from file handles
//    	String previewUrl = DisplayUtils.createPreviewFileHandleUrl(previewFileHandle);
//    	String fullUrl = DisplayUtils.createPreviewFileHandleUrl(fileHandle);
    	SafeUri previewUri = UriUtils.fromString("https://www.google.com/images/srpr/logo11w.png");
    	SafeUri fullUri = UriUtils.fromString("https://www.google.com/images/srpr/logo11w.png");
    	SafeHtml filename = fileHandle.getFileName() != null ? SafeHtmlUtils.fromString(fileHandle.getFileName()) : SafeHtmlUtils.EMPTY_SAFE_HTML;
    	
    	SafeHtml preview; 
    	if(fullUri != null) {
    		SafeHtml download = SafeHtmlUtils.fromSafeConstant("<span class=\"margin-top-5\" style=\"color:#000; font-size:16px;\"> "+ filename.asString() +"</span>");
    		if(hasPreview) {    			
	    		preview = templates.previewImageLink(fullUri, previewUri, download);	    				
	    	} else {
	    		preview = templates.fileLink(fullUri, download);
	    	}
    	} else {
    		preview = SafeHtmlUtils.EMPTY_SAFE_HTML;
    	}
    	
		sb.append(preview);
		// TODO : open up file uplaoder and replace correct cell value
		if(canEdit) sb.appendHtmlConstant("<div class=\"margin-top-5\"><a id='"+ context.getKey() +"'>Upload File</a></div>");      
		
		
	}

    
    // TODO : remove, this is a placeholder 
    private void getFileHandle(String value, AsyncCallback<String> asyncCallback) {
		asyncCallback.onSuccess("");
	}

	/**
     * onEnterKeyDown is called when the user presses the ENTER key will the
     * Cell is selected. You are not required to override this method, but its a
     * common convention that allows your cell to respond to key events.
     */
    @Override
    protected void onEnterKeyDown(Context context, Element parent, String value, NativeEvent event,
        ValueUpdater<String> valueUpdater) {
      doAction(value, valueUpdater);
    }

    private void doAction(String value, ValueUpdater<String> valueUpdater) {
      // Alert the user that they selected a value.
      Window.alert("clicked filehandle URL: " + value);

      // Trigger a value updater. In this case, the value doesn't actually
      // change, but we use a ValueUpdater to let the app know that a value
      // was clicked.
      if(valueUpdater != null) valueUpdater.update(value);
    }
  }

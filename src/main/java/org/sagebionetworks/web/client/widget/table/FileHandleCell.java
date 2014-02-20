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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;


public class FileHandleCell extends AbstractCell<String> {

	boolean canEdit = false;
    /**
     * The HTML templates used to render the cell.
     */
    interface Templates extends SafeHtmlTemplates {
      /**
       * The template for this Cell, which includes styles and a value.
       * 
       * @param styles the styles to include in the style attribute of the div
       * @param value the safe value. Since the value type is {@link SafeHtml},
       *          it will not be escaped before including it in the template.
       *          Alternatively, you could make the value type String, in which
       *          case the value would be escaped.
       * @return a {@link SafeHtml} instance
       */
      @SafeHtmlTemplates.Template("<div><img src=\"{0}\" /></div>")
      SafeHtml cell(String value);
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
        	Window.alert("link clicked for " + context.getKey());
        }
      }
    }

    @Override
    public void render(final Context context, String value, final SafeHtmlBuilder sb) {
    	if(value == null) return;
    	 
    	// TODO: replace fake getFileHandle method with call to synapse client
    	getFileHandle(value, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				// TODO : actually properly create file handles
				S3FileHandle fileHandle = new S3FileHandle();
				fileHandle.setFileName("example1.png");
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
    	 
    	String previewUrl = DisplayUtils.createPreviewFileHandleUrl(previewFileHandle);
    	String fullUrl = DisplayUtils.createPreviewFileHandleUrl(fileHandle);
    	String filename = fileHandle.getFileName();
    	
    	SafeHtml preview; 
    	if(fullUrl != null) {
    		String download = "<span class=\"margin-top-5\" style=\"color:#000; font-size:16px;\"> "+ filename +"</span>";
    		if(hasPreview) {
	    		preview = SafeHtmlUtils.fromSafeConstant("<a href=\""+ fullUrl +"\" target=\"_blank\"><img class=\"margin-top-5\" src=\""+ previewUrl +"\" /><br/>"+ download +"</a>");    		
	    	} else {
	    		preview = SafeHtmlUtils.fromSafeConstant("<a href=\""+ fullUrl +"\" target=\"_blank\">"+ download +"</a>");
	    	}
    	} else {
    		preview = SafeHtmlUtils.EMPTY_SAFE_HTML;
    	}
    	
    	SafeHtmlBuilder shb = new SafeHtmlBuilder();      
		shb.append(preview);
		if(canEdit) shb.appendHtmlConstant("<div class=\"margin-top-5\"><a href='#' id='"+ context.getKey() +"'>Upload File</a></div>");      
		sb.append(shb.toSafeHtml());
		
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

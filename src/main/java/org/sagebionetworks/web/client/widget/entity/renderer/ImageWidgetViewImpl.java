package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageWidgetViewImpl extends LayoutContainer implements ImageWidgetView {

	private Presenter presenter;
	
	@Inject
	public ImageWidgetViewImpl() {
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, String fileName,
			String explicitWidth, String alignment) {
		this.removeAll();
		//add a html panel that contains the image src from the attachments server (to pull asynchronously)
		//create img
		StringBuilder sb = new StringBuilder();
		sb.append("<img class=\"imageDescriptor\" ");
		if (explicitWidth != null && explicitWidth.trim().length() > 0) {
			sb.append(" width=\"");
			sb.append(explicitWidth);
			sb.append("\"");
		}
		if (alignment != null && alignment.trim().length() > 0) {
			sb.append(" align=\"");
			sb.append(alignment);
			sb.append("\" style=\"margin:10px;\"");
		}
		
		sb.append(" src=\"");
		sb.append(createWikiAttachmentUrl(wikiKey, fileName,false));
		sb.append("\"></img>");
		
		add(new HTMLPanel(sb.toString()));
		this.layout(true);
	}
	
	/**
	 * Create the url to a wiki filehandle.
	 * @param baseURl
	 * @param id
	 * @param tokenId
	 * @param fileName
	 * @return
	 */
	public static String createWikiAttachmentUrl(WikiPageKey wikiKey, String fileName, boolean preview){
		//direct approach not working.  have the filehandleservlet redirect us to the temporary wiki attachment url instead
//		String attachmentPathName = preview ? "attachmentpreview" : "attachment";
//		return repoServicesUrl 
//				+"/" +wikiKey.getOwnerObjectType().toLowerCase() 
//				+"/"+ wikiKey.getOwnerObjectId()
//				+"/wiki/" 
//				+wikiKey.getWikiPageId()
//				+"/"+ attachmentPathName+"?fileName="+URL.encodePathSegment(fileName);
		String wikiIdParam = wikiKey.getWikiPageId() == null ? "" : "&" + DisplayUtils.WIKI_ID_PARAM_KEY + "=" + wikiKey.getWikiPageId();
		return GWT.getModuleBaseURL()+"filehandle?" +
				DisplayUtils.WIKI_OWNER_ID_PARAM_KEY + "=" + wikiKey.getOwnerObjectId() + "&" +
				DisplayUtils.WIKI_OWNER_TYPE_PARAM_KEY + "=" + wikiKey.getOwnerObjectType() + "&"+
				DisplayUtils.WIKI_FILENAME_PARAM_KEY + "=" + fileName + "&" +
				DisplayUtils.WIKI_PREVIEW_PARAM_KEY + "=" + Boolean.toString(preview) +
				wikiIdParam;
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	
	/*
	 * Private Methods
	 */

}

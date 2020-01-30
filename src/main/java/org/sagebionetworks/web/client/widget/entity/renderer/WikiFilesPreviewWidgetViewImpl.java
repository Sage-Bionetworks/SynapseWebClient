package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Iterator;
import java.util.List;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.FileHandleUtils;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiFilesPreviewWidgetViewImpl extends FlowPanel implements WikiFilesPreviewWidgetView {

	private Presenter presenter;
	private SynapseJSNIUtils synapseJsniUtils;

	@Inject
	public WikiFilesPreviewWidgetViewImpl(SynapseJSNIUtils synapseJsniUtils) {
		this.synapseJsniUtils = synapseJsniUtils;
	}

	@Override
	public void configure(WikiPageKey wikiKey, List<FileHandle> list) {
		this.clear();
		StringBuilder sb = new StringBuilder();
		sb.append("<h5>Wiki Attachments</h5>");
		sb.append("<div>");
		int col = 0;

		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			FileHandle fileHandle = (FileHandle) iterator.next();
			if (!FileHandleUtils.isPreviewFileHandle(fileHandle)) {
				String style = "span-5 left";
				if (col == 3 || col == list.size() - 1) {
					style = style + " last";
					col = 0;
				} else {
					col++;
				}
				sb.append("<div class=\"" + style + "\">");
				sb.append("<div class=\"preview-image-loading view\" >");
				sb.append(ClientProperties.IMAGE_CENTERING_TABLE_START);
				sb.append("<a class=\"item-preview spec-border-ie\" href=\"");
				sb.append(DisplayUtils.createWikiAttachmentUrl(synapseJsniUtils.getBaseFileHandleUrl(), wikiKey, fileHandle.getFileName(), false));
				sb.append("\"><img class=\"center-in-div\" alt ");
				sb.append(" src=\"");
				sb.append(DisplayUtils.createWikiAttachmentUrl(synapseJsniUtils.getBaseFileHandleUrl(), wikiKey, fileHandle.getFileName(), true));
				sb.append("\"></img></a>");
				sb.append(ClientProperties.IMAGE_CENTERING_TABLE_END);
				sb.append("</div>");
				sb.append("</div>");
			}
		}
		sb.append("</div>");
		add(new HTMLPanel(sb.toString()));
	}

	@Override
	public void showErrorMessage(String error) {

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

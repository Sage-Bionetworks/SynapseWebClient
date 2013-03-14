package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PreviewWidgetViewImpl extends SimplePanel implements PreviewWidgetView, IsWidget{
	private Presenter presenter;
	private SynapseJSNIUtils synapseJSNIUtils;
	private IconsImageBundle iconsImageBundle;
	
	@Inject
	public PreviewWidgetViewImpl(SynapseJSNIUtils synapseJSNIUtils, IconsImageBundle iconsImageBundle) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.iconsImageBundle = iconsImageBundle;
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}
	
	@Override
	public void setImagePreview(String fullFileUrl, String previewUrl) {
		clear();
		StringBuilder sb = new StringBuilder();
		sb.append("<a href=\"");
		sb.append(fullFileUrl);
		sb.append("\"><img class=\"imageDescriptor\" ");
		sb.append(" src=\"");
		sb.append(previewUrl);
		sb.append("\"></img></a>");
		add(new HTMLPanel(sb.toString()));
	}
	
	@Override
	public void setCodePreview(String code) {
		clear();
		setStylePrimaryName("markdown");
		StringBuilder sb = new StringBuilder();
		sb.append("<pre style=\"overflow:auto;white-space:pre\"><code>");
		sb.append(code);
		sb.append("</code></pre>");
		add(new HTMLPanel(sb.toString()));
		synapseJSNIUtils.highlightCodeBlocks();
	}
	
	@Override
	public void setBlockQuotePreview(String text) {
		clear();
		setStylePrimaryName("markdown");
		StringBuilder sb = new StringBuilder();
		sb.append("<blockquote>");
		sb.append(text);
		sb.append("</blockquote>");
		add(new HTMLPanel(sb.toString()));
	}
	
	@Override
	public void setTablePreview(String csv) {
		clear();
		setStylePrimaryName("markdown");
		StringBuilder sb = new StringBuilder();
		sb.append("<table style=\"overflow:auto;display:block;max-height:200px\">");
		String[] lines = csv.split("\n");
		for (int i = 0; i < lines.length; i++) {
			sb.append("<tr>");
			String[] cells = lines[i].split(",");
			for (int j = 0; j < cells.length; j++) {
				sb.append("<td style=\"border-top:0px\">");
				sb.append(cells[j]);
				sb.append("</td>");
			}
			sb.append("</tr>");
		}
		sb.append("</table>");
		add(new HTMLPanel(sb.toString()));
	}
	
	@Override
	public void setTreePreview(TreeStore<ModelData> store) {
		FlowPanel wrapperPanel = new FlowPanel();
		wrapperPanel.add(new HTMLPanel("<h5>Contents</h5>"));
		final TreePanel<ModelData> tree = new TreePanel<ModelData>(store);
	    tree.setDisplayProperty("name");
	    tree.setTrackMouseOver(false);
	    final ImageResource folderIcon = DisplayUtils.getSynapseIconForEntityClassName(Folder.class.getName(), DisplayUtils.IconSize.PX16, iconsImageBundle);
	    final ImageResource fileIcon = DisplayUtils.getSynapseIconForEntityClassName(FileEntity.class.getName(), DisplayUtils.IconSize.PX16, iconsImageBundle);
	    tree.setIconProvider(new ModelIconProvider<ModelData>() {
			public AbstractImagePrototype getIcon(ModelData model) {
				ImageResource icon = fileIcon;
				if (model instanceof PreviewWidget.FolderTreeModel) {
					icon = folderIcon;
				}
				return AbstractImagePrototype.create(icon);
			}
		});
	    wrapperPanel.add(tree);
	    add(wrapperPanel);
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
}

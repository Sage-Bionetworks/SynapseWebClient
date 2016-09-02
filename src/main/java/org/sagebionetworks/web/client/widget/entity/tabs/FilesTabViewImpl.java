package org.sagebionetworks.web.client.widget.entity.tabs;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FilesTabViewImpl implements FilesTabView {
	private static final String DISCUSSION_ABOUT = "Discussion about ";
	@UiField
	SimplePanel fileBrowserContainer;
	@UiField
	SimplePanel filesWikiPageContainer;
	@UiField
	Column filePreviewContainer;
	@UiField
	Div filePreviewContainerHighlightBox;
	@UiField
	Column fileProvenanceContainer;
	@UiField
	Div fileProvenanceContainerHighlightBox;
	@UiField
	SimplePanel fileModifiedAndCreatedContainer;
	@UiField
	SimplePanel fileBreadcrumbContainer;
	@UiField
	SimplePanel fileTitlebarContainer;
	@UiField
	SimplePanel folderTitlebarContainer;
	
	@UiField
	SimplePanel fileMetadataContainer;
	@UiField
	SimplePanel fileActionMenuContainer;
	@UiField
	SimplePanel synapseAlertContainer;
	@UiField
	SimplePanel refreshAlertContainer;

	@UiField
	Div discussionThreadsContainer;
	@UiField
	Column discussionContainer;
	@UiField
	Text discussionText;
	
	public interface TabsViewImplUiBinder extends UiBinder<Widget, FilesTabViewImpl> {}
	Widget widget;
	UserBadge createdByBadge, modifiedByBadge;
	@Inject
	public FilesTabViewImpl(
			UserBadge createdByBadge,
			UserBadge modifiedByBadge
			) {
		//empty constructor, you can include this widget in the ui xml
		TabsViewImplUiBinder binder = GWT.create(TabsViewImplUiBinder.class);
		widget = binder.createAndBindUi(this);
		filePreviewContainerHighlightBox.getElement().setAttribute("highlight-box-title", "Preview");
		this.createdByBadge = createdByBadge;
		this.modifiedByBadge = modifiedByBadge;
	}
	
	@Override
	public void setFileTitlebar(Widget w) {
		fileTitlebarContainer.add(w);
	}
	
	@Override
	public void setFolderTitlebar(Widget w) {
		folderTitlebarContainer.add(w);
	}
	
	
	
	@Override
	public void setBreadcrumb(Widget w) {
		fileBreadcrumbContainer.add(w);		
	}

	
	@Override
	public void setFileBrowser(Widget w) {
		fileBrowserContainer.add(w);		
	}

	@Override
	public void setPreview(Widget w) {
		filePreviewContainerHighlightBox.add(w);		
	}

	@Override
	public void setProvenance(Widget w) {
		fileProvenanceContainerHighlightBox.clear();
		fileProvenanceContainerHighlightBox.add(w);		
	}

	@Override
	public void setMetadata(Widget w) {
		fileMetadataContainer.add(w);		
	}
	@Override
	public void setMetadataVisible(boolean visible) {
		fileMetadataContainer.setVisible(visible);
	}
	@Override
	public void setActionMenu(Widget w) {
		fileActionMenuContainer.add(w);		
	}
	@Override
	public void clearActionMenuContainer() {
		fileActionMenuContainer.clear();
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setWikiPage(Widget w) {
		filesWikiPageContainer.add(w);	
	}
	
	@Override
	public void setProvenanceVisible(boolean visible) {
		fileProvenanceContainer.setVisible(visible);
	}
	
	@Override
	public void setWikiPageWidgetVisible(boolean visible) {
		filesWikiPageContainer.setVisible(visible);
	}
	
	@Override
	public void setFileBrowserVisible(boolean visible) {
		fileBrowserContainer.setVisible(visible);
	}
	@Override
	public void setPreviewVisible(boolean visible) {
		filePreviewContainer.setVisible(visible);
	}
	@Override
	public void setSynapseAlert(Widget w) {
		synapseAlertContainer.setWidget(w);
	}
	
	@Override
	public void setFolderTitlebarVisible(boolean visible) {
		folderTitlebarContainer.setVisible(visible);
	}
	@Override
	public void setFileTitlebarVisible(boolean visible) {
		fileTitlebarContainer.setVisible(visible);
	}

	@Override
	public void setModifiedCreatedBy(IsWidget modifiedCreatedBy) {
		fileModifiedAndCreatedContainer.setWidget(modifiedCreatedBy);
	}
	@Override
	public void setRefreshAlert(Widget w) {
		refreshAlertContainer.setWidget(w);
	};

	@Override
	public void setDiscussionThreadListWidget(Widget widget){
		discussionThreadsContainer.clear();
		discussionThreadsContainer.add(widget);
	}

	@Override
	public void setDiscussionThreadListWidgetVisible(Boolean visible) {
		discussionContainer.setVisible(visible);
	}

	@Override
	public void setDiscussionText(String entityName) {
		discussionText.setText(DISCUSSION_ABOUT + entityName);
	}
}

package org.sagebionetworks.web.client.widget.entity.tabs;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.CommandLineClientModalWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.JavaClientModalWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.PythonClientModalWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.RClientModalWidgetViewImpl;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FilesTabViewImpl implements FilesTabView {
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
	HTMLPanel fileProgrammaticClientsContainer;
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
	
	public interface TabsViewImplUiBinder extends UiBinder<Widget, FilesTabViewImpl> {}
	
	Widget widget;
	private RClientModalWidgetViewImpl rLoadWidget;
	private PythonClientModalWidgetViewImpl pythonLoadWidget;
	private JavaClientModalWidgetViewImpl javaLoadWidget;
	private CommandLineClientModalWidgetViewImpl commandLineLoadWidget;
	UserBadge createdByBadge, modifiedByBadge;
	@Inject
	public FilesTabViewImpl(
			RClientModalWidgetViewImpl rLoadWidget,
			PythonClientModalWidgetViewImpl pythonLoadWidget,
			JavaClientModalWidgetViewImpl javaLoadWidget,
			CommandLineClientModalWidgetViewImpl commandLineLoadWidget,
			UserBadge createdByBadge,
			UserBadge modifiedByBadge
			) {
		//empty constructor, you can include this widget in the ui xml
		TabsViewImplUiBinder binder = GWT.create(TabsViewImplUiBinder.class);
		widget = binder.createAndBindUi(this);
		filePreviewContainerHighlightBox.getElement().setAttribute("highlight-box-title", "Preview");
		fileProvenanceContainerHighlightBox.getElement().setAttribute("highlight-box-title", "Provenance");
		this.createdByBadge = createdByBadge;
		this.modifiedByBadge = modifiedByBadge;
		this.rLoadWidget = rLoadWidget;
		this.javaLoadWidget = javaLoadWidget;
		this.commandLineLoadWidget = commandLineLoadWidget;
		this.pythonLoadWidget = pythonLoadWidget;
		
		fileProgrammaticClientsContainer.add(rLoadWidget.asWidget());
		fileProgrammaticClientsContainer.add(pythonLoadWidget.asWidget());
		fileProgrammaticClientsContainer.add(javaLoadWidget.asWidget());
		fileProgrammaticClientsContainer.add(commandLineLoadWidget.asWidget());
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
		fileProvenanceContainerHighlightBox.add(w);		
	}

	@Override
	public void setMetadata(Widget w) {
		fileMetadataContainer.add(w);		
	}

	@Override
	public void setActionMenu(Widget w) {
		fileActionMenuContainer.add(w);		
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
	public void configureProgrammaticClients(String id, Long versionNumber) {
		rLoadWidget.configure(id, versionNumber);
		pythonLoadWidget.configure(id);
		javaLoadWidget.configure(id);
		commandLineLoadWidget.configure(id);
	}
	
	@Override
	public void setProvenanceVisible(boolean visible) {
		fileProvenanceContainer.setVisible(visible);
	}
	@Override
	public void setProgrammaticClientsVisible(boolean visible) {
		fileProgrammaticClientsContainer.setVisible(visible);
	}
	
	@Override
	public void configureModifiedAndCreatedWidget(Entity entity)  {
		fileModifiedAndCreatedContainer.clear();
		createdByBadge.asWidget().removeFromParent();
		modifiedByBadge.asWidget().removeFromParent();
		
		FlowPanel attributionPanel = new FlowPanel();
		createdByBadge.configure(entity.getCreatedBy());
		modifiedByBadge.configure(entity.getModifiedBy());
		
		InlineHTML inlineHtml = new InlineHTML(DisplayConstants.CREATED_BY);
		attributionPanel.add(inlineHtml);
		Widget createdByBadgeWidget = createdByBadge.asWidget();
		createdByBadgeWidget.addStyleName("movedown-4 margin-left-5");
		attributionPanel.add(createdByBadgeWidget);
		
		inlineHtml = new InlineHTML(" on " + DisplayUtils.convertDataToPrettyString(entity.getCreatedOn()) + "<br>" + DisplayConstants.MODIFIED_BY);
		
		attributionPanel.add(inlineHtml);
		Widget modifiedByBadgeWidget = modifiedByBadge.asWidget();
		modifiedByBadgeWidget.addStyleName("movedown-4 margin-left-5");
		attributionPanel.add(modifiedByBadgeWidget);
		inlineHtml = new InlineHTML(" on " + DisplayUtils.convertDataToPrettyString(entity.getModifiedOn()));
		
		attributionPanel.add(inlineHtml);
		fileModifiedAndCreatedContainer.add(attributionPanel);
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
}

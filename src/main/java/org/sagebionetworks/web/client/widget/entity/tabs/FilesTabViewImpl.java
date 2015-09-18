package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.web.client.widget.entity.CommandLineClientModalWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.JavaClientModalWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.PythonClientModalWidgetViewImpl;
import org.sagebionetworks.web.client.widget.entity.RClientModalWidgetViewImpl;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class FilesTabViewImpl implements FilesTabView {
	@UiField
	SimplePanel fileDescriptionContainer;
	@UiField
	SimplePanel fileBrowserContainer;
	@UiField
	SimplePanel filesWikiPageContainer;
	@UiField
	SimplePanel filePreviewContainer;
	@UiField
	SimplePanel fileProvenanceContainer;
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
	
	public interface TabsViewImplUiBinder extends UiBinder<Widget, FilesTabViewImpl> {}
	
	Widget widget;
	private RClientModalWidgetViewImpl rLoadWidget;
	private PythonClientModalWidgetViewImpl pythonLoadWidget;
	private JavaClientModalWidgetViewImpl javaLoadWidget;
	private CommandLineClientModalWidgetViewImpl commandLineLoadWidget;
	
	public FilesTabViewImpl(
			RClientModalWidgetViewImpl rLoadWidget,
			PythonClientModalWidgetViewImpl pythonLoadWidget,
			JavaClientModalWidgetViewImpl javaLoadWidget,
			CommandLineClientModalWidgetViewImpl commandLineLoadWidget
			) {
		//empty constructor, you can include this widget in the ui xml
		TabsViewImplUiBinder binder = GWT.create(TabsViewImplUiBinder.class);
		widget = binder.createAndBindUi(this);
		
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
	public void setFileDescription(Widget w) {
		fileDescriptionContainer.add(w);		
	}

	@Override
	public void setFileBrowser(Widget w) {
		fileBrowserContainer.add(w);		
	}

	@Override
	public void setPreview(Widget w) {
		filePreviewContainer.add(w);		
	}

	@Override
	public void setProvenance(Widget w) {
		fileProvenanceContainer.add(w);		
	}

	@Override
	public void setModifiedAndCreated(Widget w) {
		fileModifiedAndCreatedContainer.add(w);		
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
}

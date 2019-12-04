package org.sagebionetworks.web.client.widget.entity.tabs;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FilesTabViewImpl implements FilesTabView {
	private static final String DISCUSSION_ABOUT = "Discussion about ";
	@UiField
	LoadingSpinner loading;
	@UiField
	SimplePanel fileBrowserContainer;
	@UiField
	SimplePanel filesWikiPageContainer;
	@UiField
	Column filePreviewContainer;
	@UiField
	Div filePreviewWidgetContainer;
	@UiField
	Column fileProvenanceContainer;
	@UiField
	Div fileProvenanceGraphContainer;
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
	@UiField
	Anchor expandProvenanceLink;
	@UiField
	Anchor expandPreviewLink;
	Widget provenanceGraphWidget, previewWidget;
	HandlerRegistration expandPreviewHandlerRegistration, expandProvHandlerRegistration;

	public interface TabsViewImplUiBinder extends UiBinder<Widget, FilesTabViewImpl> {
	}

	Widget widget;
	UserBadge createdByBadge, modifiedByBadge;
	public static final String DEFAULT_WIDGET_HEIGHT = 197 + "px";

	@Inject
	public FilesTabViewImpl(UserBadge createdByBadge, UserBadge modifiedByBadge) {
		// empty constructor, you can include this widget in the ui xml
		TabsViewImplUiBinder binder = GWT.create(TabsViewImplUiBinder.class);
		widget = binder.createAndBindUi(this);
		this.createdByBadge = createdByBadge;
		this.modifiedByBadge = modifiedByBadge;
	}

	private ClickHandler getExpandClickHandler(final Widget w) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final Modal window = new Modal();
				window.addStyleName("modal-fullscreen");
				final ModalBody body = new ModalBody();
				final Div oldParent = (Div) w.getParent();
				w.removeFromParent();
				body.add(new ScrollPanel(w));
				w.setHeight(new Double(com.google.gwt.user.client.Window.getClientHeight()).intValue() - 170 + "px");
				ClickHandler closeHandler = new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						w.removeFromParent();
						oldParent.add(w);
						w.setHeight(DEFAULT_WIDGET_HEIGHT);
						window.hide();
					}
				};
				window.addCloseHandler(closeHandler);
				window.add(body);
				ModalFooter footer = new ModalFooter();
				Button closeButton = new Button(DisplayConstants.CLOSE, closeHandler);
				footer.add(closeButton);
				window.add(footer);
				window.show();
			}
		};
	}

	@Override
	public void setFileTitlebar(Widget w) {
		fileTitlebarContainer.setWidget(w);
	}

	@Override
	public void setFolderTitlebar(Widget w) {
		folderTitlebarContainer.setWidget(w);
	}

	@Override
	public void setBreadcrumb(Widget w) {
		fileBreadcrumbContainer.setWidget(w);
	}


	@Override
	public void setFileBrowser(Widget w) {
		fileBrowserContainer.setWidget(w);
	}

	@Override
	public void setPreview(Widget w) {
		previewWidget = w;
		w.setHeight(DEFAULT_WIDGET_HEIGHT);
		filePreviewWidgetContainer.clear();
		filePreviewWidgetContainer.add(w);
		if (expandPreviewHandlerRegistration != null) {
			expandPreviewHandlerRegistration.removeHandler();
		}
		expandPreviewHandlerRegistration = expandPreviewLink.addClickHandler(getExpandClickHandler(previewWidget));
	}

	@Override
	public void setProvenance(Widget w) {
		provenanceGraphWidget = w;
		w.setHeight(DEFAULT_WIDGET_HEIGHT);
		fileProvenanceGraphContainer.clear();
		fileProvenanceGraphContainer.add(w);
		if (expandProvHandlerRegistration != null) {
			expandProvHandlerRegistration.removeHandler();
		}
		expandProvHandlerRegistration = expandProvenanceLink.addClickHandler(getExpandClickHandler(provenanceGraphWidget));
	}

	@Override
	public void setMetadata(Widget w) {
		fileMetadataContainer.setWidget(w);
	}

	@Override
	public void setFileFolderUIVisible(boolean visible) {
		fileMetadataContainer.setVisible(visible);
	}

	@Override
	public void setActionMenu(Widget w) {
		fileActionMenuContainer.setWidget(w);
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
		filesWikiPageContainer.setWidget(w);
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
	public void clearRefreshAlert() {
		refreshAlertContainer.clear();
	}

	@Override
	public void setDiscussionThreadListWidget(Widget widget) {
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

	@Override
	public void showLoading(boolean value) {
		loading.setVisible(value);
	}

}

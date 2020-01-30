package org.sagebionetworks.web.client.widget.docker;

import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DockerRepoWidgetViewImpl implements DockerRepoWidgetView {
	@UiField
	PanelBody dockerRepoWikiPageContainer;
	@UiField
	Div dockerRepoProvenanceContainer;
	@UiField
	FlowPanel provenancePanel;
	@UiField
	TextBox dockerPullCommand;
	@UiField
	SimplePanel synapseAlertContainer;
	@UiField
	SimplePanel dockerMetadataContainer;
	@UiField
	SimplePanel dockerTitlebarContainer;
	@UiField
	SimplePanel dockerModifiedAndCreatedContainer;
	@UiField
	Div dockerCommitListContainer;

	public interface Binder extends UiBinder<Widget, DockerRepoWidgetViewImpl> {
	}

	Widget widget;

	@Inject
	public DockerRepoWidgetViewImpl(Binder binder, final SynapseJSNIUtils jsniUtils) {
		this.widget = binder.createAndBindUi(this);
		dockerPullCommand.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dockerPullCommand.selectAll();
			}
		});
	}

	@Override
	public void setProvenance(Widget widget) {
		dockerRepoProvenanceContainer.add(widget);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setWikiPage(Widget widget) {
		dockerRepoWikiPageContainer.add(widget);
	}

	@Override
	public void setSynapseAlert(Widget widget) {
		synapseAlertContainer.setWidget(widget);
	}

	@Override
	public void setDockerPullCommand(String command) {
		dockerPullCommand.setText(command);
	}

	@Override
	public void setTitlebar(Widget widget) {
		dockerTitlebarContainer.setWidget(widget);
	}

	@Override
	public void setEntityMetadata(Widget widget) {
		dockerMetadataContainer.setWidget(widget);
	}

	@Override
	public void setModifiedCreatedBy(IsWidget widget) {
		dockerModifiedAndCreatedContainer.setWidget(widget);
	}

	@Override
	public void setDockerCommitListWidget(Widget widget) {
		dockerCommitListContainer.add(widget);
	}

	@Override
	public void setProvenanceWidgetVisible(boolean visible) {
		provenancePanel.setVisible(visible);
	}
}

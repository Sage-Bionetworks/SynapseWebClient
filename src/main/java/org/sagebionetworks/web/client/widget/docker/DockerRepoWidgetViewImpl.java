package org.sagebionetworks.web.client.widget.docker;

import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DockerRepoWidgetViewImpl implements DockerRepoWidgetView{
	@UiField
	Div dockerRepoWikiPageContainer;
	@UiField
	Div dockerRepoProvenanceContainer;
	@UiField
	TextBox dockerPullCommand;
	@UiField
	SimplePanel synapseAlertContainer;

	public interface Binder extends UiBinder<Widget, DockerRepoWidgetViewImpl> {}
	private Presenter presenter;
	Widget widget;

	@Inject
	public DockerRepoWidgetViewImpl(Binder binder){
		this.widget = binder.createAndBindUi(this);
		dockerRepoProvenanceContainer.getElement().setAttribute("highlight-box-title", "Provenance");
		dockerPullCommand.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dockerPullCommand.selectAll();
			}
		});
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
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

}

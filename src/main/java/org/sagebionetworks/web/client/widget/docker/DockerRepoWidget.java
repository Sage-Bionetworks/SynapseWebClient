package org.sagebionetworks.web.client.widget.docker;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.tabs.DockerTab;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DockerRepoWidget implements DockerRepoWidgetView.Presenter{
	private PreflightController preflightController;
	private DockerRepoWidgetView view;
	private SynapseAlert synAlert;
	private WikiPageWidget wikiPageWidget;

	@Inject
	public DockerRepoWidget(
			PreflightController preflightController,
			DockerRepoWidgetView view,
			SynapseAlert synAlert,
			WikiPageWidget wikiPageWidget
			) {
		this.preflightController = preflightController;
		this.view = view;
		this.synAlert = synAlert;
		this.wikiPageWidget = wikiPageWidget;
		view.setPresenter(this);
		view.setSynapseAlert(synAlert.asWidget());
		view.setWikiPage(wikiPageWidget.asWidget());
		// TODO: add provenance
	}

	public Widget asWidget() {
		return view.asWidget();
	}

	public void configure(EntityBundle bundle, DockerTab dockerTab) {
		// TODO Auto-generated method stub
		
	}

}

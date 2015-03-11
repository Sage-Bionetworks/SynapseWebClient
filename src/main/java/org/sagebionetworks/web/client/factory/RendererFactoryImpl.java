package org.sagebionetworks.web.client.factory;

import org.sagebionetworks.web.client.widget.entity.TutorialWizard;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.AttachmentPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ShinySiteWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.TableOfContentsWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiFilesPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.YouTubeWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Factory for creating Renderers
 * 
 * @author John
 *
 */
public class RendererFactoryImpl implements RendererFactory {

	@Inject
	private Provider<YouTubeWidget> youTubeWidgetProvider;
	@Inject
	private Provider<TutorialWizard> tutorialWizardWidgetProvider;
	@Inject
	private Provider<ProvenanceWidget> provenanceWidgetProvider;
	@Inject
	private Provider<ImageWidget> imageWidgetProvider;
	@Inject
	private Provider<AttachmentPreviewWidget> attachmentPreviewWidgetProvider;
	@Inject
	private Provider<APITableWidget> apiTableWidgetProvider;
	@Inject
	private Provider<TableOfContentsWidget> tableOfContentsWidgetProvider;
	@Inject
	private Provider<WikiFilesPreviewWidget> wikiFilesPreviewWidgetProvider;
	@Inject
	private Provider<EntityListWidget> entityListWidgetProvider;
	@Inject
	private Provider<ShinySiteWidget> shinySiteWidgetProvider;
	@Inject
	private Provider<UserBadge> userBadgeProvider;
	
	@Override
	public YouTubeWidget getYouTubeRenderer() {
		return youTubeWidgetProvider.get();
	}

	@Override
	public ProvenanceWidget getProvenanceRenderer() {
		return provenanceWidgetProvider.get();
	}

	@Override
	public ImageWidget getImageRenderer() {
		return imageWidgetProvider.get();
	}

	@Override
	public AttachmentPreviewWidget getAttachmentPreviewRenderer() {
		return attachmentPreviewWidgetProvider.get();
	}

	@Override
	public APITableWidget getSynapseAPICallRenderer() {
		return apiTableWidgetProvider.get();
	}

	@Override
	public TableOfContentsWidget getTableOfContentsRenderer() {
		return tableOfContentsWidgetProvider.get();
	}

	@Override
	public WikiFilesPreviewWidget getWikiFilesPreviewRenderer() {
		return wikiFilesPreviewWidgetProvider.get();
	}

	@Override
	public EntityListWidget getEntityListRenderer() {
		return entityListWidgetProvider.get();
	}

	@Override
	public ShinySiteWidget getShinySiteRenderer() {
		return shinySiteWidgetProvider.get();
	}

	@Override
	public UserBadge getUserBadgeWidget() {
		return userBadgeProvider.get();
	}
	
	@Override
	public TutorialWizard getTutorialWidgetRenderer() {
		return tutorialWizardWidgetProvider.get();
	}

}

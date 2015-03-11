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

/**
 * Factory for creating new Renderers
 * 
 * @author John
 *
 */
public interface RendererFactory {

	////// Renderers
	public YouTubeWidget getYouTubeRenderer();
	public TutorialWizard getTutorialWidgetRenderer();
	public ProvenanceWidget getProvenanceRenderer();
	public ImageWidget getImageRenderer();
	public AttachmentPreviewWidget getAttachmentPreviewRenderer();
	public APITableWidget getSynapseAPICallRenderer();
	public TableOfContentsWidget getTableOfContentsRenderer();
	public WikiFilesPreviewWidget getWikiFilesPreviewRenderer();
	public EntityListWidget getEntityListRenderer();
	public ShinySiteWidget getShinySiteRenderer(); 
	
	// Other widgets
	public UserBadge getUserBadgeWidget();
}

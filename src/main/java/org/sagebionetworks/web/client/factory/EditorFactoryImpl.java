package org.sagebionetworks.web.client.factory;

import org.sagebionetworks.web.client.widget.entity.editor.APITableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.AttachmentConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.EntityListConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.LinkConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ShinySiteConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.TabbedTableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.YouTubeConfigEditor;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * A factory for creating editors.
 * 
 * @author John
 *
 */
public class EditorFactoryImpl implements EditorFactory {
	@Inject
	private Provider<YouTubeConfigEditor> youTubeConfigEditorProvider;
	@Inject
	private Provider<ProvenanceConfigEditor> provenanceConfigEditorProvider;
	@Inject
	private Provider<ImageConfigEditor> imageConfigEditorProvider;
	@Inject
	private Provider<AttachmentConfigEditor> attachmentConfigEditorProvider;
	@Inject
	private Provider<LinkConfigEditor> linkConfigEditorProvider;
	@Inject
	private Provider<APITableConfigEditor> apiTableConfigEditorProvider;
	@Inject
	private Provider<TabbedTableConfigEditor> tabbedTableConfigEditorProvider;
	@Inject
	private Provider<EntityListConfigEditor> entityListConfigEditorProvider;
	@Inject
	private Provider<ShinySiteConfigEditor> shinySiteConfigEditorProvider;

	@Override
	public YouTubeConfigEditor getYouTubeConfigEditor() {
		return youTubeConfigEditorProvider.get();
	}

	@Override
	public ProvenanceConfigEditor getProvenanceConfigEditor() {
		return provenanceConfigEditorProvider.get();
	}

	@Override
	public ImageConfigEditor getImageConfigEditor() {
		return imageConfigEditorProvider.get();
	}

	@Override
	public AttachmentConfigEditor getAttachmentConfigEditor() {
		return attachmentConfigEditorProvider.get();
	}

	@Override
	public LinkConfigEditor getLinkConfigEditor() {
		return linkConfigEditorProvider.get();
	}

	@Override
	public APITableConfigEditor getSynapseAPICallConfigEditor() {
		return apiTableConfigEditorProvider.get();
	}

	@Override
	public TabbedTableConfigEditor getTabbedTableConfigEditor() {
		return tabbedTableConfigEditorProvider.get();
	}

	@Override
	public EntityListConfigEditor getEntityListConfigEditor() {
		return entityListConfigEditorProvider.get();
	}

	@Override
	public ShinySiteConfigEditor getShinySiteConfigEditor() {
		return shinySiteConfigEditorProvider.get();
	}

}

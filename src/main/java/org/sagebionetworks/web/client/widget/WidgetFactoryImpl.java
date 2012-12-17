package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.LinkConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.YouTubeConfigEditor;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.YouTubeWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;

import com.google.inject.Inject;

public class WidgetFactoryImpl implements WidgetFactory {

	PortalGinInjector ginInjector;
	
	@Inject
	public WidgetFactoryImpl(PortalGinInjector ginInjector) {
		this.ginInjector = ginInjector;
	}

	@Override
	public ProvenanceWidget createProvenanceWidget() {
		return ginInjector.getProvenanceRenderer();
	}
	@Override
	public ProvenanceConfigEditor createProvenanceWidgetEditor() {
		return ginInjector.getProvenanceConfigEditor();
	}
	
	@Override
	public ImageWidget createImageWidget() {
		return ginInjector.getImageRenderer();
	}	
	
	@Override
	public ImageConfigEditor createImageWidgetEditor() {
		return ginInjector.getImageConfigEditor();
	}
	
	@Override
	public YouTubeWidget createYouTubeWidget() {
		return ginInjector.getYouTubeRenderer();
	}
	
	@Override
	public YouTubeConfigEditor createYouTubeWidgetEditor() {
		return ginInjector.getYouTubeConfigEditor();
	}
	
	@Override
	public LinkConfigEditor createLinkWidgetEditor() {
		return ginInjector.getLinkConfigEditor();
	}

	
}

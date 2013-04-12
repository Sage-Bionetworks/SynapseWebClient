package org.sagebionetworks.web.client.utils;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.shared.EntityType;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;
/**
 * Extracted from DisaplyUtils
 *
 */
public class UploadUtils {
	/**
	 * 'Upload File' button
	 * @param entity 
	 * @param entityType 
	 */
	public static Widget getUploadButton(final EntityBundle entityBundle,
			EntityType entityType, final Uploader uploader,
			IconsImageBundle iconsImageBundle, EntityUpdatedHandler handler) {
		Button uploadButton = new Button(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK, AbstractImagePrototype.create(iconsImageBundle.NavigateUp16()));
		uploadButton.setHeight(25);
		final Window window = new Window();  
		window.addButton(new Button(DisplayConstants.BUTTON_CANCEL, new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				window.hide();
			}
		}));
		uploader.clearHandlers();
		// add user defined handler
		uploader.addPersistSuccessHandler(handler);
		
		// add handlers for closing the window
		uploader.addPersistSuccessHandler(new EntityUpdatedHandler() {			
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				window.hide();
			}
		});
		uploader.addCancelHandler(new CancelHandler() {				
			@Override
			public void onCancel(CancelEvent event) {
				window.hide();
			}
		});
		
		uploadButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				window.removeAll();
				window.setSize(uploader.getDisplayWidth(), uploader.getDisplayHeight());
				window.setPlain(true);
				window.setModal(true);		
				window.setHeading(DisplayConstants.TEXT_UPLOAD_FILE_OR_LINK);
				window.setLayout(new FitLayout());			
				window.add(uploader.asWidget(entityBundle.getEntity(), entityBundle.getAccessRequirements()), new MarginData(5));
				window.show();
			}
		});
		return uploadButton;
	}
}

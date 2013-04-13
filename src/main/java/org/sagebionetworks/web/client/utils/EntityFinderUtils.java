package org.sagebionetworks.web.client.utils;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

/**
 * Extracted from DisplayUtils
 * 
 */
public class EntityFinderUtils {

	public static void configureAndShowEntityFinderWindow(final EntityFinder entityFinder, final Window window, final SelectedHandler<Reference> handler) {  				
		window.setSize(entityFinder.getViewWidth(), entityFinder.getViewHeight());
		window.setPlain(true);
		window.setModal(true);
		window.setHeading(DisplayConstants.FIND_ENTITIES);
		window.setLayout(new FitLayout());
		window.add(entityFinder.asWidget(), new FitData(4));				
		window.addButton(new Button(DisplayConstants.SELECT, new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				Reference selected = entityFinder.getSelectedEntity();
				handler.onSelected(selected);
			}
		}));
		window.addButton(new Button(DisplayConstants.BUTTON_CANCEL, new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				window.hide();
			}
		}));
		window.setButtonAlign(HorizontalAlignment.RIGHT);
		window.show();
		entityFinder.refresh();
	}
}

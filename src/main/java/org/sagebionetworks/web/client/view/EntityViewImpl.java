package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.entity.EntityPageTop;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityViewImpl extends Composite implements EntityView {
	
	private SageImageBundle sageImageBundle;
	private HorizontalPanel loadingPanel;

	public interface EntityViewImplUiBinder extends UiBinder<Widget, EntityViewImpl> {}

	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	SimplePanel entityPageTopPanel;
		
	private Presenter presenter;
	private Header headerWidget;
	private EntityPageTop entityPageTop;
	private Footer footerWidget;
		
	@Inject
	public EntityViewImpl(
			EntityViewImplUiBinder binder,
			Header headerWidget,
			Footer footerWidget,
			EntityPageTop entityPageTop,
			SageImageBundle sageImageBundle) {		
		initWidget(binder.createAndBindUi(this));

		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.entityPageTop = entityPageTop;
		this.sageImageBundle = sageImageBundle;
		
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		// TODO : need to dynamically set the header widget
		//headerWidget.setMenuItemActive(MenuItems.PROJECTS);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		EntityUpdatedHandler handler = new EntityUpdatedHandler() {			
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				presenter.refresh();
			}
		};
		entityPageTop.setEntityUpdatedHandler(handler);
		header.clear();
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();

		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void setEntityBundle(EntityBundle bundle, Long versionNumber) {
		entityPageTop.clearState();
		entityPageTop.setBundle(bundle, versionNumber);
		entityPageTopPanel.setWidget(entityPageTop.asWidget());
		entityPageTop.refresh();
	}
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
		if (loadingPanel == null)
			loadingPanel = DisplayUtils.createFullWidthLoadingPanel(sageImageBundle);
		entityPageTopPanel.setWidget(loadingPanel);
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}


	@Override
	public void clear() {

	}

	@Override
	public void show404() {
		entityPageTop.clearState();
		entityPageTopPanel.setWidget(new HTML(DisplayUtils.get404Html()));
	}

	@Override
	public void show403() {
		entityPageTop.clearState();
		entityPageTopPanel.setWidget(new HTML(DisplayUtils.get403Html()));
	}

}

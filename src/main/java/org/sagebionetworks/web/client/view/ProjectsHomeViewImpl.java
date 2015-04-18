package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProjectsHomeViewImpl extends Composite implements ProjectsHomeView {

	public interface ProjectsHomeViewImplUiBinder extends UiBinder<Widget, ProjectsHomeViewImpl> {}

	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	SimplePanel createProjectPanel;
		
	private Presenter presenter;
	private IconsImageBundle icons;
	private Header headerWidget;
	private Footer footerWidget;

	
	@Inject
	public ProjectsHomeViewImpl(ProjectsHomeViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget, IconsImageBundle icons,
			SageImageBundle imageBundle) {		
		initWidget(binder.createAndBindUi(this));

		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.icons = icons;
		
		headerWidget.configure();
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
	}


	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		header.clear();
		headerWidget.configure();
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
		
		addCreateProject();		
	}

	private void addCreateProject() {		
		SimplePanel container;
		FlowPanel horizontalTable = new FlowPanel();
		horizontalTable.addStyleName("row");

		// title
	    container = new SimplePanel(new HTML(SafeHtmlUtils.fromSafeConstant("<h1>" + DisplayConstants.LABEL_PROJECT_NAME + "</h1>")));
	    container.addStyleName("col-md-3");
	    horizontalTable.add(container);
	    	    
		final TextBox textField = new TextBox();
		textField.setStyleName("form-control input-lg");
		textField.addKeyDownHandler(new KeyDownHandler() {				
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					createProject(textField.getValue());
	            }					
			}
		});
	    container = new SimplePanel(textField);
	    container.addStyleName("col-md-7 col-sm-8 col-xs-8");
	    horizontalTable.add(container);

		
		FlowPanel searchButtonContainer = new FlowPanel();		
		Button createBtn = DisplayUtils.createButton(DisplayConstants.LABEL_CREATE, ButtonType.DEFAULT);
		createBtn.addStyleName("btn-block btn-lg");
		createBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				createProject(textField.getValue());
			}
		});
	    searchButtonContainer.add(createBtn);
	    container = new SimplePanel(searchButtonContainer);
	    container.addStyleName("col-md-2 col-sm-4 col-xs-4");
	    horizontalTable.add(container);


	    createProjectPanel.clear();
	    createProjectPanel.add(horizontalTable);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}


	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}


	@Override
	public void clear() {
	}

	/*
	 * Private Methods
	 */
	private void createProject(String name) {
		if(name == null || name.isEmpty()) {
			showErrorMessage(DisplayConstants.PLEASE_ENTER_PROJECT_NAME);
			return;
		}
		presenter.createProject(name);
	}

}

package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;

import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
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
		
	private static final String ADD_PROJECT_BOX_STYLE = "addProjectBox";

	private Presenter presenter;
	private IconsImageBundle icons;
	private Header headerWidget;

	
	@Inject
	public ProjectsHomeViewImpl(ProjectsHomeViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget, IconsImageBundle icons,
			SageImageBundle imageBundle) {		
		initWidget(binder.createAndBindUi(this));

		this.headerWidget = headerWidget;
		this.icons = icons;
		
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
	}


	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;		
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
		
		addCreateProject();		
	}

	private void addCreateProject() {		
		FlexTable horizontalTable = new FlexTable();

		horizontalTable.setWidget(0, 0, new Html("<h1 class=\"left\">" + DisplayConstants.LABEL_PROJECT_NAME + "</h1>"));
		
		final TextBox searchField = new TextBox();
	    searchField.setStyleName(DisplayUtils.HOMESEARCH_BOX_STYLE_NAME + " " + ADD_PROJECT_BOX_STYLE);
	    horizontalTable.setWidget(0, 1, searchField);
		searchField.addKeyDownHandler(new KeyDownHandler() {				
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					presenter.createProject(searchField.getValue());
	            }					
			}
		});			
		
		LayoutContainer searchButtonContainer = new LayoutContainer();		
		Anchor anchor = new Anchor(DisplayConstants.LABEL_CREATE);
		anchor.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.createProject(searchField.getValue());
			}
		});
		searchButtonContainer.setStyleName("mega-button");
	    searchButtonContainer.add(anchor);
	    horizontalTable.setWidget(0, 2, searchButtonContainer);


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

}

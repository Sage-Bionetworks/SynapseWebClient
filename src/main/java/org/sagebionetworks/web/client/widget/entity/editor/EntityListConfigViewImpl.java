package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
import org.gwtbootstrap3.extras.bootbox.client.callback.PromptCallback;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.entity.EntityGroupRecordDisplay;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListRenderer;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityListConfigViewImpl extends FlowPanel implements EntityListConfigView {

	private Presenter presenter;
	IconsImageBundle iconsImageBundle;
	SynapseJSNIUtils synapseJSNIUtils;
	PortalGinInjector ginInjector;
	
	private FlowPanel tableContainer; 
	private Button addEntityButton;
	private EntityListRenderer renderer;
	private EntityFinder entityFinder;
	
	@Inject
	public EntityListConfigViewImpl(IconsImageBundle iconsImageBundle, SynapseJSNIUtils synapseJSNIUtils, PortalGinInjector ginInjector, EntityFinder entityFinder) {
		this.iconsImageBundle = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.ginInjector = ginInjector;
		this.entityFinder = entityFinder;
	}
	
	@Override
	public void initView() {
		//build the view
		clear();
		tableContainer = new FlowPanel();
		ClickHandler buttonClicked = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				entityFinder.configure(true, new SelectedHandler<Reference>() {					
					@Override
					public void onSelected(Reference selected) {
						if(selected.getTargetId() != null) {					
							presenter.addRecord(selected.getTargetId(), selected.getTargetVersionNumber(), null);
							entityFinder.hide();
						} else {
							showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
						}
					}
				});	
				entityFinder.show();
			}
		};
		
		addEntityButton = new Button(DisplayConstants.ADD_ENTITY, IconType.PLUS, buttonClicked);
		
		add(new HTML("&nbsp;"));
		add(tableContainer);
		add(addEntityButton);
		add(new HTML("&nbsp;"));
	}
	
	@Override
	public void configure() {	
		renderer = new EntityListRenderer(iconsImageBundle,	synapseJSNIUtils, ginInjector, false);
		tableContainer.clear();
		tableContainer.add(renderer);
	}

	@Override
	public void setEntityGroupRecordDisplay(int rowIndex,
			final EntityGroupRecordDisplay entityGroupRecordDisplay,
			boolean isLoggedIn) {
		renderer.setRow(rowIndex, entityGroupRecordDisplay, isLoggedIn);
		
		ClickHandler editRow = new ClickHandler() {				
			@Override
			public void onClick(ClickEvent event) {
				final int row = renderer.getRowIndexForEvent(event);
				Bootbox.prompt(DisplayConstants.NOTE, new PromptCallback() {
					@Override
					public void callback(String note) {
						if(note == null) note = "";
						entityGroupRecordDisplay.setNote(new SafeHtmlBuilder().appendEscapedLines(note).toSafeHtml());
						renderer.updateRowNote(row, entityGroupRecordDisplay.getNote());
						presenter.updateNote(row, note);
					}
				});
			}
		};
		ClickHandler deleteRow = new ClickHandler() {				
			@Override
			public void onClick(final ClickEvent event) {
				final int row = renderer.getRowIndexForEvent(event);
				Bootbox.confirm(DisplayConstants.PROMPT_SURE_DELETE + "?", new ConfirmCallback() {
					@Override
					public void callback(boolean isConfirmed) {
						if (isConfirmed) {
							renderer.removeRow(row);
							presenter.removeRecord(row);	
						}
					}
				});
			}
		};			
		renderer.setRowEditor(rowIndex, editRow, deleteRow);
	}

	
	@Override
	public void checkParams() throws IllegalArgumentException {
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	
	
	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
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


}

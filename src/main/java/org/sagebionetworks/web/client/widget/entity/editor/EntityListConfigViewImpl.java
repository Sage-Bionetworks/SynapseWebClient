package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.AlertUtils;
import org.sagebionetworks.web.client.utils.EntityFinderUtils;
import org.sagebionetworks.web.client.widget.entity.EntityGroupRecordDisplay;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.dialog.DeleteConfirmDialog;
import org.sagebionetworks.web.client.widget.entity.dialog.NameAndDescriptionEditorDialog;
import org.sagebionetworks.web.client.widget.entity.dialog.NameAndDescriptionEditorDialog.Callback;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListRenderer;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityListConfigViewImpl extends LayoutContainer implements EntityListConfigView {

	private Presenter presenter;
	IconsImageBundle iconsImageBundle;
	SynapseJSNIUtils synapseJSNIUtils;
	
	private LayoutContainer tableContainer; 
	private Button addEntityButton;
	private EntityListRenderer renderer;
	private EntityFinder entityFinder;
	
	@Inject
	public EntityListConfigViewImpl(IconsImageBundle iconsImageBundle, SynapseJSNIUtils synapseJSNIUtils, EntityFinder entityFinder) {
		this.iconsImageBundle = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.entityFinder = entityFinder;
	}
	
	@Override
	public void initView() {
		//build the view
		LayoutContainer lc = new LayoutContainer();
		lc.addStyleName("whiteBackground");		

		tableContainer = new LayoutContainer();
		
		addEntityButton = new Button(DisplayConstants.ADD_ENTITY, AbstractImagePrototype.create(iconsImageBundle.addSquareGrey16()));
		
		addEntityButton.addStyleName("clearleft");
		addEntityButton.addSelectionListener(new SelectionListener<ButtonEvent>() {			
			@Override
			public void componentSelected(ButtonEvent ce) {
				entityFinder.configure(true);				
				final Window window = new Window();
				EntityFinderUtils.configureAndShowEntityFinderWindow(entityFinder, window, new SelectedHandler<Reference>() {					
					@Override
					public void onSelected(Reference selected) {
						if(selected.getTargetId() != null) {					
							presenter.addRecord(selected.getTargetId(), selected.getTargetVersionNumber(), null);
							window.hide();
						} else {
							showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
						}
					}
				});				
			}
		});
		
		lc.add(new HTML("&nbsp;"));
		lc.add(tableContainer, new MarginData(0, 0, 5, 10));
		lc.add(addEntityButton, new MarginData(0, 0, 5, 10));
		lc.add(new HTML("&nbsp;"));
		add(lc);
	}
	
	@Override
	public void configure() {	
		renderer = new EntityListRenderer(iconsImageBundle,	synapseJSNIUtils, false);
		tableContainer.removeAll();
		tableContainer.add(renderer);
		tableContainer.layout();
		this.layout();
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
				NameAndDescriptionEditorDialog.showTextAreaDialog(entityGroupRecordDisplay.getNote().asString(), DisplayConstants.NOTE, new Callback() {						
					@Override
					public void onSave(String notused, String note) {
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
				DeleteConfirmDialog.showDialog(new DeleteConfirmDialog.Callback() {					
					@Override
					public void onAccept() {									
						renderer.removeRow(row);
						presenter.removeRecord(row);
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
		AlertUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		AlertUtils.showInfo(title, message);
	}

	@Override
	public int getDisplayHeight() {
		return 400;
	}
	@Override
	public int getAdditionalWidth() {
		return 631;
	}
	@Override
	public void clear() {
	}
	
	/*
	 * Private Methods
	 */


}

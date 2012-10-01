package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A widget that renders the standard edit menu for widgets
 * 
 * @author dburdick
 *
 */
public class WidgetMenu extends LayoutContainer {

	public static int COG = 0x1;
	public static int ADD = 0x2;
	public static int EDIT = 0x4;
	public static int DELETE = 0x8;
	
	private IconsImageBundle iconsImageBundle;
	
	/**
	 * Constructor
	 * @param iconsImageBundle Icons Images Bundle
	 */
	@Inject
	public WidgetMenu(IconsImageBundle iconsImageBundle) {
		this.iconsImageBundle = iconsImageBundle;
	}
	
	/*
	 * Pseudo Presenter
	 */
	int mask = 0;
	private ClickHandler addHandler;
	private ClickHandler editHandler;
	private ClickHandler deleteHandler;
	boolean showCog = true;
		
	@Override
	public Widget asWidget() {	
		if(showCog) mask = mask | COG;		
		createMenu(mask);
		
		return this;
	}
	
	public void showAdd(ClickHandler handler) {
		this.addHandler = handler;
		mask = mask | ADD;
	}
	
	public void showEdit(ClickHandler handler) {
		this.editHandler = handler;
		mask = mask | EDIT;
	}
	
	public void showDelete(ClickHandler handler) {
		this.deleteHandler = handler;
		mask = mask | DELETE;
	}
	
	/**
	 * Show the cog menu which gives text versions of each icon option
	 * @param show
	 */
	public void showCog(boolean show) {
		this.showCog = show;
	}
	
	public void addClicked(ClickEvent event) {
		addHandler.onClick(event);
	}

	public void editClicked(ClickEvent event) {
		editHandler.onClick(event);
	}

	public void deleteClicked(ClickEvent event) {
		deleteHandler.onClick(event);
	}
	
	
	/*
	 * Pseudo View Impl
	 */	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
	}
	
	public void createMenu(int mask) {
		boolean showCog = ((mask & COG) > 0) ? true : false;
		Menu cogMenu = new Menu();			
		
		// Add
		if((mask & ADD) > 0) {						
			AbstractImagePrototype icon = AbstractImagePrototype.create(iconsImageBundle.add16());
			Anchor anchor = DisplayUtils.createIconLink(icon, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					addClicked(event);
				}
			});
			anchor.addStyleName("margin-right-5");

			this.add(anchor);
			
//			if(showCog) {
//				cogMenu.add(new MenuItem(DisplayConstants.BUTTON_ADD, icon, new SelectionListener<MenuEvent>() {
//					@Override
//					public void componentSelected(MenuEvent ce) {
//						addClicked();
//					}
//				}));
//			}
		}
				
		// Edit
		if((mask & EDIT) > 0) {						
			AbstractImagePrototype icon = AbstractImagePrototype.create(iconsImageBundle.editGrey16());
			Anchor anchor = DisplayUtils.createIconLink(icon, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					editClicked(event);
				}
			});
			anchor.addStyleName("margin-right-5");

			this.add(anchor);
			
//			if(showCog) {
//				cogMenu.add(new MenuItem(DisplayConstants.BUTTON_EDIT, icon, new SelectionListener<MenuEvent>() {
//					@Override
//					public void componentSelected(MenuEvent ce) {
//						editClicked();
//					}
//				}));
//			}
		}
				
		// Delete
		if((mask & DELETE) > 0) {			
			AbstractImagePrototype icon = AbstractImagePrototype.create(iconsImageBundle.delete16());
			Anchor anchor = DisplayUtils.createIconLink(icon, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					deleteClicked(event);
				}
			});
			anchor.addStyleName("margin-right-5");

			this.add(anchor);
			
//			if(showCog) {
//				cogMenu.add(new MenuItem(DisplayConstants.LABEL_DELETE, icon, new SelectionListener<MenuEvent>() {
//					@Override
//					public void componentSelected(MenuEvent ce) {						
//						deleteClicked( ce.getEvent());						
//					}
//				}));
//			}
		}
		
		// add the cog menu
		if(showCog) {
//			Button btn = new Button();
//			btn.setIcon(AbstractImagePrototype.create(iconsImageBundle.cog16()));
//			btn.setMenu(cogMenu);
//			btn.setStyleName("button-link");
//			this.add(btn);
		}
				
	}
	
}

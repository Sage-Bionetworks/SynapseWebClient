package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.widget.entity.dialog.NameAndDescriptionEditorDialog;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PagesBrowserViewImpl extends LayoutContainer implements PagesBrowserView {

	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private EntityTreeBrowser entityTreeBrowser;
		
	@Inject
	public PagesBrowserViewImpl(
			IconsImageBundle iconsImageBundle,
			EntityTreeBrowser entityTreeBrowser) {
		this.iconsImageBundle = iconsImageBundle;
		this.entityTreeBrowser = entityTreeBrowser;
		this.setLayout(new FitLayout());
	}
	
	@Override
	protected void onRender(com.google.gwt.user.client.Element parent, int index) {
		super.onRender(parent, index);		
		
	};

	@Override
	public void configure(String entityId, String title) {
		this.removeAll(true);
		LayoutContainer lc = new LayoutContainer();
		lc.addStyleName("span-24 notopmargin");
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
		LayoutContainer topbar = new LayoutContainer();		
		LayoutContainer left = new LayoutContainer();
		left.setStyleName("left span-17 notopmargin");
		LayoutContainer right = new LayoutContainer();
		right.setStyleName("right span-7 notopmargin");
		topbar.add(left);
		topbar.add(right);

		if(title == null) title = "&nbsp;"; 
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant("<h3>" + title + "</h3>");
		left.add(new HTML(shb.toSafeHtml()));

		Button addPage = new Button(DisplayConstants.ADD_PAGE, AbstractImagePrototype.create(iconsImageBundle.synapseFolderAdd16()), new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				NameAndDescriptionEditorDialog.showNameDialog(DisplayConstants.LABEL_NAME, new NameAndDescriptionEditorDialog.Callback() {					
					@Override
					public void onSave(String name, String description) {
						presenter.createPage(name);
					}
				});
			}
		});
		addPage.setHeight(25);
		addPage.addStyleName("right");
		right.add(addPage, new MarginData(0, 3, 0, 0));
		
		LayoutContainer files = new LayoutContainer();
		files.setStyleName("span-24 notopmargin");
		refreshTreeView(entityId);
		files.add(entityTreeBrowser.asWidget());
		lc.add(topbar);
		lc.add(files);
		lc.layout();
		this.add(lc);
		this.layout(true);
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

	@Override
	public void refreshTreeView(String entityId) {
		entityTreeBrowser.configure(entityId, true);
	}
	
}

package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.Iterator;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
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
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PagesBrowserViewImpl extends LayoutContainer implements PagesBrowserView {

	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
		
	@Inject
	public PagesBrowserViewImpl(
			IconsImageBundle iconsImageBundle) {
		this.iconsImageBundle = iconsImageBundle;
		this.setLayout(new FitLayout());
	}
	
	@Override
	protected void onRender(com.google.gwt.user.client.Element parent, int index) {
		super.onRender(parent, index);		
		
	};

	@Override
	public void configure(List<EntityHeader> entityHeaders, boolean canEdit) {
		this.removeAll(true);
		//this widget shows nothing if the user can't edit the entity and it doesn't have any pages!
		if (!canEdit && entityHeaders.size() == 0)
			return;
		LayoutContainer lc = new LayoutContainer();
		lc.addStyleName("span-24 notopmargin");
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
		LayoutContainer titleBar = new LayoutContainer();		
		titleBar.setStyleName("left span-17 notopmargin");
		
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant("<h5>" + DisplayConstants.PAGES + "</h5>");
		titleBar.add(new HTML(shb.toSafeHtml()));

		LayoutContainer files = new LayoutContainer();
		files.setStyleName("span-24 notopmargin");
		files.add(getEntityList(entityHeaders));
		lc.add(titleBar);
		lc.add(files);
		if (canEdit) {
			Button insertButton = new Button(DisplayConstants.ADD_PAGE, AbstractImagePrototype.create(iconsImageBundle.add16()));
			insertButton.setWidth(115);
			insertButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
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
			
			LayoutContainer addPageBar = new LayoutContainer();		
			addPageBar.setStyleName("left span-17 notopmargin");
			addPageBar.add(insertButton, new MarginData(0, 3, 0, 0));
			lc.add(addPageBar);
		}
			
		lc.layout();
		this.add(lc);
		this.layout(true);
	}	
	
	private HTMLPanel getEntityList(List<EntityHeader> entityHeaders) {
		StringBuilder htmlBuilder = new StringBuilder();
		htmlBuilder.append("<ol class=\"pagelist\">");
		
		for (Iterator iterator = entityHeaders.iterator(); iterator.hasNext();) {
			EntityHeader header = (EntityHeader) iterator.next();
			htmlBuilder.append("<li><a class=\"link\" href=\"" + DisplayUtils.getSynapseHistoryToken(header.getId()) + "\">" + header.getName() + "</a></li>");
		}
		htmlBuilder.append("</ol>");
		HTMLPanel htmlPanel = new HTMLPanel(htmlBuilder.toString());
		return htmlPanel;
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
}

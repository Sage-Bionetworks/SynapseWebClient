package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.inject.Inject;

public class FavoriteWidgetViewImpl extends Composite implements FavoriteWidgetView {
	
	private static String favoriteStarOffHtml;
	private static String favoriteStarHtml;
	
	private Presenter presenter;

	final IconsImageBundle icons;

	private Anchor favoriteAnchor;
	boolean isFavorite = false;
	
	@Inject
	public FavoriteWidgetViewImpl(IconsImageBundle iconsImageBundle) {
		this.icons = iconsImageBundle;
		
		favoriteStarHtml = AbstractImagePrototype.create(iconsImageBundle.star16()).getHTML();
		favoriteStarOffHtml = AbstractImagePrototype.create(iconsImageBundle.starEmpty16()).getHTML();
		
		favoriteAnchor = new Anchor();
		favoriteAnchor.setHTML(favoriteStarOffHtml);
		favoriteAnchor.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				isFavorite = isFavorite ? false : true;
				setFavoriteIcon();
				presenter.setIsFavorite(isFavorite);
			}
		});
		initWidget(favoriteAnchor);
	}

	@Override
	public void showIsFavorite(boolean isFavorite) {
		this.isFavorite = isFavorite;
		setFavoriteIcon();		
	}

	private void setFavoriteIcon() {
		if(isFavorite)
			favoriteAnchor.setHTML(favoriteStarHtml);
		else 
			favoriteAnchor.setHTML(favoriteStarOffHtml);
	}

	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void clear() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	

}

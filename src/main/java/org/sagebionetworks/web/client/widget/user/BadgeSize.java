package org.sagebionetworks.web.client.widget.user;

import org.gwtbootstrap3.client.ui.constants.IconSize;

public enum BadgeSize {
    LARGE(IconSize.TIMES5, "font-size-20", "64px", true),
    DEFAULT(IconSize.TIMES2, "font-size-17", "32px", true),
    SMALL(IconSize.LARGE, "font-size-15", "24px", true),
    DEFAULT_PICTURE_ONLY(IconSize.TIMES2, "", "32px", false),
    SMALL_PICTURE_ONLY(IconSize.LARGE, "", "24px", false),
    EXTRA_SMALL(IconSize.NONE, "", "16px", false);
    
    private IconSize iconSize;
    private String textStyle;
    private boolean isTextVisible;
    private String pictureHeight;
    
    BadgeSize (IconSize iconSize, String textStyle, String pictureHeight, boolean isTextVisible) {
    	this.iconSize = iconSize;
    	this.textStyle = textStyle;
    	this.isTextVisible = isTextVisible;
    	this.pictureHeight = pictureHeight;
    }
    
    public IconSize iconSize(){
    	return iconSize;
    }
    
    public String textStyle(){
    	return textStyle;
    }
    
    public boolean isTextVisible() {
    	return isTextVisible;
    }
    
    public String pictureHeight() {
		return pictureHeight;
	}
    
}

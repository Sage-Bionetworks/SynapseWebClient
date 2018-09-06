package org.sagebionetworks.web.client.widget.user;


public enum BadgeSize {
    LARGER("fa-2x", "font-size-20", 60, true),
    LARGE("", "font-size-14", 28, true),
    DEFAULT("", "", 23, true),
    SMALLER("font-size-0-9em", "font-size-13", 18, true),
    LARGER_PICTURE_ONLY("font-size-12x", "", 124, false),
    LARGE_PICTURE_ONLY("", "", 28, false),
    SMALL_PICTURE_ONLY("", "", 23, false),
    EXTRA_SMALL("font-size-0-9em", "", 12, false);
    
    private String defaultPictureStyle;
    private String textStyle;
    private boolean isTextVisible;
    private int pictureHeightPx;
    
    BadgeSize (String defaultPictureStyle, String textStyle, int pictureHeightPx, boolean isTextVisible) {
    	this.defaultPictureStyle = defaultPictureStyle;
    	this.textStyle = textStyle;
    	this.isTextVisible = isTextVisible;
    	this.pictureHeightPx = pictureHeightPx;
    }
    
    public String getDefaultPictureStyle() {
		return defaultPictureStyle;
	}
    
    public String textStyle(){
    	return textStyle;
    }
    
    public boolean isTextVisible() {
    	return isTextVisible;
    }
    
    public int pictureHeightPx() {
		return pictureHeightPx;
	}
    
}

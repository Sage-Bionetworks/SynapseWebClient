package org.sagebionetworks.web.client.widget.user;


public enum BadgeSize {
    LARGE("font-size-2x", "font-size-20", "64px", true),
    DEFAULT("font-size-2x", "font-size-17", "32px", true),
    SMALL("font-size-lg", "font-size-15", "27px", true),
    LARGER_PICTURE_ONLY("font-size-12x", "", "128px", false),
    DEFAULT_PICTURE_ONLY("font-size-2x", "", "32px", false),
    SMALL_PICTURE_ONLY("font-size-lg", "", "27px", false),
    EXTRA_SMALL("font-size-1x", "", "16px", false);
    
    private String defaultPictureStyle;
    private String textStyle;
    private boolean isTextVisible;
    private String pictureHeight;
    
    BadgeSize (String defaultPictureStyle, String textStyle, String pictureHeight, boolean isTextVisible) {
    	this.defaultPictureStyle = defaultPictureStyle;
    	this.textStyle = textStyle;
    	this.isTextVisible = isTextVisible;
    	this.pictureHeight = pictureHeight;
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
    
    public String pictureHeight() {
		return pictureHeight;
	}
    
}

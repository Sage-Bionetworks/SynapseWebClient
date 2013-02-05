package org.sagebionetworks.web.client.widget.entity.dialog;

import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;

public abstract class UploadFormPanel extends FormPanel{
	public abstract FileUploadField getFileUploadField();
}
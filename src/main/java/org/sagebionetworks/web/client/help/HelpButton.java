package org.sagebionetworks.web.client.help;

/*
 * #%L
 * GwtBootstrap3
 * %%
 * Copyright (C) 2013 GwtBootstrap3
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Popover;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Trigger;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.user.client.ui.Widget;

/**
 * Context-sensitive help button/modal combination.
 * 
Recipe for Context-Sensitive Help dialog
<b:Modal title="Help" closable="true" fade="true" dataBackdrop="STATIC" dataKeyboard="true" b:id="discussionForum">
  <b:ModalBody>
    <bh:Text><ui:text from='{help.discussionForum}'/></bh:Text>
  </b:ModalBody>
  <b:ModalFooter>
  	<b:Anchor href="https://docs.synapse.org/discussionForum" target="_blank">
  		<button class="btn btn-primary margin-right-10">Learn More...</button>
  	</b:Anchor>
    <b:Button dataDismiss="MODAL">Close</b:Button>
  </b:ModalFooter>
</b:Modal>
<b:Button dataTarget="#discussionForum" dataToggle="MODAL" icon="QUESTION" type="PRIMARY" size="EXTRA_SMALL" />

 */
public class HelpButton extends Div {
	
    private final Icon helpButton = new Icon(IconType.QUESTION_CIRCLE);
    private final Popover popover = new Popover(helpButton);
    
    public HelpButton() {
    	addStyleName("displayInline margin-left-5 margin-right-5");
    	popover.setTrigger(Trigger.CLICK);
    	popover.setIsHtml(true);
    	helpButton.addStyleName("imageButton synapse-blue");
    	add(helpButton);
    }
    
    public void setHref(String href) {
    	popover.setContent("<a href=\""+href+"\" target=\"_blank\"><button class=\"btn btn-primary right\">Learn More</button></a>");
    }
    
    public void setPlacement(Placement placement) {
    	popover.setPlacement(placement);
    }
    
    @Override
    public void add(final Widget w) {
    	// user should specify content
        if (w instanceof Text) {
        	popover.setTitle(((Text)w).getText());
        } else {
            super.add(w);
        }
    }


}

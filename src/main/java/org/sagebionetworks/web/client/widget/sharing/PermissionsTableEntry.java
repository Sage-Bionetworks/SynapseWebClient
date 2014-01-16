package org.sagebionetworks.web.client.widget.sharing;

import java.util.HashSet;
import java.util.Map;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.AclUtils;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.extjs.gxt.ui.client.data.BaseModelData;

/*
 * Private Classes
 */
public class PermissionsTableEntry extends BaseModelData {
	private static final long serialVersionUID = -5153720887903543399L;
	private AclEntry aclEntry;
	public PermissionsTableEntry(Map<PermissionLevel, String> permissionDisplay, AclEntry aclEntry) {			
		super();
		this.aclEntry = aclEntry;
		PermissionLevel level = AclUtils.getPermissionLevel(new HashSet<ACCESS_TYPE>(aclEntry.getAccessTypes()));			
		if(level != null) {
			this.set(EvaluationAccessControlListEditorViewImpl.ACCESS_COLUMN_ID, permissionDisplay.get(level)); 
		}			
	}
	public AclEntry getAclEntry() {
		return aclEntry;
	}		
}
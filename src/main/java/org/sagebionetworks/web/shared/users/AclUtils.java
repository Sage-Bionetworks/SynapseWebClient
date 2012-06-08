package org.sagebionetworks.web.shared.users;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.shared.NodeType;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class AclUtils {	 

	
	/**
	 * Returns the highest permission level for the logged in user for the given entity
	 * @param nodeType
	 * @param nodeId
	 * @return
	 */
	public static void getHighestPermissionLevel(final NodeType nodeType, final String nodeId, final SynapseClientAsync synapseClient, final AsyncCallback<PermissionLevel> callback) {		
		
		// TODO : making two rest calls is not ideal. need to change hasAccess API to include multi params
		synapseClient.hasAccess(nodeId, ACCESS_TYPE.UPDATE.name(), new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean canUpdate) {
				if(canUpdate) {
					// CAN EDIT, now check can administer
					synapseClient.hasAccess(nodeId, ACCESS_TYPE.CHANGE_PERMISSIONS.name(), new AsyncCallback<Boolean>() {
						@Override
						public void onSuccess(Boolean canAdmin) {
							if(canAdmin) {
								// user can administer
								callback.onSuccess(PermissionLevel.CAN_ADMINISTER);
							} else {
								callback.onSuccess(PermissionLevel.CAN_EDIT);
							}
						}
						
						@Override
						public void onFailure(Throwable caught) {
							callback.onFailure(caught);
						}
					});				
				} else {
					// user can only view
					callback.onSuccess(PermissionLevel.CAN_VIEW);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	public static PermissionLevel getPermissionLevel(List<ACCESS_TYPE> accessTypes) {		
		// TODO : this should be updated to be more generic
		if(accessTypes.contains(ACCESS_TYPE.READ)
			&& accessTypes.contains(ACCESS_TYPE.CREATE)
			&& accessTypes.contains(ACCESS_TYPE.UPDATE)
			&& accessTypes.contains(ACCESS_TYPE.DELETE)
			&& accessTypes.contains(ACCESS_TYPE.CHANGE_PERMISSIONS)) {
			return PermissionLevel.CAN_ADMINISTER;
		} else if(accessTypes.contains(ACCESS_TYPE.READ) 
				&& accessTypes.contains(ACCESS_TYPE.CREATE)
				&& accessTypes.contains(ACCESS_TYPE.UPDATE)
				&& !accessTypes.contains(ACCESS_TYPE.DELETE)
				&& !accessTypes.contains(ACCESS_TYPE.CHANGE_PERMISSIONS)) {
			return PermissionLevel.CAN_EDIT;
		} else if(accessTypes.contains(ACCESS_TYPE.READ) 
				&& !accessTypes.contains(ACCESS_TYPE.CREATE)
				&& !accessTypes.contains(ACCESS_TYPE.UPDATE)
				&& !accessTypes.contains(ACCESS_TYPE.DELETE)
				&& !accessTypes.contains(ACCESS_TYPE.CHANGE_PERMISSIONS)) {
			return PermissionLevel.CAN_VIEW;
		} else {
			return null;
		}
	}
	
	public static List<ACCESS_TYPE> getACCESS_TYPEs(PermissionLevel permissionLevel) {
		return getPermissionLevelMap().get(permissionLevel);
	}
	
	public static ACCESS_TYPE getACCESS_TYPE(String accessType) {
		if(ACCESS_TYPE.READ.toString().equals(accessType)) return ACCESS_TYPE.READ;
		else if(ACCESS_TYPE.CREATE.toString().equals(accessType)) return ACCESS_TYPE.CREATE;
		else if(ACCESS_TYPE.DELETE.toString().equals(accessType)) return ACCESS_TYPE.DELETE;
		else if(ACCESS_TYPE.CHANGE_PERMISSIONS.toString().equals(accessType)) return ACCESS_TYPE.CHANGE_PERMISSIONS;
		else if(ACCESS_TYPE.UPDATE.toString().equals(accessType)) return ACCESS_TYPE.UPDATE;
		else return null;
	}


	private static Map<PermissionLevel, List<ACCESS_TYPE>> getPermissionLevelMap() {
		Map<PermissionLevel, List<ACCESS_TYPE>> permToACCESS_TYPE = new HashMap<PermissionLevel, List<ACCESS_TYPE>>();
		permToACCESS_TYPE.put(PermissionLevel.CAN_VIEW,
				Arrays.asList(new ACCESS_TYPE[] { ACCESS_TYPE.READ }));
		permToACCESS_TYPE.put(
				PermissionLevel.CAN_EDIT,
				Arrays.asList(new ACCESS_TYPE[] { ACCESS_TYPE.READ,
						ACCESS_TYPE.READ, ACCESS_TYPE.CREATE,
						ACCESS_TYPE.UPDATE }));
		permToACCESS_TYPE.put(PermissionLevel.CAN_ADMINISTER,
				Arrays.asList(new ACCESS_TYPE[] { 
						ACCESS_TYPE.READ, ACCESS_TYPE.CREATE,
						ACCESS_TYPE.UPDATE, ACCESS_TYPE.DELETE,
						ACCESS_TYPE.CHANGE_PERMISSIONS }));
		
		return permToACCESS_TYPE;
	}
	
	private static Map<ACCESS_TYPE, List<PermissionLevel>> getACCESS_TYPEMap() {
		Map<ACCESS_TYPE, List<PermissionLevel>> accessTypeToPerm = new HashMap<ACCESS_TYPE, List<PermissionLevel>>();
		
		accessTypeToPerm.put(ACCESS_TYPE.READ,
				Arrays.asList(new PermissionLevel[] { PermissionLevel.CAN_VIEW, PermissionLevel.CAN_EDIT, PermissionLevel.CAN_ADMINISTER }));
		accessTypeToPerm.put(ACCESS_TYPE.CREATE,
				Arrays.asList(new PermissionLevel[] { PermissionLevel.CAN_EDIT, PermissionLevel.CAN_ADMINISTER }));
		accessTypeToPerm.put(ACCESS_TYPE.UPDATE,
				Arrays.asList(new PermissionLevel[] { PermissionLevel.CAN_EDIT, PermissionLevel.CAN_ADMINISTER }));
		accessTypeToPerm.put(ACCESS_TYPE.DELETE,
				Arrays.asList(new PermissionLevel[] { PermissionLevel.CAN_ADMINISTER }));
		accessTypeToPerm.put(ACCESS_TYPE.CHANGE_PERMISSIONS,
				Arrays.asList(new PermissionLevel[] { PermissionLevel.CAN_ADMINISTER }));
		
		return accessTypeToPerm;
	}

}

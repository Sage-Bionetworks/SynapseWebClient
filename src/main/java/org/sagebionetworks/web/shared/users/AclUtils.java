package org.sagebionetworks.web.shared.users;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.shared.NodeType;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class AclUtils {

	private static Map<PermissionLevel, Set<ACCESS_TYPE>> permToACCESS_TYPE;

	private static Map<ACCESS_TYPE, Set<PermissionLevel>> accessTypeToPerm;

	// Build up a list of increasing permissions, starting with VIEW
	// It's acceptable to use the shallow copy of the ArrayList(arrList) constructor
	// because the elements are all ENUMS.
	static {
		permToACCESS_TYPE = new HashMap<PermissionLevel, Set<ACCESS_TYPE>>();
		TreeSet<ACCESS_TYPE> accessList = new TreeSet<ACCESS_TYPE>();

		accessList.add(ACCESS_TYPE.READ);
		permToACCESS_TYPE.put(PermissionLevel.CAN_VIEW, new TreeSet<ACCESS_TYPE>(accessList));

		accessList.add(ACCESS_TYPE.DOWNLOAD);
		permToACCESS_TYPE.put(PermissionLevel.CAN_DOWNLOAD, new TreeSet<ACCESS_TYPE>(accessList));

		accessList.add(ACCESS_TYPE.CREATE);
		accessList.add(ACCESS_TYPE.UPDATE);
		permToACCESS_TYPE.put(PermissionLevel.CAN_EDIT, new TreeSet<ACCESS_TYPE>(accessList));

		accessList.add(ACCESS_TYPE.DELETE);
		accessList.add(ACCESS_TYPE.CHANGE_PERMISSIONS);
		permToACCESS_TYPE.put(PermissionLevel.CAN_ADMINISTER, new TreeSet<ACCESS_TYPE>(accessList));

		// Build the reverse mapping from the first map
		accessTypeToPerm = new HashMap<ACCESS_TYPE, Set<PermissionLevel>>();
		for (ACCESS_TYPE type : ACCESS_TYPE.values()) {
			TreeSet<PermissionLevel> levels = new TreeSet<PermissionLevel>();

			for (Entry<PermissionLevel, Set<ACCESS_TYPE>> entry : permToACCESS_TYPE
					.entrySet()) {
				if (entry.getValue().contains(type)) {
					levels.add(entry.getKey());
				}
			}
			accessTypeToPerm.put(type, levels);
		}
	}

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
					// user can only view/download
					synapseClient.hasAccess(nodeId, ACCESS_TYPE.DOWNLOAD.name(), new AsyncCallback<Boolean>() {
						@Override
						public void onSuccess(Boolean canDownload) {
							if(canDownload) {
								// user can download
								callback.onSuccess(PermissionLevel.CAN_DOWNLOAD);
							} else {
								callback.onSuccess(PermissionLevel.CAN_VIEW);
							}
						}

						@Override
						public void onFailure(Throwable caught) {
							callback.onFailure(caught);
						}
					});
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	public static PermissionLevel getPermissionLevel(Set<ACCESS_TYPE> accessTypes) {
		for (PermissionLevel level : PermissionLevel.values()) {
			if (accessTypes.equals(permToACCESS_TYPE.get(level))) {
				return level;
			}
		}
		return null;
	}

	public static Set<ACCESS_TYPE> getACCESS_TYPEs(PermissionLevel permissionLevel) {
		return permToACCESS_TYPE.get(permissionLevel);
	}

	public static Set<PermissionLevel> getPermisionLevels(ACCESS_TYPE accessType) {
		return accessTypeToPerm.get(accessType);
	}

	public static ACCESS_TYPE getACCESS_TYPE(String accessType) {
		ACCESS_TYPE valueOf;
		try {
			valueOf = ACCESS_TYPE.valueOf(accessType);
		} catch (IllegalArgumentException e) {
			valueOf = null;
		}

		return valueOf;
	}

}

package org.sagebionetworks.web.client.widget.upload;

public class FileUpload {
	private FileMetadata fileMeta;
	private String fileHandleId;

	public FileUpload(FileMetadata fileMeta, String fileHandleId) {
		this.fileMeta = fileMeta;
		this.fileHandleId = fileHandleId;
	}

	public FileMetadata getFileMeta() {
		return fileMeta;
	}

	public String getFileHandleId() {
		return fileHandleId;
	}
}

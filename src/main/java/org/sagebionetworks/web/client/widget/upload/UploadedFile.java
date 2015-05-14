package org.sagebionetworks.web.client.widget.upload;

public class UploadedFile {
	private FileMetadata fileMeta;
	private String fileHandleId;
	
	public UploadedFile(FileMetadata fileMeta, String fileHandleId) {
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

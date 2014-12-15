package pl.edu.agh.kis.dataretrieval.gui;
import java.io.File;


public class FilePathHolder {
	private String filePath;
	private String filename;
	private String directory;
	
	public FilePathHolder(String filePath) {
		super();
		this.filePath = filePath;
		this.filename = getFilenameFromPath(filePath);
		this.directory = getDirectoryFromPath(filePath);
		
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
		this.filename = filePath.substring(0, filePath.lastIndexOf('.')).substring(filePath.lastIndexOf(File.separator)+1);
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String getDirectory() {
		return filename;
	}
	
	@Override
	public String toString() {
		return filename;
	}
	
	private String getFilenameFromPath(String filePath){
		return filePath.substring(0, filePath.lastIndexOf('.')).substring(filePath.lastIndexOf(File.separator)+1);
	}
	
	private String getDirectoryFromPath(String filePath){
		return filePath.substring(0, filePath.lastIndexOf(File.separator));
	}
}

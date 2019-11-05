package model;

import java.io.File;
import java.util.List;

import raf.rs.FIleStorageSpi.MyDir;

public class MyRemoteDir implements MyDir {
	private static final String ACCESS_TOKEN = "defy6oaann6viza";
	public boolean initFileStorage(String path, String rootDirName) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	public File[] searchDirectory(String dirPath, String searchFor) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean createMultipleDirectories(String path, String dirsName, int numberOfDirs) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	public File createEmptyDirectory(String path, String fileName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean delDirectory(String path, String dirName) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean downloadDirectory(String pathSource, String pathDest) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	public String listDirectories(String dirPath) {
		// TODO Auto-generated method stub
		return null;
	}

	public String listFiles(String dirPath) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<File> getFilesWithExtension(String path, String extension) {
		// TODO Auto-generated method stub
		return null;
	}

	public File getFilesWithMetadata(boolean withMetaData) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getAllFiles(boolean sorted, String dirPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public void setForbiddenExtension(String extension) {
		// TODO Auto-generated method stub
		
	}
	
}

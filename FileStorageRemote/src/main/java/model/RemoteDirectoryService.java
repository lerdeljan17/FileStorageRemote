package model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.DownloadBuilder;
import com.dropbox.core.v2.files.DownloadZipResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import exceptions.CreateException;
import exceptions.NotFoundException;
import raf.rs.FIleStorageSpi.MyDir;
import raf.rs.FIleStorageSpi.MyFile;

public class RemoteDirectoryService extends DropBoxProvider implements MyDir {
	
	private RemoteStorage fileStorage;
	
	public RemoteDirectoryService(RemoteStorage fileStorage) {
		super();
		this.fileStorage = fileStorage;
	}

	public boolean initFileStorage(String path, String rootDirName) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void clearResults() {
		File toDel = new File("Results");
		String[] entries = toDel.list();
		for (String s : entries) {
			File currentFile = new File(toDel.getPath(), s);
			currentFile.delete();
		}
	}
	public File[] searchDirectory(String dirPath, String searchFor) {
		// TODO Auto-generated method stub
		//delete all files in resukts
		this.clearResults();
		File[] toRet;
		RemoteFileService rm = new RemoteFileService(fileStorage);
		ListFolderResult listing;
		try {
			listing = getClient().files().listFolderBuilder(fileStorage.getRootPath() + "/" +dirPath).start();
			 for (Metadata child : listing.getEntries()) {
				  if(child.getName().contains(searchFor)) {
					 rm.downloadFile(child.getPathLower(), "Results/"+child.getName());
				  }
			            }
		
		} catch (DbxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final String search = searchFor;
		String path = FilenameUtils.separatorsToSystem("Results");
		File dir = new File(path);

		File[] matches = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.contains(search);
			}
		});
		 
		return matches;
	}

	public boolean createMultipleDirectories(String path, String dirsName, int numberOfDirs) throws Exception {
		// TODO Auto-generated method stub
		if (numberOfDirs <= 0) {
			throw new CreateException();
		}
		for (int i = 0; i < numberOfDirs; i++) {
			File dir = createEmptyDirectory(fileStorage.getRootPath() + "/" +path, dirsName + i);
		}
		return false;
	}

	public boolean createEmptyDirectoryB(String path, String fileName) throws Exception {
		// TODO Auto-generated method stub
		  try {
	            FolderMetadata folder = getClient().files().createFolder(fileStorage.getRootPath() + "/" +path+"/"+fileName);
	            System.out.println(folder.getName());
	        } catch (CreateFolderErrorException err) {
	            if (err.errorValue.isPath() && err.errorValue.getPathValue().isConflict()) {
	                System.out.println("Something already exists at the path.");
	            } else {
	                System.out.print("Some other CreateFolderErrorException occurred...");
	                System.out.print(err.toString());
	            }
	        } catch (Exception err) {
	            System.out.print("Some other Exception occurred...");
	            System.out.print(err.toString());
	        }
		return true;
	}

	public boolean delDirectory(String path, String dirName) throws Exception {
		// TODO Auto-generated method stub
		  try
	        {
	            Metadata metadata = getClient().files().delete(fileStorage.getRootPath() + "/" +path+"/"+dirName);
	        }
	        catch (DbxException dbxe)
	        {
	            dbxe.printStackTrace();
	        }
		return true;
	}

	public boolean downloadDirectory(String pathSource, String pathDest) throws Exception {
		// TODO Auto-generated method stub
		OutputStream outputStream = new FileOutputStream(pathDest);
		DownloadZipResult metadata = getClient().files().downloadZipBuilder(fileStorage.getRootPath() + "/" +pathSource).download(outputStream);
		return false;
	}

	public String listDirectories(String dirPath) {
		// TODO Auto-generated method stub
		ArrayList<String> toRet = new ArrayList<String>();
		ListFolderResult listing;
		try {
			listing = getClient().files().listFolderBuilder(dirPath).start();
			 for (Metadata child : listing.getEntries()) {
				  if(child instanceof FolderMetadata) {
					  toRet.add(child.getName());
				  }
			            }
		} catch (DbxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		return toRet.toString();
	}

	public String listFiles(String dirPath,boolean withMetaData) {
		ArrayList<String> toRet = new ArrayList<String>();
		ListFolderResult listing;
		try {
			listing = getClient().files().listFolderBuilder(fileStorage.getRootPath() + "/" +dirPath).start();
			 for (Metadata child : listing.getEntries()) {
				  if(child instanceof FileMetadata) {
					  toRet.add(child.getName());
				  }
			            }
		} catch (DbxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		return toRet.toString();
	}

	public List<File> getFilesWithExtension(String path, String extension) {

		this.clearResults();
		File[] toRet;
		RemoteFileService rm = new RemoteFileService(fileStorage);
		ListFolderResult listing;
		try {
			listing = getClient().files().listFolderBuilder(fileStorage.getRootPath() + "/" +path).start();
			 for (Metadata child : listing.getEntries()) {
				  if(child.getPathLower().contains(extension)) {
					 rm.downloadFile(child.getPathLower(), "Results/"+child.getName());
				  }
			            }
		
		} catch (DbxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] ext = { extension };
		String tpath = FilenameUtils.separatorsToSystem("Results");
		File file = new File(tpath);
		List<File> files = (List<File>) FileUtils.listFiles(file, ext, false);
		// TODO Da li treba da se vrati lista imena ili lista fajlova?
		return files;
	}


	public List<String> getAllFiles(boolean sorted, String dirPath) throws Exception {

		this.clearResults();
		File[] toRet;
		RemoteFileService rm = new RemoteFileService(fileStorage);
		ListFolderResult listing;
		try {
			listing = getClient().files().listFolderBuilder(fileStorage.getRootPath() + "/" +dirPath).start();
			 for (Metadata child : listing.getEntries()) {
				  if(child instanceof FileMetadata) {
					 rm.downloadFile(child.getPathLower(), "Results/"+child.getName());
				  }
			            }
		
		} catch (DbxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		String path = FilenameUtils.separatorsToSystem("Results");
		File root = new File(path);
		// Za slucaj da se na prosledjenoj putanji ne nalazi direktorijum
		if (!root.isDirectory()) {
			throw new NotFoundException(dirPath);
		}

		List<File> files = (List<File>) FileUtils.listFiles(root, null, true);
		if (files.isEmpty()) {
			return null;
		}

		List<String> filesName = new ArrayList<String>();
		for (File f : files) {
			filesName.add(f.getName());
		}

		if (sorted) {
			Collections.sort(filesName);
		}

		return filesName;
		
	}

	@Override
	public File createEmptyDirectory(String path, String fileName) throws Exception {
		//ne treba da se implementira ovde
		return null;
	}

	@Override
	public File getFilesWithMetadata(boolean withMetaData) {
		// TODO Auto-generated method stub
		//ne treba da se implementira
		return null;
	}

	public RemoteStorage getFileStorage() {
		return fileStorage;
	}

	public void setFileStorage(RemoteStorage fileStorage) {
		this.fileStorage = fileStorage;
	}
	
}

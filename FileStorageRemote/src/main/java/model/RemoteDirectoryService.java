package model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
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

public class RemoteDirectoryService implements MyDir {

	private RemoteStorage fileStorage;

	public RemoteDirectoryService(RemoteStorage fileStorage) {
		this.fileStorage = fileStorage;
	}

	public File[] searchDirectory(String dirPath, String searchFor) {
		this.clearResults();
		File[] toRet;
		RemoteFileService rm = new RemoteFileService(fileStorage);
		ListFolderResult listing;
		try {
			listing = fileStorage.getProvider().getClient().files()
					.listFolderBuilder(fileStorage.getRootPath() + "/" + dirPath).start();
			for (Metadata child : listing.getEntries()) {
				if (child.getName().contains(searchFor)) {
					// System.out.println("..." + child.getPathDisplay().substring(1,
					// child.getPathDisplay().length()));
					if (child instanceof FileMetadata) {
						rm.downloadFile(child.getPathDisplay().substring(1, child.getPathDisplay().length()),
								"Results/" + child.getName());
					} else if (child instanceof FolderMetadata) {
						this.downloadDirectory(child.getPathDisplay().substring(1, child.getPathDisplay().length()),
								"Results/" + child.getName());
					}
				}
			}

		} catch (DbxException e) {
			System.out.println("Nije bilo moguce pretraziti direktorijum! - 1");
			// e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Nije bilo moguce pretraziti direktorijum! - 2");
			// e.printStackTrace();
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
		if (numberOfDirs <= 0) {
			throw new CreateException();
		}

		for (int i = 0; i < numberOfDirs; i++) {
			createEmptyDirectoryB(path, dirsName + " " + i);
		}

		return true;
	}

	public boolean createEmptyDirectoryB(String path, String fileName) throws Exception {
		String fullPath = fileStorage.getRootPath() + "/" + path + "/" + fileName;
		try {
			FolderMetadata folder = fileStorage.getProvider().getClient().files().createFolder(fullPath);
			// System.out.println(folder.getName());
		} catch (CreateFolderErrorException err) {
			if (err.errorValue.isPath() && err.errorValue.getPathValue().isConflict()) {
				System.out.println("Nesto vec postoji na prosledjenoj putanji (" + fullPath + ")!");
			} else {
				System.out.println("Greska prilikom kreiranja praznog direktorijuma");
				// System.out.print(err.toString());
			}
		} catch (Exception err) {
			System.out.println("Greska prilikom kreiranja praznog direktorijuma");
			// System.out.print(err.toString());
		}
		return true;
	}

	public boolean delDirectory(String path, String dirName) throws Exception {
		try {
			Metadata metadata = fileStorage.getProvider().getClient().files()
					.delete(fileStorage.getRootPath() + "/" + path + "/" + dirName);
		} catch (DbxException dbxe) {
			// dbxe.printStackTrace();
			System.out.println("Greska prilikom brisanja direktorijuma " + dirName);
		}
		return true;
	}

	public boolean downloadDirectory(String pathSource, String pathDest) throws Exception {
		String path = pathDest;
		if (!pathDest.contains(".zip")) {
			path += ".zip";
		}
		OutputStream outputStream = new FileOutputStream(path);
		DownloadZipResult metadata = fileStorage.getProvider().getClient().files()
				.downloadZipBuilder(fileStorage.getRootPath() + "/" + pathSource).download(outputStream);
		return true;
	}

	public String listDirectories(String dirPath) {
		ArrayList<String> toRet = new ArrayList<String>();
		ListFolderResult listing;
		try {
			listing = fileStorage.getProvider().getClient().files()
					.listFolderBuilder(fileStorage.getRootPath() + "/" + dirPath).start();
			for (Metadata child : listing.getEntries()) {
				if (child instanceof FolderMetadata) {
					toRet.add(child.getName());
				}
			}
		} catch (DbxException e) {
			// e.printStackTrace();
			System.out.println("Greska prilikom izlistavanja direktorijuma!");
		}

		return toRet.toString();
	}

	public String listFiles(String dirPath, boolean withMetaData) {
		ArrayList<String> toRet = new ArrayList<String>();
		ListFolderResult listing;
		try {
			listing = fileStorage.getProvider().getClient().files()
					.listFolderBuilder(fileStorage.getRootPath() + "/" + dirPath).start();
			for (Metadata child : listing.getEntries()) {
				if (child instanceof FileMetadata) {
					toRet.add(child.getName());
				}
			}
		} catch (DbxException e) {
			// e.printStackTrace();
			System.out.println("Greska prilikom izlistavanja fajlova!");
		}

		return toRet.toString();
	}

	public List<File> getFilesWithExtension(String path, String extension) {

		this.clearResults();
		File[] toRet;
		RemoteFileService rm = new RemoteFileService(fileStorage);
		ListFolderResult listing;
		try {
			listing = fileStorage.getProvider().getClient().files()
					.listFolderBuilder(fileStorage.getRootPath() + "/" + path).start();
			for (Metadata child : listing.getEntries()) {
				if (child.getPathLower().contains(extension.toLowerCase())) {
					// System.out.println("lower : " + child.getPathDisplay());
					rm.downloadFile(child.getPathDisplay().substring(1, child.getPathDisplay().length()),
							"Results/" + child.getName());
				}
			}

		} catch (DbxException e) {
			// e.printStackTrace();
			System.out.println("Greska kod preuzimanja fajlova sa ekstenzijom {" + extension + "} - 1");
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("Greska kod preuzimanja fajlova sa ekstenzijom {" + extension + "} - 2");
		}

		String exten = extension;
		if (exten.contains(".")) {
			exten = exten.replace(".", "");
		}
		String[] ext = { exten };
		// System.out.println(exten);
		String tpath = FilenameUtils.separatorsToSystem("Results/");
		File file = new File(tpath);
		List<File> files = (List<File>) FileUtils.listFiles(file, ext, true);

		// TODO Da li treba da se vrati lista imena ili lista fajlova?
		return files;
	}

	public List<String> getAllFiles(boolean sorted, String dirPath) throws Exception {

		this.clearResults();
		File[] toRet;
		RemoteFileService rm = new RemoteFileService(fileStorage);
		ListFolderResult listing;
		try {
			listing = fileStorage.getProvider().getClient().files()
					.listFolderBuilder(fileStorage.getRootPath() + "/" + dirPath).start();
			for (Metadata child : listing.getEntries()) {
				if (child instanceof FileMetadata) {
					rm.downloadFile(child.getPathDisplay().substring(1, child.getPathDisplay().length()),
							"Results/" + child.getName());
				}
			}

		} catch (DbxException e) {
			// e.printStackTrace();
			System.out.println("Greska kod preuzimanja svih fajlova iz nekog direktorijuma");
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("Greska kod preuzimanja svih fajlova iz nekog direktorijuma");
		}

		String path = FilenameUtils.separatorsToSystem("Results/");
		File root = new File(path);
		// Za slucaj da se na prosledjenoj putanji ne nalazi direktorijum
		if (!root.isDirectory()) {
			throw new NotFoundException(dirPath);
		}

		List<File> files = (List<File>) FileUtils.listFiles(root, null, false);

		if (files.isEmpty()) {
			return null;
		}

		List<String> filesName = new ArrayList<String>();
		for (File f : files) {
			// System.out.println(f.getAbsolutePath());
			filesName.add(f.getName());
		}

		if (sorted) {
			Collections.sort(filesName);
		}

		return filesName;

	}

	private void clearResults() {
		File toDel = new File("Results");
		/*
		 * String[] entries = toDel.list(); System.out.println("clearResults: " +
		 * Arrays.toString(entries)); for (String s : entries) {
		 * if(s.equals(".settings")) {
		 * System.out.println("----------------------------------"); } File currentFile
		 * = new File(toDel.getPath(), s); currentFile.delete(); }
		 */
		try {
			FileUtils.cleanDirectory(toDel);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public RemoteStorage getFileStorage() {
		return fileStorage;
	}

	public void setFileStorage(RemoteStorage fileStorage) {
		this.fileStorage = fileStorage;
	}

	@Override
	public File createEmptyDirectory(String path, String fileName) throws Exception {
		// ne treba da se implementira ovde
		return null;
	}

	@Override
	public File getFilesWithMetadata(boolean withMetaData) {
		// ne treba da se implementira
		return null;
	}

}

package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Hashtable;
import java.util.List;

import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

import com.dropbox.core.DbxException;
import com.dropbox.core.v1.DbxEntry;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.WriteMode;

import exceptions.CreateException;
import exceptions.CustomException;
import exceptions.PrivilageException;
import exceptions.UploadException;
import raf.rs.FIleStorageSpi.MyFile;
import raf.rs.FIleStorageSpi.PrivilageType;

public class RemoteFileService implements MyFile {

	private RemoteStorage storage;

	public RemoteFileService(RemoteStorage storage) {
		this.storage = storage;
	}

	@Override
	public boolean delFile(String path, String fileName) throws Exception {
		if(!checkPrivilage(PrivilageType.DELETE)) {
			throw new PrivilageException("Nemate privilegiju");
		}
		
		
		try {
			Metadata metadata = storage.getProvider().getClient().files().delete(storage.getRootPath() + "/" + path);
		} catch (DbxException dbxe) {
			if (dbxe instanceof DeleteErrorException) {
				throw new CustomException("Ne postoji " + fileName + " u udaljenom skladistu!");
			}
			dbxe.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean createEmptyFile(String path, String fileName) throws Exception {
		if(!checkPrivilage(PrivilageType.CREATE)) {
			throw new PrivilageException("Nemate privilegiju");
		}
		
		
		clearResults();
		String sourcePath = FilenameUtils.separatorsToSystem("Results" + "\\" + fileName);
		File file = new File(sourcePath);
		if (!file.exists()) {
			if (!file.createNewFile()) {
				throw new CreateException("Greska prilikom kreiranja file-a " + fileName);
			}
		}

		uploadFile(sourcePath, path);

		// Files.delete(file.toPath());

		return true;
	}

	@Override
	public boolean downloadFile(String pathSource, String pathDest) throws Exception {
		if(!checkPrivilage(PrivilageType.DOWNLOAD)) {
			throw new PrivilageException("Nemate privilegiju");
		}
		
		
		String destinationPath = FilenameUtils.separatorsToSystem(pathDest);
		OutputStream downloadFile = new FileOutputStream(destinationPath);
		try {
			String path = storage.getRootPath() + "/" + pathSource;
			System.out.println("Download file path " + path);
			FileMetadata metadata = storage.getProvider().getClient().files().downloadBuilder(path)
					.download(downloadFile);
		} finally {
			downloadFile.close();
		}

		return true;
	}

	@Override
	public boolean uploadFile(String pathSource, String pathDest) throws Exception {
		if(!checkPrivilage(PrivilageType.UPLOAD)) {
			throw new PrivilageException("Nemate privilegiju");
		}
		
		String path = FilenameUtils.separatorsToSystem(pathSource);
		File file = new File(path);
		if (!file.exists()) {
			throw new FileNotFoundException("Fajl " + path + " ne postoji!");
		}

		String extension = FilenameUtils.getExtension(file.getName());
		if (storage.getForbiddenExtension().contains(extension.toLowerCase())) {
			throw new UploadException("Skladiste ne podrzava fajlove sa ekstanzijom (" + extension + ")");
		}

		// System.out.println(file.getName());
		// System.out.println(FilenameUtils.getExtension(path));

		String pathDestination = storage.getRootPath() + "/" + pathDest;
		if (pathDest.equals("") || pathDest.equals("/")) {
			pathDestination = storage.getRootPath() + "/" + file.getName();
		}
		System.out.println("PathDestionation " + pathDestination);

		try {
			InputStream in = new FileInputStream(path);
			// FileMetadata metadata =
			// storage.getProvider().getClient().files().uploadBuilder(pathDestination).uploadAndFinish(in);
			FileMetadata metadata = storage.getProvider().getClient().files().uploadBuilder(pathDestination)
					.withMode(WriteMode.OVERWRITE).uploadAndFinish(in);
		} catch (Exception e) {
			if (e instanceof UploadErrorException) {
				if (pathDest.contains("//")) {
					throw new CustomException("Pogresno navedena putanja na DropBox-u. Navesti u obliku: dir1/dir2");
				}
				e.printStackTrace();
				throw new UploadException();
			}
			if (e instanceof FileNotFoundException) {
				throw new FileNotFoundException();
			}
			throw new UploadException();
		}

		return true;
	}

	@Override
	public boolean createMultipleFiles(String path, String fileName, int numberOfFiles) throws Exception {
		if(!checkPrivilage(PrivilageType.CREATE)) {
			throw new PrivilageException("Nemate privilegiju");
		}
		
		if (numberOfFiles <= 0) {
			throw new CreateException("Prosledjeni broj mora biti pozitivan!");
		}

		for (int i = 0; i < numberOfFiles; i++) {
			String name = fileName + " " + i;
			createEmptyFile(path + "/" + name, name);
		}

		return true;
	}

	@Override
	public boolean uploadMultipleFiles(String pathDest, List<File> files) throws Exception {
		if(!checkPrivilage(PrivilageType.UPLOAD)) {
			throw new PrivilageException("Nemate privilegiju");
		}
		
		if (files.isEmpty()) {
			throw new UploadException("Lista prosledjenih fajlova je prazna!");
		}

		for (File f : files) {
			if (f.isDirectory()) {
				throw new UploadException("Prosledjeni fajl je direktorijum!");
			}
			uploadFile(f.getPath(), pathDest);
		}

		return true;
	}

	public String getFileNameFormPath(String filePath) {
		String toRet[] = filePath.split("/");
		return toRet[toRet.length - 1];
	}

	@Override
	public boolean createMetaDataFile(String filePath, Hashtable<String, String> metaData) {
		if(!checkPrivilage(PrivilageType.META)) {
			System.out.println("Nemate privilegiju");
			return false;
		}
		
		// TODO provera da je filePath file
		clearResults();
		JSONObject js = new JSONObject(metaData);
		try {
			String path = FilenameUtils.separatorsToSystem("Results/" + getFileNameFormPath(filePath) + ".metaData");
			FileWriter file = new FileWriter(path);
			PrintWriter pw = new PrintWriter(file);
			pw.append(js.toString());
			System.out.println("Successfully Copied JSON Object to File...");
			file.close();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			String temp = filePath.replace(storage.getRootPath(), "");
			String[] parts = temp.split("/");
			String destination = "";
			for (int i = 0; i < parts.length - 1; i++) {
				if (parts[i].isBlank() || parts[i].isEmpty()) {
					continue;
				}
				destination += parts[i] + "/";
			}
			destination += getFileNameFormPath(filePath) + ".metaData";
			System.out.println("DestinationPath u createMetaDataFile: " + destination);
			uploadFile("Results/" + getFileNameFormPath(filePath) + ".metaData", destination);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean addMetaData(String metaFilePath, Hashtable<String, String> metaData) {
		if(!checkPrivilage(PrivilageType.META)) {
			System.out.println("Nemate privilegiju");
			return false;
		}
		
		clearResults();
		try {
			downloadFile(metaFilePath, "Results/" + getFileNameFormPath(metaFilePath));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		JSONObject js = new JSONObject(metaData);
		System.out.println(js.toString());
		try {
			FileWriter file = new FileWriter("Results/" + getFileNameFormPath(metaFilePath), true);
			PrintWriter pw = new PrintWriter(file);
			pw.append("\n" + js.toString());
			System.out.println("Successfully Copied JSON Object to File...");
			file.close();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			delFile(metaFilePath, "");
			uploadFile("Results/" + getFileNameFormPath(metaFilePath), metaFilePath);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	@Override
	public boolean uploadArchive(String archivePath, String destPath) throws Exception {
		if(!checkPrivilage(PrivilageType.UPLOAD)) {
			throw new PrivilageException("Nemate privilegiju");
		}
		
		if (storage.getForbiddenExtension().contains("zip")) {
			throw new UploadException("Skladiste ne podrzava fajlove sa ekstanzijom {zip}");
		}
		return uploadFile(archivePath, destPath);
	}

	private void clearResults() {
		File toDel = new File("Results");
		String[] entries = toDel.list();
		for (String s : entries) {
			File currentFile = new File(toDel.getPath(), s);
			currentFile.delete();
		}
	}

	private boolean checkPrivilage(PrivilageType prv) {
		if(storage.getCurrentUser().getPrivilages().contains(prv)) return true;
		return false;
	}

}

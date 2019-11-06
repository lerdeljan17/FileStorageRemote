package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;

import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;

import exceptions.CustomException;
import exceptions.UploadException;
import raf.rs.FIleStorageSpi.MyFile;

public class MyRemoteFile extends AbstractDropBoxClient implements MyFile{
	
	private String name;
	
	public MyRemoteFile(String name) {
		this.name = name;
	}

	@Override
	public boolean delFile(String path, String fileName) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean createEmptyFile(String path, String fileName) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean downloadFile(String pathSource, String pathDest) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean uploadFile(String pathSource, String pathDest) throws Exception {
		String path = FilenameUtils.separatorsToSystem(pathSource);
		File file = new File(path);
		if(!file.exists()) {
			throw new FileNotFoundException("Fajl " + path + " ne postoji!");
		}
		
		System.out.println(file.getName());
		System.out.println(FilenameUtils.getExtension(path));
		
		//TODO da li treba proveriti da li se vec nalazi na storage-u
		//TODO proveriti da li se ekstanzija fajla nalazi u listi ekstanzija
		
		try {
			InputStream in = new FileInputStream(file.getAbsolutePath());
			FileMetadata metadata = getClient().files().uploadBuilder(pathDest + "/" + file.getName()).uploadAndFinish(in);
		} catch (Exception e) {
			if (e instanceof UploadErrorException) {
				if (pathDest.contains("//")) {
					throw new CustomException("Pogresno navedena putanja na DropBox-u. Navesti u obliku: /dir1/dir2/");
				}
				throw new UploadException();
			}
			if (e instanceof FileNotFoundException) {
				throw new FileNotFoundException();
			}
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public boolean createMultipleFiles(String path, String fileName, int numberOfFiles) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean uploadMultipleFiles(String pathDest, List<File> files, List<File> metaDataFiles) throws Exception {
		if(files.isEmpty()) {
			throw new UploadException("Lista prosledjenih fajlova je prazna!");
		}
		
		for(File f : files) {
			if(f.isDirectory()) {
				throw new UploadException("Prosledjeni fajl je direktorijum!");
			}
			uploadFile(f.getPath(), pathDest);
		}
		
		return true;
	}

	@Override
	public boolean createMetaDataFile(String FilePath, String metaFileName, Hashtable<String, String> metaData) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addMetaData(String metaFilePath, Hashtable<String, String> metaData) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean uploadArchive(String archivePath, String destPath) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean uploadFilesAsArchive(String archiveName, String destPath, List<File> filesToArchive)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

}

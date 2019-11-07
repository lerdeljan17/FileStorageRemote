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
import com.dropbox.core.v2.files.DeleteErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.UploadErrorException;

import exceptions.CreateException;
import exceptions.CustomException;
import exceptions.UploadException;
import raf.rs.FIleStorageSpi.MyFile;

public class RemoteFileService implements MyFile{
	
	private RemoteStorage storage;
	
	public RemoteFileService(RemoteStorage storage) {
		this.storage = storage;
	}

	@Override
	public boolean delFile(String path, String fileName) throws Exception {
		try {
			
			Metadata metadata = storage.getClient().files().delete(storage.getRootPath() + "/" + path);
		} catch (DbxException dbxe) {
			if(dbxe instanceof DeleteErrorException) {
				throw new CustomException("Ne postoji " + fileName + " u udaljenom skladistu!");
			}
			dbxe.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean createEmptyFile(String path, String fileName) throws Exception {
		String sourcePath = FilenameUtils.separatorsToSystem("Results" + "\\" + fileName);
		File file = new File(sourcePath);
		if(!file.exists()) {
			if(!file.createNewFile()) {
				throw new CreateException("Greska prilikom kreiranja file-a " + fileName);
			}
		}
		
		uploadFile(sourcePath, path);
		
		//TODO proveriti zasto ne brise!
		if(!file.delete()) {
			System.out.println("Nije obrisao fajl");
		}
	
	//	Files.delete(file.toPath());
		
		return true;
	}

	@Override
	public boolean downloadFile(String pathSource, String pathDest) throws Exception {
		String destinationPath = FilenameUtils.separatorsToSystem(pathDest);
		OutputStream downloadFile = new FileOutputStream(destinationPath);
		try {
			FileMetadata metadata = storage.getClient().files().downloadBuilder(storage.getRootPath() + "/" + pathSource).download(downloadFile);
		} finally {
			downloadFile.close();
		}

		return true;
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
			String pathDestination = storage.getRootPath() +"/"+ pathDest;
			if(pathDest.equals("") || pathDest.equals("/")) {
				pathDestination = storage.getRootPath() + file.getName();
			}			
			System.out.println(pathDestination);
			FileMetadata metadata = storage.getClient().files().uploadBuilder(pathDestination).uploadAndFinish(in);
		} catch (Exception e) {
			if (e instanceof UploadErrorException) {
				if (pathDest.contains("//")) {
					throw new CustomException("Pogresno navedena putanja na DropBox-u. Navesti u obliku: dir1/dir2/");
				}
				e.printStackTrace();
			//	throw new UploadException();
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
		
		if(numberOfFiles <= 0) {
			throw new CreateException("Prosledjeni broj mora biti pozitivan!");
		}
		
		for(int i = 0; i < numberOfFiles; i++) {
			String name = fileName + " " + i;
			createEmptyFile(path, name);
		}
		
		return true;
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
			uploadFile(f.getPath(), storage.getRootPath() + "/" + pathDest);
		}
		
		return true;
	}

	public String getFileNameFormPath(String filePath) {
		String toRet[] = filePath.split("/");
		return toRet[toRet.length-1];
	}
	@Override
	public boolean createMetaDataFile(String FilePath, String metaFileName, Hashtable<String, String> metaData) {
		// TODO Auto-generated method stub
		clearResults();
		JSONObject js = new JSONObject(metaData);
		try {
			FileWriter file = new FileWriter("Results/"+getFileNameFormPath(FilePath)+".metaData");
			PrintWriter pw = new PrintWriter(file);
			pw.append(js.toString());
			System.out.println("Successfully Copied JSON Object to File...");
			file.close();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			String dest = FilePath.replace("/"+metaFileName, "");
			uploadFile("Results/"+getFileNameFormPath(FilePath)+".metaData", dest+"/"+metaFileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean addMetaData(String metaFilePath, Hashtable<String, String> metaData) {
		// TODO Auto-generated method stub
		clearResults();
		try {
			downloadFile(metaFilePath, "Results/"+getFileNameFormPath(metaFilePath));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JSONObject js = new JSONObject(metaData);
		System.out.println(js.toString());
		try {
			FileWriter file = new FileWriter("Results/"+getFileNameFormPath(metaFilePath));
			PrintWriter pw = new PrintWriter(file);
			pw.append(js.toString());
			System.out.println("Successfully Copied JSON Object to File...");
			file.close();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			delFile(metaFilePath, "");
			uploadFile("Results/"+getFileNameFormPath(metaFilePath), metaFilePath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public boolean uploadArchive(String archivePath, String destPath) throws Exception {
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



}

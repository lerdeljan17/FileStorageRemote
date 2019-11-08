package raf.rs.FileStorageRemote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import com.dropbox.core.DbxApiException;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.UploadErrorException;

import model.RemoteDirectoryService;
import model.RemoteFileService;
import model.RemoteStorage;
import model.RemoteUser;

/**
 * Hello world!
 *
 */
public class App {

	public static void main(String[] args) {

		RemoteUser admin = new RemoteUser("Student", "Studentic", true);
		
		try {
			RemoteStorage rs = new RemoteStorage("", true, admin);
			rs.addForbiddenExtension(".jpg");
			admin.setFileStorage(rs);
			System.out.println("Users: " + rs.getUsers());
			admin.disconnectFromFileStorage(rs.getRootPath());
			System.out.println("Users: " + rs.getUsers());
			admin.connectToFileStorage("");
			System.out.println(rs.getUsers());
			System.out.println(rs.getForbiddenExtension());
/*			
			Hashtable<String, String> table = new Hashtable<String, String>();
			table.put("Kuca", "Kucica");
			table.put("Zgrada", "Zgradica");
			table.put("Put", "Putic");
			table.put("Skola", "Skolica");
			
			RemoteFileService rfs = new RemoteFileService(rs);
			rfs.createMetaDataFile("Deda/proba3.txt", "hashtable", table);
			rfs.addMetaData("Deda/proba3.txt.metaData", table);
			
			rfs.uploadFile("C:\\Users\\subot\\Desktop\\Konzolna aplikacija.txt", "Deda/Tetka/Konzolna aplikacija.txt");
			rfs.downloadFile("Deda/proradi", "C:\\Users\\subot\\Desktop\\Proba3.txt");
			rfs.uploadArchive("C:\\Users\\subot\\Desktop\\Desktop.zip", "");
	//		rfs.delFile("Deda/Proba3.txt", "");
			List<File> files = new ArrayList<File>();
			files.add(new File("C:\\Users\\subot\\Desktop\\App.txt"));
			files.add(new File("C:\\Users\\subot\\Desktop\\Analiza Zbirka\\MAPraktikum.pdf"));
			files.add(new File("D:\\V semestar\\Programski prevodioci\\PP Projekat 1.pdf"));
			rfs.uploadMultipleFiles("", files);
			rfs.createEmptyFile("", "deckoHajdeOladi");
			rfs.createMultipleFiles("Deda", "nocasssMiSrceeePatiii", 5);
*/
			RemoteDirectoryService rds = new RemoteDirectoryService(rs);
			rds.createEmptyDirectoryB("Deda", "Prazan");
			rds.createMultipleDirectories("Deda/Tata", "Praznjikav", 3);
			rds.delDirectory("Deda/Tata", "Praznjikav 1");
			rds.downloadDirectory("Deda", "C:\\Users\\subot\\Desktop\\PreuzetDeda.zip");
			rds.downloadDirectory("Deda", "C:\\Users\\subot\\Desktop\\PreuzetDedaBezZip");
			System.out.println("listFiles: " + rds.listFiles("Deda", false));
			System.out.println("listDirectories: " + rds.listDirectories("Deda"));
			System.out.println("getFilesWithExtension: " + rds.getFilesWithExtension("Deda", ".metaData"));
			System.out.println("getAllFiles: " + rds.getAllFiles(true, "Deda"));
			System.out.println("searchDirectory: " + Arrays.toString(rds.searchDirectory("Deda", "silvija")));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
}

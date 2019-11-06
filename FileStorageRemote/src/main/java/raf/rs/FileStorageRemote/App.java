package raf.rs.FileStorageRemote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;

import com.dropbox.core.DbxApiException;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.UploadErrorException;

import model.MyRemoteFile;
import model.RemoteStorage;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		
	/*
		File file = new File("C:\\Users\\subot\\Desktop\\root\\lazar1");
		System.out.println(file.getAbsolutePath());
		if (file.exists()) {
			System.out.println("postoji");
		}
	*/
		
		RemoteStorage rs = null;
		try {
			rs = new RemoteStorage();
		} catch (DbxException e) {
			e.printStackTrace();
		}

		MyRemoteFile mrf = new MyRemoteFile("Something");
		try {
			mrf.uploadFile("C:\\Users\\subot\\Desktop\\root\\lazar1.txt", "/Deda/Mama");
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	/*
		DbxClientV2 client = rs.getClient();

		try { // Get files and folder metadata from Dropbox root directory
			ListFolderResult result = client.files().listFolder("");
			while (true) {
				for (Metadata metadata : result.getEntries()) {
					System.out.println(metadata.getPathLower());
					if (metadata instanceof FolderMetadata) {
						FolderMetadata fm = (FolderMetadata) metadata;
						System.out.println(fm.getName());
					}
				}

				if (!result.getHasMore()) {
					break;
				}

				result = client.files().listFolderContinue(result.getCursor());
			}
		} catch (DbxException dbxe) {
			dbxe.printStackTrace();
		}
	 */
	}
}

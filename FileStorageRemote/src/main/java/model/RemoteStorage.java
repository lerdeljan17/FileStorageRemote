package model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.dropbox.core.DbxApiException;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.users.FullAccount;

import exceptions.NoSuchUserException;
import raf.rs.FIleStorageSpi.FileStorage;
import raf.rs.FIleStorageSpi.MyDir;
import raf.rs.FIleStorageSpi.User;

public class RemoteStorage extends DropBoxProvider implements FileStorage {

	private DbxClientV2 client;
	private List<String> forbiddenExtension;
	private List<User> users;
	private User currentUser;
	private String rootPath;

	public RemoteStorage(String rootPath,User user) throws DbxException {
		// TODO dodati da proveri i napravi folder ako ne postoji
		// TODO rootpath mora da se zavrsava /
		this.rootPath = rootPath;
		if(rootPath.equals("/"))this.rootPath = "";
		this.client = super.getClient();
		this.forbiddenExtension = new ArrayList<String>();
		this.users = new ArrayList<User>();
		users.add(user);
		currentUser = user;
		this.currentUser.setRootUser(true);
		System.out.println(getAccount().getName().getDisplayName());
		this.initFileStorage(this.rootPath, "", user);
	}
	
	public RemoteStorage(String rootPath,User user,String name) throws Exception {
		// TODO dodati da proveri i napravi folder ako ne postoji
		// TODO rootpath mora da se zavrsava /
		this.rootPath = rootPath;
		if(rootPath.equals("/"))this.rootPath = "";
		this.client = super.getClient();
		this.forbiddenExtension = new ArrayList<String>();
		users.add(user);
		currentUser = user;
		System.out.println(getAccount().getName().getDisplayName());
		this.openConnectionWithUser(this.currentUser);
	}
	

	public void addForbiddenExtension(String extension) {
		if (extension.contains(".")) {
			extension.replace(".", "");
		}
		this.forbiddenExtension.add(extension);
	}

	public boolean openConnectionWithUser(User user) throws Exception {
		// TODO Auto-generated method stub
		this.clearResults();
		RemoteFileService rm = new RemoteFileService(this);
		try {
			rm.downloadFile(this.rootPath+"/"+getName()+".settings", "Results/"+getName()+".settings");
			rm=null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File settings = new File("Results/"+getName()+".settings");
		String jsonStr = FileUtils.readFileToString(settings, Charset.defaultCharset());
		System.out.println(jsonStr);
		JSONObject mainObj = new JSONObject(jsonStr);
		//JSONArray schemaObject = new JSONArray(mainObj.getJSONArray("users"));
		for (int i = 0; i < mainObj.getJSONArray("users").length(); i++) {
			ArrayList<String> prv = new ArrayList<String>();
			for (Object object : mainObj.getJSONArray("users").getJSONObject(i).getJSONArray("privilages").toList()) {
				prv.add((String) object);
			}
			User newUser = new User(mainObj.getJSONArray("users").getJSONObject(i).getString("username").toString(),
					mainObj.getJSONArray("users").getJSONObject(i).getString("password").toString(),
					mainObj.getJSONArray("users").getJSONObject(i).getBoolean("isRoot"), prv);
			this.users.add(newUser);
			// System.out.println("Novi user " + newUser);
		}
		//JSONArray extensions = new JSONArray(mainObj.get("extensions"));
		for (int i = 0; i < mainObj.getJSONArray("extensions").length(); i++) {
			forbiddenExtension.add((String) mainObj.getJSONArray("extensions").getString(i));
		}
		if (this.users.contains(user) && this.users.get(this.users.indexOf(user)).getPassword().equals(user.getPassword())) {
			System.out.println("Connection established");
			return true;
		} else {
			throw new NoSuchUserException();
		}
		
		
	}
	public String getName() {
		String toRet[] = this.rootPath.split("/");
		return toRet[toRet.length-1];
	}
	private void clearResults() {
		File toDel = new File("Results");
		String[] entries = toDel.list();
		for (String s : entries) {
			File currentFile = new File(toDel.getPath(), s);
			currentFile.delete();
		}
	}
	public boolean closeConnectionWithUser(User user) {
		File settings = new File("Results\\"+getName()+".settings");
		JSONArray jsa = new JSONArray();
		//users.add(new User("laco", "slaco", null));
		//rootUser.createNewUser("laco", "slaco");
		//rootUser.revokePrivilage(new User("laco", "slaco"),"del");
		for (User o : users) {
			JSONObject jso = new JSONObject();
			jso.put("username", o.getUsername());
			jso.put("password", o.getPassword());
			jso.put("isRoot", o.isRootUser());
			jso.put("privilages", o.getPrivilages());
			jsa.put(jso);
			
		}
		JSONArray jse = new JSONArray(forbiddenExtension);
		JSONObject js = new JSONObject();
		js.put("users", jsa);
		js.put("extensions", jse);
//		for (String ext : forbiddenExtension) {
//			JSONObject js = new JSONObject();
//			js.put("extension", ext);
//		}
		FileWriter fw;
		try {
			fw = new FileWriter(settings);
			PrintWriter pw = new PrintWriter(fw);
			pw.write(jsa.toString());
			System.out.println("Successfully Copied JSON Object to File...");
			fw.close();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		RemoteFileService rm = new RemoteFileService(this);
		try {
			rm.uploadFile(settings.getPath().toString(), "");
			rm=null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public boolean initFileStorage(String rootDir, String name, User rootUser) {
			if(rootDir.equals("")) {
		try {
	            FolderMetadata folder = getClient().files().createFolder(rootDir);
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
		}
		return true;
	}

	public DbxClientV2 getClient() {
		return client;
	}

	public List<String> getForbiddenExtension() {
		return forbiddenExtension;
	}

	public void setForbiddenExtension(List<String> forbiddenExtension) {
		this.forbiddenExtension = forbiddenExtension;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}
/*
	@Override
	public boolean initFileStorage(String rootDir, String name, User rootUser) {
		// TODO Auto-generated method stub
		return false;
	}
*/
	@Override
	public void addForbiddenExtension(String extension) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean initFileStorage(String rootDir, User rootUser) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

}

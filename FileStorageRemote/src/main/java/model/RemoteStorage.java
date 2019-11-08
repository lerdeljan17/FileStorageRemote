package model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.FolderMetadata;

import exceptions.CreateException;
import exceptions.CustomException;
import exceptions.NoSuchUserException;
import raf.rs.FIleStorageSpi.FileStorage;
import raf.rs.FIleStorageSpi.PrivilageType;
import raf.rs.FIleStorageSpi.User;

public class RemoteStorage implements FileStorage {

	private String rootPath;
	private User currentUser;
	private List<User> users;
	private List<User> connectedUsers;
	private DropBoxProvider provider;
	private List<String> forbiddenExtension;

	public RemoteStorage(String rootPath, boolean initBoolean, User user) throws Exception {
		if (rootPath.equals("/")) {
			this.rootPath = "";
		} else {
			this.rootPath = rootPath;
		}
		this.forbiddenExtension = new ArrayList<String>();
		this.users = new ArrayList<User>();
		this.connectedUsers = new ArrayList<User>();
		this.users.add(user);
		this.connectedUsers.add(user);
		this.currentUser = user;
		this.provider = new DropBoxProvider();

		if (initBoolean) {
			user.setRootUser(true);
			((RemoteUser) user).setFileStorage(this);
			if (!rootPath.equals("")) {
				initFileStorage(rootPath, user);
			}
		} else {
			openConnectionWithUser(user);
		}
	}

	@Override
	public boolean initFileStorage(String rootDir, User rootUser) throws Exception {
		try {
			FolderMetadata folder = provider.getClient().files().createFolder(rootDir);
			// System.out.println(folder.getName());
		} catch (CreateFolderErrorException err) {
			if (err.errorValue.isPath() && err.errorValue.getPathValue().isConflict()) {
				throw new CreateException("Nesto vec postoji na putanji!");
			} else {
				throw new CustomException("Greska prilikom inicijalizacije skladista");
			}
		} catch (Exception err) {
			err.printStackTrace();
			throw new CustomException("Greska prilikom inicijalizacije skladista");
		}
		return true;
	}

	public boolean openConnectionWithUser(User user) throws Exception {
		this.clearResults();
		RemoteFileService rm = new RemoteFileService(this);
		try {
			rm.downloadFile(getName() + ".settings", "Results1/" + getName() + ".settings");
			rm = null;
		} catch (Exception e) {
			e.printStackTrace();
		}

		File settings = new File("Results1/" + getName() + ".settings");
		String jsonStr = FileUtils.readFileToString(settings, Charset.defaultCharset());
		// System.out.println(jsonStr);
		JSONObject mainObj = new JSONObject(jsonStr);
		// JSONArray schemaObject = new JSONArray(mainObj.getJSONArray("users"));
		for (int i = 0; i < mainObj.getJSONArray("users").length(); i++) {
			ArrayList<PrivilageType> prv = new ArrayList<PrivilageType>();
			for (Object object : mainObj.getJSONArray("users").getJSONObject(i).getJSONArray("privilages").toList()) {
				PrivilageType type = PrivilageType.valueOf((String) object);
				prv.add(type);
			}
			String username = mainObj.getJSONArray("users").getJSONObject(i).getString("username").toString();
			String password = mainObj.getJSONArray("users").getJSONObject(i).getString("password").toString();
			boolean isRootUser = mainObj.getJSONArray("users").getJSONObject(i).getBoolean("isRoot");

			User newUser = new User(username, password, isRootUser, prv);
			this.users.add(newUser);
			// System.out.println("Novi user " + newUser);
		}
		// JSONArray extensions = new JSONArray(mainObj.get("extensions"));
		for (int i = 0; i < mainObj.getJSONArray("extensions").length(); i++) {
			forbiddenExtension.add((String) mainObj.getJSONArray("extensions").getString(i));
		}

		if (!connectedUsers.contains(user)) {
			this.connectedUsers.add(user);
		}

		if (this.users.contains(user)
				&& this.users.get(this.users.indexOf(user)).getPassword().equals(user.getPassword())) {
			System.out.println("Konekcija je uspostavljena");
			return true;
		} else {
			throw new NoSuchUserException();
		}

	}

	public boolean closeConnectionWithUser(User user) {
		if (!this.connectedUsers.contains(user)) {
			return false;
		}
		// System.out.println("Metoda closeConnectionWithUser " + user);

		String path = FilenameUtils.separatorsToSystem("Results1\\" + getName() + ".settings");
		File settings = new File(path);

		JSONArray jsa = new JSONArray();
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

		FileWriter fw;
		try {
			fw = new FileWriter(settings);
			PrintWriter pw = new PrintWriter(fw);
			pw.write(js.toString());
			// System.out.println("Successfully Copied JSON Object to File...");
			fw.close();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		RemoteFileService rm = new RemoteFileService(this);
		try {
			rm.uploadFile(settings.getPath().toString(), "");
			rm = null;
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.connectedUsers.remove(user);

		return true;
	}

	public String getName() {
		String toRet[] = this.rootPath.split("/");
		return toRet[toRet.length - 1];
	}

	private void clearResults() {
		File toDel = new File("Results");
		String[] entries = toDel.list();
		for (String s : entries) {
			File currentFile = new File(toDel.getPath(), s);
			currentFile.delete();
		}
	}

	@Override
	public void addForbiddenExtension(String extension) {
		if (extension.contains(".")) {
			extension = extension.replace(".", "");
		}
		this.forbiddenExtension.add(extension.toLowerCase());
	}

	public DropBoxProvider getProvider() {
		return provider;
	}

	public void setProvider(DropBoxProvider provider) {
		this.provider = provider;
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

}

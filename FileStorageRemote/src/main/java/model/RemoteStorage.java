package model;

import java.util.ArrayList;
import java.util.List;

import com.dropbox.core.DbxApiException;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;

import raf.rs.FIleStorageSpi.FileStorage;
import raf.rs.FIleStorageSpi.MyDir;
import raf.rs.FIleStorageSpi.User;

public class RemoteStorage extends DropBoxProvider implements FileStorage {

	private DbxClientV2 client;
	private List<String> forbiddenExtension;
	private List<User> users;
	private User currentUser;
	private String rootPath;

	public RemoteStorage(String rootPath) throws DbxException {
		// TODO dodati da proveri i napravi folder ako ne postoji
		// TODO rootpath mora da se zavrsava /
		this.rootPath = rootPath;
		this.client = super.getClient();
		this.forbiddenExtension = new ArrayList<String>();
		System.out.println(getAccount().getName().getDisplayName());
	}

	public void setForbiddenExtension(String extension) {
		if (!forbiddenExtension.contains(extension)) {
			forbiddenExtension.add(extension);
		}
	}

	public boolean openConnectionWithUser(User user) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean closeConnectionWithUser(User user) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean initFileStorage(MyDir rootDir, String name, User rootUser) {
		
		return false;
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

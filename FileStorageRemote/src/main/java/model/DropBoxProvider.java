package model;

import com.dropbox.core.DbxApiException;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;

import java8.util.Objects;

abstract class DropBoxProvider {
	
	private static final String ACCESS_TOKEN = "4dCicgzo2sAAAAAAAAAADbZ3DJO8Bceev5b7i81cIEr99APC7hsJbez5ZcvGKTNt";

	private DbxClientV2 client;
	private FullAccount account;

	private void connect() {
		DbxRequestConfig config = DbxRequestConfig.newBuilder("StorageRemote").build();
		this.client = new DbxClientV2(config, ACCESS_TOKEN);
	}
	
	protected DbxClientV2 getClient() {
		if(Objects.isNull(client)) {
			connect();
		}
		return client;
	}

	
	protected FullAccount getAccount() {
		if(Objects.isNull(client)) {
			connect();
		}
		try {
			this.account = client.users().getCurrentAccount();
		} catch (DbxApiException e) {
			e.printStackTrace();
		} catch (DbxException e) {
			e.printStackTrace();
		}
		
		return this.account;
	}
}

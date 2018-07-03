package Learning.com.learning.objectResources;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

//import org.glassfish.grizzly.http.server.Response;

import DBUtils.DBConnection;
import DBUtils.MemCacheDB;
import constants.ReturnCode;
import constants.Constants;

public class UserService {
	private String username;
	private String passwd;
	private MemCacheDB mcc;
	public UserService(String username, String password) throws SQLException{
		this.username = username;
		this.passwd = password;
		mcc = MemCacheDB.getInstance();
	}
	
	public boolean isUserExist(){
		String sqlcmd = "select name from " + Constants.CRED_TABLE + " where name=\'" + this.username + "\'";
		String userKey = this.username + "_username";
		boolean exist = false;
		try{
			if(mcc.keyExist(userKey)){
				return true;
			} else {
				ResultSet rs = mcc.getDbcon().query(sqlcmd);
				if(rs.next()){
			    	exist = true;
			    }
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return exist;
	}
	
	public boolean authenticate(){
		boolean valid = false;
		String sqlcmd = "select password from " + Constants.CRED_TABLE + " where name=\'" + this.username + "\'";
		String userKey = this.username + "_password";

		try{
			if(mcc.keyExist(userKey)){
				String passwd = mcc.getValue(userKey).toString();
				if(passwd.equals(this.passwd)){
					valid = true;
				}
			}else {
			    ResultSet rs = mcc.getDbcon().query(sqlcmd);
			    if(rs.next()){
				    String passwd = rs.getString("password");
				    if(passwd.equals(this.passwd)){
					    valid = true;
				    }
			    }
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		return valid;
	}

	public Response login(){
		try{
			if(authenticate()){
				Token tokenInst = new Token(this.username);
				String token = tokenInst.getToken();
				JSONObject resp = new JSONObject();
				resp.put("test-token", token);
				//return Response.ok(resp.toString()).build();
				return Response.ok().header("test-token", token).build();
			} else {
				return Response.status(ReturnCode.INVALID_USER).build();
			}
		}catch(JSONException e){
			e.printStackTrace();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return Response.status(ReturnCode.GENERAL_ERR).build();
		
	}

}
package org.area515.security.accesscontrol;

import java.io.File;
import java.io.IOException;
import java.util.Properties;


public class HostProperties {

	private static HostProperties m_instance = null;
	public static HostProperties Instance() throws IOException {
		if (m_instance == null) {
			m_instance = new HostProperties();
		}
		return m_instance;
	}
	
	public HostProperties() throws IOException{HostProperties.init();}
	
//	static String membersFile = "";
//	public String getMembersFile(){
//		return membersFile;
//	}
	
	static String aclFile = "";
	public String getAclFile(){
		return aclFile;
	}
	
	
	public static void init() throws IOException{
		aclFile = new File(System.getProperty("user.home"),"rfid/rfidreader/acl").getAbsolutePath();
		System.out.println(System.getProperty("user.home"));
		
		if(new File(aclFile).exists()){throw new IOException("ACL file doesn't exist at: " + new File(aclFile).getAbsolutePath());}
		
	}
	
	public static Properties getHostProperties() throws IOException{
		Properties properties = new Properties();
		properties.load(HostProperties.class.getClassLoader().getResourceAsStream("config.properties"));
		return properties;
	}
	
}

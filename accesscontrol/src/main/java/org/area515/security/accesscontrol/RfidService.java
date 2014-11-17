package org.area515.security.accesscontrol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;

@Path("rfid")
public class RfidService {

	
	// boolean Contains rfid(rfid)
	@POST
    @Path("query/")
    @Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed("read")
	public boolean containsRfid(Rfid rfid) throws IOException{
		return exists(rfid.getKey());
	}
	
	private boolean exists(String key) throws IOException{
		List<Rfid> rfids = getAcl();
		System.out.println("acl size: "+ rfids.size());
		for(Rfid rfid : rfids){
			System.out.println("RFID: " + rfid.toString());
			if(rfid.key.equals(key)){return true;}
		}
		return false;
	}
	
	@POST
	@Path("add/")
	@RolesAllowed("write")
	@Consumes("application/json")
	@Produces("application/json")
	public MachineResponse addRfid(List<Rfid> rfids) throws IOException{
		
		if(rfids == null || rfids.isEmpty()){
			return new MachineResponse("add",false,"Error: List of rfids is empty.  Cannot add rfids.");
		}
		
		for(Rfid rfid : rfids){
			if(exists(rfid.getKey())){
				return new MachineResponse("add", false, "Error: key "+rfid.getKey()+"exists already.  No add operation attempted");
			}
		}
		
		ArrayList<String> newRfids = new ArrayList<String>();
		for(Rfid rfid : rfids){
			newRfids.add(rfid.toString());
		}
		
		try{
			FileUtils.writeLines(new File(HostProperties.Instance().getAclFile()), newRfids, true);
			return new MachineResponse("add", true, newRfids.toString());
		}
		catch(Exception ex){
			return new MachineResponse("add",false,ex.getMessage());
		}
		
	}
	@POST
	@Path("delete/")	
	@RolesAllowed("delete")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public MachineResponse deleteRfid(List<Rfid> incoming) throws IOException{
	
		if(incoming == null || incoming.isEmpty()){
			return new MachineResponse("delete", false, "Error, rfids received.  Cannot delete.");
		}
		for(Rfid rfid : incoming){
			if(!exists(rfid.getKey())){
				return new MachineResponse("delete", false, "Key " + rfid.getKey() + " does not exist.  No delete operations attempted.");
			}
		}
		// Get the acl list
		List<Rfid> rfids = getAcl();
		
		// redo naming
		try{
			// Iterate over the acl list
			Iterator<Rfid> itr = rfids.iterator();
			while (itr.hasNext()){
				Rfid aclRfid = itr.next();
				// iterate over all the rfids that we are going to delete
				for(Rfid rfidToDelete : incoming){
					// delete the rfid
					if(aclRfid.key.equals(rfidToDelete.getKey())){
						itr.remove();
					}
				}
			}
		
			// Convert the remaining rfids to a collection of strings
			ArrayList<String> outData = new ArrayList<String>();
			for(Rfid rfid : rfids){
				outData.add(rfid.toString());
			}
		
			File outFile = new File(HostProperties.Instance().getAclFile());
			
			// overwrite the new data to the acl file
			FileUtils.writeLines(outFile, outData, false);
			
			return new MachineResponse("delete", true,"");
		}catch(Exception ex){
			return new MachineResponse("delete", false,ex.getMessage());
		}
		
		
	}
	
	@GET
	@Path("queryall")
	@RolesAllowed("read")
    @Produces(MediaType.APPLICATION_JSON)
	public List<Rfid> queryAll() throws IOException{
		return getAcl();
	}
	// List<Rfid>getAll()
	private List<Rfid> getAcl() throws IOException{
		File aclFile = new File(HostProperties.Instance().getAclFile());
		ArrayList<Rfid> acl = new ArrayList<Rfid>();
		for(String line : FileUtils.readLines(aclFile)){
			String[] rawRfid = line.split("\\|");
			//0=id
			//1=key
			// Debug

			acl.add(new Rfid(rawRfid[1],rawRfid[0]));
		}
		return acl;
	}
	
//	// boolean Contains rfid(rfid)
//	@GET
//    @Path("exists/{key}")
//    @Produces(MediaType.TEXT_PLAIN)
//	@RolesAllowed("read")
//	public boolean containsRfid(@PathParam("key") String key) throws IOException{
//		return exists(key);
//	}
	
//	// void Add rfid (rfid)
//	@GET
//	@Path("add/{id}/{key}")
//	@RolesAllowed("write")
//	@Produces(MediaType.APPLICATION_JSON)
//	public MachineResponse addRfid(@PathParam("id") String id, @PathParam("key") String key) throws IOException{
//		
//		if(id == null | id.isEmpty()){
//			return new MachineResponse("add", false, "Error: id is empty.  Cannot add the new rfid");
//		}
//		if(key == null | key.isEmpty()){
//			return new MachineResponse("add", false, "Error: key is empty.  Cannot add the new rfid");
//		}
//		
//		if(exists(key)){
//			return new MachineResponse("add", false, "Error: key "+key+"exists already.  Cannot add the new rfid");
//		}
//		
//		try{
//			String newLine = id + "|" + key;
//			FileUtils.writeStringToFile(new File(HostProperties.Instance().getAclFile()), newLine, true);
//			return new MachineResponse("add", true, "");
//		}
//		catch(Exception ex){
//			return new MachineResponse("add",false,ex.getMessage());
//		}
//	}
	
	
//	@GET
//	@Path("delete/{key}")
//	@RolesAllowed("delete")
//	@Produces(MediaType.APPLICATION_JSON)
//	public MachineResponse deleteRfid(@PathParam("key") String key) throws IOException{
//	
//		if(key == null || key.isEmpty()){
//			return new MachineResponse("delete", false, "Error, no key received.  Cannot delete key.");
//		}
//		if(!exists(key)){
//			return new MachineResponse("delete", false, "Key given does not exist.  Cannot delete key.");
//		}
//		
//		try{
//			List<Rfid> rfids = getAcl();
//			Iterator<Rfid> itr = rfids.iterator();
//			while (itr.hasNext()){
//				Rfid rfid = itr.next();
//				if(rfid.key.equals(key)){
//					itr.remove();
//				}
//			}
//			
//			ArrayList<String> outData = new ArrayList<String>();
//			for(Rfid rfid : rfids){
//				outData.add(rfid.toString());
//			}
//			File outFile = new File(HostProperties.Instance().getAclFile());
//			
//			// overwrite the new data to the acl file
//			FileUtils.writeLines(outFile, outData, false);
//			
//			return new MachineResponse("delete", true,"");
//		}catch(Exception ex){
//			return new MachineResponse("delete", false,ex.getMessage());
//		}
//		
//		
//	}
//	
	
//	// The members file was used only to develop the rfid service
//	// The getMembers is not used
//	@SuppressWarnings("unused")
//	private List<Member> getMembers() throws IOException{
//		File membersFile = new File(HostProperties.Instance().getMembersFile());
//		ArrayList<Member> members = new ArrayList<Member>();
//		for(String line : FileUtils.readLines(membersFile)){
//			String[] rawMember = line.split("\\|");
//			/*
//			 * 0=id
//			 * 1=name
//			 * 2=email
//			 * 3=key
//			 */
//			members.add(new Member(rawMember[1],rawMember[2], new Rfid(rawMember[3],rawMember[0])));
//		}
//		return members;
//	}
	
//	@GET
//    @Produces("text/plain")
//    @Path("/cliche")
//    public Response getClichedMessage(@Context HttpServletRequest request) {
////        String roles = request.getAuthType();   
//        
//		return Response.
//                    ok().
//                    entity("Sending \"Hello World\" to user \"" + request.getUserPrincipal().getName() + "\"").
//                    build();
//
//    }
}

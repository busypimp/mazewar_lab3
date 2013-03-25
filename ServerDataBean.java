import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class ServerDataBean implements Serializable{
	
	HashMap nameToConnMap;
	private int seqNumber = 0;
	
	public ServerDataBean(){
		this.nameToConnMap = new HashMap();
	}
	
	public synchronized void addClientDetails(String name, ConnectionDetails details){
		if(!this.nameToConnMap.containsKey(name)){
			this.nameToConnMap.put(name, details);
		}else{
			System.err.println("Name already in the map!!");
		}
	}
	
	public synchronized HashMap getNameToConnMap(){
		return this.nameToConnMap;
	}
	
	public synchronized int getSeqNum(){
		int retValue = this.seqNumber;
		this.seqNumber++;
		return retValue;
	}
	
	public synchronized int getSeqSnapshot(){
		int retValue = this.seqNumber;
//		this.seqNumber++;
		return retValue;
	}
}

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class ServerDataBean implements Serializable{
	
	Map nameToConnMap;
	
	public ServerDataBean(){
		this.nameToConnMap = new HashMap();
	}
	
	public synchronized void addClientDetails(String name, String host, int port){
		if(!this.nameToConnMap.containsKey(name)){
			this.nameToConnMap.put(name, new ConnectionDetails(port, host));
		}else{
			System.err.println("Name already in the map!!");
		}
	}
}

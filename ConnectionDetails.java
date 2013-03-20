import java.io.Serializable;


public class ConnectionDetails implements Serializable {
	private int port;
	private String hostname;
	
	public ConnectionDetails(int portIn, String hostIn){
		this.port = portIn;
		this.hostname = hostIn;
	}
	
	public int getPortNumber(){
		return this.port;
	}
	
	public String getHostName(){
		return this.hostname;
	}
}

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MazewarServerHandlerThread extends Thread{
	
//	private BlockingQueue<ClientPacket> clientQueue = new ArrayBlockingQueue<ClientPacket>(10);
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Socket sock;
	private ServerDataBean gameData;
	private ClientPacket packetFromClient;
	
	public MazewarServerHandlerThread(Socket socket, int clientId, ServerDataBean serverData) {
		this.sock = socket;
		this.gameData = serverData;

	}
	
	

	public void run(){
		try {
			this.out = new ObjectOutputStream(sock.getOutputStream());
			this.in = new ObjectInputStream(sock.getInputStream());
			while((packetFromClient = (ClientPacket) in.readObject()) != null){
				switch(packetFromClient.type){
				case ClientPacket.CLIENT_REQUEST_PLAYERS:
					sendPlayerList();
					break;
				case ClientPacket.CLIENT_REGISTER:
					registerClient();
					break;
				default:
					System.err.println("Not a valid pakcet type!!");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}



	private void registerClient() throws IOException {
		// TODO Auto-generated method stub
		
		String name = packetFromClient.clientName;
		ConnectionDetails connDeets = packetFromClient.connDetails;
		
		this.gameData.addClientDetails(name, connDeets);
		
		ClientPacket packToClient = new ClientPacket();
		packToClient.type = ClientPacket.SERVER_ACKNOWLEDGE;
		sendToClient(packToClient);
		
	}



	private void sendPlayerList() throws IOException {
		// TODO Auto-generated method stub
		ClientPacket packetToClient = new ClientPacket();
		packetToClient.type = ClientPacket.SERVER_RESPOND_PLAYERS;
		packetToClient.serverData = this.gameData;
		
		sendToClient(packetToClient);
		
		
	}
	
	private void sendToClient(ClientPacket packToClient) throws IOException{
		out.writeObject(packToClient);
	}


}
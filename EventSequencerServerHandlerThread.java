import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class EventSequencerServerHandlerThread extends Thread{
	private Socket socket = null;
	public int sequenceID = 0;
	
	//Please don't make me regret this
	private ClientPacket packetFromClient;
	private ObjectInputStream fromClient;
	public ObjectOutputStream toClient;

	public EventSequencerServerHandlerThread(Socket sock, int seqID) {//, int clientID) {
		super("EventSequencerServerHandlerThread");
		this.socket = sock;
		this.sequenceID = seqID;
	}
	
	public void run(){

		/* Dealing with the packet received from the client */
		try {
			fromClient = new ObjectInputStream(socket.getInputStream());
			
			/* stream to write back to client */
			toClient = new ObjectOutputStream(socket.getOutputStream());
			
			while (( packetFromClient = (ClientPacket) fromClient.readObject()) != null) {
				switch (packetFromClient.type){
					case ClientPacket.CLIENT_EVENT_SEQ:
						sendSequence();
						break;
					default:
						System.err.println("ERROR: Unknown packet!!");
						System.exit(-1);
				}
			}
			fromClient.close();
			toClient.close();
			socket.close();
			
		} catch (IOException e) {
			System.err.println("ERROR: Couldn't get I/O for the connection.");
			e.printStackTrace();
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err.println("ERROR: Couldn't get class for the packet server sent.");
			System.exit(1);
		}
	}

	private synchronized void sendSequence() throws IOException {
		ClientPacket packetToClient = new ClientPacket();
		packetToClient.type = ClientPacket.SERVER_RESPOND_SEQ;
		packetToClient.event = packetFromClient.event;
		packetToClient.clientName = packetFromClient.clientName;
		packetToClient.sequence = sequenceID;
		
		sendToClient(packetToClient);
	}
	
	public synchronized void sendToClient(ClientPacket pack) throws IOException{
		this.toClient.writeObject(pack);
	}

}
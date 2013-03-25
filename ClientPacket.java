import java.io.Serializable;

/**
 * 
 * @author rachittyagi
 *
 */
public class ClientPacket implements Serializable {

	public static final int CLIENT_REGISTER = 100;
	public static final int CLIENT_REQUEST_PLAYERS = 101;
	public static final int CLIENT_REQUEST_QUEUE = 102;
	public static final int CLIENT_EVENT_REQUEST = 103;
	public static final int CLIENT_ACK = 104;
	public static final int CLIENT_EVENT_SEQ = 105;
	
	public static final int SERVER_RESPOND_PLAYERS = 106;
	public static final int SERVER_RESPOND_QUEUE = 107;
	public static final int SERVER_ACKNOWLEDGE = 108;
	public static final int SERVER_EVENT_BROADCAST = 109;
	public static final int SERVER_RESPOND_SEQ = 110;

	public static final int GENERAL_NULL = 111;
	public static final int GENERAL_BYE = 112;
	
	public static final int CLIENT_REQUEST_ORIENTATION = 113;
	public static final int CLIENT_RESPOND_ORIENTATION = 114;

	//TODO add error codes

	public int type = GENERAL_NULL;
	public int errorCode = -1;
	public String message;
//	public ClientEvent event;
	public ServerDataBean serverData;
	public int event = 0;
	public int sequence = 0;
//	public ServerDataBean serverData;
	public String clientName = null;
	public ConnectionDetails connDetails;
	public ClientInformation info;
}

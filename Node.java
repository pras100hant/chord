import java.util.*;
import java.io.*;
import java.rmi.registry.Registry;  
import java.rmi.registry.LocateRegistry; 
import java.rmi.server.UnicastRemoteObject; 
// import java.io.DataInputStream;
// import java.io.DataOutputStream;
// import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Node implements ChordIntf{
	NodeInfo predecessor;
	NodeInfo successor;
	NodeInfo n;
	NodeInfo finger[] = new NodeInfo[m];
	int next = 0;

	public static boolean is_within(int x, int l, int r){
		if(l>r){
			if(x<r){
				x = x + N;
			}
			r = r + N;
		}
		if(x>l && x<r)
			return true;
		else return false;
	}

	public NodeInfo find_successor(int i){

		// if(i>n.node_no && i<= successor.node_no)
		// 	return new NodeInfo(successor);

		if(is_within(i,n.node_no,successor.node_no) || i==successor.node_no)
			return new NodeInfo(successor);
		NodeInfo n_d = closest_preceding_node(i);
		if(n_d.node_no==n.node_no){
			// System.out.println("inside find successor: "+n.node_no+", "+i);
			return new NodeInfo(n);
		}
		try{
			Registry registry = LocateRegistry.getRegistry(n_d.ip_addr, n_d.port_no);
			ChordIntf stub = (ChordIntf) registry.lookup("ChordIntf");
			return stub.find_successor(i);
		} catch(Exception e){
			System.out.println("problem in find_successor"+e);
			e.printStackTrace();
		}

		return new NodeInfo(n.node_no, n.ip_addr, n.port_no);
	}
	public NodeInfo closest_preceding_node(int id){
		// for i=m downto 1{
		// 	if(finger[i] in (n,id])
		// 		return finger[i]
		// }
		for(int i=m-1;i>=0;i--){
			if(is_within(finger[i].node_no, n.node_no, id)){
				return new NodeInfo(finger[i]);
			}
		}
		return new NodeInfo(n.node_no, n.ip_addr, n.port_no);


	}

	//join(n_dash, n_dash_ip_addr, n_dash_port_no){
	public boolean join(NodeInfo n_dash)// throws InterruptedException
	{

		for(int i=0;i<3;i++) {
			try{
				predecessor = null;
				//stub = ChordIntf stub of n_dash with (n_dash.ip_addr, n_dash.port_no)
				Registry registry = LocateRegistry.getRegistry(n_dash.ip_addr, n_dash.port_no);
				ChordIntf stub = (ChordIntf) registry.lookup("ChordIntf");
				//System.out.println("hey " + successor.node_no);
				successor = stub.find_successor(n.node_no);
				System.out.println("inside join succ:" + successor.node_no);
				return true;
			} catch (Exception e){
				//Thread.sleep(1000);
			}
		}
		try{
		// stub = TrackerIntf of Tracker at 8000
			Registry registry_1 = LocateRegistry.getRegistry("localhost", 8000);
			TrackerIntf trac_stub = (TrackerIntf) registry_1.lookup("TrackerIntf");
			trac_stub.node_failure_update(n_dash.node_no);
			return false;
		} catch(Exception e){
			System.out.println("Can't join");
		}
		return false;
	}
	public NodeInfo get_predecessor(){
		return new NodeInfo(predecessor);
	}

	public void stabilize(){
		// stub = ChordIntf rmi of successor with (successor.ip_addr, successor.port_no)
		// x = stub.get_predeceessor() // get_predecessor will return (int,ip,port)
		// if( x.node_no in (n.node_no, successor.node_no))
		// 	successor = x
		// new stub for successor 
		// stub.notify(n)	

		try{
			Registry registry = LocateRegistry.getRegistry(successor.ip_addr, successor.port_no);
			ChordIntf stub = (ChordIntf) registry.lookup("ChordIntf");
			NodeInfo x =  stub.get_predecessor();
			if(x!=null){
				if(is_within(x.node_no, n.node_no, successor.node_no) || n.node_no==successor.node_no){
					System.out.print("hello stabilize");
					successor = x;
				}
			}
			registry = LocateRegistry.getRegistry(successor.ip_addr, successor.port_no);
			stub = (ChordIntf) registry.lookup("ChordIntf");
			stub.notify(n);
		} catch(Exception e) {
			System.out.println("Can't stabilize..."+e.toString());
			e.printStackTrace(); 
		}
	}
	
	public void notify(NodeInfo n_dash){
		
		if(predecessor==null || is_within(n_dash.node_no, predecessor.node_no, n.node_no))
					 //|| predecessor.node_no==n.node_no) 
		{
			System.out.println("hi notify");
			predecessor = n_dash;
		}
	}

	public void fix_fingers(){
		next = next + 1;
		if(next >= m)
			next = 0;
		//finger[next] = find_successor(n.node_no + 2^(next-1))
		finger[next] = find_successor( (n.node_no+(1<<next)) & ((1<<m)-1) );
	}

	public void check_predecessor(){
		// try{
		// 	stub = ChordIntf with (ip_addr, port_no)
		// 	predecessor = stub.get_predecessor()
		// }catch{
		// 	predecessor = nil
		// }
		if(predecessor!=null){
			try{
				Registry registry = LocateRegistry.getRegistry(predecessor.ip_addr, predecessor.port_no);
				ChordIntf stub = (ChordIntf) registry.lookup("ChordIntf");
			}catch(Exception e) {
				predecessor = null;
				System.out.println("Predecessor down..."+e);
				//e.printStackTrace(); 
				try{
					Registry registry = LocateRegistry.getRegistry("localhost", 8000);
					TrackerIntf stub = (TrackerIntf) registry.lookup("TrackerIntf");  
					stub.node_failure_update(predecessor.node_no);
				} catch (Exception ex){
					System.out.println("Error while informing node failure.");
				}
			}
		}
	}

	public NodeInfo lookup(int peer){
		int i;
		if(peer == n.node_no)
			return n;
		for(i = m-1;i>=0;i--){
			if(finger[i].node_no == peer)
				return finger[i];
			else if(is_within(peer, finger[i].node_no, n.node_no))
				break;
		}

		NodeInfo n_d = finger[i];
		try{
			Registry registry = LocateRegistry.getRegistry(n_d.ip_addr, n_d.port_no);
			ChordIntf stub = (ChordIntf) registry.lookup("ChordIntf"); 
			//stub = ChordIntf rmi of successor with (peer.ip_addr, peer.port_no)
			return stub.lookup(peer);
		} catch (Exception e) {
			System.err.println("Can't talk to peer due to lookup" + e); 
			e.printStackTrace(); 
		}
		return null;
		
	}

	public void display_peers(ArrayList<Integer> peers){
		for(int i=0;i<peers.size();i++){
			System.out.print(peers.get(i)+"  ");
		}
		System.out.println();
	}

	public void printMsg(int node_no){
		System.out.println("This is a peer-to-peer ping from node no. " +node_no);
	}

	
	public void  start_server(int listening_port){
		
		try{
			new Server(listening_port).start();
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	
	private static DataOutputStream dataOutputStream = null;
	private static DataInputStream dataInputStream = null;

	public void talk_to_peer(int peer_no, int listening_port, String filename){

		//   	DO SOMETHING LIKE CHATTING, FILE TRANSFER, ETC.
		
		// I am just gonna call a printMsg on the peer
		try{
			NodeInfo peer = lookup(peer_no);
			Registry registry = LocateRegistry.getRegistry(peer.ip_addr, peer.port_no);
			ChordIntf stub = (ChordIntf) registry.lookup("ChordIntf"); 
			//stub = ChordIntf rmi of successor with (peer.ip_addr, peer.port_no)
			stub.printMsg(n.node_no);
			stub.start_server(listening_port);
			Socket socket = new Socket("localhost", listening_port);
			dataInputStream = new DataInputStream(socket.getInputStream());
			dataOutputStream = new DataOutputStream(socket.getOutputStream());

			System.out.println("Sending the File to the peer");
			// Call SendFile Method
			//sendFile("/home/dachman/Desktop/Program/gfg/JAVA_Program/File Transfer/txt.pdf");

			sendFile(filename);
			dataInputStream.close();
			dataInputStream.close();
		} catch (Exception e) {
			System.err.println("Can't talk to peer due to talk_to_peer" + e); 
			e.printStackTrace(); 
		}
	}
	private static void sendFile(String path)throws Exception
	{
		int bytes = 0;
		// Open the File where he located in your pc
		File file = new File(path);
		FileInputStream fileInputStream = new FileInputStream(file);

		// Here we send the File to Server
		dataOutputStream.writeLong(file.length());
		// Here we break file into chunks
		byte[] buffer = new byte[4 * 1024];
		while ((bytes = fileInputStream.read(buffer))!= -1) {
			// Send the file to Server Socket
			dataOutputStream.write(buffer, 0, bytes);
			dataOutputStream.flush();
		}
		// close the file here
		fileInputStream.close();
	}

	public void print_state(){

		System.out.println("Node no: "+n.node_no+ "   pred:"+(predecessor==null?"nil":predecessor.node_no)
									+" succ: "+successor.node_no);
		for(int i=0;i<m;i++)
			System.out.println("finger["+i+"]: "+finger[i].node_no);
		System.out.println();
	}

	public static void main(String args[]) throws IOException,InterruptedException{
		Node nd = new Node();
		nd.n = new NodeInfo();

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		nd.n.ip_addr = "localhost";

		System.out.print("Enter rmi port no. for this node:");
		nd.n.port_no = Integer.parseInt(br.readLine());

		System.out.print("Enter node no. for this node:");
		nd.n.node_no = Integer.parseInt(br.readLine());


		//Node nd = new Node()
		//n.port_no = input rmi port no this node //e.g. 9001, 9002, etc.
		//start 'ChordIntf' rmi at this n.port_no with object nd


		Registry registry,registry_1;
		TrackerIntf stub;

		try { 

			registry = LocateRegistry.getRegistry("localhost", 8000);
			stub = (TrackerIntf) registry.lookup("TrackerIntf");  

			registry_1 = LocateRegistry.getRegistry(nd.n.port_no); 
			ChordIntf this_node_stub = (ChordIntf) UnicastRemoteObject.exportObject(nd, 0);  
			registry_1.bind("ChordIntf", this_node_stub);  
			System.err.println("Node ready...");
		// } catch (Exception e) { 
		// 	System.err.println("Node exception: " + e.toString()); 
		// 	e.printStackTrace(); 
		// } 

		//stub = TrackerIntf of Tracker at 8000

		// try {  
			
		// } catch (Exception e) {
		// 	System.err.println("Client exception: " + e.toString()); 
		// 	e.printStackTrace(); 
		// } 
			nd.n.node_no = stub.create(nd.n.node_no, nd.n.ip_addr, nd.n.port_no);    //own ip addr
			System.out.println("This is node no. "+ nd.n.node_no);
			NodeInfo n_dash = stub.get_available_node();
			System.out.println("Got one available node on ring (on first trial) :"+n_dash.node_no);
			nd.predecessor = null;
			nd.successor = new NodeInfo(nd.n.node_no, nd.n.ip_addr, nd.n.port_no);
			for (int i=0;i<m;i++ ) {
				nd.finger[i] = new NodeInfo(nd.n);
				//nd.finger[i] = new NodeInfo(n_dash);
			}
			if(nd.n.node_no!=0){
				nd.join(n_dash);
				// while(nd.join(n_dash) == false){
				// 	n_dash = stub.get_available_node();
				// 	//nd.predecessor = new NodeInfo(nd.n);
				// 	System.out.println("Trying to join");
				// }
			}
			System.out.println("succ:"+nd.successor.node_no);

		} catch (Exception e) {
				System.err.println("Client exception: " + e.toString()); 
				e.printStackTrace(); 
		}

		try{
			//System.out.println("hey");
			Thread t = new Conversation(nd);
			t.start();
			int wait_time = 4000;
			Thread.sleep(wait_time);
			while(true){
				//nd.print_state();
				
				//Thread.sleep(wait_time);
				nd.stabilize();
				//Thread.sleep(wait_time);

				//for(int i=0;i<m;i++)
				nd.fix_fingers();

				nd.check_predecessor();

				Thread.sleep(wait_time);
				
			}
		}catch(Exception e){
			System.out.println("Thread prolem :" +e);
			e.printStackTrace(); 
		}
	}
}

class Server extends Thread{
	
	int listening_port;

	private static DataOutputStream dataOutputStream = null;
	private static DataInputStream dataInputStream = null;

	public Server(int l_port){
		listening_port = l_port;
	}

	public void run(){
		try {
				ServerSocket serverSocket = new ServerSocket(listening_port);
				System.out.println("Server is Starting in Port" + listening_port);
				// Accept the Client request using accept method
				Socket clientSocket = serverSocket.accept();
				System.out.println("Connected");
				dataInputStream = new DataInputStream(
					clientSocket.getInputStream());
				dataOutputStream = new DataOutputStream(
					clientSocket.getOutputStream());
				// Here we call receiveFile define new for that
				// file
				receiveFile("NewFile");

				dataInputStream.close();
				dataOutputStream.close();
				clientSocket.close();
		} catch (Exception e) {
				e.printStackTrace();
		}
	}

	
	private static void receiveFile(String fileName) throws Exception
	{
		int bytes = 0;
		FileOutputStream fileOutputStream = new FileOutputStream(fileName);

		long size = dataInputStream.readLong(); // read file size
		byte[] buffer = new byte[4 * 1024];
		while (size>0 && (bytes=dataInputStream.read(buffer,0,(int)Math.min(buffer.length, size)))!=-1){
			// Here we write the file using write method
			fileOutputStream.write(buffer, 0, bytes);
			size -= bytes; // read upto file size
		}
		// Here we received file
		System.out.println("File is Received");
		fileOutputStream.close();
	}		

}

class Conversation extends Thread{
	Node nd;
	Conversation(Node node){
		nd=node;
	}
	public void run(){
		int peer;
		ArrayList<Integer> peers;
		Registry registry;
		TrackerIntf stub;
		while(true){
			
			try{

				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

				System.out.print("Enter 0 to refresh peers 1 to communicate to a peer: ");
				int comm = Integer.parseInt(br.readLine());
				switch(comm){
				case 0:
					registry = LocateRegistry.getRegistry("localhost", 8000);
					stub = (TrackerIntf) registry.lookup("TrackerIntf");  
					peers = stub.get_peers();
					nd.display_peers(peers);
					break;
				case 1:
					System.out.print("Which peer no. to talk with :");
					peer = Integer.parseInt(br.readLine());
					System.out.print("at which the receiving peer should listen :");
					int listening_port = Integer.parseInt(br.readLine());
					System.out.print("Enter the filename to send :");
					String filename = br.readLine();
					nd.talk_to_peer(peer,listening_port,filename);
				}
			} catch(Exception e){
				System.out.println("Exception in Conversation.");
				e.printStackTrace();
			}
		}

	}
}


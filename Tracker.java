import java.util.*;
import java.rmi.registry.Registry;  
import java.rmi.registry.LocateRegistry; 
import java.rmi.server.UnicastRemoteObject; 

public class Tracker implements TrackerIntf { 
	int cnt = 0;
	NodeInfo arrayOfNodes[] = new NodeInfo[N];
	boolean isAlive[] = new boolean[N];

	public NodeInfo get_available_node(){
		//just for testing
		// int i=cnt-1;
		// while(isAlive[i]==false){
		// 	i--;
		// } 
		int last_node_no = (1<<m)-1;
		return arrayOfNodes[last_node_no];
	}

	public ArrayList<Integer> get_peers(){



		/// TO GET A LIST OF PEERS
		ArrayList<Integer> peers = new ArrayList<Integer>();	
		for(int i=0;i<N;i++){
			if(isAlive[i])
				peers.add(i);
		}
		return peers;

	}

	//synchronized 
	public int create(int node_no, String ip_addr, int port_no){
		// if(cnt==0){
		// 	arrayOfNodes[cnt] = new NodeInfo(0,ip_addr,port_no);
		// 	isAlive[cnt]=true;
		// 	cnt++;
		// 	return 0;
		// }
		arrayOfNodes[node_no] = new NodeInfo(node_no, ip_addr, port_no);
		isAlive[node_no]=true;
		++cnt;

		return node_no;
	}

	public void node_failure_update(int n){
		isAlive[n] = false;
		
	}

	public static void main(String args[]){
		//Tracker trc = new Tracker()
		//start 'TrackerIntf' rmi at port 8000 with object trc
		Tracker trc = new Tracker();     
		try { 
			                               
			Registry registry = LocateRegistry.getRegistry(8000);
			TrackerIntf stub = (TrackerIntf) UnicastRemoteObject.exportObject(trc, 0); //8000 is rmi listening port
			registry.rebind("TrackerIntf", stub);  
			System.err.println("Tracker ready");
		} catch (Exception e) { 
			System.err.println("Tracker exception: " + e.toString()); 
			e.printStackTrace(); 
		} 
		// while(true){
		// 	try{
		// 		//System.out.println(trc.cnt);
		// 		Thread.sleep(1000);
		// 	}catch(Exception e){
		// 		System.out.println("Thread in tracker error.");
		// 	}
		// }

		int last_node_no = (1<<m)-1;

		Node node_0 = new Node();
		Node node_last = new Node();

		node_0.n = new NodeInfo(0,"localhost", 9000);
		node_last.n = new NodeInfo(last_node_no, "localhost", 10000);

		trc.arrayOfNodes[0] = new NodeInfo(node_0.n);
		trc.arrayOfNodes[last_node_no] = new NodeInfo(node_last.n);

		trc.isAlive[0] = true;
		trc.isAlive[last_node_no] = true;

		node_0.successor = node_last.n;
		node_0.predecessor = node_last.n;


		for(int i=0;i<m;i++)
			node_0.finger[i] = new NodeInfo(node_0.n);
		
		node_last.successor = node_0.n;
		node_last.predecessor = node_0.n;

		
		node_last.finger[0] = new NodeInfo(node_0.n);
		for(int i=1;i<m;i++)
			node_last.finger[i] = new NodeInfo(node_last.n);

		try{
			Registry registry_0, registry_last;


			registry_0 = LocateRegistry.getRegistry(node_0.n.port_no); 
			ChordIntf zero_node_stub = (ChordIntf) UnicastRemoteObject.exportObject(node_0, 0);  
			registry_0.bind("ChordIntf", zero_node_stub); 

			registry_last = LocateRegistry.getRegistry(node_last.n.port_no); 
			ChordIntf last_node_stub = (ChordIntf) UnicastRemoteObject.exportObject(node_last, 0);  
			registry_last.bind("ChordIntf", last_node_stub);  


			int wait_time = 1000;
			
			while(true){
				node_0.print_state();
				node_last.print_state();
				
				
				//Thread.sleep(wait_time);
				node_0.stabilize();
				node_0.fix_fingers();
				node_0.check_predecessor();

				Thread.sleep(wait_time);
				node_last.stabilize();

				//for(int i=0;i<m;i++)
					
				//for(int i=0;i<m;i++)

				node_last.fix_fingers();

				
				node_last.check_predecessor();
				
				Thread.sleep(wait_time);
				System.out.println("---------------------------------------------");
			}
		} catch (Exception e){
			System.out.println("Exception in tracker due to initial nodes."+e);
			e.printStackTrace();
		}
	}
}
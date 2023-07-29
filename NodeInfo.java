import java.io.Serializable;
public class NodeInfo implements Serializable {
	int node_no;
	String ip_addr;
	int port_no; // rmi port : we have to take input
	public NodeInfo(){
		node_no=0;
		ip_addr="";
		port_no=0;
	}
	public NodeInfo(int no, String ip, int port){
		node_no = no;
		ip_addr = ip;
		port_no = port;
	}
	public NodeInfo(NodeInfo n){
		node_no = n.node_no;
		ip_addr = n.ip_addr;
		port_no = n.port_no;
	}
}
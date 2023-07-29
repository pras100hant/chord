# chord

# Introduction

A peer-to-peer network allows us to communicate with any other machine without any centralized
server. Here we have implemented a peer-to-peer network using CHORD protocol. Using this we can
easily connect with any other peer and perform the task, e.g., file transfer. In this protocol all the peers
are arranged in a logical ring and also maintain a table called finger table using which it can easily
shoot at-least half of the peers ahead and hence can connect to any peer in O(log N) lookups which is
great. In this project we have implemented this using Java RMI (RPC in general) which will make it
platform-independent as well.
# Demo

Requirements: JDK or JRE must be installed to run the application.

What have we done?

We simulated many nodes on the same machine, so to uniquely identify every node we have used port
no (as IP address is the same for all). If we run this application on different machines, then we don’t
have to use different ports for every node. We could use a dedicated port no for the RMI registry of that
particular node.

How to run?

To compile all the files we can use:

javac *.java

As the Tracker starts with two default nodes i.e. the first and the last one. We have to start total of 3
RMI registries. We have used port 8000 for the Tracker’s core functionality and 9000 and 10000 for the
default nodes. So we have to first start RMI registries as shown below(on the same terminal):

rmiregistry 8000 &

rmiregistry 9000 &

rmiregistry 10000 &

Note: The above commands are for the linux distros (preferably Ubuntu).
Then we can run the Tracker node by the following command:

java Tracker

To make things simple we have made a shell file to execute all these commands. The file name is
start_tracker.sh. Run it as shown below:
bash start_tracker.sh
To turn off the Tracker close the terminal. That’s it.
Start again using the same method. The start_tracker.sh might not work right away. Sometimes it
doesn’t work because for ports and throws Exceptions. So wait for some time as RMI registries take
time to close when we close the terminal and again try.

To start a node is very easy. Just start a RMI registry at certain port no which is not already in use.
(don’t use 8000, 9000, and 10000).

Run command:

rmiregistry 9001 &

java Node

Now it will ask for the port no. use the same port no., in this case 9001.
Similarly create more nodes using new terminal. Can use 9002 the next time.
From here you can follow as the instruction come on terminal.

See the demo below:

Starting tracker. It is also showing the state for the default nodes. For other nodes also we can see the
finger table but that way the terminal become very clumsy so we have removed the print_state method.

Starting node 5 at 9001 port.

Refreshing using 0 to see the current peers in the network. 0 and 7 are default nodes. N=8.

Now after node 3 is started at 9002 port. On refreshing both are showing same list of peers.

Enter 1 to connect to any peer. Node 3 want to connect with node 5. We entered the port as 9009 but it
refused. So we will try with other port no.

We now try with port no. 12001. And we enterd the name of the file to be transferred. Here, testfile

The node 5 received the file sent by node 3.

# Limitations
This is a very simple prototype of the p2p chord protocol. So it is not very robust. Once any node is
down, the entire ring can’t stabilize. We will work in that direction later.

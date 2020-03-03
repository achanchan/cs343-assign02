import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Set;

public class Peer implements FileSharingInterface{
    Hashtable<String, FileSharingInterface> neighbors;
    Vector<String> filenames;
    String my_ip;
    Hashtable<String, String> request_record;

    public Peer(String ip){
        my_ip=ip;
        filenames = new Vector<String>();
        request_record = new Hashtable<String,String>();
        neighbors = new Hashtable<String, FileSharingInterface>();

        String[] a = {"Welcometochilis", "Yeet", "Merrychrysler"};
        String[] b = {"Avocadothanks", "Isthataweed","Roadworkahead"};
        String[] c = {"Lookatallthosechickens", "Whatarethose", "Welcometochilis", "Avocadothanks", "Jared19"};
        String[] d = {"Yeet", "Roadworkahead", "Freshavocado"};
        String[] e = {"Yeet", "Jared19", "Merrychrysler"};

        filenames.addAll(Arrays.asList(a));
    }

	/*
	 * receiveQuery(Query q) takes a Query object q and checks to see if the Peer
	 * object has the requested file. If it does, it sends a QueryResponse
	 * to the node that passed along the request. If it doesn't have the file,
	 * it will propagate the query to its neighbors.
     */
    public void receiveQuery(Query q){
        // check if has file
        // check to see if already in map
        // if yes, do nothing
        // if no, add to map and propagate to neighbors

        synchronized(this){
            String current = q.getCurrentIP();
            String prev = q.getOriginalIP();
            String filename = q.getFilename();

            System.out.println("Received a query from " + current
            +": " + prev +" is looking for " +filename + "!");

						//Checks to see if the node has the file.
						//If it does, send a query response back to the node
            if (filenames.contains(filename)){
                try{
								System.out.println("I have the file!");
                Registry registry = LocateRegistry.getRegistry(current);
								FileSharingInterface return_stub = (FileSharingInterface) registry.lookup(current);
                QueryResponse newQR = new QueryResponse(prev, my_ip, filename);

                sendQueryResponse(newQR, return_stub);
            } catch (Exception e){
                System.err.println("Exception: " + e.toString());
            	}
            }

            else if (!request_record.containsKey(filename)){
                // add filename and original requestor to map and propagate to neighbors
                request_record.put(filename, prev);
                Query newQuery = new Query(my_ip, current, filename);
                for (String n: neighbors.keySet()){
                    if (!n.equals(current) && !n.equals(prev)){
                        FileSharingInterface nextNeighbor = neighbors.get(n);
                        try{
                        System.out.println("Sending a query to neighbor " + n +"!");
                        sendQuery(newQuery, nextNeighbor);

                        } catch (Exception e){
                            System.err.println("Client exception: " + e.toString());
                            e.printStackTrace();
                        }

                    }
                }

            }
        }
    }

	/*
	* sendQuery(Query q, FileSharingInterface neighbor) is called by a
	* neighboring node to send the current node a query.
	*/
    public void sendQuery(Query q, FileSharingInterface neighbor) throws RemoteException, MalformedURLException{
        try{
		neighbor.receiveQuery(q);
        } catch (Exception e){
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

	/*
	* receiveQueryResponse(QueryResponse qr) takes a QueryResponse object and
	* checks to see if the current peer is the original requestor. If it is,
	* it prints out the filename and location of the file. If it is not the
	* current requestor, it passes the QueryResponse to the next neighbor.
	*/
    public void receiveQueryResponse(QueryResponse qr) throws RemoteException{
        String nextpeer_ip = qr.getNextPeer();
        String owner_ip = qr.getOwner();
        String filename = qr.getFilename();

	// check if I am original requestor
	if (nextpeer_ip.equals(my_ip)){
		System.out.println(filename + " was found! The owner is " + owner_ip);
	}else if (request_record.containsKey(filename)){
            String prev = request_record.get(filename);
            System.out.println("Received a query response: Please send to " + prev
            + " that " + owner_ip + " has " + filename + "!");
            QueryResponse newQR = new QueryResponse(prev, owner_ip, filename);
            FileSharingInterface nextNeighbor = neighbors.get(nextpeer_ip);
            	try{
            		sendQueryResponse(newQR, nextNeighbor);
                    System.out.println("Sending query response to " + prev + "!");
                } catch (Exception e){
                    System.err.println("Client exception: " + e.toString());
                    e.printStackTrace();
            }
        }
    }

	/*
	* sendQueryResponse is called by a neighboring node for the current node to
	* receive a query response.
	*/
    public void sendQueryResponse(QueryResponse qr, FileSharingInterface neighbor)
    throws RemoteException, MalformedURLException{
        try{
            neighbor.receiveQueryResponse(qr);

            } catch (Exception e){
                System.err.println("Peer exception: " + e.toString());
                e.printStackTrace();
            }
    }

	/*
	* connect(String[] neighborIPs) takes a String array of the neighboring
	* IPs and adds the neighbor registries to the stub of the current node.
	*/
    public boolean connect(String[] neighborIPs){
        try{
        for (int i = 0; i < neighborIPs.length; i++){
            Registry registry = LocateRegistry.getRegistry(neighborIPs[i]);
            FileSharingInterface stub = (FileSharingInterface) registry.lookup(neighborIPs[i]);
            neighbors.put(neighborIPs[i],stub);
            System.out.println("connected to " + neighborIPs[i]+"!");
        }
	return true;
    } catch (Exception e){
        System.err.println("Peer exception: " + e.toString());
        e.printStackTrace();
	return false;
    }
    }

    public static void main(String[] args){
        try{

			// IP numbers and connections
            // //A -> B & C
            String[] ips = {"34.201.153.14","3.83.161.143"};
            // // //B -> A & D
            // String[] ips = {"54.166.141.199", "54.227.231.162"};
            // // //C -> A & D
            // String[] ips = {"54.166.141.199", "54.227.231.162"};
            // // //D -> B, C, E
            // String[] ips = {"34.201.153.14", "3.83.161.143", "3.84.6.57"};
            // // //E -> D
            // String[] ips = {"54.227.231.162"};

            Scanner s = new Scanner(System.in);

            Peer p = new Peer(args[0]);
            FileSharingInterface stub = (FileSharingInterface) UnicastRemoteObject.exportObject(p, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(args[0], stub);

            String input = "help";
            String[] split_msg = input.split("\\s+");
	        boolean connected = false;
            while (!(split_msg[0].equals("quit"))){
                if (split_msg.length == 1 && split_msg[0].equals("help")){
                    System.out.println("Commands:\nhelp - show this message again\nconnect - connect to your neighbors"
                    +"\nfind <filename> - find a file you want\nlist - list the files that you have");
                }else if(split_msg.length == 1 && split_msg[0].equals("connect")){
                    connected = p.connect(ips);

                }else if(split_msg.length == 1 && split_msg[0].equals("list")){
                    System.out.println(p.filenames.toString());

                }else if(split_msg[0].equals("find")){

		    if (!connected){
		    	System.out.println("Please try to connect again");
                }
            else{
			//try to find the file
                System.out.println("Looking for " + split_msg[1] + "...");

			    if (p.filenames.contains(split_msg[1])){
			    System.out.println("You already have that file!");
                }

                else{
                	    //try to find the file
                    	Query q = new Query(p.my_ip, p.my_ip, split_msg[1]);
                    	Set<String> keys = p.neighbors.keySet();
                        for (String key: keys)
                        {
                        	p.sendQuery(q, p.neighbors.get(key));
                        }
			    }
            }
                }
                else{ System.out.println("Sorry, I don't understand that");
            }
                System.out.print(">>>");
                split_msg = s.nextLine().trim().split("\\s+");
            }

            if(split_msg[0].equals("quit")){
                s.close();
		        System.exit(0);
            }


        }
        catch(Exception e){
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}

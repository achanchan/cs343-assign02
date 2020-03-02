import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.util.Scanner;
import java.util.Arrays;
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

        String[] a = {"Welcometochilis", "Yeet", "Merrychrystler"};
        String[] b = {"Avacadothanks", "Isthataweed","Roadworkahead"};
        String[] c = {"Lookatallthosechickens", "Whatarethose", "Welcometochilis", "Avacadothanks", "Jared19"};
        String[] d = {"Yeet", "Roadworkahead", "Freshavacado"};
        String[] e = {"Yeet", "Jared19", "Merrychystler"};

        filenames.addAll(Arrays.asList(a));

    }

    public void receiveQuery(Query q){
        // check if has file
        // check to see if already in map
        // if yes, do nothing
        // if no, add to map and propagate to neighbors

        synchronized(this){
            String current = q.getCurrentIP();
            String prev = q.getOriginalIP();
            String filename = q.getFilename();

            System.out.println("current: " + current
            +"/n prev: " + prev
            +"/n filename: " +filename);


            if (filenames.contains(filename)){
                try{
                Registry registry = LocateRegistry.getRegistry(prev);
                QueryResponse newQR = new QueryResponse(prev, my_ip, filename);
                FileSharingInterface return_stub = (FileSharingInterface) registry.lookup(prev);

                sendQueryResponse(newQR, return_stub);
            } catch (Exception e){
                System.err.println("Exception: " + e.toString());
            }
            }
            if (!request_record.containsKey(filename)){
                // add to map and propagate to neighbors
                request_record.put(filename, prev);
                Query newQuery = new Query(my_ip, current, filename);
                for (String n: neighbors.keySet()){
                    if (!n.equals(current)){
                        FileSharingInterface nextNeighbor = neighbors.get(n);
                        try{
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

    public void sendQuery(Query q, FileSharingInterface neighbor) throws RemoteException, MalformedURLException{
        try{

        neighbor.receiveQuery(q);
        System.out.println("Sending a query!");

        } catch (Exception e){
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }


    }

    public void receiveQueryResponse(QueryResponse qr) throws RemoteException{
        String nextpeer_ip = qr.getNextPeer();
        String owner_ip = qr.getOwner();
        String filename = qr.getFilename();

        System.out.println("nextpeer: " + nextpeer_ip
        +"/n owner: " + owner_ip
        +"/n filename: " +filename);

        if (request_record.containsKey(filename)){
            String prev = request_record.get(filename);
            QueryResponse newQR = new QueryResponse(prev, owner_ip, filename);
            FileSharingInterface nextNeighbor = neighbors.get(nextpeer_ip);
                        try{
                            sendQueryResponse(newQR, nextNeighbor);
                        } catch (Exception e){
                            System.err.println("Client exception: " + e.toString());
                            e.printStackTrace();
                        }


        }
    }

    public void sendQueryResponse(QueryResponse qr, FileSharingInterface neighbor)
    throws RemoteException, MalformedURLException{
        try{
            // Registry registry = LocateRegistry.getRegistry(neighbor_ip);
            // neighbor = (FileSharingInterface) registry.lookup(neighbor_ip);
            System.out.println("Sending query response!");
            neighbor.receiveQueryResponse(qr);

            } catch (Exception e){
                System.err.println("Peer exception: " + e.toString());
                e.printStackTrace();
            }


    }

    public boolean connect(String[] neighborIPs){
        try{
        for (int i = 0; i < neighborIPs.length; i++){
            Registry registry = LocateRegistry.getRegistry(neighborIPs[i]);
            FileSharingInterface stub = (FileSharingInterface) registry.lookup(neighborIPs[i]);
            neighbors.put(neighborIPs[i],stub);
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
            // //A -> B & C
            String[] ips = {"3.83.162.66","184.72.111.106"};
            // // //B -> A & D
            // String[] ips = {"35.172.139.12", "54.208.160.31"};
            // // //C -> A & D
            // String[] ips = {"35.172.139.12", "54.208.160.31"};
            // // //D -> B, C, E
            // String[] ips = {"3.83.162.66", "184.72.111.106", "18.206.217.187"};
            // // //E -> D
            // String[] ips = {"54.208.160.31"};


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
                    System.out.println("Commands:\nhelp - show this message again\nconnect - connect to your neighbors\nfind <filename> - find a file you want");
                }else if(split_msg.length == 1 && split_msg[0].equals("connect")){
                    connected = p.connect(ips);
                    System.out.println(p.neighbors.toString());
                }else if(split_msg[0].equals("find")){
		    if (!connected){
		    	System.out.println("Please try to connect again");
		    }else{
                    	System.out.println("Received find command");
                    	//try to find the file
                    	Query q = new Query(p.my_ip, p.my_ip, split_msg[1]);
                    	Set<String> keys = p.neighbors.keySet();
                    	for (String key: keys){
                        	p.sendQuery(q, p.neighbors.get(key));
                    	}
		    }
                }
                System.out.print(">>>");
                split_msg = s.nextLine().trim().split("\\s+");
            }

            if(split_msg[0].equals("quit")){
                System.exit(0);
            }
            s.close();


        }
        catch(Exception e){
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }


    }
}

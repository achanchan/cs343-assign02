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
                // send query response
            }
            if (!request_record.containsKey(filename)){
                // add to map and propagate to neighbors
                request_record.put(filename, prev);
                Query newQuery = new Query(my_ip, current, filename);
                for (String n: neighbors.keySet()){
                    if (!n.equals(current)){
                        FileSharingInterface nextNeighbor = neighbors.get(n);
                        try{
                        sendQuery(newQuery, nextNeighbor, n); 
                        } catch (Exception e){
                            System.err.println("Client exception: " + e.toString());
                            e.printStackTrace();
                        }

                    }
                }

            }
        }    
    }

    public void sendQuery(Query q, FileSharingInterface neighbor, String neighbor_ip) throws RemoteException, MalformedURLException{
        try{
        //Registry registry = LocateRegistry.getRegistry(neighbor_ip);
        //neighbor = (FileSharingInterface) registry.lookup(neighbor_ip);
        neighbor.receiveQuery(q);

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
                            sendQueryResponse(newQR, nextNeighbor, prev); 
                        } catch (Exception e){
                            System.err.println("Client exception: " + e.toString());
                            e.printStackTrace();
                        }
            

        }
    }

    public void sendQueryResponse(QueryResponse qr, FileSharingInterface neighbor, String neighbor_ip) 
    throws RemoteException, MalformedURLException{
        try{
            // Registry registry = LocateRegistry.getRegistry(neighbor_ip);
            // neighbor = (FileSharingInterface) registry.lookup(neighbor_ip);
            neighbor.receiveQueryResponse(qr);
    
            } catch (Exception e){
                System.err.println("Peer exception: " + e.toString());
                e.printStackTrace();
            }
    

    }

    public void connect(String[] neighborIPs){
        try{
        for (int i = 0; i < neighborIPs.length; i++){
            Registry registry = LocateRegistry.getRegistry(neighborIPs[i]);
            FileSharingInterface stub = (FileSharingInterface) registry.lookup(neighborIPs[i]);
            neighbors.put(neighborIPs[i],stub);
        }
    } catch (Exception e){
        System.err.println("Peer exception: " + e.toString());
        e.printStackTrace();

    }
    

    }

    public static void main(String[] args){
        try{
            // //A
            String[] ips = {"3.86.81.106","3.87.3.13"};
            // //B
            // String[] ips = {"54.161.28.70", "54.158.15.101"};
            // //C
            // String[] ips = {"54.161.28.70", "54.158.15.101"};
            // //D
            // String[] ips = {"3.86.81.106","3.87.3.13","3.87.168.243"};
            // //E
            // String[] ips = {"54.158.15.101"};


            Scanner s = new Scanner(System.in);
            
            Peer p = new Peer(args[0]);
            FileSharingInterface stub = (FileSharingInterface) UnicastRemoteObject.exportObject(p, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(args[0], stub);

            String input = "help";
            String[] split_msg = input.split("\\s+");
            while (!(split_msg[0].equals("quit"))){
                if (split_msg.length == 1 && split_msg[0].equals("help")){
                    System.out.println("Commands:\nhelp - show this message again\nconnect - connect to your neighbors\nfind <filename> - find a file you want");
                }else if(split_msg.length == 1 && split_msg[0].equals("connect")){
                    p.connect(ips);
                }else if(split_msg.length == 1 && split_msg[0].equals("find")){
                    //try to find the file
                    Query q = new Query(p.my_ip, p.my_ip, split_msg[1]);
                    Set<String> keys = p.neighbors.keySet();
                    for (String key: keys){
                        p.sendQuery(q, p.neighbors.get(key), key);
                    }

                }
                System.out.println(">>>");
                split_msg = s.nextLine().trim().split("\\s+");
            }

            if(split_msg[0].equals("quit")){
                exit(0); 
            }
            s.close(); 



            






        }
        catch(Exception e){
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
        

    }
}
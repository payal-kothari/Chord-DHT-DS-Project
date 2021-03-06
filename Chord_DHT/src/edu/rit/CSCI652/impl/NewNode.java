package edu.rit.CSCI652.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;

/**
 * Created by payalkothari on 10/10/17.
 */
public class NewNode
{
    private static URL serverURL = null;
    private static int PORT = 8080;
    private static String centralServerIp = "localhost";
    private static Node predecessor = null;
    private static Node successor = null;
    private static int ownGUID = 0;
    private static int maxFingerTableSize = 0;
    private static int maxNodesInTheNetwork = 0;
    private static Node ownNode = new Node();
    private static List<FingerTableEntry> fingerTable = new ArrayList<>();
    private static String ip = "localhost";

    public static ServerSocket getServerSocket() {
        return serverSocket;
    }

    private static ServerSocket serverSocket;


    public static void main(String[] args) throws IOException, ClassNotFoundException, JSONRPC2SessionException {
        init();
        serverSocket = new ServerSocket(PORT);
//        new JSON_RPC_Subclass(ownNode, fingerTable, maxFingerTableSize).start();
    }

    private static void init() throws IOException, ClassNotFoundException, JSONRPC2SessionException {

        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.print("\n\n");
            System.out.println("***************************************************************************************");
            System.out.println("1 - Join the network");
            System.out.println("2 - Display finger table ");
            System.out.println("3 - Upload a file");
            System.out.println("4 - Search a file");
            System.out.println("5 - Leave the network");
            System.out.println("Enter an option : ");


            int option = scanner.nextInt();
            scanner.nextLine();                 // need this after reading nextInt()

            switch (option){

                case 1 :
                    System.out.println("Joining the network");
                    Socket subscriberSocket = new Socket(centralServerIp, 2000);
                    ObjectInputStream objectInStream = new ObjectInputStream(subscriberSocket.getInputStream());
                    int reconnectPort = objectInStream.readInt();
                    System.out.println("received sec port");
                    Socket reconnectSocket = new Socket(centralServerIp, reconnectPort);
                    ObjectOutputStream outObject = new ObjectOutputStream(reconnectSocket.getOutputStream());
                    outObject.writeUTF("Join");
                    outObject.flush();
                    ObjectInputStream inputStream = new ObjectInputStream(reconnectSocket.getInputStream());
                    maxFingerTableSize = inputStream.readInt();
                    maxNodesInTheNetwork = (int) Math.pow(2, maxFingerTableSize);
                    ownGUID = inputStream.readInt();
                    ownNode.setGUID(ownGUID);
                    ownNode.setIp(reconnectSocket.getLocalAddress());
                    ownNode.setPort(8080);
                    predecessor = (Node) inputStream.readObject();
                    successor = (Node) inputStream.readObject();
                    ArrayList<Node> fingerTableSuccessors = new ArrayList<>();
                    for(int i = 0; i < maxFingerTableSize ; i++){
                        fingerTableSuccessors.add((Node) inputStream.readObject());
                    }
//                    initializeFingerTable(fingerTableSuccessors);
                    System.out.println("Joined the network");
                    break;
                case 2 :
                    System.out.println("*******************  Finger Table  ******************** \n");
                    System.out.print(" Start ");
                    System.out.print("\t\t  Interval ");
                    System.out.print("\t\t\t\t   Successor \n");
                    for(int i =0; i< maxFingerTableSize; i++){
                        FingerTableEntry entry = fingerTable.get(i);
                        System.out.print("\n");
                        System.out.print(" " + entry.getStart());
                        System.out.print("\t\t\t     " + entry.getIntervalBegin() + "," + entry.getIntervalEnd());
                        System.out.print("\t\t\t\t\t   " + entry.getSucc().getGUID());
                    }
                    break;
            }

        }
    }

//    private static void initializeFingerTable() throws IOException, JSONRPC2SessionException {
//        ownNode.setFingerTable(fingerTable);
//
//        for(int i = 0; i < maxFingerTableSize ; i++){
//            FingerTableEntry entry = new FingerTableEntry();
//            entry.setStart((int) ((ownGUID + Math.pow(2, i)) % maxNodesInTheNetwork));  // k + 2^i
//            fingerTable.add(entry);
//        }
//
//        for(int i = 0; i < maxFingerTableSize-1 ; i++){
//            FingerTableEntry entry = fingerTable.get(i);
//            entry.setIntervalBegin(entry.getStart());
//            FingerTableEntry nextEntry = fingerTable.get(i+1);
//            entry.setIntervalEnd(nextEntry.getStart());
//        }
//        FingerTableEntry lastEntry = fingerTable.get(maxFingerTableSize-1);
//        lastEntry.setIntervalBegin(lastEntry.getStart());
//        lastEntry.setIntervalEnd(fingerTable.get(0).getStart());
//
//        if(ownGUID == predecessor.getGUID()){
//            // only single node in the network
//            for(int i = 0; i < maxFingerTableSize ; i++){
//                FingerTableEntry entry = fingerTable.get(i);
//                entry.setSucc(ownNode);
//            }
//        }else {
//            for(int i = 0; i < maxFingerTableSize-1 ; i++){
//                fingerTable.get(i).setSucc(ownNode);
//            }
//            for(int i = 0; i < maxFingerTableSize ; i++){
//                FingerTableEntry entry = fingerTable.get(i);
//                int id = entry.getStart();
//                entry.setSucc(find_successor(id));
//            }
//
//        }
//        System.out.println("fingr table");
//        update_others();
//    }

    private static void update_others() {
    }

    private static Node find_successor(int id) throws IOException, JSONRPC2SessionException {
        Node nRemote = find_predecessor(id);
        return nRemote.getFingerTable().get(1).getSucc();
    }

    private static Node find_predecessor(int id) throws IOException, JSONRPC2SessionException {
        Node nRemote = ownNode;
        int nRemoteGUID = nRemote.getGUID();
        Node nRemoteSucc = nRemote.getFingerTable().get(1).getSucc();
        int nRemoteSuccGUID = nRemoteSucc.getGUID();

        while (!(nRemoteGUID < nRemoteSuccGUID && id > nRemoteGUID && id < nRemoteSuccGUID)
                || !(nRemoteGUID > nRemoteSuccGUID && id > nRemoteGUID && id > 0)
                || !(nRemoteGUID > nRemoteSuccGUID && id >= 0 && id < nRemoteSuccGUID) ){

            System.out.println("remote ip : " + nRemote.getIp());
            String urlString = "http:/" + nRemote.getIp() + ":8080";
            serverURL = new URL("http://127.0.0.1:8080");

            JSONRPC2Session mySession = new JSONRPC2Session(serverURL);

            String method = "get_closest_preceding_finger";
            int requestID = ownNode.getGUID();
            HashMap param = new HashMap<>();
            param.put("id", id);

            JSONRPC2Request request = new JSONRPC2Request(method, param, requestID);
            JSONRPC2Response response = null;

            response = mySession.send(request);

            if (response.indicatesSuccess()){

//                Item item = new ObjectMapper().readerFor(FingerTableEntry.class).readValue(response.getResult());
//                new ObjectMapper().getDeserializationContext((Node)response.getResult());
//                nRemote = (Node) response.getResult();
                String json = (String) response.getResult();
                ObjectMapper mapper = new ObjectMapper();
                nRemote = mapper.readValue((String) response.getResult(), Node.class);

                nRemoteGUID = nRemote.getGUID();
                nRemoteSucc = nRemote.getFingerTable().get(1).getSucc();
                nRemoteSuccGUID = nRemoteSucc.getGUID();
                System.out.println("result : " + json);
            }
        }
        return nRemote;
    }

//    public static Node get_closest_preceding_finger(int id){
//        int n = ownGUID;
//
//        for(int i = maxFingerTableSize-1; i >= 0; i--){
//            Node ans = fingerTable.get(i).getSucc();
//            int ansID = ans.getGUID();
//            if((n < id && ansID > n && ansID < id)
//                    || (n > id && ansID > n && ansID > 0)
//                    || (n > id && ansID >= 0 && ansID < id)){
//
//                return fingerTable.get(i).getSucc();
//
//            }
//        }
//
//        return ownNode;
//    }

}

package edu.rit.CSCI652.impl;

import javafx.print.Collation;
import org.omg.CORBA.Object;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.Socket;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by payalkothari on 10/17/17.
 */
public class ThreadHandler extends Thread implements Serializable {

    private static Socket socket;
    private static CentralServer centralServer = null;
    private static int currentPort;
    private static CentralServer threadSyncObject;
    private static String cmd;



    public ThreadHandler(Socket socket, int port,CentralServer threadSyncObject,String command) {
        this.centralServer = new CentralServer();
        this.socket = socket;
        this.currentPort = port;
        this.threadSyncObject = threadSyncObject;
        this.cmd = command;
    }

    public void run() {
        synchronized (threadSyncObject) {
            try {
                switch (cmd){

                    case "Join" :
                        InetAddress nodeIp = socket.getInetAddress();
                        int nodePort = socket.getPort();
                        String ipAndPortString = String.valueOf(nodeIp) + String.valueOf(nodePort);
                        int GUID = createGUID(ipAndPortString);
                        System.out.println("NewNode with ip " + "'" + nodeIp + "'" +" has joined the network");
                        System.out.println("GUID is : " + GUID);
                        ConcurrentHashMap globalTable = centralServer.getGlobalTable();
                        Node newNode = new Node(GUID, nodeIp, 8000);
                        globalTable.put(GUID, newNode);
                        Node predecessor = findPredecessor(GUID);
                        Node successor = findSuccessor(GUID);
                        ObjectOutputStream outObject = new ObjectOutputStream(socket.getOutputStream());
                        outObject.writeInt(centralServer.getMaxFingerTableSize());
                        outObject.writeInt(GUID);
                        outObject.writeObject(predecessor);
                        outObject.writeObject(successor);

                        for(int i = 0; i < centralServer.getMaxFingerTableSize() ; i++){
                            int nextStart = (int) ((GUID + Math.pow(2, i)) % centralServer.getMaxNodes());
                            outObject.writeObject(findSuccessor(nextStart));
                        }
                        outObject.flush();
                        update_others(newNode);
                        System.out.println("");
                        break;

                    case "Upload" :
                        byte[] byteArr = new byte[10000];
                        FileOutputStream fileOutputStream = new FileOutputStream("/Users/payalkothari/Documents/DS/Chord_Project/Chord_DHT/src/edu/rit/CSCI652/impl/z.txt");
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                        InputStream inputStream = socket.getInputStream();
                        int bytesRead = 0;
                        while((bytesRead=inputStream.read(byteArr))!=-1)
                            bufferedOutputStream.write(byteArr, 0, bytesRead);
                        bufferedOutputStream.flush();

                        break;

                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    CentralServer.getBusyPorts().remove(currentPort);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void update_others(Node newNode) throws IOException {
        ConcurrentHashMap globalTable = centralServer.getGlobalTable();
        ObjectOutputStream outObject;

        Collection values = globalTable.values();
        Iterator iter = values.iterator();
        while (iter.hasNext()){
            Node node = (Node) iter.next();
            if(newNode != node){
                Socket socket = new Socket(node.getIp(), 8000);
                if(socket.isConnected()) {
                    outObject = new ObjectOutputStream(socket.getOutputStream());
                    outObject.writeUTF("New Node");
                    outObject.writeObject(newNode);
                    outObject.flush();
                }
            }
        }


    }

    private Node findSuccessor(int GUID) {

        List<Integer> GUIDList = centralServer.getGUIDList();
        Collections.sort(GUIDList);

        int successor = GUID;
        Iterator<Integer> iterator = GUIDList.iterator();
        while (iterator.hasNext()) {
            int nextID = iterator.next();
            if (nextID > successor) {
                successor = nextID;
                break;
            }
        }
        if (successor == GUID)
            successor = Collections.min(GUIDList);   // to rotate around the chord circle

        ConcurrentHashMap globalTable = centralServer.getGlobalTable();
        return (Node) globalTable.get(successor);
    }

    private Node findPredecessor(int GUID) {
        List<Integer> GUIDList = centralServer.getGUIDList();
        Collections.sort(GUIDList, Collections.reverseOrder());

        int predecessor = GUID;
        Iterator<Integer> iterator = GUIDList.iterator();
        while (iterator.hasNext()) {
            int nextID = iterator.next();
            if (nextID < predecessor) {
                predecessor = nextID;
                break;
            }
        }
        if (predecessor == GUID)
            predecessor = Collections.max(GUIDList);   // to rotate around the chord circle

        ConcurrentHashMap globalTable = centralServer.getGlobalTable();
        return (Node) globalTable.get(predecessor);
    }


    public int createGUID(String stringToHash) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        messageDigest.reset();
        byte[] byteArr =  messageDigest.digest(stringToHash.getBytes());
        BigInteger bigNum = new BigInteger(1, byteArr);
        int GUID = Math.abs(bigNum.intValue()) % centralServer.getMaxNodes();

        while (centralServer.getGUIDList().contains(GUID)){
            messageDigest.reset();
            byteArr =  messageDigest.digest(byteArr);
            bigNum = new BigInteger(1, byteArr);
            GUID = Math.abs(bigNum.intValue()) % centralServer.getMaxNodes();
        }

        List<Integer> list = centralServer.getGUIDList();
        list.add(GUID);

        return GUID;
    }




}

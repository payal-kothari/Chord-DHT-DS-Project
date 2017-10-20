package edu.rit.CSCI652.impl;

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
                        globalTable.put(GUID, new Node(GUID, nodeIp.toString(), 7000));
                        int predGUID = findPredecessor(GUID);
                        Node node = (Node) globalTable.get(predGUID);
                        String predIP = node.getIp();
                        int predPort = node.getPort();
                        ObjectOutputStream outObject = new ObjectOutputStream(socket.getOutputStream());
                        outObject.writeInt(centralServer.getMaxFingerTableSize());
                        outObject.writeInt(GUID);
                        outObject.writeInt(predGUID);
                        outObject.writeUTF(predIP);
                        outObject.writeInt(predPort);
                        outObject.flush();
                        System.out.println("");
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

    private int findPredecessor(int GUID) {

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

        return predecessor;
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

package edu.rit.CSCI652.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by payalkothari on 10/16/17.
 */
public class CentralServer {

    private static int maxNodes = 0;                    // N
    private static int maxFingerTableSize = 0;          // m
    private static ServerSocket centralServerSocket = null;
    private static Socket origSocket;
    private static HashSet<Integer> busyPorts = new HashSet<>();
    private static List<Integer> GUIDList = new ArrayList<>();
    private static ConcurrentHashMap<String, InetAddress>  globalTable = new ConcurrentHashMap<>();


    public static int getMaxFingerTableSize() {
        return maxFingerTableSize;
    }

    public static List<Integer> getGUIDList() {
        return GUIDList;
    }

    public static int getMaxNodes() {
        return maxNodes;
    }

    public static ConcurrentHashMap<String, InetAddress> getGlobalTable() {
        return globalTable;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        CentralServer threadSyncObject = new CentralServer();

//        maxNodes = Integer.parseInt(args[0]);
        maxNodes = 16;
        maxFingerTableSize = (int) ( Math.log(maxNodes) / Math.log(2) );


        new CentralServer().startService(threadSyncObject);
    }

    private void startService(CentralServer threadSyncObject) throws IOException {
        centralServerSocket = new ServerSocket(2000);
        busyPorts.add(2000);
        System.out.println("Central Server is running ......\n");
        while (true){
            int nextFreePort = getNewFreePort();
            busyPorts.add(nextFreePort);
            ServerSocket subServerSocket = new ServerSocket(nextFreePort);
            origSocket = centralServerSocket.accept();
            ObjectOutputStream outObject = new ObjectOutputStream(origSocket.getOutputStream());
            outObject.writeInt(nextFreePort);
            outObject.flush();
            origSocket.close();
            Socket subSocket = subServerSocket.accept();
            ObjectInputStream objectInStream = new ObjectInputStream(subSocket.getInputStream());
            String command = objectInStream.readUTF();
            new ThreadHandler(subSocket, nextFreePort, threadSyncObject, command).start();  // new thread for new connection
            System.out.println();
        }
    }

    public static HashSet<Integer> getBusyPorts() {
        return busyPorts;
    }

    public int getNewFreePort() {
        Random random = new Random();
        int max = 65535;  // total number of ports
        int min = 1024;   // 0-1023 are reserved ports , so starting with 1024
        int newPort = 2000;

        while(busyPorts.contains(newPort)){
            newPort = random.nextInt(max - min) + min;   // inclusive of min and max
        }
        return newPort;
    }
}

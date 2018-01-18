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
    private static ObjectInputStream objectInStream;


    public ThreadHandler(Socket socket, int port,CentralServer threadSyncObject,String command, ObjectInputStream objectInStream) {
        this.centralServer = new CentralServer();
        this.socket = socket;
        this.currentPort = port;
        this.threadSyncObject = threadSyncObject;
        this.cmd = command;
        this.objectInStream = objectInStream;
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
                        Node newNode = new Node(GUID, nodeIp, 8780);
                        globalTable.put(GUID, newNode);
                        Node predecessor = findPredecessor(GUID);
                        Node successor = findSuccessor2(GUID);
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
                        outObject.close();
                        socket.close();
                        break;

                    case "Upload" :
                        String fileName = objectInStream.readUTF();
                        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
                        messageDigest.reset();
                        byte[] byteArr = new byte[1020];
                        FileOutputStream fileOutputStream = new FileOutputStream(centralServer.getFileStorageFolderPath() + fileName);
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                        InputStream inputStream = socket.getInputStream();
                        int bytesRead = 0;
                        while( (bytesRead=inputStream.read(byteArr))!=-1){
                            bufferedOutputStream.write(byteArr, 0, bytesRead);
                            messageDigest.update(byteArr);                  // SHA-1 of content of the file
                        }
                        bufferedOutputStream.flush();
                        byte[] resultByteArray =  messageDigest.digest();
                        BigInteger bigNum = new BigInteger(1, resultByteArray);
                        int fileHashID = Math.abs(bigNum.intValue()) % centralServer.getMaxNodes(); 
                        bufferedOutputStream.close();
                        objectInStream.close();
                        inputStream.close();
                        fileOutputStream.close();
                        socket.close();
                        HashMap map = centralServer.getFilesMap();
                        map.put(fileName, fileHashID);
                        Node contactNode;
                        if(centralServer.getGUIDList().contains(fileHashID)){
                            contactNode =  centralServer.getGlobalTable().get(fileHashID);
                        }else {
                            contactNode = findSuccessor(fileHashID);
                        }
                        sendFile(contactNode, fileHashID, fileName);
                        break;
                    case "Search" :
                        String fileNameToSearch = objectInStream.readUTF();
                        Node requestingNode =  (Node) objectInStream.readObject();
                        Map fileHashMap = centralServer.getFilesMap();
                        int hashId = 0;
                        if(fileHashMap.get(fileNameToSearch) != null){
                            hashId = (int) fileHashMap.get(fileNameToSearch);
                            Node succ = findSuccessor(hashId);
                            Socket socket = new Socket(succ.getIp(), succ.getPort());
                            ObjectOutputStream outObject2 = new ObjectOutputStream(socket.getOutputStream());
                            outObject2.writeUTF("ProcessRequest");
                            outObject2.writeUTF(fileNameToSearch);
                            outObject2.writeObject(requestingNode);
                            outObject2.flush();
                            outObject2.close();
                        }else {
                            Socket socket = new Socket(requestingNode.getIp(), requestingNode.getPort());
                            ObjectOutputStream outObject2 = new ObjectOutputStream(socket.getOutputStream());
                            outObject2.writeUTF("PrintResult");
                            outObject2.writeUTF("File is not available");
                            outObject2.flush();
                            outObject2.close();
                        }
                        break;
                    case "Leave" :
                        Node nodeAboutToLeave = (Node) objectInStream.readObject();
                        Node succNode = (Node) objectInStream.readObject();
                        Node predNode = (Node) objectInStream.readObject();
                        List<Integer> list = centralServer.getGUIDList();
                        list.remove((Integer) nodeAboutToLeave.getGUID());
                        ConcurrentHashMap globalTableMap = centralServer.getGlobalTable();
                        globalTableMap.remove(nodeAboutToLeave.getGUID());
                        update_others2(nodeAboutToLeave, succNode, predNode);
                        break;

                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
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

    private Node findSuccessor2(int GUID) {
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

    private void update_others2(Node nodeAboutToLeave, Node succNode, Node predNode) throws IOException {

        ConcurrentHashMap globalTable = centralServer.getGlobalTable();
        ObjectOutputStream outObject;

        Collection values = globalTable.values();
        Iterator iter = values.iterator();
        while (iter.hasNext()){
            Node node = (Node) iter.next();
            Socket socket = new Socket(node.getIp(), node.getPort());
            if(socket.isConnected()) {
                outObject = new ObjectOutputStream(socket.getOutputStream());
                outObject.writeUTF("Change PredSucc");
                outObject.writeObject(nodeAboutToLeave);
                outObject.writeObject(succNode);
                outObject.writeObject(predNode);
                outObject.flush();
            }
            socket.close();
        }

    }

    private void sendFile(Node contactNode, int fileHashID, String fileName) throws IOException {
        Socket socketToUpload = new Socket(contactNode.getIp(), contactNode.getPort());
        OutputStream outputStream = socketToUpload.getOutputStream();
        ObjectOutputStream outObject = new ObjectOutputStream(outputStream);
        outObject.writeUTF("Store File");
        outObject.writeInt(fileHashID);
        outObject.writeUTF(fileName);
        outObject.flush();

        File file = new File(centralServer.getFileStorageFolderPath() + fileName);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));

        long fileLen = file.length();
        byte [] byteArr  = new byte [(int)fileLen];
        bufferedInputStream.read(byteArr,0,byteArr.length);
        outputStream.write(byteArr,0,byteArr.length);
        outputStream.flush();
        outputStream.close();
        bufferedInputStream.close();
        socketToUpload.close();
        file.delete();
    }

    private void update_others(Node newNode) throws IOException {
        ConcurrentHashMap globalTable = centralServer.getGlobalTable();
        ObjectOutputStream outObject;

        Collection values = globalTable.values();
        Iterator iter = values.iterator();
        while (iter.hasNext()){
            Node node = (Node) iter.next();
            if(newNode != node){
                Socket socket = new Socket(node.getIp(), node.getPort());
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
        int flag = 0;

        int successor = GUID;
        Iterator<Integer> iterator = GUIDList.iterator();
        while (iterator.hasNext()) {
            int nextID = iterator.next();
            if (nextID >= successor) {
                successor = nextID;
                flag = 1;
                break;
            }
        }
        if (flag == 0)
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

package edu.rit.CSCI652.impl;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.*;

/**
 * Created by payalkothari on 10/21/17.
 */
public class incomingNotifHandler extends Thread{

    private static Node newNode;

    public void run(){
        try {
        while (true){
            System.out.println("waiting for connection ");
                Socket socket = ClientNode.getServerSocket().accept();

                ObjectInputStream objectInStream = new ObjectInputStream(socket.getInputStream());
                String in = objectInStream.readUTF();

            System.out.println("accepted a connection for : " + in);
                switch (in){
                    case "New Node" :
                        System.out.println("******  New node has joined the network, updating the values......");
                        newNode = (Node) objectInStream.readObject();

                        ClientNode.setSuccessor(find_successor(ClientNode.getOwnGUID(), ClientNode.getSuccessor()));
                        ClientNode.setPredecessor(find_predecessor(ClientNode.getOwnGUID(), ClientNode.getPredecessor()));

                        update_fingerTable();
//                        check_files();
                        break;
                    case "Store File" :
                        System.out.println("Receiving a file from server");
                        int fileHashId = objectInStream.readInt();
                        String fileName = objectInStream.readUTF();
                        List filesList =  ClientNode.getFileHashIDAndNameMap().get(fileHashId);
                        if(filesList != null){
                            filesList.add(fileName);
                        }else {
                            List<String> fileList = new ArrayList<>();
                            fileList.add(fileName);
                            ClientNode.getFileHashIDAndNameMap().put(fileHashId, fileList);
                        }
                        Map map= ClientNode.getFileHashIDAndNameMap();
                        byte[] byteArr = new byte[1020];
                        FileOutputStream fileOutputStream = new FileOutputStream(ClientNode.getFileStorageFolderPath() +  fileName);
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                        InputStream inputStream = socket.getInputStream();
                        int bytesRead = 0;
                        while( (bytesRead=inputStream.read(byteArr))!=-1){
                            bufferedOutputStream.write(byteArr, 0, bytesRead);
                        }
                        bufferedOutputStream.flush();
                        bufferedOutputStream.close();
                        inputStream.close();
                        fileOutputStream.close();
                        socket.close();

                        break;
                }


            }

        }catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void check_files() throws IOException {

        Map map = ClientNode.getFileHashIDAndNameMap();
        Collection keys = map.keySet();
        Iterator iter = keys.iterator();
        while (iter.hasNext()){
            int fileHashId = (int) iter.next();
            if(fileHashId != ClientNode.getOwnGUID()){
               Node succNode = find_successor(fileHashId, ClientNode.getOwnNode());
               if(succNode != ClientNode.getOwnNode()){
                   List files = (List) map.get(fileHashId);
                   Iterator it = files.iterator();
                   while (it.hasNext()){
                       String fileName = (String) it.next();
                       send_files(succNode, fileHashId, fileName);
                   }
               }
            }
        }

    }

    private void send_files(Node contactNode, int fileHashID, String fileName) throws IOException {
        Socket socketToUpload = new Socket(contactNode.getIp(), contactNode.getPort());
        OutputStream outputStream = socketToUpload.getOutputStream();
        ObjectOutputStream outObject = new ObjectOutputStream(outputStream);
        outObject.writeUTF("Store File");
        outObject.writeInt(fileHashID);
        outObject.writeUTF(fileName);
        outObject.flush();
        System.out.println("Sending file to node : " + contactNode.getGUID());

        File file = new File(ClientNode.getFileStorageFolderPath()  + fileName);
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

    private void update_fingerTable() {

        List<FingerTableEntry> fingerTable =  ClientNode.getFingerTable();
        int tableSize = ClientNode.getMaxFingerTableSize();

        for (int i = 0; i < tableSize; i++){
            FingerTableEntry entry = fingerTable.get(i);
            int start = entry.getStart();
            entry.setSucc(find_successor(start, entry.getSucc()));
        }
    }

    private Node find_successor(int GUID, Node oldSucc) {

        int newNodeGUID = newNode.getGUID();
        int succGUID = oldSucc.getGUID();

        if(newNodeGUID == GUID){
            return newNode;
        }

        int max = Math.max(newNodeGUID, succGUID);
        int min = Math.min(newNodeGUID, succGUID);

        ArrayList<Integer> arr = new ArrayList<>();
        arr.add(min);
        arr.add(max);

        int successor = GUID;

        for(int i = 0; i < 2;i++){
            int temp = arr.get(i);
            if(temp > successor){
                successor = temp;
                break;
            }
        }

        if (successor == GUID){
            successor = min;
        }

        if(successor == newNodeGUID){
            return newNode;
        }

        return oldSucc;
    }

    private Node find_predecessor(int GUID, Node oldPred) {

        int newNodeGUID = newNode.getGUID();
        int predGUID = oldPred.getGUID();

        int max = Math.max(newNodeGUID, predGUID);
        int min = Math.min(newNodeGUID, predGUID);

        ArrayList<Integer> arr = new ArrayList<>();
        arr.add(max);
        arr.add(min);

        int predecessor = GUID;

        for(int i = 0; i < 2;i++){
            int temp = arr.get(i);
            if(temp < predecessor){
                predecessor = temp;
                break;
            }
        }

        if (predecessor == GUID){
            predecessor = max;
        }

        if(predecessor == newNodeGUID){
            return newNode;
        }

        return oldPred;
    }


}

package edu.rit.CSCI652.impl;


import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created by payalkothari on 10/10/17.
 */
public class ClientNode
{
    private static int ServerSocketPort = 8780;
    //private static String centralServerIp = "129.21.30.37";  // queeg
    private static String centralServerIp = "localhost";
    private static Node predecessor = null;
    private static Node successor = null;
    private static int ownGUID = 0;
    private static HashMap<Integer, List<String>> fileHashIDAndNameMap = new HashMap<>();
    private static String fileStorageFolderPath;
    //private static String filesOnThisMCPath = "/home/stu10/s16/pak4180/Distributed Systems/Chord/edu/rit/CSCI652/impl/";
    private static String filesOnThisMCPath = "/Users/payalkothari/Documents/DS/Chord_Project/Chord_DHT/src/edu/rit/CSCI652/impl/";
    //private static String downloadsFolderPath = "/home/stu10/s16/pak4180/Distributed Systems/Chord/edu/rit/CSCI652/impl/downloads/";
    private static String downloadsFolderPath = "/Users/payalkothari/Documents/DS/Chord_Project/Chord_DHT/src/edu/rit/CSCI652/impl/downloads/";



    public static String getDownloadsFolderPath() {
        return downloadsFolderPath;
    }


    public static HashMap<Integer, List<String>> getFileHashIDAndNameMap() {
        return fileHashIDAndNameMap;
    }

    public static String getFileStorageFolderPath() {
        return fileStorageFolderPath;
    }


    public static int getMaxFingerTableSize() {
        return maxFingerTableSize;
    }

    private static int maxFingerTableSize = 0;
    private static int maxNodesInTheNetwork = 0;
    private static Node ownNode = new Node();

    public static Node getOwnNode() {
        return ownNode;
    }

    public static List<FingerTableEntry> getFingerTable() {
        return fingerTable;
    }

    private static List<FingerTableEntry> fingerTable = new ArrayList<>();
    private static String ip = "localhost";
    private static ServerSocket serverSocket;

    public static ServerSocket getServerSocket() {
        return serverSocket;
    }

    public static int getOwnGUID() {
        return ownGUID;
    }


    public static Node getPredecessor() {
        return predecessor;
    }

    public static Node getSuccessor() {
        return successor;
    }

    public static void setPredecessor(Node predecessor) {
        ClientNode.predecessor = predecessor;
    }

    public static void setSuccessor(Node successor) {
        ClientNode.successor = successor;
    }



    public static void main(String[] args) throws IOException, ClassNotFoundException {
        serverSocket = new ServerSocket(ServerSocketPort);
        System.out.println("starting thread");
        new incomingNotifHandler().start();

//        new JSON_RPC_Subclass(ownNode, fingerTable, maxFingerTableSize).start();
        init();
    }

    private static void init() throws IOException, ClassNotFoundException {

        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.print("\n\n");
            System.out.println("***************************************************************************************");
            System.out.println("1 - Join the network");
            System.out.println("2 - Display finger table, predecessor, successor ");
            System.out.println("3 - Upload a file");
            System.out.println("4 - Show list of the files stored on this machine");
            System.out.println("5 - Search a file");
            System.out.println("6 - Leave the network");
            System.out.println("Enter an option : ");


            int option = scanner.nextInt();
            scanner.nextLine();                 // need this after reading nextInt()

            switch (option){

                case 1 :
                    System.out.println("Joining the network");
                    Socket socket = new Socket(centralServerIp, 2000);
                    ObjectInputStream objectInStream = new ObjectInputStream(socket.getInputStream());
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
                    // fileStorageFolderPath = "/home/stu10/s16/pak4180/Distributed Systems/Chord/edu/rit/CSCI652/impl/Client " + ownGUID + "FileStorage/";
                    fileStorageFolderPath = "/Users/payalkothari/Documents/DS/Chord_Project/Chord_DHT/src/edu/rit/CSCI652/impl/Client " + ownGUID + "FileStorage/";
                    File dir = new File(fileStorageFolderPath);
                    if(!dir.exists()){
                        dir.mkdirs();
                    }
                    File dir2 = new File(downloadsFolderPath);
                    if(!dir2.exists()){
                        dir2.mkdirs();
                    }
                    ownNode.setIp(reconnectSocket.getLocalAddress());
                    ownNode.setPort(ServerSocketPort);
                    predecessor = (Node) inputStream.readObject();
                    successor = (Node) inputStream.readObject();
                    ArrayList<Node> fingerTableSuccessors = new ArrayList<>();
                    for(int i = 0; i < maxFingerTableSize ; i++){
                       fingerTableSuccessors.add((Node) inputStream.readObject());
                    }
                    initializeFingerTable(fingerTableSuccessors);
                    System.out.println("Joined the network");
                    break;
                case 2 :
                    System.out.println("*******************  Finger Table  ******************** \n");
                    System.out.println("Predecessor  : " + predecessor.getGUID());
                    System.out.println("Successor  : " + successor.getGUID());
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

                case 3 :
//                    System.out.println("Uploading a file");
                    System.out.println("Enter the file name : ");
                    Socket socketToUpload = new Socket(centralServerIp, 2000);
                    ObjectInputStream objectInStream3 = new ObjectInputStream(socketToUpload.getInputStream());
                    int reconnectPort3 = objectInStream3.readInt();
//                    System.out.println("received sec port");
                    Socket reconnectSocket3 = new Socket(centralServerIp, reconnectPort3);
                    OutputStream outputStream = reconnectSocket3.getOutputStream();
                    ObjectOutputStream outObject3 = new ObjectOutputStream(outputStream);
                    outObject3.writeUTF("Upload");
                    String fileName = scanner.nextLine();
                    outObject3.writeUTF(fileName);
                    outObject3.flush();
                    File file = new File(filesOnThisMCPath + fileName);
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                    long fileLen = file.length();
                    byte [] byteArr  = new byte [(int)fileLen];
                    bufferedInputStream.read(byteArr,0,byteArr.length);
                    outputStream.write(byteArr,0,byteArr.length);
                    outputStream.flush();
                    outputStream.close();
                    outObject3.close();
                    bufferedInputStream.close();
                    reconnectSocket3.close();
                    break;

                case 4 :
                    Set<Map.Entry<Integer, List<String>>> entrySet = fileHashIDAndNameMap.entrySet();
                    Iterator<Map.Entry<Integer, List<String>>> entrySetIter = entrySet.iterator();
                    System.out.println("*********  List of the files ********* \n");
                    System.out.print(" File ID ");
                    System.out.print("\t\t File Name");
                    while (entrySetIter.hasNext()){
                        System.out.print("\n");
                        Map.Entry entry = entrySetIter.next();
                        System.out.print("   " + entry.getKey());
                        List<String> files = (List<String>) entry.getValue();
                        Iterator iter = files.iterator();
                        System.out.print("\t\t\t  ");
                        while (iter.hasNext()){
                            String name = (String) iter.next();
                            System.out.print(name + ", ");
                        }
                    }
                    break;
                case 5 :
                    System.out.println("Enter the file name");
                    String fileName1 = scanner.nextLine();
                    Socket socket5 = new Socket(centralServerIp, 2000);
                    ObjectInputStream objectInStream5 = new ObjectInputStream(socket5.getInputStream());
                    int reconnectPort5 = objectInStream5.readInt();
                    Socket reconnectSocket5 = new Socket(centralServerIp, reconnectPort5);
                    OutputStream outputStream5 = reconnectSocket5.getOutputStream();
                    ObjectOutputStream outObject5 = new ObjectOutputStream(outputStream5);
                    outObject5.writeUTF("Search");
                    outObject5.writeUTF(fileName1);
                    outObject5.writeObject(ownNode);
                    outObject5.flush();
                    break;

                case 6 :
                    File direct = new File(fileStorageFolderPath);
                    File[] list = direct.listFiles();
                    if (list != null) {
                        for (File f : list) {
                            int hashID = getFileHash(f.getName());
                            if(hashID != -1){
                                Socket socketSucc = new Socket(successor.getIp(), successor.getPort());
                                OutputStream outputStream6 = socketSucc.getOutputStream();
                                ObjectOutputStream outObjectSucc = new ObjectOutputStream(outputStream6);
                                outObjectSucc.writeUTF("Store File");
                                System.out.println("Sending file to node : " + successor.getGUID());
                                System.out.println(" Ip is : " + successor.getIp() + "  " + successor.getPort());
                                outObjectSucc.writeInt(hashID);
                                outObjectSucc.writeUTF(f.getName());
                                outObjectSucc.flush();
                                outObjectSucc.close();
                                BufferedInputStream bufferedInputStreamSucc = new BufferedInputStream(new FileInputStream(f));
                                long fLen = f.length();
                                byte [] byteArr6  = new byte [(int)fLen];
                                bufferedInputStreamSucc.read(byteArr6,0,byteArr6.length);
                                outputStream6.write(byteArr6,0,byteArr6.length);
                                outputStream6.flush();
                                outputStream6.close();
                                bufferedInputStreamSucc.close();
                                socketSucc.close();
                                f.delete();
                                System.out.println("file deleted");
                            }
                        }
                    }
                    Socket socket6 = new Socket(centralServerIp, 2000);
                    ObjectInputStream objectInStream6 = new ObjectInputStream(socket6.getInputStream());
                    int reconnectPort6 = objectInStream6.readInt();
                    Socket reconnectSocket6 = new Socket(centralServerIp, reconnectPort6);
                    OutputStream outputStream6 = reconnectSocket6.getOutputStream();
                    ObjectOutputStream outObject6 = new ObjectOutputStream(outputStream6);
                    outObject6.writeUTF("Leave");
                    outObject6.writeObject(ownNode);
                    outObject6.writeObject(successor);
                    outObject6.writeObject(predecessor);
                    outObject6.flush();
                    break;
            }

        }
    }

    private static void initializeFingerTable(ArrayList<Node> successorsList)  {
        ownNode.setFingerTable(fingerTable);

        for(int i = 0; i < maxFingerTableSize ; i++){
            FingerTableEntry entry = new FingerTableEntry();
            entry.setStart((int) ((ownGUID + Math.pow(2, i)) % maxNodesInTheNetwork));  // k + 2^i
            fingerTable.add(entry);
        }

        for(int i = 0; i < maxFingerTableSize-1 ; i++){
            FingerTableEntry entry = fingerTable.get(i);
            entry.setIntervalBegin(entry.getStart());
            FingerTableEntry nextEntry = fingerTable.get(i+1);
            entry.setIntervalEnd(nextEntry.getStart());
        }
        FingerTableEntry lastEntry = fingerTable.get(maxFingerTableSize-1);
        lastEntry.setIntervalBegin(lastEntry.getStart());
        lastEntry.setIntervalEnd(fingerTable.get(0).getStart());

        for(int i = 0; i < maxFingerTableSize ; i++){
                FingerTableEntry entry = fingerTable.get(i);
                entry.setSucc(successorsList.get(i));
        }
    }

    public static int getFileHash(String fileName) {
        Set<Map.Entry<Integer, List<String>>> entrySet = fileHashIDAndNameMap.entrySet();
        Iterator<Map.Entry<Integer, List<String>>> entrySetIter = entrySet.iterator();
        while (entrySetIter.hasNext()){
            Map.Entry entry = entrySetIter.next();
            List<String> files = (List<String>) entry.getValue();
            if(files.contains(fileName)){
                return (int) entry.getKey();
            }
        }
        return -1;
    }
}

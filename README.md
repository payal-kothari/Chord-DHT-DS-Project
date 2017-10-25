# Chord DHT

attach docker containers from - payalContainer0 - payalContainer16

In payalContainer0   // ip of this container must be 172.17.0.4                                 
cd Payal/                                       
java CentralServer                          

In payalContainer1-16                                   
cd Payal/                               
java ClientNode                             


// 32 can be replaced with any other such as 16, 64 etc.                            
// This number represents total number of nodes in a Chord network

// Note : Consider only following files
   1. CentralServer.java
   2. ThreadHandler.java
   3. ClientNode.java
   4. Node.java
   5. FingerTableEntry.java
   6. incomingNotifHandler.java


// For any problem, refresh all the docker container using following steps:                     
ssh pak4180@glados.cs.rit.edu                           
cd Distributed\ Systems/                                
chmod 777 script.sh                             
./script.sh                                     








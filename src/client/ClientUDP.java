/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author ASUS
 */
public class ClientUDP {
    private DatagramSocket socket ;
    private String SERVER_ADDRESS ;
    private int SERVER_PORT ;

    
     public ClientUDP(DatagramSocket socket, String SERVER_ADDRESS, int SERVER_PORT) {
        this.socket = socket;
        this.SERVER_ADDRESS = SERVER_ADDRESS;
        this.SERVER_PORT = SERVER_PORT;
    }
    
    private void Register(String name , String password) throws IOException{
        InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);
         String command = "";
         
    }

   
}

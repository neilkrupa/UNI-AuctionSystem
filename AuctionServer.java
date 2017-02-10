import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import java.rmi.Naming;	//Import naming classes to bind to rmiregistry
import java.util.*;
import java.lang.*;
import java.lang.reflect.*;

import java.io.*;

public class AuctionServer extends ReceiverAdapter{
  AuctionInterface s;
  String sellResponse;
  String[] updateList;
  Listing updateAuctions;
  int serverCounter = 1;
  int numberOfServers = 3;

  public AuctionServer() {
    try {
      s = new AuctionImpl();
      Naming.rebind("rmi://localhost/AuctionServiceA", s);

      Runtime.getRuntime().exec("cmd.exe /c start java AuctionServerB 1");
      Runtime.getRuntime().exec("cmd.exe /c start java AuctionServerB 2");
      Runtime.getRuntime().exec("cmd.exe /c start java AuctionServerB 3");
    }catch(Exception e) {}
  }


  JChannel channel;
  List<Listing> listings = new ArrayList<Listing>();
  List<Listing> tempListings = new ArrayList<Listing>();

   public void receive(Message msg) {
        try{
        System.out.println("message recieved...");
        String message = new String(msg.getBuffer(), "UTF-8");

        String[] split = message.split(",");

        if(split[0].equals("sell")){

          String name = split[2];
          double sPrice = Double.parseDouble(split[3]);
          double rPrice = Double.parseDouble(split[4]);
          int sellerId = Integer.parseInt(split[5]);

          sellResponse = ("sellRes," + s.addListing(name, sPrice,  rPrice,  sellerId));
          updateList = s.addListing(name, sPrice,  rPrice,  sellerId);
          updateAuctions = new Listing(s.getListId(), name, sPrice,  rPrice,  sellerId);
        }

        if(split[0].equals("sellRes")){
            if(split[1].toString().equals(updateList[0].toString())){
              serverCounter++;
            }else{
              System.out.println("Listings do not match...");
            }

          if(numberOfServers == serverCounter){
            s.setAuctions(updateAuctions);
            String btrUd = "updateSell";
            byte[] sellMessage = btrUd.getBytes();

            Message sellMsg = new Message(null, sellMessage);
            channel.send(sellMsg);
            serverCounter=0;
          }else{
            System.out.println("waiting...");
          }
        }



        }catch(Exception e) {
          System.out.println("Server Error: " + e);
        }
    }

    public void getState(OutputStream output) throws Exception {
        synchronized(listings) {
            Util.objectToStream(listings, new DataOutputStream(output));
        }
    }

    @SuppressWarnings("unchecked")
    public void setState(InputStream input) throws Exception {
        List<Listing> list=(List<Listing>)Util.objectFromStream(new DataInputStream(input));
        synchronized(listings) {
            listings.clear();
            listings.addAll(list);
        }
    }

    private void start() throws Exception {
        channel=new JChannel();
        channel.setReceiver(this);
        channel.connect("AuctionServers");
        channel.getState(null, 10000);
        eventLoop();
        channel.close();
    }

    private void eventLoop() {
      String method = "no command yet";
      while(true) {
          try{
            System.out.println(s.getAuctions());
            if(s.sCall().equals(method)){
            }else{
              method = s.sCall();
              sendMessage(method);
            }
            Thread.sleep(100);
          }catch(Exception e) {}
        }
    }

    private void sendMessage(String data) {
      System.out.println("send message");
      try {
        byte[] message = data.getBytes();

        Message msg = new Message(null, message);
        channel.send(msg);
      }catch (Exception e) {
       System.out.println("Server Error: " + e);
     }

    }



  public static void main(String args[]) {
    try {
        AuctionServer server = new AuctionServer();
        server.start();
     } 
     catch (Exception e) {
       System.out.println("Server Error: " + e);
     }
  }
}
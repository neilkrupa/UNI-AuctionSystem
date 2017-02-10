import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import java.rmi.Naming;	//Import naming classes to bind to rmiregistry
import java.util.*;
import java.io.*;

public class AuctionServerB extends ReceiverAdapter{

  public static int replicaId;
  AuctionInterface b;
  String sellResponse;
  String[] updateList;
  Listing updateAuctions;

  public AuctionServerB(int id) {
    try {
      AuctionInterface a = (AuctionInterface)
      Naming.lookup("rmi://localhost/AuctionServiceA");

      System.out.println(id);
        

      b = new AuctionImpl();
      Naming.rebind("rmi://localhost/AuctionService" + String.valueOf(id), b);

      b.setAllAuctions(a.getAuctions());
    } 
     catch (Exception e) {
       System.out.println("Server Error: " + e);
     }
   }

  JChannel channel;
  List<Listing> listings = new ArrayList<Listing>();

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

          sellResponse = ("sellRes," + name + "," + String.valueOf(sPrice) + "," +  String.valueOf(rPrice) + "," +  Integer.toString(sellerId));
          updateAuctions = new Listing(b.getListId(), name, sPrice,  rPrice,  sellerId);
          byte[] sellMessage = sellResponse.getBytes();

          Message sellMsg = new Message(msg.getSrc(), sellMessage);
          channel.send(sellMsg);
        }

        if(split[0].equals("updateSell")){
            b.setAuctions(updateAuctions);
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
        channel = new JChannel();
        channel.setReceiver(this);
        channel.connect("AuctionServers");
        channel.getState(null, 10000);
        eventLoop();
        channel.close();
    }

    private void eventLoop() {
      while(true) {
        try{
            System.out.println(b.getAuctions());
            Thread.sleep(100);
          }catch(Exception e) {}
        }
    }

  public static void main(String[] args) {
    

    try {
      AuctionServerB replicaServer = new AuctionServerB(Integer.parseInt(args[0]));
      replicaServer.start();
     } catch (Exception e) {
       System.out.println("Server Error: " + e);
     }
  }
}
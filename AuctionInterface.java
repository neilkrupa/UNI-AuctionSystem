import java.util.*;
import java.io.*;
import javax.crypto.*;

public interface AuctionInterface 
          extends java.rmi.Remote {	

    public String[] addListing(String name, double sPrice, double rPrice, int sellerId)
        throws java.rmi.RemoteException;

    public String removeListing(int id, int sellerId)
    	throws java.rmi.RemoteException;

    public String[] listAuctions()
    	throws java.rmi.RemoteException;

    public String bid(int lid, double price, int buyerId, String bidderName, String bidderEmail)
        throws java.rmi.RemoteException;

    public int reqSellerId()
        throws java.rmi.RemoteException;

    public int reqBuyerId()
        throws java.rmi.RemoteException;

    public String[] listMyAuctions(int thisSellerId)
        throws java.rmi.RemoteException;

    public double getBid(int number)
        throws java.rmi.RemoteException;

    public boolean exists(int lid)
        throws java.rmi.RemoteException;

    public ServerResponse auth(int uid, SealedObject req, String ab) 
        throws java.rmi.RemoteException;

    public void verif(byte[] cSnonse)
        throws java.rmi.RemoteException;

    public List<Listing> getAuctions()
        throws java.rmi.RemoteException;

    public void setAuctions(Listing setAucs)
        throws java.rmi.RemoteException;

    public void setAllAuctions(List<Listing> setAucs)
        throws java.rmi.RemoteException;

    public void call(String method)
        throws java.rmi.RemoteException;

    public String sCall()
        throws java.rmi.RemoteException;

    public int getListId()
        throws java.rmi.RemoteException;
}
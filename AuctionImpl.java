import java.util.*;
import java.io.*;
import javax.crypto.*;
import java.security.*;
import java.text.DecimalFormat;
import java.nio.ByteBuffer;


public class AuctionImpl
    extends java.rmi.server.UnicastRemoteObject
    implements AuctionInterface {

    // Implementations must have an explicit constructor
    // in order to declare the RemoteException exception
    private List<Listing> openAucs = new ArrayList<Listing>();
    private String[] liveAucs;
    private int serverSellerId = -1;
    private int buyerId = -1;
    public int listingId = 0;
    private DecimalFormat df = new DecimalFormat("#.##");
    private Random rand = new Random();
    private PublicKey publicKey; 
    private PrivateKey privateKey;
    private ServerResponse res;
    private int nonse;
    private PublicKey clientKey;
    public String command = "no command yet";



    public AuctionImpl()
        throws java.rmi.RemoteException {
        super();

        try{

            FileInputStream privIn = new FileInputStream("Keys/PrivateAS");
            ObjectInputStream privOb = new ObjectInputStream(privIn);
            privateKey = (PrivateKey) privOb.readObject();
            privOb.close();

            FileInputStream pubIn = new FileInputStream("Keys/PublicAS");
            ObjectInputStream pubObj = new ObjectInputStream(pubIn);
            publicKey = (PublicKey) pubObj.readObject();
            pubObj.close();

        }catch (Exception e) {
            System.out.println("Server Error: " + e);
        }
    }

    /***********************************************************************
    Recieves sealed request from client, decrypted and encrypted with public
    keys given. New challenge offered.
    ***********************************************************************/
    public ServerResponse auth(int uid, SealedObject req, String ab) {
        try{

            System.out.println("Client requesting authentication...");

            Cipher decipher = Cipher.getInstance("RSA");
            decipher.init(Cipher.DECRYPT_MODE, privateKey);

            ClientRequest clientRequest = (ClientRequest)req.getObject(decipher);

            FileInputStream clientIn = new FileInputStream("Keys/Public" + ab + uid);
            ObjectInputStream clientOb = new ObjectInputStream(clientIn);
            clientKey = (PublicKey) clientOb.readObject();
            clientOb.close();

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, clientKey);

            Signature dsa = Signature.getInstance("SHA1withRSA");
            dsa.initSign(privateKey);
            byte[] data = ByteBuffer.allocate(4).putInt(clientRequest.getNonse()).array();
            dsa.update(data);
            byte[] sCnonse = dsa.sign();
            this.nonse = rand.nextInt(100);
            SealedObject sealedNonse = new SealedObject(nonse, cipher);
            ServerResponse sResponse = new ServerResponse(sCnonse, sealedNonse);

            return sResponse;

        }catch (Exception e) {
            System.out.println("Server Error: " + e);
        }
        return res;
    }

    public void verif(byte[] cSnonse) {
        try{
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(clientKey);
            byte[] data = ByteBuffer.allocate(4).putInt(nonse).array();
            sig.update(data);
            boolean verifies = sig.verify(cSnonse);
            System.out.println(verifies);
            if (verifies == false){
                System.out.println("Client not authenticated.");
                //MAKE IT SO CLIENT IS BLOCKED FROM ACCESSING SERVER IF NOT CORRECT!
            }else{
                System.out.println("Client authenticated.");
            }
        }catch (Exception e) {
            System.out.println("Server Error: " + e);
        }
    }

    /***********************************************************************
    Add Listing to openAucs list with name of item, start price, reserve 
    price and seller ID. Write to server terminal that a listing has been
    added. Returns listing ID.
    ***********************************************************************/
    public String[] addListing(String name, double sPrice, double rPrice, int sellerId)
        throws java.rmi.RemoteException {
        Listing newListing = new Listing(this.listingId, name, sPrice, rPrice, sellerId);
        String[] response = {name, String.valueOf(sPrice), String.valueOf(rPrice), Integer.toString(sellerId)};
        return response;
    } 

    /***********************************************************************
    Remove listing takes seller id and listing id number and checks if the 
    seller owns that listing. If not then it does not remove the listing
    if it does it returns the status of the auction when closed.
    ***********************************************************************/
    public String removeListing(int id, int sellerId)
        throws java.rmi.RemoteException{
        for(int i=0; i<openAucs.size(); i++){
            if(openAucs.get(i).getLid() == id){ //Find the listing.

                if(openAucs.get(i).getSellerId() == sellerId){
                    if(openAucs.get(i).open() == false){ //If the auction is already closed.
                        return "\nListing has already been closed.";
                    }
                    if(openAucs.get(i).getResPrice() < openAucs.get(i).getPrice()){ //Check and see if the price is greater than the reserve price.
                        String closedAuction = new String(  "\nItem Sold:\nWinner ID:" + openAucs.get(i).getBidderId() + "\nWinner Name: " + openAucs.get(i).getBidderName() + "\nWinner e-mail :" + openAucs.get(i).getBidderEmail() + "\nSale Price: \u00a3" + String.valueOf(df.format(openAucs.get(i).getPrice())));
                        openAucs.get(i).close();
                        return closedAuction;
                    }else{
                        if(openAucs.get(i).getBidCount() == 0){
                            openAucs.get(i).close();
                            return "\nItem recieved no bids. Listing has been removed and the item has not been sold.";
                    }
                    openAucs.get(i).close();
                    return "\nReserve price not met. Listing has been removed and the item has not been sold.";
                    }
                }else{
                    return "\nThis listing is not yours to remove.";
                }
            }
        }
        return "\nListing has either previously been removed or does not exist. Please try again.";
    }

    /***********************************************************************
    Buyer client listing method. Returns all open auctions as a string array.
    ***********************************************************************/
    public String[] listAuctions()
        throws java.rmi.RemoteException {
        int liveAucSize = 0;
        //GET SIZE OF OPEN AUCTIONS
        for(int i = 0; i<openAucs.size(); i++){
            if(openAucs.get(i).open() == true){
                liveAucSize++;
            }
        }

        liveAucs = new String[liveAucSize*4];
        String listingBreak = new String("--------------------------");
        int conversiontInt = 0;
        for(int i=0; i<openAucs.size(); i++){
            if(openAucs.get(i).open() == true){
                liveAucs[conversiontInt] = listingBreak;
                conversiontInt++;
                liveAucs[conversiontInt] = ("ID: " + String.valueOf(openAucs.get(i).getLid()));
                conversiontInt++;
                liveAucs[conversiontInt] = ("Name: " + openAucs.get(i).getName());
                conversiontInt++;
                liveAucs[conversiontInt] = ("Current Bid: \u00a3" + String.valueOf(df.format(openAucs.get(i).getPrice())));
                conversiontInt++;
            }
        }
        return liveAucs;
    }

    /****************************************************************************
    Seller client listing method. Returns all auctions that the seller has 
    created as string array.
    ****************************************************************************/
    public String[] listMyAuctions(int sid)
        throws java.rmi.RemoteException {
        int sellerItemCount = 0;
        for(int i=0; i<openAucs.size(); i++){
            if(openAucs.get(i).getSellerId() == sid){
                sellerItemCount++;
            }
        }

        liveAucs = new String[sellerItemCount*7];
        String listingBreak = new String("--------------------------");
        int conversiontInt = 0;
        for(int i=0; i<openAucs.size(); i++){
            if(openAucs.get(i).getSellerId() == sid){
                liveAucs[conversiontInt] = listingBreak;
                conversiontInt++;
                liveAucs[conversiontInt] = ("ID: " + String.valueOf(openAucs.get(i).getLid()));
                conversiontInt++;
                liveAucs[conversiontInt] = ("Name: " + openAucs.get(i).getName());
                conversiontInt++;
                liveAucs[conversiontInt] = ("Reserve Price: \u00a3" + String.valueOf(openAucs.get(i).getResPrice()));
                conversiontInt++;
                liveAucs[conversiontInt] = ("Current Bid: \u00a3" + String.valueOf(openAucs.get(i).getPrice()));
                conversiontInt++;
                liveAucs[conversiontInt] = ("Number of bids: " + String.valueOf(openAucs.get(i).getBidCount()));
                conversiontInt++;
                liveAucs[conversiontInt] = ("Auction Live: " + String.valueOf(openAucs.get(i).open()));
                conversiontInt++;

            }
        }
        return liveAucs;
    }

    /***********************************************************************
    Buyer bidding method. Takes in listing ID, price, buyer ID, bidder name
    and bidder email. Sets new price of listing if price is greater than currnet
    bid. Return string of bid success rate.
    ***********************************************************************/
    public String bid(int lid, double price, int buyerId, String bidderName, String bidderEmail)
        throws java.rmi.RemoteException{
        if(openAucs.get(lid).getLid() == lid && openAucs.get(lid).getPrice() <= price){
            openAucs.get(lid).setPrice(price);
            openAucs.get(lid).bid();
            openAucs.get(lid).setBidderId(buyerId);
            openAucs.get(lid).setBidderEmail(bidderEmail);
            openAucs.get(lid).setBidderName(bidderName);
            return ("\nBid successful. You are now the highest bidder at: \u00a3" + openAucs.get(lid).getPrice());
        }else{
            return ("\nYou have been outbid. Please enter another bid higher than: \u00a3" + openAucs.get(lid).getPrice());
        }
    }

    /***********************************************************************
    Returns new seller ID value for seller client.
    ***********************************************************************/
    public int reqSellerId()
        throws java.rmi.RemoteException{
        serverSellerId++;
        return serverSellerId;
    }

    /***********************************************************************
     Returns new buyer ID value for buyer client.
    ***********************************************************************/
    public int reqBuyerId()
        throws java.rmi.RemoteException{
        buyerId++;
        return buyerId;
    }

    /***********************************************************************
    Returns the current price of a listing.
    ***********************************************************************/
    public double getBid(int number)
        throws java.rmi.RemoteException{
        return openAucs.get(number).getPrice();
    }

    public boolean exists(int lid){
        List<Integer> aucList = new ArrayList<Integer>();
        for(int i=0; i<openAucs.size(); i++){
            if(openAucs.get(i).open()==true){
                System.out.println(openAucs.get(i).getLid());
                aucList.add(openAucs.get(i).getLid());
            }
        }

        System.out.println(aucList);
        System.out.println(Arrays.asList(aucList).contains(lid));

        return aucList.contains(lid);
    }

    public List<Listing> getAuctions(){
        return openAucs;
    }

    public void setAuctions(Listing setAucs){
        openAucs.add(setAucs);
        this.listingId++;
    }

    public void setAllAuctions(List<Listing> setAucs){
        this.openAucs = setAucs;
    }

    public void call(String method){
        this.command = method;
    }

    public String sCall(){
        return this.command;
    }

    public int getListId(){
        return this.listingId;
    }
}

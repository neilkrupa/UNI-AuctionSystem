
import java.rmi.Naming;			//Import the rmi naming - so you can lookup remote object
import java.rmi.RemoteException;	//Import the RemoteException class so you can catch it
import java.net.MalformedURLException;	//Import the MalformedURLException class so you can catch it
import java.rmi.NotBoundException;	//Import the NotBoundException class so you can catch it
import java.util.*;
import java.io.*;
import java.text.DecimalFormat;
import javax.crypto.*;
import java.security.*;
import java.nio.ByteBuffer;


public class BuyerClient{

    public BuyerClient() {
    }

    public static void main(String[] args) {

        Random rand = new Random();
        PublicKey publicKey;
        PublicKey serverPublicKey;
        PrivateKey privateKey;

	   try{

            /***********************************************************************
            Create link to auction server via the auction interface.
            Allocate the buyer an ID.
            ***********************************************************************/
            AuctionInterface a = (AuctionInterface)
            Naming.lookup("rmi://localhost/AuctionServiceA");
            int buyerId = a.reqBuyerId();

/***********************************************************************
                                Authentication 
***********************************************************************/

            /***********************************************************************
            Read in all the public and private keys required.
            ***********************************************************************/
            FileInputStream privIn = new FileInputStream("Keys/privateB" + buyerId);
            ObjectInputStream privOb = new ObjectInputStream(privIn);
            privateKey = (PrivateKey) privOb.readObject();
            privOb.close();

            FileInputStream pubIn = new FileInputStream("Keys/PublicB" + buyerId);
            ObjectInputStream pubObj = new ObjectInputStream(pubIn);
            publicKey = (PublicKey) pubObj.readObject();
            pubObj.close();

            FileInputStream spubIn = new FileInputStream("Keys/PublicAS");
            ObjectInputStream spubObj = new ObjectInputStream(spubIn);
            serverPublicKey = (PublicKey) spubObj.readObject();
            spubObj.close();

            /***********************************************************************
            Generate a new client request encapsulating the buyer ID and the nonse.
            ***********************************************************************/
            ClientRequest clientRequest = new ClientRequest(buyerId, rand.nextInt(100));

            /***********************************************************************
            Initialize the cipher to encrypt using RSA with the servers public key.
            ***********************************************************************/
            System.out.println("Encrypting and sending connection request...");
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);

            /***********************************************************************
            Create a sealed object containing the client request encrypted with 
            servers public key. Send encrypted data to server, recieve server response.
            ***********************************************************************/
            SealedObject sealedClientReq = new SealedObject(clientRequest, cipher);
            ServerResponse serverRes = a.auth(buyerId, sealedClientReq, "B");

            /***********************************************************************
            Split server response into signed byte array and new server challenge.
            ***********************************************************************/
            byte[] clientSigResponse = serverRes.cNonse;
            SealedObject sNonse = serverRes.nonse;

            /***********************************************************************
            Initialize the decipher to use RSA and the clients private key.
            ***********************************************************************/
            Cipher decipher = Cipher.getInstance("RSA");
            decipher.init(Cipher.DECRYPT_MODE, privateKey);

            /***********************************************************************
            Decipher the nonse sent by the server.
            ***********************************************************************/
            int serverNonse = (int)sNonse.getObject(decipher);

            /*****************************************************************************
            Verify the signature of the client nonse response using the server public key.
            *****************************************************************************/
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(serverPublicKey);
            byte[] data = ByteBuffer.allocate(4).putInt(clientRequest.getNonse()).array();
            sig.update(data);

            /***********************************************************************
            If the signature is not verified then disconnect and close the client.
            ***********************************************************************/
            boolean verifies = sig.verify(clientSigResponse);
            System.out.println(verifies);
            if (verifies == false){
                System.out.println("Server not authenticated, connection closed. Please try again");
                System.exit(0);
            }else{
                System.out.println("Server authenticated, sending challenge response.");
            }

            /***********************************************************************
            If signature is verified then return signed server challenge response.
            ***********************************************************************/
            sig.getInstance("SHA1withRSA");
            sig.initSign(privateKey);
            byte[] serverChallenge = ByteBuffer.allocate(4).putInt(serverNonse).array();
            sig.update(serverChallenge);
            byte[] cSnonse = sig.sign();
            // Send signed nonse back to server
            a.verif(cSnonse);


            DecimalFormat df = new DecimalFormat("#.##");

            /* Variables for option selection.*/
            Scanner s = new Scanner(System.in);
            String option;
            boolean stop = false;
            String email;
            String bidderName;

            System.out.println("--------------------------------------------");
            System.out.println("To bid on items you must enter some details:");
            System.out.println("--------------------------------------------");
            System.out.println("Please enter your name: ");
            bidderName = s.nextLine();
            String[] splitName = bidderName.split("\\s+");
            while (bidderName.matches(".*\\d.*") || ((splitName.length <= 1) || (splitName.length > 2))){
                System.out.println("You must include your first and second name which can't contain numbers, please try again.");
                bidderName = s.nextLine();
                splitName = bidderName.split("\\s+");
            }

            /***********************************************************************
            Ask for buyer email address. Validate to ensure that an '@'' symbol is
            included.
            ***********************************************************************/
            System.out.println("Please enter your e-mail address: ");
            email = s.nextLine();
            while (email.indexOf('@') == -1) {
                System.out.println("Email address must contain an '@' symbol. Please check and try again.");
                email = s.nextLine();
            }


            /***************************************************************************
            Give the buyer a list of options they can type in for differing operations.
            ***************************************************************************/
            while(!stop){

                System.out.println("\n--------------------------");
                System.out.println("Please type an option from the following list:");
                System.out.println("bid - bid on an item.");
                System.out.println("list - list items that are for sale.");
                System.out.println("exit - exit the seller client.");
                System.out.println("--------------------------");
                /***********************************************************************
                Check the option that has been typed in by the buyer, if something is 
                typed in that is not an option ignore it and give the user the list again
                ***********************************************************************/
                option = s.nextLine();

                /***********************************************************************
                If bid is typed in request the users name ensuring that both first and
                second names are typed in.
                ***********************************************************************/
                if(option.equals("bid")){
                    System.out.println("--------------------------");

                    /***********************************************************************
                    Ask which auction the buyer wants to bid for. If it does not exist then
                    report error.
                    ***********************************************************************/
                    System.out.println("If you wish to bid on an auction please type in the ID: ");
                    int bidReq = Integer.valueOf(s.nextLine());
                    while(a.exists(bidReq)==false){
                        System.out.println("Listing does not exist, please type an existing ID number: ");
                        bidReq = Integer.valueOf(s.nextLine());
                    }

                    /***********************************************************************
                    Ask for price the buyer wants to bid. Validate to ensure it is in the 
                    format value.## as well as ensuring that the bid is over the current 
                    price of the item.
                    ***********************************************************************/
                    System.out.println("Please enter the amount of your bid (Including pence): ");
                    boolean format = false;
                    String bidPrice = s.nextLine();
                    while (format == false){
                        if (bidPrice.matches("([0-9]+)([.]{1})([0-9]{2})")){
                            format = true;
                        }else{
                            System.out.println("Incorrect format, please enter a price, including pence.");
                            bidPrice = s.nextLine();
                        }
                    }
                    double priceReq = Double.valueOf(bidPrice);

                    while (priceReq <= a.getBid(bidReq)){
                        System.out.println("Bid needs to be higher than the original price of: \u00a3" + a.getBid(bidReq) + ". Type exit to cancel.");
                        String optOut = s.nextLine();
                        if (optOut.equals("exit")){
                            break;
                        }else{
                            format = false;
                            while (format == false){
                                if (optOut.matches("([0-9]+)([.]{1})([0-9]{2})")){
                                    format = true;
                                    priceReq = Double.valueOf(optOut);
                                }else{
                                    System.out.println("Incorrect format, please enter a price, including pence.");
                                    optOut = s.nextLine();
                                }
                            }
                        }
                    }

                    /***********************************************************************
                    Display the bidding information and send bid to server. Display whether
                    the bid was successful or not.
                    ***********************************************************************/
                    if (priceReq > a.getBid(bidReq)){
                        System.out.println(" Auction ID: " + bidReq + "\n Bid Price: \u00a3" + df.format(priceReq) + "\n Buyer ID: " + buyerId + "\n Bidder Name: " + bidderName + "\n Bidder Email: " + email);
                    }
                    System.out.println(a.bid(bidReq, priceReq, buyerId, bidderName, email));
                }

                /***********************************************************************
                If list is typed in display all listings.
                ***********************************************************************/
                if(option.equals("list")){
                    String[] liveAuctions = a.listAuctions();

                    System.out.println("\nLive Auctions:");
                    for(int i=0; i<liveAuctions.length; i++){
                        System.out.println(liveAuctions[i]);
                    }
                }

                /***********************************************************************
                If exit is typed in stop the options loop and exit the interface.
                ***********************************************************************/
                if(option.equals("exit")){
                    System.out.println("");
                    System.out.println("Thank you - Goodbye");
                    stop = true;
                }
            }
        }catch (Exception e) {
            System.out.println("Exception"+e); 
        }
    }

}


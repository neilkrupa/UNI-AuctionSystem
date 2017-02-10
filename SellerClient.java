
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


public class SellerClient {

    public SellerClient() {
    }

    public static void main(String[] args) {

        Random rand = new Random();
        PublicKey publicKey;
        PublicKey serverPublicKey;
        PrivateKey privateKey;

	   try{

	   	/***********************************************************************
        Create link to auction server via the auction interface.
        Allocate the seller an ID.
        ***********************************************************************/
        AuctionInterface a = (AuctionInterface)
        Naming.lookup("rmi://localhost/AuctionServiceA");
        int sellerId = a.reqSellerId();

/***********************************************************************
                                Authentication 
***********************************************************************/

            /***********************************************************************
            Read in all the public and private keys required.
            ***********************************************************************/
            FileInputStream privIn = new FileInputStream("Keys/privateS" + sellerId);
            ObjectInputStream privOb = new ObjectInputStream(privIn);
            privateKey = (PrivateKey) privOb.readObject();
            privOb.close();

            FileInputStream pubIn = new FileInputStream("Keys/PublicS" + sellerId);
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
            ClientRequest clientRequest = new ClientRequest(sellerId, rand.nextInt(100));

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
            ServerResponse serverRes = a.auth(sellerId, sealedClientReq, "S");

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
        boolean stop = false;
        String option;

        /***************************************************************************
		Give the seller a list of options they can type in for differing operations.
        ***************************************************************************/
        while(!stop){

        	System.out.println("\n--------------------------");
        	System.out.println("Please type an option from the following list:");
        	System.out.println("sell - List a new item to sell.");
        	System.out.println("close - remove an item previously listed.");
        	System.out.println("list - list items you are selling.");
        	System.out.println("exit - exit the seller client.");
        	System.out.println("--------------------------");

        	/***********************************************************************
        	Check the option that has been typed in by the seller, if something is 
        	typed in that is not an option ignore it and give the user the list again
        	***********************************************************************/
        	option = s.nextLine();

        	/***********************************************************************
        	If sell is typed in ask for the name of the item being sold and the price.
        	Validate the price to ensure that it is in the correct format.
        	***********************************************************************/
        	if(option.equals("sell")){
        		System.out.println("\nWhat is the item you would like to list?");
        		String name = s.nextLine();
        		System.out.println("What is the price you would like to start the auction at?");
        		String strPrice = s.nextLine();
        		double sPrice =0;
        		boolean format = false;
        		while (format == false){
                    if (strPrice.matches("([0-9]+)([.]{1})([0-9]{2})")){
                        format = true;
                        sPrice = Double.valueOf(strPrice);
                    }else{
                        System.out.println("Incorrect format, please enter a price, including pence.");
                        strPrice = s.nextLine();
                    }
                }

                /***********************************************************************
        		Validate the  reserve price to ensure that it is in the correct format
        		and that it is more than the starting price.
        		***********************************************************************/
        		double rPrice = 0;
        		System.out.println("What is the reserve price of the item?");
        		strPrice = s.nextLine();
        		format = false;
        		while (format == false){
                    if (strPrice.matches("([0-9]+)([.]{1})([0-9]{2})")){
                        format = true;
                        rPrice = Double.valueOf(strPrice);
                    }else{
                        System.out.println("Incorrect format, please enter a price, including pence.");
                        strPrice = s.nextLine();
                    }
                }

        		while(sPrice>rPrice){
        			System.out.println("Reserve price must be equal to or greater than the starting price. Please enter a reserve price which is equal to or greater than the starting price.");
        			strPrice = s.nextLine();
        			format = false;
        			while (format == false){
                    	if (strPrice.matches("([0-9]+)([.]{1})([0-9]{2})")){
                        	format = true;
                        	rPrice = Double.valueOf(strPrice);
                    	}else{
                        	System.out.println("Incorrect format, please enter a price, including pence.");
                        	strPrice = s.nextLine();
                   		}
                	}
        		}

        		/******************************************************************************************
        		Show the user their listing and add it to the server calling [serverInterface].addListing()
        		*******************************************************************************************/
        		System.out.println("\nYour Listing is as follows:");
        		System.out.println("Name: " + name);
        		System.out.println("Start Price: \u00a3" + df.format(sPrice));
        		System.out.println("Reserve Price: \u00a3" + df.format(rPrice));
                String caller = ("sell,a.addListing,"+ name + "," + sPrice + " ," + rPrice + "," + sellerId);
                a.call(caller);
        		a.addListing(name, sPrice, rPrice, sellerId);
        	}

        	/****************************************************************************
        	If close is typed ask what that id of the listing to be closed is. 
        	If the seller owns that listing then close it, if not then deny them access.
        	****************************************************************************/
        	if(option.equals("close")){
        		System.out.println("");
        		System.out.println("What is the ID of the listing you would like to close?");
        		int remove = Integer.parseInt(s.nextLine());
        		System.out.println(a.removeListing(remove, sellerId));
        	}

        	/***********************************************************************
        	If list is typed in display all listings associated with that seller.
        	***********************************************************************/
        	if(option.equals("list")){
        		String[] liveAuctions = a.listMyAuctions(sellerId);

        		System.out.println("\nItems I am selling (open or closed auctions):");
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
import java.io.*;

public class Listing implements Serializable{
	
	private String name;
	private int listingID = 0;
	private double sPrice = 0;
	private double lPrice = 0;
	private double rPrice = 0;
	private boolean open = true;

	private int bidderId = 0;
	private int bidCount = 0;
	private String bidderName;
	private String bidderEmail;


	private int sellerId = 0;

	/*Listing constructor*/
	public Listing(int listingID, String name, double sPrice, double rPrice, int sellerId){
		this.name = name;
		this.listingID = listingID;
		this.sPrice = sPrice;
		this.lPrice = sPrice;
		this.rPrice = rPrice;
		this.sellerId = sellerId;
	}

	/*Return listing ID*/
	public int getLid(){
		return listingID;
	}

	/*Return listing name*/
	public String getName(){
		return name;
	}

	/*Return listing price*/
	public double getPrice(){
		return lPrice;
	}

	/*Set listing price to price param*/
	public void setPrice(double price){
		this.lPrice = price;
	}

	/*Return listing reserve price*/
	public double getResPrice(){
		return rPrice;
	}

	/*Return listing bidder ID*/
	public int getBidderId(){
		return bidderId;
	}
	
	/*Return listing bidder Email*/
	public String getBidderEmail(){
		return bidderEmail;
	}

	/*Return listing bidder name*/
	public String getBidderName(){
		return bidderName;
	}

	/*Set bidder ID to bidder ID param*/
	public void setBidderId(int bidderId){
		this.bidderId = bidderId;
	}

	/*Set bidder email to bidderEmail param*/
	public void setBidderEmail(String bidderEmail){
		this.bidderEmail = bidderEmail;
	}

	/*Set bidder name to bidderName param*/
	public void setBidderName(String bidderName){
		this.bidderName = bidderName;
	}

	/*increase listing bid count*/
	public void bid(){
		this.bidCount++;
	}

	/*Return listing bid count*/
	public int getBidCount(){
		return bidCount;
	}

	/*Return the seller ID*/
	public int getSellerId(){
        return sellerId;
    }

    /*Sec autcion to closed*/
    public void close(){
    	this.open=false;
    }

    public boolean open(){
    	return this.open;
    }

}
import java.io.*;

public class JgroupsMessage implements Serializable{
	
	private String type;
	private Listing listing;
	private int alive;
	private int index;

	public JgroupsMessage(String type, Listing listing){
		this.type = type;
		this.listing = listing;
		
	}

	public JgroupsMessage(String type, Listing listing, int index){
		this.type = type;
		this.listing = listing;
		this.index = index;
		
	}

	public JgroupsMessage(String type, int alive){
		this.type = type;
		this.alive = alive;
		
	}

	public String getType(){
		return this.type;
	}

	public Listing getListing(){
		return this.listing;
	}

	public int getIndex(){
		return this.index;
	}



}
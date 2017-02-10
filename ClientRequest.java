
import java.io.*;
import javax.crypto.*;
import java.security.*;

public class ClientRequest implements Serializable{
	private int id;
	private int nonse;

  public ClientRequest(int id, int nonse) {
  	this.id = id;
  	this.nonse = nonse;
  }

  public int getNonse(){
  	return nonse;
  }

}
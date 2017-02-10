import java.io.*;
import javax.crypto.*;
import java.security.*;

public class ServerResponse implements Serializable{
	public byte[] cNonse;
	public SealedObject nonse;

  public ServerResponse(byte[] cNonse, SealedObject nonse) {
  	this.cNonse = cNonse;
  	this.nonse = nonse;
  }

  public SealedObject getNonse(){
  	return nonse;
  }

}
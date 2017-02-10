import java.security.*;
import java.io.*;

import javax.crypto.Cipher;

public class GenKey {
  public static void main(String[] args) throws Exception {

    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    SecureRandom random = new SecureRandom();
    keyGen.initialize(2048, random);
    KeyPair pair = keyGen.generateKeyPair();
    PrivateKey priv = pair.getPrivate();
    PublicKey pub = pair.getPublic();

    /* save the public key in a file */
    byte[] key = pub.getEncoded();
    FileOutputStream keyfos = new FileOutputStream("publicB3");
    ObjectOutputStream out = new ObjectOutputStream(keyfos);
    out.writeObject(pub);
    out.close();

    /* save the private key in a file */
    byte[] pkey = priv.getEncoded();
    FileOutputStream pkeyfos = new FileOutputStream("privateB3");
    ObjectOutputStream pout = new ObjectOutputStream(pkeyfos);
    pout.writeObject(priv);
    pout.close();
  }
}
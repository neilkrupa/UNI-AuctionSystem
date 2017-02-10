import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import java.io.*;
import java.util.List;
import java.util.LinkedList;

public class SimpleChat extends ReceiverAdapter {
    JChannel channel1;
    String user_name=System.getProperty("user.name", "n/a");
    final List<String> state = new LinkedList<String>();


    public void receive(Message msg) {
        String line=": " + msg.getObject();
        System.out.println(line);
        synchronized(state) {
            state.add(line);
        }
    }

    public void getState(OutputStream output) throws Exception {
        synchronized(state) {
            Util.objectToStream(state, new DataOutputStream(output));
        }
    }

    @SuppressWarnings("unchecked")
    public void setState(InputStream input) throws Exception {
        List<String> list=(List<String>)Util.objectFromStream(new DataInputStream(input));
        synchronized(state) {
            state.clear();
            state.addAll(list);
        }
    }


    private void start() throws Exception {
        channel1=new JChannel();
        channel1.setReceiver(this);
        channel1.connect("ChatCluster");
        channel1.getState(null, 10000);
        eventLoop();
        channel1.close();
    }

    private void eventLoop() {
        BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            try {
                System.out.print("Check Life:" + user_name); 
                System.out.flush();
                Thread.sleep(1000);
                String line = "Still alive";
                Message msg=new Message(null, line);
                channel1.send(msg);
            }
            catch(Exception e) {
            }
        }
    }


    public static void main(String[] args) throws Exception {
        new SimpleChat().start();
    }
}
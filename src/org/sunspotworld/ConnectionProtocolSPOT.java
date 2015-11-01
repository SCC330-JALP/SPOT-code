package org.sunspotworld;

import com.sun.spot.io.j2me.radiogram.Radiogram;
import com.sun.spot.io.j2me.radiostream.RadiostreamConnection;
import com.sun.spot.util.Utils;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.io.Connector;

/**
 * @author Povilas Marcinkevicius
 * @version 1.1.2
 */
public class ConnectionProtocolSPOT
{
  // Make sure these match with the PC version!
  public static final String STREAM_KILL = "kill";
  public static final String STREAM_CONN = "strm";
  private static final int MAX_FOUND_CONNS = 20;
  private static final int FIND_CONNS_TIME = 5000;
  
  public static final int PORT_BASE_SEARCH = 33;
  public static final int PORT_BASE_SEARCH_RESPONSE = 34;
  public static final int PORT_TO_PC_DATAGRAMS = 35;
  public static final int STREAM_PORT = 36;
  
  public static String getClosestBaseMac()
  {
    class Entry
    {
      int zone;
      String addr;
      int signalStr;
      
      public Entry(int zone, String addr, int signalStr)
      {
        this.zone = zone;
        this.addr = addr;
        this.signalStr = signalStr;
      }
    }

    // Ask to introduce and listen; No longer applicable; Bases broadcast constantly
    /*ConnectionSPOT singleConn = new ConnectionSPOT(ConnectionSPOT.BROADCAST, PORT_BASE_SEARCH, 127);
    try
    { singleConn.getNewRadiogram().writeUTF(FIND_CONNS); }
    catch(IOException e)
    { ConnectionSPOT.throwError(e, 3, 1); }
    singleConn.send();
    singleConn.close();*/
    
    final ConnectionSPOT conn = new ConnectionSPOT(ConnectionSPOT.LISTEN, PORT_BASE_SEARCH_RESPONSE, 10);
    final Entry responses[] = new Entry[MAX_FOUND_CONNS];
    final int responseNum[] = {0};

    Thread listener = new Thread(new Runnable()
    {
      public void run()
      {
        while(true) try
        {
          Radiogram radiogram = conn.receive(); 
          responses[responseNum[0]] = new Entry(radiogram.readInt(), radiogram.getAddress(), radiogram.getRssi());
          responseNum[0]++;
        }
        catch(Exception e)
        { return; } // Just stop when interrupted
      }
    });

    listener.start();
    Utils.sleep(FIND_CONNS_TIME);
    listener.interrupt();
    conn.close(); // Should close automatically, but to be safe

    // No responses - return null
    if(responseNum[0] == 0)
      return null;

    // Select the strongest response and return connection to it
    Entry bestEntry = responses[0];
    for(int i = 1; i < responseNum[0]; i++)
      if(responses[i].signalStr > bestEntry.signalStr)
        bestEntry = responses[i];

    return bestEntry.addr;
  }
  
  // MUST call getClosestConnection first
  public static DataOutputStream getOutputStreamConn(ConnectionSPOT connection, String command)
  {
    try
    {
      connection.getNewRadiogram().writeUTF(STREAM_CONN + command);
      connection.send();

      DataOutputStream stream = ((RadiostreamConnection) Connector.open("radiostream://" + connection.address + ":" + STREAM_PORT)).openDataOutputStream();
      Blinker.blinkAndWait(1500, 0, 0, 0, 255, 60, 1); // 0011 1100 BLUE LEDs
      return stream;
    }
    catch(IOException e)
    {
      Blinker.blinkAndWait(1500, 0, 255, 0, 0, 60, 1); // 0011 1100 RED LEDs
      return null;
    }
  }
}
package org.sunspotworld;

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.io.j2me.radiostream.RadiostreamConnection;
import com.sun.spot.util.Utils;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.io.Connector;

/**
 * @author Povilas Marcinkevicius
 * 
 * @version 1.0.0
 */
public class ConnectionSPOT
{
  public static final String BROADCAST = "broadcast";
  public static final String LISTEN = "";
  
  private static final String FIND_CONNS = "find";
  private static final String STREAM_CONN = "strm";
  private static final int MAX_FOUND_CONNS = 20;
  private static final int FIND_CONNS_TIME = 5000;
  
  public static final int PORT_BASE_SEARCH = 33;
  public static final int PORT_BASE_SEARCH_RESPONSE = 34;
  public static final int PORT_TO_PC_DATAGRAMS = 35;
  public static final int STREAM_PORT = 36;
  
  private RadiogramConnImpl conn;
  private Radiogram radiogram;
  
  private static final ConnectionSPOT broadcastOnBaseSearchConn = new ConnectionSPOT(BROADCAST, PORT_BASE_SEARCH, 127);

  public ConnectionSPOT(String addr, int port, int radiogramSize)
  {
    try
    {
      conn = (RadiogramConnImpl) Connector.open("radiogram://" + addr + ":" + port);
      radiogram = new Radiogram(radiogramSize, conn);
    }
    catch(IOException e)
    { throwError(e, 18); }
  }
  
  public Radiogram receive() throws IOException
  {
    conn.receive(radiogram);
    return radiogram;
  } // SPOTError.throwError(e, 34);
  
  public void send()
  {
    try
    { conn.send(radiogram); }
    catch (IOException e)
    { throwError(e, 50); }
  }
  
  public void close()
  {
    try
    { conn.close(); }
    catch(IOException e)
    { throwError(e, 66); }
  }
  
  public Radiogram getNewRadiogram()
  {
    radiogram.reset();
    return radiogram;
  }
  
  public static ConnectionSPOT getClosestConnection(int packetSize, int port)
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

    // Ask to introduce and listen
    sendSingleUTFDatagram(FIND_CONNS);
    final ConnectionSPOT conn = new ConnectionSPOT(LISTEN, PORT_BASE_SEARCH_RESPONSE, 10);
    final Entry responses[] = new Entry[MAX_FOUND_CONNS];
    final int responseNum[] = new int[1];
    responseNum[0] = 0;

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
        catch(IOException e)
        { /* Nothing as it's the interrupt */ }
      }
    });

    listener.start();
    Utils.sleep(FIND_CONNS_TIME);
    listener.interrupt();

    // No responses - return null
    if(responseNum[0] == 0)
    {
      Blinker.blink(1000, 1000, 0, 0, 255, 195, 3); // 1100 0011 BLUE LEDs
      return null;
    }

    // Select the strongest response and return connection to it
    Entry bestEntry = responses[0];
    for(int i = 1; i < responseNum[0]; i++)
      if(responses[i].signalStr > bestEntry.signalStr)
        bestEntry = responses[i];

    Blinker.blinkAndWait(800, 200, 0, 0, 255, responseNum[0], 1);
    Blinker.blinkAndWait(800, 200, 0, 0, 255, bestEntry.zone, 1);
    conn.close();
    return new ConnectionSPOT(bestEntry.addr, port, packetSize);
  }
  
  // MUST call getClosestConnection first
  public DataOutputStream getOutputStreamConn()
  {
    DataOutputStream stream = null;
    
    try
    {
      getNewRadiogram().writeUTF(STREAM_CONN);
      send();

      stream = ((RadiostreamConnection) Connector.open("radiostream://" + conn.getMacAddress() + ":" + STREAM_PORT)).openDataOutputStream();
    }
    catch(IOException e)
    { throwError(e, 82); }
    
    Blinker.blinkAndWait(1500, 0, 0, 0, 255, 60, 1); // 0011 1100 BLUE LEDs
    return stream;
  }
  
  public static void sendSingleUTFDatagram(final String text)
  {
    Radiogram radiogram = broadcastOnBaseSearchConn.getNewRadiogram();
    try
    { radiogram.writeUTF(text); }
    catch(IOException e)
    { throwError(e, 98); }

    broadcastOnBaseSearchConn.send();
  }
  
  public static final void throwError(Exception e, int leds)
  {
    ConnectionSPOT.sendSingleUTFDatagram(e.toString());
    Blinker.blinkAndWait(1000, 1000, 255, 0, 0, leds, 1000);
  }
}
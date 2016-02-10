package org.sunspotworld;

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.util.Utils;
import java.io.IOException;
import javax.microedition.io.Connector;

/**
 * @author Povilas Marcinkevicius
 * @version 1.0.4
 */
public class RadiogramConnectionSPOT
{
  public static final String BROADCAST = "broadcast";
  public static final String LISTEN = "";
  
  private RadiogramConnImpl conn;
  private Radiogram radiogram;
  private boolean sending = false; // acts as a lock between getNewRadiogram() and send()
  
  public RadiogramConnectionSPOT(String addr, int port, int radiogramSize)
  {
    try
    {
      conn = (RadiogramConnImpl) Connector.open("radiogram://" + addr + ":" + port);
      radiogram = new Radiogram(radiogramSize, conn);
    }
    catch(IOException e)
    { Blinker.blinkAndWaitError(e, 2, 1); }
  }
  
  public synchronized Radiogram receive() throws IOException
  {
    conn.receive(radiogram);
    return radiogram;
  }
  
  public synchronized void send()
  {
    try
    { conn.send(radiogram); }
    catch (IOException e)
    { Blinker.blinkAndWaitError(e, 2, 2); }
    finally
    { sending = false; }
  }
  
  public void close()
  {
    try
    { conn.close(); }
    catch(IOException e)
    { Blinker.blinkAndWaitError(e, 2, 3); }
  }
  
  public synchronized Radiogram getNewRadiogram()
  {
    try
    { while(sending) Thread.sleep(50); }
    catch (InterruptedException e)
    { /* not fatal in any way; ignore */ }
    sending = true;
    radiogram.reset();
    return radiogram;
  }

  public static void sendSingleUtfMessage(String addr, int port, String message)
  {
    RadiogramConnectionSPOT responseConn = new RadiogramConnectionSPOT(addr, port, message.length() + 10);

    try
    {
      responseConn.getNewRadiogram().writeUTF(message);
      responseConn.send();
    }
    catch(IOException e)
    { e.printStackTrace(); }
    
    responseConn.close();
  }
  
  // Unless I copy data from radiograms to such data structure right away, they don't function properly. Strange. But neccessary
  public static class RadiogramRef
  {
    public String address;
    public int rssi;
    public String utf;
    public RadiogramRef(Radiogram radiogram)
    {
      try
      {
        this.address = radiogram.getAddress();
        this.rssi = radiogram.getRssi();
        this.utf = radiogram.readUTF();
      } catch(IOException e)
      { e.printStackTrace(); }
    }
  }
  
  public static RadiogramRef[] receiveMessagesForTime(String address, int port, final int maxResponses, int searchTimeMs)
  {
    final RadiogramConnectionSPOT conn = new RadiogramConnectionSPOT(address, port, 127);
    final RadiogramRef responses[] = new RadiogramRef[maxResponses];
    final int responseNum[] = {0};

    Thread listener = new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          while(true)
          {
            if(responseNum[0] != maxResponses)
            {
              responses[responseNum[0]] = new RadiogramRef(conn.receive());
              responseNum[0]++;
            }
          }
        }
        catch(IOException e)
        { return; } // Just stop when interrupted
      }
    });

    listener.start();
    Utils.sleep(searchTimeMs);
    listener.interrupt();
    conn.close(); // Should close automatically, but to be safe
    
    // Trim to results only and converts to Radiograms
    RadiogramRef[] toReturn = new RadiogramRef[responseNum[0]];
    for(int i = 0; i < responseNum[0]; i++)
      toReturn[i] = responses[i];
    
    return toReturn;
  }
}
package org.sunspotworld;

import com.sun.spot.io.j2me.radiogram.*;
import java.io.IOException;
import javax.microedition.io.Connector;

/**
 * @author Povilas Marcinkevicius
 * @version 1.0.3
 */
public class ConnectionSPOT
{
  public static final boolean PRINT_REMOTE = true;
  public static ConnectionSPOT printRemoteConn = null;
  
  public static final String BROADCAST = "broadcast";
  public static final String LISTEN = "";
  
  private RadiogramConnImpl conn;
  private Radiogram radiogram;
  
  public String address;
  
  public ConnectionSPOT(String addr, int port, int radiogramSize)
  {
    try
    {
      address = addr;
      conn = (RadiogramConnImpl) Connector.open("radiogram://" + addr + ":" + port);
      radiogram = new Radiogram(radiogramSize, conn);
    }
    catch(IOException e)
    { Blinker.blinkAndWaitError(2, 1); }
  }
  
  public Radiogram receive() throws IOException
  {
    conn.receive(radiogram);
    return radiogram;
  }
  
  public void send()
  {
    try
    { conn.send(radiogram); }
    catch (IOException e)
    { Blinker.blinkAndWaitError(2, 2); }
  }
  
  public void close()
  {
    try
    { conn.close(); }
    catch(IOException e)
    { Blinker.blinkAndWaitError(2, 3); }
  }
  
  public Radiogram getNewRadiogram()
  {
    radiogram.reset();
    return radiogram;
  }
  
  public static final void printRemote(String message)
  {
    if(PRINT_REMOTE)
    {
      if(printRemoteConn == null)
        printRemoteConn = new ConnectionSPOT(BROADCAST, ConnectionProtocolSPOT.PORT_BASE_SEARCH, 127);
       
      try
      {
        printRemoteConn.getNewRadiogram().writeUTF(message);
        printRemoteConn.send();
      }
      catch(IOException e)
      { Blinker.blinkAndWaitError(2, 4); }
    }
  }
}
package org.sunspotworld;

import com.sun.spot.io.j2me.radiogram.Radiogram;
import java.io.IOException;

/**
 * @author Povilas Marcinkevicius
 * @version 1.2.0
 */
public class ConnectionProtocolSPOT
{
  private static final int MAX_FOUND_CONNS = 20;
  private static final int FIND_CONNS_TIME = 500;
  
  public static final String REQUEST_INTRODUCTION = "intr";
  public static final String REQUEST_ESTABLISH_CONNECTION = "conn";
  private static final String RESPONSE_CONFIRM_CONNECTION = "cnok";
  
  public static final int PORT_NEW_SPOT_LISTEN  = 33;
  public static final int PORT_NEW_SPOT_RESPOND = 34;
  public static final int PORT_LISTEN_SPOT_DATA = 36;
  public static final int PORT_COMMAND_RELAY    = 37;
  
  private static String lastBestAddress = null;
  
  private static String findClosestBase()
  {
    RadiogramConnectionSPOT.sendSingleUtfMessage(RadiogramConnectionSPOT.BROADCAST, PORT_NEW_SPOT_LISTEN, REQUEST_INTRODUCTION);
    RadiogramConnectionSPOT.RadiogramRef[] radiograms = RadiogramConnectionSPOT.receiveMessagesForTime(RadiogramConnectionSPOT.LISTEN, PORT_NEW_SPOT_RESPOND, MAX_FOUND_CONNS, FIND_CONNS_TIME);

    // No responses - return null
    if(radiograms.length == 0)
      return null;
    
    // Select the strongest response and return connection to it
    RadiogramConnectionSPOT.RadiogramRef bestSignal = radiograms[0];
    for(int i = 0; i < radiograms.length; i++)
      if(radiograms[i].rssi > bestSignal.rssi)
        bestSignal = radiograms[i];
    return bestSignal.address;
  }
  
  // OK but a bit bloated / inellegant
  public static StreamConnectionOut connectToClosestBase()
  {
    String bestConnection = null;
    while(bestConnection == null)
    {
      Blinker.blinkAndWait(500, 0, 0, 0, 128, 1, 1); // 1000 0000 BLUE 1X
      bestConnection = findClosestBase();
    }
    Blinker.blinkAndWait(500, 0, 0, 0, 128, 3, 1); // 1100 0000 BLUE 1X

    // Establishing stream connection to base with the best entry data
    boolean ok = false;
    while(!ok)
    {
      Blinker.blinkAndWait(500, 0, 0, 0, 128, 7, 1); // 1110 0000 BLUE 1X
      RadiogramConnectionSPOT.sendSingleUtfMessage(bestConnection, PORT_NEW_SPOT_LISTEN, REQUEST_ESTABLISH_CONNECTION);
      RadiogramConnectionSPOT.RadiogramRef[] response = RadiogramConnectionSPOT.receiveMessagesForTime(bestConnection, PORT_NEW_SPOT_RESPOND, 1, 1000);
      if(response.length != 0 && response[0].utf.equals(RESPONSE_CONFIRM_CONNECTION))
      {
        Blinker.blinkAndWait(500, 0, 0, 128, 0, 15, 1); // 1111 0000 GREEN 1X
        ok = true;
        StreamConnectionOut stream = new StreamConnectionOut(response[0].address, PORT_LISTEN_SPOT_DATA);
        lastBestAddress = response[0].address;
        return stream;
      }
    }
    System.err.println("Error while connecting to Base: this portion of code is not supposed to be reached.");
    return null;
  }

  public static StreamConnectionIn connectToClosestBaseIn()
  {
    if(lastBestAddress == null)
      throw new IllegalArgumentException("must call connectToClosestBase() first");
    return new StreamConnectionIn(lastBestAddress, PORT_COMMAND_RELAY);
  }
}
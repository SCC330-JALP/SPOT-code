package org.sunspotworld;

/**
 * @author Povilas Marcinkevicius
 *
 * The purpose is making sure the SPOT does not interchangeably connect between two bases
 */
public class ZoneChecker
{
  private int minConsecutiveMatches;
  private int consecutiveMatches = 0;
  private String curAddress;
  private String trackedAddress;
  
  public ZoneChecker(int matchAtLeastNCases)
  {
    this.minConsecutiveMatches = matchAtLeastNCases;
    this.curAddress = null;
    this.trackedAddress = null;
    this.consecutiveMatches = 0;
  }
  
  public boolean shouldIConnectToThis(String closestAddress)
  {
    if(closestAddress == null)
      return false;
    
    if(curAddress == null)
    {
      curAddress = closestAddress;
      trackedAddress = closestAddress;
    }
    
    if(closestAddress.equals(trackedAddress))
    {
      consecutiveMatches++;
      if(consecutiveMatches >= minConsecutiveMatches && !curAddress.equals(trackedAddress))
      {
        curAddress = trackedAddress;
        return true;
      }
      return false;
    }
    else
    {
      trackedAddress = closestAddress;
      consecutiveMatches = 1;
      return false;
    }
  }
}

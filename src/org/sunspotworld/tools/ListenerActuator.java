/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sunspotworld.tools;

/**
 * @author @author Povilas Marcinkevicius
 * @version 1.0.0
 * 
 * Instance of listener to sensor readings
 */
public interface ListenerActuator
{
  public void onListenerTrigger(double value, byte type);
}

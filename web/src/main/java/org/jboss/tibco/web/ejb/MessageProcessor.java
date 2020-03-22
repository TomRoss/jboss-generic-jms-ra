/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.tibco.web.ejb;

import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 *
 * @author tomr
 */

public interface MessageProcessor {

    public void sendMessages(String destiationName,String messageText,int messageNumber) throws JMSException, NamingException;
    
}

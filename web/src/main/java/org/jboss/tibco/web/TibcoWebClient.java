package org.jboss.tibco.web;


import javax.annotation.Resource;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import org.jboss.logging.Logger;

/**
 * Created by tomr on 22/01/15.
 */

@WebServlet(name = "TibcoWebClient",urlPatterns = "/WebClient",description = "Send messages to TIBCO destination.")
public class TibcoWebClient extends HttpServlet {
   Logger LOG = Logger.getLogger(TibcoWebClient.class);

    @Resource(name = "${tibco.external.context}")
    private InitialContext externalContext;

    @Resource(name = "${tibco.qcf}")
    private QueueConnectionFactory qcf;
    private QueueConnection queueConnection = null;

    private Queue queue;
    private QueueSession queueSession = null;
    private MessageProducer msgProducer = null;
    private QueueSender queueSender = null;
    private TextMessage txtMsg = null;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String destinationName = null;
        String destinationType = null;
        String messageText = null;
        String messageNumber = null;
        int msgNum = 0;


        response.setContentType("text/html; charset=UTF-8");

        PrintWriter out = response.getWriter();

        try {

            out.println("<!DOCTYPE html>");
            out.println("<html><head>");
            out.println("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
            out.println("<title>Tibco Servlet</title></head>");
            out.write("<body><h1 align='center'>TIBCO WEB Client</h1>");

            Enumeration names = request.getParameterNames();


            while(names.hasMoreElements()){

                String name = (String) names.nextElement();

                if (name.equals("message_number")){

                    messageNumber = request.getParameter(name);

                    msgNum = Integer.parseInt(messageNumber);
                }

                if (name.equals("message_text")){

                    messageText = request.getParameter(name);
                }

                if (name.equals("destination_type")){

                    destinationType = request.getParameter(name);
                }

                if (name.equals("destination_name")){

                    destinationName = request.getParameter(name);
                }

            }

            try {

                Object obj = externalContext.lookup(destinationName);

                queue = (Queue) obj;

                queueConnection = qcf.createQueueConnection("admin", "admin");

                out.println("<h5>Connection created.</h5>");

                queueSession = queueConnection.createQueueSession(true,Session.SESSION_TRANSACTED);

                out.println("<h5>Session created.</h5>");

                txtMsg = queueSession.createTextMessage("Hello TIBCO.");

                queueSender = queueSession.createSender(queue);

                queueSender.send(txtMsg);

                out.println("<h5>Message sent.</h5>");

                queueSession.commit();

            } catch ( JMSException jmsEx){

               LOG.error("JMS error",jmsEx);

            } catch (NamingException nmEx) {

               LOG.error("Maning error looking up.",nmEx);

            } finally {

                try {

                    if (queueSender != null){

                        queueSender.close();
                    }

                    if (queueSession != null){

                        queueSession.close();
                    }

                    if (queueConnection != null){

                        queueConnection.close();
                    }
                } catch (JMSException jmsEx){

                   LOG.warn("Error cleaning up JMS resources");
                }
            }

        } finally {

            out.println("<input type='button' value='Back' onclick='history.go(-1);return true;\'/>");

            out.println("</body></html>");

            out.close();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        processRequest(request, response);

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        processRequest(request, response);

    }

    public <T> T getObject(String url) throws NamingException {
        Object obj = null;
        InitialContext ctx = null;

        try {

            //lock.lock();

            ctx = new InitialContext();

            if (ctx != null){

                obj = ctx.lookup(url);

            }

        } finally{

            if ( ctx != null){

                ctx.close();

            }

            //lock.unlock();

        }

        return (T) obj;
    }
}

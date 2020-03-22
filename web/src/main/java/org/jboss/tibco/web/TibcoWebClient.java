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

/**
 * Created by tomr on 22/01/15.
 */

@WebServlet("/WebClient")
public class TibcoWebClient extends HttpServlet {

    //@Resource(name = "${tibco.external.context}")
    private Context externalContext;

    // this connection comes from RA not TIBCO broker
    @Resource(name = "java:/tibco/jms/cf/XAQueueConnectionFactory")
    private QueueConnectionFactory qcf;
    private QueueConnection queueConnection = null;

    // this comes from TIBCO JNDI lookup
    @Resource(name = "java:global/jms/tibco/queue/inQueue")
    private Queue queue;
    private QueueSession queueSession = null;
    private MessageProducer msgProducer = null;
    private QueueSender queueSender = null;
    private TextMessage txtMsg = null;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {



        response.setContentType("text/html; charset=UTF-8");

        PrintWriter out = response.getWriter();

        try {

            out.println("<!DOCTYPE html>");
            out.println("<html><head>");
            out.println("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
            out.println("<title>Tibco Servlet</title></head>");
            out.write("<body><h1 align=\"center\">TIBCO WEB Client</h1>");

            try {

                queueConnection = qcf.createQueueConnection("admin", "quick123+");

                out.println("<h5>Connection created.</h5>");

                queueSession = queueConnection.createQueueSession(true,Session.SESSION_TRANSACTED);

                out.println("<h5>Session created.</h5>");

                txtMsg = queueSession.createTextMessage("Hello TIBCO.");

                queueSender = queueSession.createSender(queue);

                queueSender.send(txtMsg);

                out.println("<h5>Message sent.</h5>");

                queueSession.commit();

            } catch ( JMSException jmsEx){



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

                }
            }

        } finally {

            out.println("<a href='userInput.html'>BACK</a>");

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

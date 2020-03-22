<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <style type="text/css">
            
        </style>
        
        <title>TIBCO Web Client</title>
    </head>
    <body>
        <h1 align="center">TIBCO JMS Web Client.</h1>
        <form method="post" action="${pageContext.request.contextPath}/WebClient">
            <fieldset>
                <legend>Destination</legend>
                <label>Destination Name:</label><input type="text" name="destination_name" value="/jms/queue/testQueue"/><br/>
                <input type="radio" name="destination_type" value="javax.jms.Queue" checked="checked"/>Queue
                <input type="radio" name="destination_type" value="javax.jms.Topic" />Topic
            </fieldset>
            <fieldset>
                <legend>Messages</legend>
                <label>Message Number:</label>
                <input type="text" name="message_number" value="1"/><br/>
                <fieldset>
                    <legend>Message Text</legend>
                    <textarea name="message_text" rows="5" cols="20" title="Message Text">This is test message</textarea>
                </fieldset>
            </fieldset>
            <br/>
            <input type="submit" value="Publish"/>
            <input type="reset" value="Reset"/>
         
        </form>
    </body>
</html>

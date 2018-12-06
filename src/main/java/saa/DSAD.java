import java.util.Date;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.JmsUtils;

public class SimpleMessageProducer {

    protected static JmsTemplate jmsTemplate;

    protected static int numberOfMessages = 10;

    public static void sendMessages() throws JMSException {
        //configure and starts connection to AMQ
        Connection connection;
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        jmsTemplate = new JmsTemplate(connectionFactory);
        connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        final Destination queue =
                jmsTemplate.getDestinationResolver().resolveDestinationName( session, "FOO.TEST", false );

        //Send Messages
        StringBuilder payload = null;
        for (int i = 0; i < numberOfMessages; ++i) {
            payload = new StringBuilder();
            payload.append("Message [").append(i).append("] sent at: ").append(new Date());
            final String msg = payload.toString();
            final int index = i;

            jmsTemplate.send(queue,new MessageCreator() {
                public Message createMessage(Session session) throws JMSException {
                    TextMessage message = session.createTextMessage(msg.toString());
                    message.setIntProperty("messageCount", index);
                    System.out.println("Sending message [" + index + "] to tcp://localhost:61616 ["+queue+"]");
                    return message;
                }
            });
        }

        //Close ressources
        JmsUtils.closeConnection(connection);
    }

    public static void main(String[] args) {
        try {
            sendMessages();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
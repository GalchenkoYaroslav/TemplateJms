package saa;

import com.ibm.mq.jms.MQConnectionFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.JmsUtils;

import javax.jms.*;
import java.util.Date;

@SpringBootApplication
@EnableJms
public class SimpleMessageProducer {

    protected static JmsTemplate jmsTemplate;

    protected static int numberOfMessages = 10;

    public static void sendMessages() throws JMSException {
        //configure and starts connection to AMQ
        Connection connection;



        MQConnectionFactory connectionFactory = new MQConnectionFactory();

        connectionFactory.setHostName("localhost");
        connectionFactory.setPort(1414);
        connectionFactory.setQueueManager("QM1");
        connectionFactory.setChannel("DEV.APP.SVRCONN");



        jmsTemplate = new JmsTemplate(connectionFactory);
        connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        final Destination queue =
                jmsTemplate.getDestinationResolver().resolveDestinationName( session, "DEV.QUEUE.1", false );

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
package service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendMsg {

    //private final static String QUEUE_NAME = "threadExQ";
    private final static String EXCHANGE_NAME = "Exchange";
    private static ConnectionFactory factory;
    private static Connection conn = null;
    private static ArrayBlockingQueue<Channel> blockingQueue;

    public static void init() {
        factory = new ConnectionFactory();
//        factory.setVirtualHost("/");
//        factory.setHost("ec2-54-200-205-6.us-west-2.compute.amazonaws.com");
//        factory.setPort(5672);
        factory.setHost("localhost");
        blockingQueue = new ArrayBlockingQueue<>(20);

        try {
            conn = factory.newConnection();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 20; i++) {
            try {
                Channel channel = conn.createChannel();
                 channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
                channel.queueDeclare(EXCHANGE_NAME, true, false, false, null);
                blockingQueue.add(channel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void send(String message)  {
        try {
            // channel per thread
            Channel channel = blockingQueue.take();
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes(StandardCharsets.UTF_8));
            //channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            blockingQueue.add(channel);
        } catch (IOException e) {
            Logger.getLogger(SendMsg.class.getName()).log(Level.SEVERE, null, e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        try {
            while (!blockingQueue.isEmpty()) {

                Channel channel = blockingQueue.take();
                channel.close();
            }
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}

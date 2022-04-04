import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiveMsg implements Runnable{

    //private final static String QUEUE_NAME = "threadExQ";
    private final static String EXCHANGE_NAME = "Exchange";
    private static ConnectionFactory factory;
    private static Connection connection = null;
    private LiftRideDao liftRideDao = new LiftRideDao();

    static {
        factory = new ConnectionFactory();
        factory.setVirtualHost("/");
        factory.setHost("54.213.180.81");
        factory.setPort(5672);
//        factory.setHost("localhost");
        try {
            connection = factory.newConnection();
            System.out.println("Rabbitmq server connects successfully");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            final Channel channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, EXCHANGE_NAME, "");
//            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            // max one message per receiver
            channel.basicQos(20);
            System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                Type type = new TypeToken<HashMap<String, Integer>>(){}.getType();
                Map<String, Integer> param = new Gson().fromJson(message, type);
                LiftRideSkierDto liftRideSkierDto = LiftRideSkierDto.convertToLiftRideSkierDto(param);
                //liftRideDao.createLiftRide(liftRideSkierDto);
                liftRideDao.getRideSkiers().add(liftRideSkierDto);

                // System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
            };
            // process messages
            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });
        } catch (IOException ex) {
            Logger.getLogger(ReceiveMsg.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] argv) throws Exception {
        new LiftRideDao().start();
        ExecutorService executor = Executors.newFixedThreadPool(50);
        executor.submit(new ReceiveMsg());


    }


}

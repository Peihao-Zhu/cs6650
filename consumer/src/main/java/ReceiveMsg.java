import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiveMsg implements Runnable{
    private final static String QUEUE_NAME = "threadExQ";
    // key -> skier ID
    // value -> lift ID
    private static ConcurrentHashMap<Integer, List<Integer>> hashMap;
    private static ConnectionFactory factory;
    private static Connection connection = null;

    static {
        hashMap = new ConcurrentHashMap();
        factory = new ConnectionFactory();
//        factory.setVirtualHost("/");
//        factory.setHost("ec2-54-200-205-6.us-west-2.compute.amazonaws.com");
//        factory.setPort(5672);
        factory.setHost("localhost");
        try {
            connection = factory.newConnection();
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
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            // max one message per receiver
            channel.basicQos(20);
            System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                Type type = new TypeToken<HashMap<String, Integer>>(){}.getType();
                Map<String, Integer> param = new Gson().fromJson(message, type);
                int skier = param.get("skierID");
                if(!hashMap.containsKey(skier)) {
                    hashMap.put(skier, new ArrayList<>());
                }
                hashMap.get(skier).add(param.get("liftID"));

                // System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
            };
            // process messages
            channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
        } catch (IOException ex) {
            Logger.getLogger(ReceiveMsg.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public static void main(String[] argv) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(20);
        executor.submit(new ReceiveMsg());

    }
}

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.codec.JsonJacksonCodec;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiveMsg implements Runnable{
    private final static String EXCHANGE_NAME = "Exchange";
    //private final static String QUEUE_NAME = "threadExQ";
    private static ConnectionFactory factory;
    private static Connection connection = null;
    private static RedissonClient client;

    // establish connection with rabbitmq server
    static {
        factory = new ConnectionFactory();
        factory.setVirtualHost("/");
        factory.setHost("54.213.180.81");
        factory.setPort(5672);
//         factory.setHost("localhost");
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
                int skier = param.get("skierID");
//                if(!hashMap.containsKey(skier)) {
//                    hashMap.put(skier, new ArrayList<>());
//                }
//                hashMap.get(skier).add(param.get("liftID"));
//                storeData_v1(param);
                LiftRideSkierDto liftRideSkierDto = LiftRideSkierDto.convertToLiftRideSkierDto(param);
                storeData_v2(liftRideSkierDto);

                // System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
            };
            // process messages
            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });
        } catch (IOException ex) {
            Logger.getLogger(ReceiveMsg.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // model1
    private void storeData_v2(LiftRideSkierDto liftRideSkierDto) {

        RList<LiftRideSkierDto> list = client.getList(String.valueOf(liftRideSkierDto.getSkierID()), new JsonJacksonCodec());
        list.add(liftRideSkierDto);

    }

    // model2
    private void storeData_v1(Map<String, Integer> param) {
        // System.out.println("store data into redis");

        int skirID = param.get("skierID");
        int resortID = param.get("resortID");
        int seasonID = param.get("seasonID");
        int dayID = param.get("dayID");
        int liftID = param.get("liftID");
        String seasonIDStr = String.valueOf(seasonID);
        String dayIDStr = String.valueOf(dayID);

        // question “For skier N, how many days have they skied this season?”
        // key -> skier id
        // value -> map
        RMap<Integer, Map<String, Set<Integer>>> map = client.getMap("skied-days-in-season", new CompositeCodec(StringCodec.INSTANCE, new JsonJacksonCodec()));
        Map<String, Set<Integer>> days = map.getOrDefault(skirID, new HashMap<>());
        days.putIfAbsent(seasonIDStr, new HashSet<>());
        days.get(seasonIDStr).add(dayID);
        map.put(skirID, days);



        // question “For skier N, what are the vertical totals for each ski day?” (calculate vertical as liftID*10)
        // key -> skier id
        // value -> map  key1  - > day id  value1 -> vertical
        RMap<Integer, Map<String, Integer>> map1 = client.getMap("vertical-skied-days", new CompositeCodec(StringCodec.INSTANCE, new JsonJacksonCodec()));
        Map<String, Integer>  verticalMap = map1.getOrDefault(skirID, new HashMap<>());
        verticalMap.put(dayIDStr, verticalMap.getOrDefault(dayIDStr, 0) + liftID * 10);
        map1.put(skirID, verticalMap);

        // question “For skier N, show me the lifts they rode on each ski day”
        // key -> skier id
        // value -> map  key1  - > day id  value1 -> lifts number
        RMap<Integer, Map<String, Integer>> map2 = client.getMap("lifts-skied-days", new CompositeCodec(StringCodec.INSTANCE, new JsonJacksonCodec()));
        Map<String, Integer>  liftsMap =  map2.getOrDefault(skirID,  new HashMap<>());
        liftsMap.put(dayIDStr, liftsMap.getOrDefault(dayIDStr, 0) + 1);
        map2.put(skirID, liftsMap);


    }

    public static void setupRedis() {
//        Config config = new Config();
//        config.useSingleServer()
//                .setAddress("redis://54.213.180.81:6379");

        client = Redisson.create();
        System.out.println("Redis connects successfully!!");
    }


    public static void main(String[] argv) throws Exception {
        setupRedis();
        ExecutorService executor = Executors.newFixedThreadPool(50);
        executor.submit(new ReceiveMsg());

    }


}

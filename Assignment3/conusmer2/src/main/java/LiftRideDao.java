import com.mysql.cj.jdbc.SuspendableXAConnection;
import lombok.Data;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Data
public class LiftRideDao extends Thread{
    // private static BasicDataSource dataSource;
    private static BlockingQueue<LiftRideSkierDto> rideSkierDtoBlockingQueue = new LinkedBlockingQueue<>();;
    private static final int batchSize = 2000;
    // 20s 2min
    private static final int timeInterval = 120000;

    public BlockingQueue<LiftRideSkierDto> getRideSkiers() {
        return rideSkierDtoBlockingQueue;
    }

    public LiftRideDao() {

    }

    @Override
    public void run() {
        createLiftRide();
    }

    private void createLiftRide() {

        String insertQueryStatement = "INSERT INTO LiftRides (skierId, resortId, seasonId, dayId, time, liftId) " +
                "VALUES (?,?,?,?,?,?)";
        List<LiftRideSkierDto> data = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        long preTime = System.currentTimeMillis();
        int size = 0;
        while(true) {
            try {
                if(!data.isEmpty() && (data.size() == batchSize || (System.currentTimeMillis() - preTime >= timeInterval) )) {
                    executor.submit(new WriteThread(insertQueryStatement, data));
                    // new Thread(new WriteThread(insertQueryStatement, data)).start();
                    data = new ArrayList<>();
                    preTime = System.currentTimeMillis();
                }
                LiftRideSkierDto liftRideSkierDto = rideSkierDtoBlockingQueue.poll(5, TimeUnit.SECONDS);
                if(liftRideSkierDto != null) data.add(liftRideSkierDto);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
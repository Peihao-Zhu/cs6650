import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WriteThread implements Runnable{


    String insertQueryStatement;
    static BasicDataSource dataSource = DBCPDataSource.getDataSource();
    List<LiftRideSkierDto> data;
    static AtomicInteger count = new AtomicInteger(0);

    public WriteThread(String insertQueryStatement, List<LiftRideSkierDto> data) {
        this.insertQueryStatement = insertQueryStatement;
        this.data = data;
    }

    @Override
    public void run() {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            conn = dataSource.getConnection();
            preparedStatement = conn.prepareStatement(insertQueryStatement);
            conn.setAutoCommit(false);
            for(LiftRideSkierDto liftRideSkierDto : data) {
                preparedStatement.setInt(1, liftRideSkierDto.getSkierID());
                preparedStatement.setInt(2, liftRideSkierDto.getResortID());
                preparedStatement.setInt(3, liftRideSkierDto.getSeasonID());
                preparedStatement.setInt(4, liftRideSkierDto.getDayID());
                preparedStatement.setInt(5, liftRideSkierDto.getTime());
                preparedStatement.setInt(6, liftRideSkierDto.getLiftID());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            conn.commit();
//            count.getAndAdd(data.size());
//            System.out.println("batch insert  " + data.size()  +" " + count.get());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

    }
}

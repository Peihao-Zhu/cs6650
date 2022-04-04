import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// construct message got from rabbitmq to this object
public class LiftRideSkierDto implements Serializable {
    private int resortID;
    private int seasonID;
    private int dayID;
    private int skierID;
    private int time;
    private int liftID;
    private int waitTime;


    public static LiftRideSkierDto convertToLiftRideSkierDto(Map<String, Integer> map) {
        LiftRideSkierDto liftRideSkierDto = new LiftRideSkierDto();
        liftRideSkierDto.setLiftID(map.get("liftID"));
        liftRideSkierDto.setSkierID(map.get("skierID"));
        liftRideSkierDto.setDayID(map.get("dayID"));
        liftRideSkierDto.setResortID(map.get("resortID"));
        liftRideSkierDto.setTime(map.get("time"));
        liftRideSkierDto.setWaitTime(map.get("waitTime"));
        liftRideSkierDto.setSeasonID(map.get("seasonID"));
        return liftRideSkierDto;
    }
}

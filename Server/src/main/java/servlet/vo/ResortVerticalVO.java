package servlet.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class ResortVerticalVO {
    class ResortVertical {
        int seasonID;
        int totalVert;
    }
    List<ResortVertical> resortVerticals = new ArrayList<>();
}

package servlet.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ResortsVO {
    private List<Resort> resorts;
}

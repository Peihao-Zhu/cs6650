package servlet.vo;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class EndpointsStat {
    private String URL;
    private String operation;
    private int mean;
    private int max;
}
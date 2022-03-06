package servlet.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SeasonsVO {
    private List<String>  seasons;
}

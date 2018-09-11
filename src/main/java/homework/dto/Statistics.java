package homework.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Statistics {

    private Map<String, Integer> eventTypeCounters;
    private Map<String, Integer> wordCounters;
}

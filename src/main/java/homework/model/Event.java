package homework.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Event {

    @JsonProperty("event_type")
    private String eventType;
    private String data;
    private long timestamp;
}

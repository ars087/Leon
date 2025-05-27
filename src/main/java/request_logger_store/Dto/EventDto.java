package request_logger_store.Dto;

import java.time.LocalDateTime;

public class EventDto {
    private LocalDateTime dateTime;

    public EventDto(LocalDateTime localDateTime) {
        this.dateTime = localDateTime;
    }

    public EventDto() {
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "EventDto{" +
            "localDateTime=" + dateTime +
            '}';
    }
}

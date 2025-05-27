package request_logger_store.Dto;

import java.util.List;

public class EventListDto {
    private List<EventDto> eventDto;

    public EventListDto() {
    }

    public EventListDto(List<EventDto> eventDto) {
        this.eventDto = eventDto;
    }

    public List<EventDto> getEventDto() {
        return eventDto;
    }

    public void setEventDto(List<EventDto> eventDto) {
        this.eventDto = eventDto;
    }
}

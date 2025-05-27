package request_logger_store.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import request_logger_store.Dto.EventDto;
import request_logger_store.config.mapper.Mapper;
import request_logger_store.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;

@RestController
public class EventController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    private final EventRepository eventRepository;
    private final Mapper mapper;

    public EventController(EventRepository eventRepository, Mapper mapper) {
        this.eventRepository = eventRepository;
        this.mapper = mapper;
    }

    @GetMapping("/all-events")
    public ResponseEntity<List<EventDto>> getAllEventsFlat() {
        logger.info(">>>EventController");
        try {
            List<String> allData = eventRepository.findAllDataOnly();
            List<EventDto> result = new ArrayList<>();
            for (String json : allData) {
                List<EventDto> events = mapper.objectMapper().readValue(json, new TypeReference<>() {
                });
                result.addAll(events);
            }
            return ResponseEntity.ok(result);
        } catch (JsonProcessingException e) {
            logger.error("<<<<<<<Ошибка десериализации JSON из БД", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("<<<<<<<<<<<Неожиданная ошибка при обработке запроса /flat", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
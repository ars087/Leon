package request_logger_store.service;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import request_logger_store.Dto.EventDto;
import request_logger_store.Dto.EventListDto;
import request_logger_store.service.kafka.KafkaProducer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProcessorEvent {

    private static final Logger logger = LoggerFactory.getLogger(ProcessorEvent.class);

    private final LinkedBlockingQueue<EventDto> eventQueue = new LinkedBlockingQueue<>();

    private final KafkaProducer kafkaProducer;

    public ProcessorEvent(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    /**
     * Периодически собирает события и отправляет их в Kafka пакетом,
     * если количество достигло порога.
     */
    @Scheduled(initialDelay = 4000, fixedDelay = 1000)
    public void processAndSendEvents() {
        try {

            EventDto event = new EventDto( LocalDateTime.now());
             eventQueue.offer(event);
             logger.debug(">>>>>>>>>Добавлено событие в очередь. Текущий размер: {}", eventQueue.size());

            if (eventQueue.size() == 10) {
                List<EventDto> events = new ArrayList<>();
                eventQueue.drainTo(events);
                logger.info(">>>>>>>>>>Отправке в продюсер");
                kafkaProducer.sendEventToBroker(new EventListDto(events));
            }

        } catch (Exception ex) {
            logger.error("Ошибка при обработке события {}", ex.getMessage());
        }
    }

}

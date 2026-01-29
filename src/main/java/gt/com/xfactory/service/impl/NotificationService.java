package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.response.NotificationDto;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@ApplicationScoped
@Slf4j
public class NotificationService {

    private final ConcurrentHashMap<UUID, List<BroadcastProcessor<NotificationDto>>> processors = new ConcurrentHashMap<>();

    public Multi<NotificationDto> register(UUID doctorId) {
        log.info("Registering SSE stream for doctor: {}", doctorId);
        BroadcastProcessor<NotificationDto> processor = BroadcastProcessor.create();
        processors.computeIfAbsent(doctorId, k -> new CopyOnWriteArrayList<>()).add(processor);

        return processor.onTermination().invoke(() -> unregister(doctorId, processor));
    }

    public void unregister(UUID doctorId, BroadcastProcessor<NotificationDto> processor) {
        log.info("Unregistering SSE stream for doctor: {}", doctorId);
        List<BroadcastProcessor<NotificationDto>> list = processors.get(doctorId);
        if (list != null) {
            list.remove(processor);
            if (list.isEmpty()) {
                processors.remove(doctorId);
            }
        }
    }

    public void notify(UUID doctorId, NotificationDto notification) {
        log.info("Sending notification to doctor {}: {}", doctorId, notification.getType());
        List<BroadcastProcessor<NotificationDto>> list = processors.get(doctorId);
        if (list != null) {
            for (BroadcastProcessor<NotificationDto> processor : list) {
                processor.onNext(notification);
            }
        }
    }
}

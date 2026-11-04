package info.syachiku.roomslice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoomAvailabilityController {
    private final RoomAvailabilityService service;

    public RoomAvailabilityController(RoomAvailabilityService service) {
        this.service = service;
    }

    @GetMapping("/rooms/{roomId}")
    public Availability availability(@PathVariable("roomId") String roomId) {
        return new Availability(roomId, service.isAvailable(roomId));
    }

    public record Availability(String roomId, boolean available) {}
}

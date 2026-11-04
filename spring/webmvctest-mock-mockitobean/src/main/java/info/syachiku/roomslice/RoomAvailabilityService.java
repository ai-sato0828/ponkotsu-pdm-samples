package info.syachiku.roomslice;

import org.springframework.stereotype.Service;

@Service
public class RoomAvailabilityService {
    public boolean isAvailable(String roomId) {
        return "R-100".equals(roomId);
    }
}

package info.syachiku.postgres;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IncomingEventRepository extends JpaRepository<IncomingEvent, Long> {
    @Query(value = """
            select id, payload
              from incoming_event
             where payload ->> 'status' = :status
             order by id
            """, nativeQuery = true)
    List<IncomingEvent> findByPayloadStatus(@Param("status") String status);
}

package example.shipping;

import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
class ShippingNoteService {

    private final ShippingNoteRepository notes;

    ShippingNoteService(ShippingNoteRepository notes) {
        this.notes = notes;
    }

    @Transactional(readOnly = true)
    NoteView get(long id) {
        return NoteView.from(find(id));
    }

    @Transactional
    NoteView update(long id, Long expectedVersion, String memo) {
        ShippingNote note = find(id);
        if (!Objects.equals(note.getVersion(), expectedVersion)) {
            throw new StaleShippingNoteException();
        }

        note.changeMemo(memo);
        notes.flush();
        return NoteView.from(note);
    }

    private ShippingNote find(long id) {
        return notes.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    record NoteView(Long id, Long version, String memo) {
        static NoteView from(ShippingNote note) {
            return new NoteView(note.getId(), note.getVersion(), note.getMemo());
        }
    }

    static class StaleShippingNoteException extends RuntimeException {
    }
}

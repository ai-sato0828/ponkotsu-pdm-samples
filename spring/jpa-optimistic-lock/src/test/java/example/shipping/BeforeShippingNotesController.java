package example.shipping;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class BeforeShippingNotesController {

    private final ShippingNoteRepository notes;

    BeforeShippingNotesController(ShippingNoteRepository notes) {
        this.notes = notes;
    }

    @PutMapping("/before/shipping-notes/{id}")
    @Transactional
    ShippingNoteService.NoteView update(@PathVariable long id,
                                        @RequestBody @Valid ShippingNotesController.UpdateNote request) {
        ShippingNote note = notes.findById(id).orElseThrow();
        note.changeMemo(request.memo());
        notes.flush();
        return ShippingNoteService.NoteView.from(note);
    }
}

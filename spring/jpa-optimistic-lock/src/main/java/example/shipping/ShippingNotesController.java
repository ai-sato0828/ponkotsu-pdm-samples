package example.shipping;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shipping-notes")
class ShippingNotesController {

    private final ShippingNoteService service;

    ShippingNotesController(ShippingNoteService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    ShippingNoteService.NoteView get(@PathVariable long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    ShippingNoteService.NoteView update(@PathVariable long id, @RequestBody @Valid UpdateNote request) {
        return service.update(id, request.version(), request.memo());
    }

    record UpdateNote(
            @NotNull @PositiveOrZero Long version,
            @NotBlank @Size(max = 200) String memo) {
    }
}

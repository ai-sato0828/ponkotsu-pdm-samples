package info.syachiku.audit;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RequestService {
    private final RequestSubmissionRepository repository;

    RequestService(RequestSubmissionRepository repository) {
        this.repository = repository;
    }

    long create(RequestForm form) {
        return repository.save(new RequestSubmission(null, form.title())).getId();
    }

    long createOnce(RequestForm form) {
        String key = form.requestKey();
        if (key == null || key.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "申請キーがありません");
        }

        try {
            return repository.saveAndFlush(new RequestSubmission(key, form.title())).getId();
        } catch (DataIntegrityViolationException failure) {
            // The repository's failed insert has already rolled back.
            RequestSubmission saved = repository.findByRequestKey(key).orElseThrow(() -> failure);
            if (!saved.getTitle().equals(form.title())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "同じキーの申請内容が異なります");
            }
            return saved.getId();
        }
    }

    RequestSubmission find(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}

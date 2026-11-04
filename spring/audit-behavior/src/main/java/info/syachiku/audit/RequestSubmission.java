package info.syachiku.audit;

import jakarta.persistence.*;

@Entity
@Table(name = "request_submissions")
public class RequestSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String requestKey;

    @Column(nullable = false)
    private String title;

    protected RequestSubmission() {
    }

    RequestSubmission(String requestKey, String title) {
        this.requestKey = requestKey;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}

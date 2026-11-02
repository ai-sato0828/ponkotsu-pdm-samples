package info.syachiku.testing;

public final class Inquiry {
    public enum Status { DRAFT, RECEIVED, IN_PROGRESS, COMPLETED }

    private final long creatorId;
    private final Long assigneeId;
    private Status status;

    public Inquiry(long creatorId, Long assigneeId, Status status) {
        this.creatorId = creatorId;
        this.assigneeId = assigneeId;
        this.status = java.util.Objects.requireNonNull(status);
    }

    public boolean start(long actorId) {
        if (assigneeId == null || assigneeId.longValue() != actorId
                || status != Status.RECEIVED) return false;
        status = Status.IN_PROGRESS;
        return true;
    }

    public Status status() { return status; }
    public long creatorId() { return creatorId; }
    public Long assigneeId() { return assigneeId; }
}

package info.syachiku.reference;

// Measurement-only holder: values survive a thrown exception, not a live Entity.
public final class ReferenceTrace {
    public long referenceSql = -1;
    public long idSql = -1;
    public long nameSql = -1;
    public long failedSql = -1;
    public Boolean initializedAfterReference;
    public Boolean initializedAfterId;
    public Boolean initializedAfterName;
    public Long id;
    public String name;
    public String failedAt;
    public String failureClass;
}

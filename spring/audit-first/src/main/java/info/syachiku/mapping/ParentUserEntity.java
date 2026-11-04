package info.syachiku.mapping;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class ParentUserEntity {
    @Id
    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "INSERT_DATE")
    private String insertDate;

    @Column(name = "UPDATE_DATE")
    private String updateDate;

    protected ParentUserEntity() {
    }

    protected ParentUserEntity(String userId, String insertDate, String updateDate) {
        this.userId = userId;
        this.insertDate = insertDate;
        this.updateDate = updateDate;
    }
}

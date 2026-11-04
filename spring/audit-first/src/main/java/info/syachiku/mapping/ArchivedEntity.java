package info.syachiku.mapping;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ARCHIVED_USERS")
public class ArchivedEntity extends ParentUserEntity {
    protected ArchivedEntity() {
    }

    public ArchivedEntity(String userId, String insertDate, String updateDate) {
        super(userId, insertDate, updateDate);
    }
}

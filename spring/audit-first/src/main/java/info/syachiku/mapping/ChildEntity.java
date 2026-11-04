package info.syachiku.mapping;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ACTIVE_USERS")
public class ChildEntity extends ParentUserEntity {
    protected ChildEntity() {
    }

    public ChildEntity(String userId, String insertDate, String updateDate) {
        super(userId, insertDate, updateDate);
    }
}

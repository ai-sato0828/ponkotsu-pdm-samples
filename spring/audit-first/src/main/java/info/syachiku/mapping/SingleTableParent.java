package info.syachiku.mapping;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "SHARED_USERS")
public abstract class SingleTableParent {
    @Id
    private String userId;

    protected SingleTableParent() {
    }

    protected SingleTableParent(String userId) {
        this.userId = userId;
    }
}

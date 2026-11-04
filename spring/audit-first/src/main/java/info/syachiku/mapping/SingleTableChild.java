package info.syachiku.mapping;

import javax.persistence.Entity;

@Entity
public class SingleTableChild extends SingleTableParent {
    protected SingleTableChild() {
    }

    public SingleTableChild(String userId) {
        super(userId);
    }
}

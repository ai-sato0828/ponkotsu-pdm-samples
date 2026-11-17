package info.syachiku.listcopy;

import java.util.List;

// BEGIN userModel
public final class User {
    private final String id;
    private String name;
    private final List<String> tags;

    public User(String id, String name, List<String> tags) {
        this.id = id;
        this.name = name;
        this.tags = tags;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getTags() { return tags; }
}
// END userModel

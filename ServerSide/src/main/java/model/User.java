package model;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class User {
    @BsonProperty("_id")
    private ObjectId id;
    private String name;
    private String hashedPassword;
    private List<ClientItem> checkedOutItems = new ArrayList<>();

    public User() {

    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ClientItem> getCheckedOutItems() {
        return checkedOutItems;
    }

    public void setCheckedOutItems(List<ClientItem> checkedOutItems) {
        this.checkedOutItems = checkedOutItems;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", checkedOutItems=" + checkedOutItems +
                '}';
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
}

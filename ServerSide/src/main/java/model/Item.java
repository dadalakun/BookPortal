package model;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

public class Item {
    @BsonProperty("_id")
    private ObjectId id;
    private String itemType;
    private String title;
    private String author;
    private int pages;
    private String summary;
    private List<ObjectId> currentHolders;
    private List<String> pastHolders;
    private Date lastCheckedOutDate;
    private int quantity;
    private int borrowedCount;

    public Item() {

    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<ObjectId> getCurrentHolders() { return currentHolders; }

    public void setCurrentHolders(List<ObjectId> currentHolders) {
        this.currentHolders = currentHolders;
    }

    public List<String> getPastHolders() {
        return pastHolders;
    }

    public void setPastHolders(List<String> pastHolders) {
        this.pastHolders = pastHolders;
    }

    public Date getLastCheckedOutDate() {
        return lastCheckedOutDate;
    }

    public void setLastCheckedOutDate(Date lastCheckedOutDate) {
        this.lastCheckedOutDate = lastCheckedOutDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getBorrowedCount() {
        return borrowedCount;
    }

    public void setBorrowedCount(int borrowedCount) {
        this.borrowedCount = borrowedCount;
    }

}

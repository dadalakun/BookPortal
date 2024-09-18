package model;

import java.util.Date;
import java.util.List;

public class ClientItem {
    private String id; // String of ObjectId
    private String itemType;
    private String title;
    private String author;
    private int pages;
    private String summary;
    private List<String> currentHolders; // List of current holders' name
    private List<String> pastHolders;
    private Date lastCheckedOutDate;
    private int total;
    private int remained;

    public ClientItem() {

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getCurrentHolders() {
        return currentHolders;
    }

    public void setCurrentHolders(List<String> currentHolders) {
        this.currentHolders = currentHolders;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getRemained() {
        return remained;
    }

    public void setRemained(int remained) {
        this.remained = remained;
    }
}

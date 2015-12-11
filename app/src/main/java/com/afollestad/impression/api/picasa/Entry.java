
package com.afollestad.impression.api.picasa;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class Entry {

    @SerializedName("category")
    @Expose
    private List<Category_> category = new ArrayList<Category_>();
    @SerializedName("gphoto$access")
    @Expose
    private Gphoto$access gphoto$access;
    @SerializedName("updated")
    @Expose
    private Updated_ updated;
    @SerializedName("rights")
    @Expose
    private Rights rights;
    @SerializedName("gphoto$name")
    @Expose
    private Gphoto$name gphoto$name;
    @SerializedName("author")
    @Expose
    private List<Author_> author = new ArrayList<Author_>();
    @SerializedName("title")
    @Expose
    private Title_ title;
    @SerializedName("media$group")
    @Expose
    private Media$group media$group;
    @SerializedName("gd$etag")
    @Expose
    private String gd$etag;
    @SerializedName("gphoto$user")
    @Expose
    private Gphoto$user_ gphoto$user;
    @SerializedName("gphoto$numphotos")
    @Expose
    private Gphoto$numphotos gphoto$numphotos;
    @SerializedName("summary")
    @Expose
    private Summary summary;
    @SerializedName("gphoto$nickname")
    @Expose
    private Gphoto$nickname_ gphoto$nickname;
    @SerializedName("gphoto$timestamp")
    @Expose
    private Gphoto$timestamp gphoto$timestamp;
    @SerializedName("link")
    @Expose
    private List<Link_> link = new ArrayList<Link_>();
    @SerializedName("published")
    @Expose
    private Published published;
    @SerializedName("gphoto$id")
    @Expose
    private Gphoto$id gphoto$id;
    @SerializedName("gphoto$albumType")
    @Expose
    private Gphoto$albumType gphoto$albumType;
    @SerializedName("id")
    @Expose
    private Id_ id;

    /**
     * @return The category
     */
    public List<Category_> getCategory() {
        return category;
    }

    /**
     * @param category The category
     */
    public void setCategory(List<Category_> category) {
        this.category = category;
    }

    /**
     * @return The gphoto$access
     */
    public Gphoto$access getGphoto$access() {
        return gphoto$access;
    }

    /**
     * @param gphoto$access The gphoto$access
     */
    public void setGphoto$access(Gphoto$access gphoto$access) {
        this.gphoto$access = gphoto$access;
    }

    /**
     * @return The updated
     */
    public Updated_ getUpdated() {
        return updated;
    }

    /**
     * @param updated The updated
     */
    public void setUpdated(Updated_ updated) {
        this.updated = updated;
    }

    /**
     * @return The rights
     */
    public Rights getRights() {
        return rights;
    }

    /**
     * @param rights The rights
     */
    public void setRights(Rights rights) {
        this.rights = rights;
    }

    /**
     * @return The gphoto$name
     */
    public Gphoto$name getGphoto$name() {
        return gphoto$name;
    }

    /**
     * @param gphoto$name The gphoto$name
     */
    public void setGphoto$name(Gphoto$name gphoto$name) {
        this.gphoto$name = gphoto$name;
    }

    /**
     * @return The author
     */
    public List<Author_> getAuthor() {
        return author;
    }

    /**
     * @param author The author
     */
    public void setAuthor(List<Author_> author) {
        this.author = author;
    }

    /**
     * @return The title
     */
    public Title_ getTitle() {
        return title;
    }

    /**
     * @param title The title
     */
    public void setTitle(Title_ title) {
        this.title = title;
    }

    /**
     * @return The media$group
     */
    public Media$group getMedia$group() {
        return media$group;
    }

    /**
     * @param media$group The media$group
     */
    public void setMedia$group(Media$group media$group) {
        this.media$group = media$group;
    }

    /**
     * @return The gd$etag
     */
    public String getGd$etag() {
        return gd$etag;
    }

    /**
     * @param gd$etag The gd$etag
     */
    public void setGd$etag(String gd$etag) {
        this.gd$etag = gd$etag;
    }

    /**
     * @return The gphoto$user
     */
    public Gphoto$user_ getGphoto$user() {
        return gphoto$user;
    }

    /**
     * @param gphoto$user The gphoto$user
     */
    public void setGphoto$user(Gphoto$user_ gphoto$user) {
        this.gphoto$user = gphoto$user;
    }

    /**
     * @return The gphoto$numphotos
     */
    public Gphoto$numphotos getGphoto$numphotos() {
        return gphoto$numphotos;
    }

    /**
     * @param gphoto$numphotos The gphoto$numphotos
     */
    public void setGphoto$numphotos(Gphoto$numphotos gphoto$numphotos) {
        this.gphoto$numphotos = gphoto$numphotos;
    }

    /**
     * @return The summary
     */
    public Summary getSummary() {
        return summary;
    }

    /**
     * @param summary The summary
     */
    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    /**
     * @return The gphoto$nickname
     */
    public Gphoto$nickname_ getGphoto$nickname() {
        return gphoto$nickname;
    }

    /**
     * @param gphoto$nickname The gphoto$nickname
     */
    public void setGphoto$nickname(Gphoto$nickname_ gphoto$nickname) {
        this.gphoto$nickname = gphoto$nickname;
    }

    /**
     * @return The gphoto$timestamp
     */
    public Gphoto$timestamp getGphoto$timestamp() {
        return gphoto$timestamp;
    }

    /**
     * @param gphoto$timestamp The gphoto$timestamp
     */
    public void setGphoto$timestamp(Gphoto$timestamp gphoto$timestamp) {
        this.gphoto$timestamp = gphoto$timestamp;
    }

    /**
     * @return The link
     */
    public List<Link_> getLink() {
        return link;
    }

    /**
     * @param link The link
     */
    public void setLink(List<Link_> link) {
        this.link = link;
    }

    /**
     * @return The published
     */
    public Published getPublished() {
        return published;
    }

    /**
     * @param published The published
     */
    public void setPublished(Published published) {
        this.published = published;
    }

    /**
     * @return The gphoto$id
     */
    public Gphoto$id getGphoto$id() {
        return gphoto$id;
    }

    /**
     * @param gphoto$id The gphoto$id
     */
    public void setGphoto$id(Gphoto$id gphoto$id) {
        this.gphoto$id = gphoto$id;
    }

    /**
     * @return The gphoto$albumType
     */
    public Gphoto$albumType getGphoto$albumType() {
        return gphoto$albumType;
    }

    /**
     * @param gphoto$albumType The gphoto$albumType
     */
    public void setGphoto$albumType(Gphoto$albumType gphoto$albumType) {
        this.gphoto$albumType = gphoto$albumType;
    }

    /**
     * @return The id
     */
    public Id_ getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(Id_ id) {
        this.id = id;
    }

}

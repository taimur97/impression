
package com.afollestad.impression.api.picasa;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class Media$group {

    @SerializedName("media$credit")
    @Expose
    private List<Media$credit> media$credit = new ArrayList<Media$credit>();
    @SerializedName("media$title")
    @Expose
    private Media$title media$title;
    @SerializedName("media$content")
    @Expose
    private List<Media$content> media$content = new ArrayList<Media$content>();
    @SerializedName("media$thumbnail")
    @Expose
    private List<Media$thumbnail> media$thumbnail = new ArrayList<Media$thumbnail>();
    @SerializedName("media$keywords")
    @Expose
    private Media$keywords media$keywords;
    @SerializedName("media$description")
    @Expose
    private Media$description media$description;

    /**
     * @return The media$credit
     */
    public List<Media$credit> getMedia$credit() {
        return media$credit;
    }

    /**
     * @param media$credit The media$credit
     */
    public void setMedia$credit(List<Media$credit> media$credit) {
        this.media$credit = media$credit;
    }

    /**
     * @return The media$title
     */
    public Media$title getMedia$title() {
        return media$title;
    }

    /**
     * @param media$title The media$title
     */
    public void setMedia$title(Media$title media$title) {
        this.media$title = media$title;
    }

    /**
     * @return The media$content
     */
    public List<Media$content> getMedia$content() {
        return media$content;
    }

    /**
     * @param media$content The media$content
     */
    public void setMedia$content(List<Media$content> media$content) {
        this.media$content = media$content;
    }

    /**
     * @return The media$thumbnail
     */
    public List<Media$thumbnail> getMedia$thumbnail() {
        return media$thumbnail;
    }

    /**
     * @param media$thumbnail The media$thumbnail
     */
    public void setMedia$thumbnail(List<Media$thumbnail> media$thumbnail) {
        this.media$thumbnail = media$thumbnail;
    }

    /**
     * @return The media$keywords
     */
    public Media$keywords getMedia$keywords() {
        return media$keywords;
    }

    /**
     * @param media$keywords The media$keywords
     */
    public void setMedia$keywords(Media$keywords media$keywords) {
        this.media$keywords = media$keywords;
    }

    /**
     * @return The media$description
     */
    public Media$description getMedia$description() {
        return media$description;
    }

    /**
     * @param media$description The media$description
     */
    public void setMedia$description(Media$description media$description) {
        this.media$description = media$description;
    }

}


package com.afollestad.impression.api.picasa;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Media$content {

    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("medium")
    @Expose
    private String medium;
    @SerializedName("type")
    @Expose
    private String type;

    /**
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return The medium
     */
    public String getMedium() {
        return medium;
    }

    /**
     * @param medium The medium
     */
    public void setMedium(String medium) {
        this.medium = medium;
    }

    /**
     * @return The type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type The type
     */
    public void setType(String type) {
        this.type = type;
    }

}

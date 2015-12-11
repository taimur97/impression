
package com.afollestad.impression.api.picasa;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Author_ {

    @SerializedName("name")
    @Expose
    private Name_ name;
    @SerializedName("uri")
    @Expose
    private Uri_ uri;

    /**
     * @return The name
     */
    public Name_ getName() {
        return name;
    }

    /**
     * @param name The name
     */
    public void setName(Name_ name) {
        this.name = name;
    }

    /**
     * @return The uri
     */
    public Uri_ getUri() {
        return uri;
    }

    /**
     * @param uri The uri
     */
    public void setUri(Uri_ uri) {
        this.uri = uri;
    }

}

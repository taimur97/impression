
package com.afollestad.impression.api.picasa;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Author {

    @SerializedName("name")
    @Expose
    private Name name;
    @SerializedName("uri")
    @Expose
    private Uri uri;

    /**
     * @return The name
     */
    public Name getName() {
        return name;
    }

    /**
     * @param name The name
     */
    public void setName(Name name) {
        this.name = name;
    }

    /**
     * @return The uri
     */
    public Uri getUri() {
        return uri;
    }

    /**
     * @param uri The uri
     */
    public void setUri(Uri uri) {
        this.uri = uri;
    }

}

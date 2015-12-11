
package com.afollestad.impression.api.picasa;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Generator {

    @SerializedName("$t")
    @Expose
    private String $t;
    @SerializedName("version")
    @Expose
    private String version;
    @SerializedName("uri")
    @Expose
    private String uri;

    /**
     * @return The $t
     */
    public String get$t() {
        return $t;
    }

    /**
     * @param $t The $t
     */
    public void set$t(String $t) {
        this.$t = $t;
    }

    /**
     * @return The version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version The version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return The uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri The uri
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

}

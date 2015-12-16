
package com.afollestad.impression.api.picasa;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Media$title {

    @SerializedName("$t")
    @Expose
    private String $t;
    @SerializedName("type")
    @Expose
    private String type;

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

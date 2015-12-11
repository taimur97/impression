
package com.afollestad.impression.api.picasa;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class OpenSearch$totalResults {

    @SerializedName("$t")
    @Expose
    private Integer $t;

    /**
     * @return The $t
     */
    public Integer get$t() {
        return $t;
    }

    /**
     * @param $t The $t
     */
    public void set$t(Integer $t) {
        this.$t = $t;
    }

}

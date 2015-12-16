
package com.afollestad.impression.api.picasa;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class Feed {

    @SerializedName("subtitle")
    @Expose
    private Subtitle subtitle;
    @SerializedName("gphoto$user")
    @Expose
    private Gphoto$user gphoto$user;
    @SerializedName("gphoto$maxPhotosPerAlbum")
    @Expose
    private Gphoto$maxPhotosPerAlbum gphoto$maxPhotosPerAlbum;
    @SerializedName("openSearch$itemsPerPage")
    @Expose
    private OpenSearch$itemsPerPage openSearch$itemsPerPage;
    @SerializedName("id")
    @Expose
    private Id id;
    @SerializedName("category")
    @Expose
    private List<Category> category = new ArrayList<Category>();
    @SerializedName("generator")
    @Expose
    private Generator generator;
    @SerializedName("title")
    @Expose
    private Title title;
    @SerializedName("openSearch$startIndex")
    @Expose
    private OpenSearch$startIndex openSearch$startIndex;
    @SerializedName("xmlns$media")
    @Expose
    private String xmlns$media;
    @SerializedName("xmlns$app")
    @Expose
    private String xmlns$app;
    @SerializedName("gd$etag")
    @Expose
    private String gd$etag;
    @SerializedName("updated")
    @Expose
    private Updated updated;
    @SerializedName("xmlns$gd")
    @Expose
    private String xmlns$gd;
    @SerializedName("xmlns$gphoto")
    @Expose
    private String xmlns$gphoto;
    @SerializedName("gphoto$nickname")
    @Expose
    private Gphoto$nickname gphoto$nickname;
    @SerializedName("link")
    @Expose
    private List<Link> link = new ArrayList<Link>();
    @SerializedName("xmlns$openSearch")
    @Expose
    private String xmlns$openSearch;
    @SerializedName("gphoto$quotacurrent")
    @Expose
    private Gphoto$quotacurrent gphoto$quotacurrent;
    @SerializedName("icon")
    @Expose
    private Icon icon;
    @SerializedName("xmlns")
    @Expose
    private String xmlns;
    @SerializedName("openSearch$totalResults")
    @Expose
    private OpenSearch$totalResults openSearch$totalResults;
    @SerializedName("author")
    @Expose
    private List<Author> author = new ArrayList<Author>();
    @SerializedName("gphoto$quotalimit")
    @Expose
    private Gphoto$quotalimit gphoto$quotalimit;
    @SerializedName("entry")
    @Expose
    private List<Entry> entry = new ArrayList<Entry>();
    @SerializedName("gphoto$thumbnail")
    @Expose
    private Gphoto$thumbnail gphoto$thumbnail;

    /**
     * @return The subtitle
     */
    public Subtitle getSubtitle() {
        return subtitle;
    }

    /**
     * @param subtitle The subtitle
     */
    public void setSubtitle(Subtitle subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * @return The gphoto$user
     */
    public Gphoto$user getGphoto$user() {
        return gphoto$user;
    }

    /**
     * @param gphoto$user The gphoto$user
     */
    public void setGphoto$user(Gphoto$user gphoto$user) {
        this.gphoto$user = gphoto$user;
    }

    /**
     * @return The gphoto$maxPhotosPerAlbum
     */
    public Gphoto$maxPhotosPerAlbum getGphoto$maxPhotosPerAlbum() {
        return gphoto$maxPhotosPerAlbum;
    }

    /**
     * @param gphoto$maxPhotosPerAlbum The gphoto$maxPhotosPerAlbum
     */
    public void setGphoto$maxPhotosPerAlbum(Gphoto$maxPhotosPerAlbum gphoto$maxPhotosPerAlbum) {
        this.gphoto$maxPhotosPerAlbum = gphoto$maxPhotosPerAlbum;
    }

    /**
     * @return The openSearch$itemsPerPage
     */
    public OpenSearch$itemsPerPage getOpenSearch$itemsPerPage() {
        return openSearch$itemsPerPage;
    }

    /**
     * @param openSearch$itemsPerPage The openSearch$itemsPerPage
     */
    public void setOpenSearch$itemsPerPage(OpenSearch$itemsPerPage openSearch$itemsPerPage) {
        this.openSearch$itemsPerPage = openSearch$itemsPerPage;
    }

    /**
     * @return The id
     */
    public Id getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(Id id) {
        this.id = id;
    }

    /**
     * @return The category
     */
    public List<Category> getCategory() {
        return category;
    }

    /**
     * @param category The category
     */
    public void setCategory(List<Category> category) {
        this.category = category;
    }

    /**
     * @return The generator
     */
    public Generator getGenerator() {
        return generator;
    }

    /**
     * @param generator The generator
     */
    public void setGenerator(Generator generator) {
        this.generator = generator;
    }

    /**
     * @return The title
     */
    public Title getTitle() {
        return title;
    }

    /**
     * @param title The title
     */
    public void setTitle(Title title) {
        this.title = title;
    }

    /**
     * @return The openSearch$startIndex
     */
    public OpenSearch$startIndex getOpenSearch$startIndex() {
        return openSearch$startIndex;
    }

    /**
     * @param openSearch$startIndex The openSearch$startIndex
     */
    public void setOpenSearch$startIndex(OpenSearch$startIndex openSearch$startIndex) {
        this.openSearch$startIndex = openSearch$startIndex;
    }

    /**
     * @return The xmlns$media
     */
    public String getXmlns$media() {
        return xmlns$media;
    }

    /**
     * @param xmlns$media The xmlns$media
     */
    public void setXmlns$media(String xmlns$media) {
        this.xmlns$media = xmlns$media;
    }

    /**
     * @return The xmlns$app
     */
    public String getXmlns$app() {
        return xmlns$app;
    }

    /**
     * @param xmlns$app The xmlns$app
     */
    public void setXmlns$app(String xmlns$app) {
        this.xmlns$app = xmlns$app;
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
     * @return The updated
     */
    public Updated getUpdated() {
        return updated;
    }

    /**
     * @param updated The updated
     */
    public void setUpdated(Updated updated) {
        this.updated = updated;
    }

    /**
     * @return The xmlns$gd
     */
    public String getXmlns$gd() {
        return xmlns$gd;
    }

    /**
     * @param xmlns$gd The xmlns$gd
     */
    public void setXmlns$gd(String xmlns$gd) {
        this.xmlns$gd = xmlns$gd;
    }

    /**
     * @return The xmlns$gphoto
     */
    public String getXmlns$gphoto() {
        return xmlns$gphoto;
    }

    /**
     * @param xmlns$gphoto The xmlns$gphoto
     */
    public void setXmlns$gphoto(String xmlns$gphoto) {
        this.xmlns$gphoto = xmlns$gphoto;
    }

    /**
     * @return The gphoto$nickname
     */
    public Gphoto$nickname getGphoto$nickname() {
        return gphoto$nickname;
    }

    /**
     * @param gphoto$nickname The gphoto$nickname
     */
    public void setGphoto$nickname(Gphoto$nickname gphoto$nickname) {
        this.gphoto$nickname = gphoto$nickname;
    }

    /**
     * @return The link
     */
    public List<Link> getLink() {
        return link;
    }

    /**
     * @param link The link
     */
    public void setLink(List<Link> link) {
        this.link = link;
    }

    /**
     * @return The xmlns$openSearch
     */
    public String getXmlns$openSearch() {
        return xmlns$openSearch;
    }

    /**
     * @param xmlns$openSearch The xmlns$openSearch
     */
    public void setXmlns$openSearch(String xmlns$openSearch) {
        this.xmlns$openSearch = xmlns$openSearch;
    }

    /**
     * @return The gphoto$quotacurrent
     */
    public Gphoto$quotacurrent getGphoto$quotacurrent() {
        return gphoto$quotacurrent;
    }

    /**
     * @param gphoto$quotacurrent The gphoto$quotacurrent
     */
    public void setGphoto$quotacurrent(Gphoto$quotacurrent gphoto$quotacurrent) {
        this.gphoto$quotacurrent = gphoto$quotacurrent;
    }

    /**
     * @return The icon
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * @param icon The icon
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    /**
     * @return The xmlns
     */
    public String getXmlns() {
        return xmlns;
    }

    /**
     * @param xmlns The xmlns
     */
    public void setXmlns(String xmlns) {
        this.xmlns = xmlns;
    }

    /**
     * @return The openSearch$totalResults
     */
    public OpenSearch$totalResults getOpenSearch$totalResults() {
        return openSearch$totalResults;
    }

    /**
     * @param openSearch$totalResults The openSearch$totalResults
     */
    public void setOpenSearch$totalResults(OpenSearch$totalResults openSearch$totalResults) {
        this.openSearch$totalResults = openSearch$totalResults;
    }

    /**
     * @return The author
     */
    public List<Author> getAuthor() {
        return author;
    }

    /**
     * @param author The author
     */
    public void setAuthor(List<Author> author) {
        this.author = author;
    }

    /**
     * @return The gphoto$quotalimit
     */
    public Gphoto$quotalimit getGphoto$quotalimit() {
        return gphoto$quotalimit;
    }

    /**
     * @param gphoto$quotalimit The gphoto$quotalimit
     */
    public void setGphoto$quotalimit(Gphoto$quotalimit gphoto$quotalimit) {
        this.gphoto$quotalimit = gphoto$quotalimit;
    }

    /**
     * @return The entry
     */
    public List<Entry> getEntry() {
        return entry;
    }

    /**
     * @param entry The entry
     */
    public void setEntry(List<Entry> entry) {
        this.entry = entry;
    }

    /**
     * @return The gphoto$thumbnail
     */
    public Gphoto$thumbnail getGphoto$thumbnail() {
        return gphoto$thumbnail;
    }

    /**
     * @param gphoto$thumbnail The gphoto$thumbnail
     */
    public void setGphoto$thumbnail(Gphoto$thumbnail gphoto$thumbnail) {
        this.gphoto$thumbnail = gphoto$thumbnail;
    }

}

package my.playlist.Entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "Myplaylist")
public class Myplaylist{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String genres;

    @Column(name = "uploaded_date", nullable = false)
    private Date uploadedDate;

    @Column(name = "thumbnail_id", length = 100)
    private String thumbnailId;

    @Column(name = "thumbnail_url", length = 255)
    private String thumbnailUrl;

    @Column(nullable = false, length = 100)
    private String artist;

    public Myplaylist(Long id, String title, String genres, Date uploadedDate, String thumbnailId, String thumbnailUrl, String artist) {
        this.id = id;
        this.title = title;
        this.genres = genres;
        this.uploadedDate = uploadedDate;
        this.thumbnailId = thumbnailId;
        this.thumbnailUrl = thumbnailUrl;
        this.artist = artist;
    }

    public Myplaylist() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public Date getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(Date uploadedDate) {
        this.uploadedDate = uploadedDate;
    }

    public String getThumbnailId() {
        return thumbnailId;
    }

    public void setThumbnailId(String thumbnailId) {
        this.thumbnailId = thumbnailId;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
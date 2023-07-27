package my.playlist.Dto;

import java.util.Date;

public class MyplaylistDto {

    private Long id;
    private String title;
    private String genres;
    private Date uploadedDate;
    private String thumbnailId;
    private String thumbnailUrl;
    private String artist;

    public MyplaylistDto(Long id, String title, String genres, Date uploadedDate, String thumbnailId, String thumbnailUrl, String artist) {
        this.id = id;
        this.title = title;
        this.genres = genres;
        this.uploadedDate = uploadedDate;
        this.thumbnailId = thumbnailId;
        this.thumbnailUrl = thumbnailUrl;
        this.artist = artist;
    }

    public MyplaylistDto() {
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

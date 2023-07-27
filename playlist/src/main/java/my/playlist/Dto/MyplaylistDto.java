package my.playlist.Dto;

import java.text.SimpleDateFormat;
import java.sql.Date;

public class MyplaylistDto {

    private Long id;
    private String title;
    private String genres;
    private String uploadedDate; // Change the type to String
    private String thumbnailId;
    private String thumbnailUrl;
    private String artist;

    public MyplaylistDto(Long id, String title, String genres, Date uploadedDate, String thumbnailId, String thumbnailUrl, String artist) {
        this.id = id;
        this.title = title;
        this.genres = genres;
        this.thumbnailId = thumbnailId;
        this.thumbnailUrl = thumbnailUrl;
        this.artist = artist;

        // Convert the Date to a formatted String using SimpleDateFormat
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.uploadedDate = dateFormat.format(uploadedDate);
    }

    public MyplaylistDto() {
    }

//    public MyplaylistDto(Long id, String title, String genres, String uploadedDate, String thumbnailId, String thumbnailUrl, String artist) {
//    }

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

    public String getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(Date uploadedDate) {
        // Convert the Date to a formatted String using SimpleDateFormat
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.uploadedDate = dateFormat.format(uploadedDate);
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

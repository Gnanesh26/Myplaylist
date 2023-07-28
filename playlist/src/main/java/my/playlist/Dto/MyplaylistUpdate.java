package my.playlist.Dto;

import org.springframework.web.multipart.MultipartFile;

public class MyplaylistUpdate {
    private String title;
    private String genres;
    private String uploadedDate;
    private String artist;
    private MultipartFile thumbnailFile;

    public MyplaylistUpdate(String title, String genres, String uploadedDate, String artist, MultipartFile thumbnailFile) {
        this.title = title;
        this.genres = genres;
        this.uploadedDate = uploadedDate;
        this.artist = artist;
        this.thumbnailFile = thumbnailFile;
    }

    public MyplaylistUpdate() {
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

    public void setUploadedDate(String uploadedDate) {
        this.uploadedDate = uploadedDate;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public MultipartFile getThumbnailFile() {
        return thumbnailFile;
    }

    public void setThumbnailFile(MultipartFile thumbnailFile) {
        this.thumbnailFile = thumbnailFile;
    }
}
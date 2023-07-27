package my.playlist.Service;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import my.playlist.Dto.MyplaylistDto;
import my.playlist.Entity.Myplaylist;
import my.playlist.Repository.MyplaylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

@Service
public class MyplaylistService {

    @Autowired
    MyplaylistRepository myplaylistRepository;

    @Autowired
  Cloudinary cloudinary; // Autowire Cloudinary instance (configured with API key and secret)

    public MyplaylistDto createPlaylist(MultipartFile file, String title, String genres, String uploadedDate, String artist) {

        try {
            // Validate the uploadedDate format using SimpleDateFormat
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setLenient(false); // Disable lenient parsing ( will strictly enforce the date format, and any deviation from the specified format will result in a ParseException)

            Date parsedDate;
//  checks if the uploadedDate string is in the "yyyy-MM-dd" format, converts it to a Date object if valid, or throws an error if the format is incorrect.
            try {
                parsedDate = new Date(dateFormat.parse(uploadedDate).getTime());
            } catch (ParseException e) {
                throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM-dd");
            }

            // Upload the file to Cloudinary and get the thumbnail URL and ID
            String thumbnailUrl = null;
            String thumbnailId = null;
            if (file != null && !file.isEmpty()) {
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());//uploads the file to the Cloudinary service
                thumbnailUrl = (String) uploadResult.get("secure_url");
                thumbnailId = (String) uploadResult.get("public_id");
            }

            // Create the playlist entity and save it to the database
            Myplaylist playlistEntity = new Myplaylist();
            playlistEntity.setTitle(title);
            playlistEntity.setGenres(genres);
            playlistEntity.setUploadedDate(String.valueOf(parsedDate)); // Use java.sql.Date
            playlistEntity.setThumbnailUrl(thumbnailUrl);
            playlistEntity.setThumbnailId(thumbnailId);
            playlistEntity.setArtist(artist);

            Myplaylist savedEntity = myplaylistRepository.save(playlistEntity);

            // Print all the fields of the created playlist
            System.out.println("Created playlist details:");
            System.out.println("ID: " + savedEntity.getId());
            System.out.println("Title: " + savedEntity.getTitle());
            System.out.println("Genres: " + savedEntity.getGenres());
            System.out.println("Uploaded Date: " + savedEntity.getUploadedDate());
            System.out.println("Thumbnail ID: " + savedEntity.getThumbnailId());
            System.out.println("Thumbnail URL: " + savedEntity.getThumbnailUrl());
            System.out.println("Artist: " + savedEntity.getArtist());

            // Convert the saved entity to DTO and return
            return new MyplaylistDto(
                    savedEntity.getId(),
                    savedEntity.getTitle(),
                    savedEntity.getGenres(),
                    savedEntity.getUploadedDate(),
                    savedEntity.getThumbnailId(),
                    savedEntity.getThumbnailUrl(),
                    savedEntity.getArtist()
            );
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while creating playlist: " + e.getMessage());
        }
    }
}

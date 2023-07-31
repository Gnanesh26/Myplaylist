package my.playlist.Service;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import my.playlist.Dto.MyplaylistDto;
import my.playlist.Entity.Myplaylist;
import my.playlist.Entity.UserInfo;
import my.playlist.Repository.MyplaylistRepository;
import my.playlist.Repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MyplaylistService {

    @Autowired
    MyplaylistRepository myplaylistRepository;


    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    Cloudinary cloudinary;

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
                    savedEntity.getId(), savedEntity.getTitle(), savedEntity.getGenres(), savedEntity.getUploadedDate(), savedEntity.getThumbnailId(), savedEntity.getThumbnailUrl(), savedEntity.getArtist()
            );
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while creating playlist: " + e.getMessage());
        }
    }


//    public String addUser(UserInfo userInfo) {
//        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
//        userInfoRepository.save(userInfo);
//        return "user added to system ";
//    }



    public List<MyplaylistDto> getPlaylistsSortedByTitle(String searchTitle, String filterArtist, String filterGenres, String title) {
        List<Myplaylist> playlists = myplaylistRepository.findAll();

        // Filter playlists based on searchTitle, filterArtist, and filterGenres
        List<Myplaylist> filteredPlaylists = playlists.stream()
                .filter(playlist ->
                        (searchTitle == null || playlist.getTitle().toLowerCase().contains(searchTitle.toLowerCase())) &&
                                (filterArtist == null || playlist.getArtist().equalsIgnoreCase(filterArtist)) &&
                                (filterGenres == null || playlist.getGenres().equalsIgnoreCase(filterGenres)))
                .collect(Collectors.toList());

        List<MyplaylistDto> sortedPlaylists;

        if (title != null && !title.isEmpty()) {
            // Check if any playlists match the specified title in sortField
            List<Myplaylist> playlistsMatchingSortField = filteredPlaylists.stream()
                    .filter(playlist -> playlist.getTitle().equalsIgnoreCase(title))
                    .collect(Collectors.toList());

            if (playlistsMatchingSortField.isEmpty()) {
                throw new IllegalArgumentException("No playlists found with the provided title.");
            }

            // Sort by title matching the specified value
            List<MyplaylistDto> playlistsWithTitle = filteredPlaylists.stream()
                    .filter(playlist -> playlist.getTitle().equalsIgnoreCase(title))
                    .map(playlist -> new MyplaylistDto(playlist.getId(), playlist.getTitle(), playlist.getGenres(), playlist.getUploadedDate(), playlist.getThumbnailId(), playlist.getThumbnailUrl(), playlist.getArtist()))
                    .collect(Collectors.toList());

            List<MyplaylistDto> remainingPlaylists = filteredPlaylists.stream()
                    .filter(playlist -> !playlist.getTitle().equalsIgnoreCase(title))
                    .map(playlist -> new MyplaylistDto(playlist.getId(), playlist.getTitle(), playlist.getGenres(), playlist.getUploadedDate(), playlist.getThumbnailId(), playlist.getThumbnailUrl(), playlist.getArtist()))
                    .collect(Collectors.toList());

            playlistsWithTitle.sort(Comparator.comparing(MyplaylistDto::getTitle));
            remainingPlaylists.sort(Comparator.comparing(MyplaylistDto::getTitle));

            // Combine the sorted lists
            playlistsWithTitle.addAll(remainingPlaylists);

            sortedPlaylists = playlistsWithTitle;}
         else {
            sortedPlaylists = filteredPlaylists.stream()
                    .map(playlist -> new MyplaylistDto(playlist.getId(), playlist.getTitle(), playlist.getGenres(), playlist.getUploadedDate(), playlist.getThumbnailId(), playlist.getThumbnailUrl(), playlist.getArtist()))
                    .collect(Collectors.toList());

            if (sortedPlaylists.isEmpty()) {
                throw new IllegalArgumentException("No playlists found with the provided search criteria.");
            }
        }
        return sortedPlaylists;
    }










    public List<MyplaylistDto> getPlaylistsSortedByUploadedDate(String targetDateStr) {
        List<Myplaylist> playlists = myplaylistRepository.findAll();

        // Define the date format
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Parse the targetDateStr to LocalDate
        LocalDate targetDate = LocalDate.parse(targetDateStr, dateFormatter);

        // Separate playlists with the provided date
        List<Myplaylist> givenDatePlaylists = new ArrayList<>();
        List<Myplaylist> remainingPlaylists = new ArrayList<>();

        for (Myplaylist playlist : playlists) {
            LocalDate playlistDate = LocalDate.parse(playlist.getUploadedDate(), dateFormatter);
            if (playlistDate.isEqual(targetDate)) {
                givenDatePlaylists.add(playlist);
            } else {
                remainingPlaylists.add(playlist);
            }
        }

        if (givenDatePlaylists.isEmpty()) {
            throw new IllegalArgumentException("No playlists found");
        }

        // Sort the remaining playlists in ascending order of uploaded dates
        remainingPlaylists.sort(Comparator.comparing(playlist -> playlist.getUploadedDate()));

        // Combine both lists and convert to MyplaylistDto objects
        List<MyplaylistDto> playlistDtos = Stream.concat(
                givenDatePlaylists.stream().map(playlist -> new MyplaylistDto(playlist.getId(), playlist.getTitle(), playlist.getGenres(), playlist.getUploadedDate(), playlist.getThumbnailId(), playlist.getThumbnailUrl(), playlist.getArtist())),
                remainingPlaylists.stream().map(playlist -> new MyplaylistDto(playlist.getId(), playlist.getTitle(), playlist.getGenres(), playlist.getUploadedDate(), playlist.getThumbnailId(), playlist.getThumbnailUrl(), playlist.getArtist()))
        ).collect(Collectors.toList());

        return playlistDtos;
    }

}

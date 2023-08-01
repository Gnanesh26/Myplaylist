package my.playlist.Service;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import my.playlist.Dto.MyplaylistDto;
import my.playlist.Entity.Myplaylist;
import my.playlist.Entity.UserInfo;
import my.playlist.Repository.MyplaylistRepository;
import my.playlist.Repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.security.Principal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
            playlistEntity.setUploadedDate(String.valueOf(parsedDate));
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


    public String addUser(UserInfo userInfo) {
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        userInfoRepository.save(userInfo);
        return "user added to system ";
    }



    public List<MyplaylistDto> getPlaylistsSortedByTitle(String searchTitle, String filterArtist, String filterGenres, String title) {
        // Get all playlists from  repo
        List<Myplaylist> playlists = myplaylistRepository.findAll();
        List<Myplaylist> filteredPlaylists = new ArrayList<>();

        // Filter playlists based on searchTitle, filterArtist, and filterGenres
        for (Myplaylist playlist : playlists) {
            if ((searchTitle == null || playlist.getTitle().toLowerCase().contains(searchTitle.toLowerCase()))
                    && (filterArtist == null || playlist.getArtist().equalsIgnoreCase(filterArtist))
                    && (filterGenres == null || playlist.getGenres().equalsIgnoreCase(filterGenres))) {
                // Add  playlist to  filtered list if it matches the search criteria
                filteredPlaylists.add(playlist);
            }
        }

        List<MyplaylistDto> sortedPlaylists = new ArrayList<>();

        if (title != null && !title.isEmpty()) {
            // Check if any playlists match the specified title in sortField
            List<Myplaylist> playlistsMatchingSortField = new ArrayList<>();
            for (Myplaylist playlist : filteredPlaylists) {
                if (playlist.getTitle().equalsIgnoreCase(title)) {
                    // Add playlists with the specified title to a separate list
                    playlistsMatchingSortField.add(playlist);
                }
            }

            if (playlistsMatchingSortField.isEmpty()) {
                // If no playlists with given title found, throw an exception
                throw new IllegalArgumentException("No playlists found with the provided title.");
            }

            // Sort by title matching the specified value
            List<MyplaylistDto> playlistsWithTitle = new ArrayList<>();
            List<MyplaylistDto> remainingPlaylists = new ArrayList<>();

            for (Myplaylist playlist : filteredPlaylists) {
                MyplaylistDto playlistDto = new MyplaylistDto(playlist.getId(), playlist.getTitle(), playlist.getGenres(), playlist.getUploadedDate(), playlist.getThumbnailId(), playlist.getThumbnailUrl(), playlist.getArtist());
                if (playlist.getTitle().equalsIgnoreCase(title)) {
                    // Add playlists with the given title to one list
                    playlistsWithTitle.add(playlistDto);
                } else {
                    // Add remaining playlists to another list
                    remainingPlaylists.add(playlistDto);
                }
            }

            // Sort playlists with the given title and remaining playlists be sort alphabetically by title
            playlistsWithTitle.sort(Comparator.comparing(MyplaylistDto::getTitle));
            remainingPlaylists.sort(Comparator.comparing(MyplaylistDto::getTitle));

            // Combine the sorted lists
            playlistsWithTitle.addAll(remainingPlaylists);

            // Assign the sorted playlists to the result variable
            sortedPlaylists = playlistsWithTitle;

        } else {
            // No title provided, return playlists sorted based on filtering criteria
            for (Myplaylist playlist : filteredPlaylists) {
                // Create playlist DTO objects and add them to the sorted list
                sortedPlaylists.add(new MyplaylistDto(playlist.getId(), playlist.getTitle(), playlist.getGenres(), playlist.getUploadedDate(), playlist.getThumbnailId(), playlist.getThumbnailUrl(), playlist.getArtist()));
            }

            if (sortedPlaylists.isEmpty()) {
                // If no playlists match the filtering criteria, throw an exception
                throw new IllegalArgumentException("No playlists found with the provided search criteria.");
            }
        }
        return sortedPlaylists;
    }















    public List<MyplaylistDto> getPlaylistsSortedByUploadedDate(String targetDateStr) {
        // Get all playlists from the repo
        List<Myplaylist> playlists = myplaylistRepository.findAll();

        // Define the date format
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Parse the targetDateStr to LocalDate
        LocalDate targetDate = LocalDate.parse(targetDateStr, dateFormatter);

        // Separate playlists with the provided date
        List<Myplaylist> givenDatePlaylists = new ArrayList<>();
        List<Myplaylist> remainingPlaylists = new ArrayList<>();

        // Iterate through all playlists to separate them based on the uploaded date
        for (Myplaylist playlist : playlists) {
            LocalDate playlistDate = LocalDate.parse(playlist.getUploadedDate(), dateFormatter);
            if (playlistDate.isEqual(targetDate)) {
                // Add playlists with the provided date to one list
                givenDatePlaylists.add(playlist);
            } else {
                // Add remaining playlists to another list
                remainingPlaylists.add(playlist);
            }
        }
        // Check if any playlists match with given date
        if (givenDatePlaylists.isEmpty()) {
            throw new IllegalArgumentException("No playlists found");
        }

        // Sort the remaining playlists in ascending order of uploaded dates
        Collections.sort(remainingPlaylists, Comparator.comparing(Myplaylist::getUploadedDate));

        // Combine both lists and convert to MyplaylistDto objects
        List<MyplaylistDto> playlistDtos = new ArrayList<>();

        // Add playlists with the provided date to the result list
        for (Myplaylist playlist : givenDatePlaylists) {
            playlistDtos.add(new MyplaylistDto(playlist.getId(), playlist.getTitle(), playlist.getGenres(), playlist.getUploadedDate(), playlist.getThumbnailId(), playlist.getThumbnailUrl(), playlist.getArtist()));
        }

        // Add the remaining playlists to the result list
        for (Myplaylist playlist : remainingPlaylists) {
            playlistDtos.add(new MyplaylistDto(playlist.getId(), playlist.getTitle(), playlist.getGenres(), playlist.getUploadedDate(), playlist.getThumbnailId(), playlist.getThumbnailUrl(), playlist.getArtist()));
        }

        return playlistDtos;
    }




    public ResponseEntity<String> addSong(String title, String genres, String uploadedDateStr, MultipartFile thumbnailFile, String artist, Principal principal) {
        // Get the name of the authenticated artist (current user)
        String authenticatedArtist = principal.getName();

        // Check if the authenticated artist matches the provided artist name
        // If not, return error message
        if (artist != null && !authenticatedArtist.equals(artist)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to upload songs for other artists.");
        }

        // Create a new Song object with the extracted data
        Myplaylist newSong = new Myplaylist();

        // Set the song title if provided
        if (title != null) {
            newSong.setTitle(title);
        }

        // Set the song genres if provided
        if (genres != null) {
            newSong.setGenres(genres);
        }

        // Set the uploaded date for the song
        newSong.setUploadedDate(uploadedDateStr);

        // Set the artist name for the song
        newSong.setArtist(artist);

        try {
            // Upload the thumbnail image to Cloudinary
            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                // Use Cloudinary's uploader to upload the thumbnail image
                Map<?, ?> cloudinaryResponse = cloudinary.uploader().upload(thumbnailFile.getBytes(), ObjectUtils.emptyMap());

                // Get the thumbnail URL and ID from the Cloudinary response
                String thumbnailUrl = (String) cloudinaryResponse.get("secure_url");
                String thumbnailId = (String) cloudinaryResponse.get("public_id");

                // Set the thumbnailUrl and thumbnailId in the newSong object
                newSong.setThumbnailUrl(thumbnailUrl);
                newSong.setThumbnailId(thumbnailId);
            }

            // Save the new song to the database
            myplaylistRepository.save(newSong);

            // Return a successful response with a message that song was added successfully
            return ResponseEntity.ok("Song added successfully");
        } catch (IOException e) {
            // If an IOException occurs during thumbnail upload, return an internal server error status with an error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading thumbnail");
        }
    }




    public ResponseEntity<String> deleteSong(Long songId, Principal principal) {
        // Get the name of the authenticated artist (current user)
        String authenticatedArtist = principal.getName();

        // Get  song by ID from  db
        Myplaylist songToDelete = myplaylistRepository.findById(Math.toIntExact(songId)).orElse(null);

        // Checks if song exists in the db
        if (songToDelete == null) {
            // If song is not found, throw a error message
            throw new RuntimeException("Song not found");
        }

        // Check if the authenticated artist is the owner of the song
        if (songToDelete.getArtist().equals(authenticatedArtist)) {

            // If the authenticated artist is the owner, delete the song from the db
            myplaylistRepository.delete(songToDelete);

            // Return a successful response with a message indicating the song was deleted successfully
            return ResponseEntity.ok("Song deleted successfully");
        } else {

            // If the authenticated artist is not the owner, return an error message
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this song");
        }
    }
}

package my.playlist.Controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import my.playlist.Dto.MyplaylistDto;
import my.playlist.Dto.MyplaylistUpdate;
import my.playlist.Entity.Myplaylist;
import my.playlist.Entity.UserInfo;
import my.playlist.Repository.MyplaylistRepository;
import my.playlist.Service.MyplaylistService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
public class MyplaylistController {

    @Autowired
    MyplaylistService myplaylistService;

    @Autowired
    Cloudinary cloudinary;


    @Autowired
    MyplaylistRepository myplaylistRepository;

    // sample uploading to db
    @PostMapping("/upload")
    public ResponseEntity<?> createMyplaylist(@RequestParam("file") MultipartFile file, @RequestParam("title") String title, @RequestParam("genres") String genres, @RequestParam("uploadedDate") String uploadedDate, @RequestParam("artist") String artist) {
        try {
            MyplaylistDto createdPlaylist = myplaylistService.createPlaylist(file, title, genres, uploadedDate, artist);
            return new ResponseEntity<>(createdPlaylist, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Catch the IllegalArgumentException and return a custom error message in the response
            String errorMessage = "Invalid date format for uploadedDate. Expected format: yyyy-MM-dd";
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // add users  to db
//    @PostMapping("/add")
//    public String addNewUser(@RequestBody UserInfo userInfo) {
//        return myplaylistService.addUser(userInfo);
//    }


    @PreAuthorize("hasAuthority('listener')")
    @GetMapping("/songs")
    public ResponseEntity<?> getSongs(
            @RequestParam(required = false) String searchTitle,
            @RequestParam(required = false) String filterArtist,
            @RequestParam(required = false) String filterGenres,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String date) {

        // Check if at least one search, filter, or sort parameter is provided
        if (StringUtils.isAllBlank(searchTitle, filterArtist, filterGenres) && StringUtils.isBlank(title) && StringUtils.isBlank(date)) {
            String errorMessage = "No search criteria provided. Please provide at least one valid search, filter, or sort parameter.";
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.isNotBlank(date)) {
            try {
                List<MyplaylistDto> playlistsByDate = myplaylistService.getPlaylistsSortedByUploadedDate(date); // Pass the date string directly
                return new ResponseEntity<>(playlistsByDate, HttpStatus.OK);
            } catch (IllegalArgumentException e) {
                String errorMessage = e.getMessage();
                return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
            }
        } else {
            // If sortField is provided, sort by title matching the specified value
            List<MyplaylistDto> songsByTitle;
            try {
                songsByTitle = myplaylistService.getPlaylistsSortedByTitle(searchTitle, filterArtist, filterGenres, title);
            } catch (IllegalArgumentException e) {
                String errorMessage = e.getMessage();
                return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(songsByTitle, HttpStatus.OK);
        }
    }


    @PreAuthorize("hasAuthority('artist')")
    @PostMapping("/byusers")
    public ResponseEntity<String> addSong(@RequestParam String title,
                                          @RequestParam String genres,
                                          @RequestParam (value ="uploadedDate")String uploadedDateStr,
                                          @RequestParam MultipartFile thumbnailFile,
                                          @RequestParam String artist,
                                          Principal principal) {
        String authenticatedArtist = principal.getName();

        // Check if the authenticated artist matches the provided artist name
        if (artist != null && !authenticatedArtist.equals(artist)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to upload songs for other artists.");
        }

        // Create a new Song object with the extracted data
        Myplaylist newSong = new Myplaylist();

        if (title != null) {
            newSong.setTitle(title);
        }

        if (genres != null) {
            newSong.setGenres(genres);
        }
        newSong.setUploadedDate(uploadedDateStr);
        newSong.setUploadedDate(uploadedDateStr);
        newSong.setArtist(artist);

        try {
            // Upload the thumbnail image to Cloudinary
            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
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

            return ResponseEntity.ok("Song added successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading thumbnail");
        }
    }


    @PreAuthorize("hasAuthority('artist')")
    @DeleteMapping("/{songId}")
    public ResponseEntity<String> deleteSong(@PathVariable Long songId, Principal principal) {

        String authenticatedArtist = principal.getName();

        // Retrieve song by ID
        Myplaylist songToDelete = myplaylistRepository.findById(Math.toIntExact(songId)).orElse(null);

        // Check if the song exists
        if (songToDelete == null) {
            throw new RuntimeException("Song not found");
        }

        // Check if the authenticated artist is the owner of the song
        if (songToDelete.getArtist().equals(principal.getName())) {
            // Delete the song
            myplaylistRepository.delete(songToDelete);
            return ResponseEntity.ok("Song deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this song");
        }
    }

    @PreAuthorize("hasAuthority('artist')")
    @PutMapping("/{id}")
    public ResponseEntity<String> updateSongById(@PathVariable Long id,
                                                 @ModelAttribute MyplaylistUpdate myplaylistUpdate,
                                                 Principal principal) {
        String authenticatedArtist = principal.getName();

        // Check if the authenticated artist matches the artist of the song with the given ID
        Optional<Myplaylist> songOptional = myplaylistRepository.findById(Math.toIntExact(id));

        if (!songOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Song not found.");
        }

        Myplaylist song = songOptional.get();

        if (!authenticatedArtist.equals(song.getArtist())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to update this song.");
        }

        // Update the song with the provided data MyplaylistUpdate
        if (myplaylistUpdate.getTitle() != null) {
            song.setTitle(myplaylistUpdate.getTitle());
        }

        if (myplaylistUpdate.getGenres() != null) {
            song.setGenres(myplaylistUpdate.getGenres());
        }

        if (myplaylistUpdate.getUploadedDate() != null) {
            song.setUploadedDate(myplaylistUpdate.getUploadedDate());
        }

        try {
            // Upload the new thumbnail image to Cloudinary if provided
            MultipartFile thumbnailFile = myplaylistUpdate.getThumbnailFile();
            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                Map<?, ?> cloudinaryResponse = cloudinary.uploader().upload(thumbnailFile.getBytes(), ObjectUtils.emptyMap());

                // Get  new thumbnailUrl & thumbnailId from the Cloudinary response
                String thumbnailUrl = (String) cloudinaryResponse.get("secure_url");
                String thumbnailId = (String) cloudinaryResponse.get("public_id");

                // Set the new thumbnailUrl & thumbnailId in the song object
                song.setThumbnailUrl(thumbnailUrl);
                song.setThumbnailId(thumbnailId);
            }

            // Save updated song to  db
            myplaylistRepository.save(song);

            return ResponseEntity.ok("Song updated successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading thumbnail");
        }
    }
}

package my.playlist.Controller;

import my.playlist.Dto.MyplaylistDto;
import my.playlist.Entity.UserInfo;
import my.playlist.Service.MyplaylistService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;


@RestController
public class MyplaylistController {

    @Autowired
    MyplaylistService myplaylistService;
    UserInfo userInfo;

    //

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
//    @PostMapping("/upload")
//    public ResponseEntity<MyplaylistDto> createMyplaylist(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("title") String title,
//            @RequestParam("genres") String genres,
////            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date uploadedDate,
//            @RequestParam("uploadedDate") String uploadedDate, // Receive as String
//
//            @RequestParam("artist") String artist
//    ) {
//        try {
//            MyplaylistDto createdPlaylist = myplaylistService.createPlaylist(file, title, genres,uploadedDate, artist); // Pass the uploadedDate as String
//            return new ResponseEntity<>(createdPlaylist, HttpStatus.CREATED);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }


//    // add users  to db
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
//            @RequestParam(value = "title", defaultValue = "") String title,
            @RequestParam(required = false) String title,
//            @RequestParam(required = false, defaultValue = "") String date) {
            @RequestParam(required = false) String date){

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

}

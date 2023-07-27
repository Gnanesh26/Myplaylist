package my.playlist.Controller;

import my.playlist.Dto.MyplaylistDto;
import my.playlist.Service.MyplaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;



@RestController
public class MyplaylistController {

    @Autowired
    MyplaylistService myplaylistService;


    @PostMapping("/upload")
    public ResponseEntity<MyplaylistDto> createMyplaylist(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("genres") String genres,
            @RequestParam("uploadedDate") String uploadedDate, // Change the type to String
            @RequestParam("artist") String artist
    ) {
        try {
            MyplaylistDto createdPlaylist = myplaylistService.createPlaylist(file, title, genres, uploadedDate, artist); // Pass the uploadedDate as String
            return new ResponseEntity<>(createdPlaylist, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

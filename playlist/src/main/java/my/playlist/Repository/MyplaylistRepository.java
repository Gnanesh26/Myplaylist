package my.playlist.Repository;

import my.playlist.Entity.Myplaylist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyplaylistRepository extends JpaRepository <Myplaylist,Integer> {
}

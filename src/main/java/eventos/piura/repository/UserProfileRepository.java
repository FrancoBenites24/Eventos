package eventos.piura.repository;

import eventos.piura.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    @Query("SELECT p FROM UserProfile p WHERE p.user.username = :username")
    Optional<UserProfile> findByUserUsername(@Param("username") String username);
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM UserProfile p WHERE p.user.username = :username")
    boolean existsByUserUsername(@Param("username") String username);
}
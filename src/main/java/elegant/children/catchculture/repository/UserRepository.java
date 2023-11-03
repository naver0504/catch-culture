package elegant.children.catchculture.repository;

import elegant.children.catchculture.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {


    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndSocialId(String email, String socialId);
}

package com.carddemo.auth;

import com.carddemo.domain.user.UserEntity;
import com.carddemo.domain.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds USRSEC contents into the users table on first boot, mirroring DUSRSECJ.jcl from CardDemo.
 * Legacy stored plaintext "PASSWORD" - we bcrypt it here. Username + role layout preserved.
 */
@Component
public class UserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UserSeeder.class);
    private static final String DEFAULT_PASSWORD = "PASSWORD";

    private record Seed(String id, String first, String last, String type) {}

    private static final List<Seed> SEEDS = List.of(
            new Seed("ADMIN001", "MARGARET",  "GOLD",       "A"),
            new Seed("ADMIN002", "RUSSELL",   "RUSSELL",    "A"),
            new Seed("ADMIN003", "RAYMOND",   "WHITMORE",   "A"),
            new Seed("ADMIN004", "EMMANUEL",  "CASGRAIN",   "A"),
            new Seed("ADMIN005", "GRANVILLE", "LACHAPELLE", "A"),
            new Seed("USER0001", "LAWRENCE",  "THOMAS",     "U"),
            new Seed("USER0002", "AJITH",     "KUMAR",      "U"),
            new Seed("USER0003", "LAURITZ",   "ALME",       "U"),
            new Seed("USER0004", "WILLIE",    "SAUNDERS",   "U"),
            new Seed("USER0005", "LEE",       "TING",       "U"));

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserSeeder(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) {
            log.info("users table already seeded ({} rows) - skipping", repo.count());
            return;
        }
        String hash = encoder.encode(DEFAULT_PASSWORD);
        SEEDS.forEach(s -> {
            UserEntity u = new UserEntity();
            u.setUserId(s.id());
            u.setFirstName(s.first());
            u.setLastName(s.last());
            u.setPasswordHash(hash);
            u.setUserType(s.type());
            repo.save(u);
        });
        log.info("Seeded {} users (default password = '{}')", SEEDS.size(), DEFAULT_PASSWORD);
    }
}

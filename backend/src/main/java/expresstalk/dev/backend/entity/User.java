package expresstalk.dev.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import expresstalk.dev.backend.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@RequiredArgsConstructor
@NoArgsConstructor
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    @Column(nullable = false)
    private String name;

    @NonNull
    @Column(nullable = false, unique = true)
    private String login;

    @NonNull
    @Column(nullable = false, unique = true)
    @JsonIgnore
    private String email;

    @NonNull
    @Column(nullable = false)
    @JsonIgnore
    private String passwordHash;

    @Column
    @JsonIgnore
    private String emailCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(7) default 'ONLINE'")
    private UserStatus status;

    @ManyToMany
    @JoinTable(
            name = "user_private_chat",
            joinColumns = @JoinColumn(name = "users_id"),
            inverseJoinColumns = @JoinColumn(name = "private_chats_id")
    )
    @JsonIgnore
    private List<PrivateChat> privateChats = new LinkedList<>();
}

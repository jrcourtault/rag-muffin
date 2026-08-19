package fr.drjeanjean.ragmuffin.user;

import fr.drjeanjean.ragmuffin.workspace.Workspace;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idp_id", nullable = false)
    private UUID idpId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Langue langue;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime modifiedAt;

    @Builder
    private User(UUID idpId, Workspace workspace, UserRole role, String email, String firstName, String lastName, Langue langue) {
        this.idpId = idpId;
        this.workspace = workspace;
        this.role = role;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.langue = langue;
    }

    public void update(UserRole role, String firstName, String lastName, Langue langue) {
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.langue = langue;
    }

    public void update(String firstName, String lastName, Langue langue) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.langue = langue;
    }

    public void downgradeRole() {
        this.role = UserRole.VIEWER;
    }
}

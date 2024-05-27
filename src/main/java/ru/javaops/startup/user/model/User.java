package ru.javaops.startup.user.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaops.startup.common.HasIdAndEmail;
import ru.javaops.startup.common.model.NamedEntity;
import ru.javaops.startup.common.validation.NoHtml;

import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends NamedEntity implements HasIdAndEmail {
// No session, no needs Serializable

    @Column(name = "email", nullable = false, unique = true)
    @Email
    @NotBlank
    @Size(max = 64)
    @NoHtml   // https://stackoverflow.com/questions/17480809
    private String email;

    @Column(name = "last_name", nullable = true)
    @Size(max = 32)
    @NoHtml
    private String lastName;

    @Column(name = "enabled", nullable = false, columnDefinition = "bool default true")
    private boolean enabled = true;

    @Column(name = "registered", nullable = false, columnDefinition = "timestamp default now()", updatable = false)
    @NotNull
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date registered = new Date();

    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role"}, name = "uk_user_role"))
    @Column(name = "role")
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles = EnumSet.noneOf(Role.class);

    public User(User u) {
        this(u.id, u.name, u.email, u.lastName, u.enabled, u.registered, u.roles);
    }

    public User(Integer id, String name, String email, String lastName, Role... roles) {
        this(id, name, email, lastName, true, new Date(), Arrays.asList(roles));
    }

    public User(Integer id, String name, String email, String lastName, boolean enabled, Date registered, Collection<Role> roles) {
        super(id, name);
        this.email = email;
        this.lastName = lastName;
        this.enabled = enabled;
        this.registered = registered;
        setRoles(roles);
    }

    public void setRoles(Collection<Role> roles) {
        this.roles = roles.isEmpty() ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(roles);
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    @Override
    public String toString() {
        return "User:" + id + '[' + email + ']';
    }
}
package ru.javaops.startup.user.to;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Value;
import ru.javaops.startup.common.HasIdAndEmail;
import ru.javaops.startup.common.to.NamedTo;
import ru.javaops.startup.common.validation.NoHtml;

@Value
@EqualsAndHashCode(callSuper = true)
public class UserTo extends NamedTo implements HasIdAndEmail {
    @Email
    @NotBlank
    @Size(max = 64)
    @NoHtml  // https://stackoverflow.com/questions/17480809
    String email;

    @NotBlank
    @Size(max = 32)
    @NoHtml
    String lastName;

    public UserTo(Integer id, String name, String email, String lastName) {
        super(id, name);
        this.email = email;
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "UserTo:" + id + '[' + email + ']';
    }
}

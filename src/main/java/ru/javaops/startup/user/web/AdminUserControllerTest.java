package ru.javaops.startup.user.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaops.startup.AbstractControllerTest;
import ru.javaops.startup.user.model.Role;
import ru.javaops.startup.user.model.User;
import ru.javaops.startup.user.repository.UserRepository;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaops.startup.common.util.JsonUtil.writeValue;
import static ru.javaops.startup.user.UserTestData.*;
import static ru.javaops.startup.user.web.AdminUserController.REST_URL;
import static ru.javaops.startup.user.web.UniqueMailValidator.EXCEPTION_DUPLICATE_EMAIL;

class AdminUserControllerTest extends AbstractControllerTest {

    private static final String REST_URL_SLASH = REST_URL + '/';

    @Autowired
    private UserRepository repository;

    @Test
    @WithUserDetails(value = "admin")
    void get() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL_SLASH + ADMIN_ID))
                .andExpect(status().isOk())
                .andDo(print())
                // https://jira.spring.io/browse/SPR-14472
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(USER_MATCHER.contentJson(admin));
    }

    @Test
    @WithUserDetails(value = "admin")
    void getNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL_SLASH + NOT_FOUND))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails(value = "admin")
    void getByEmail() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL_SLASH + "by-email?email=" + admin.getEmail()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(USER_MATCHER.contentJson(admin));
    }

    @Test
    @WithUserDetails(value = "admin")
    void delete() throws Exception {
        perform(MockMvcRequestBuilders.delete(REST_URL_SLASH + USER_ID))
                .andDo(print())
                .andExpect(status().isNoContent());
        assertFalse(repository.findById(USER_ID).isPresent());
    }

    @Test
    @WithUserDetails(value = "admin")
    void deleteNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(REST_URL_SLASH + NOT_FOUND))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails(value = "admin")
    void enableNotFound() throws Exception {
        perform(MockMvcRequestBuilders.patch(REST_URL_SLASH + NOT_FOUND)
                .param("enabled", "false")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void getUnAuth() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = "admin")
    void update() throws Exception {
        User updated = getUpdated();
        updated.setId(null);
        perform(MockMvcRequestBuilders.put(REST_URL_SLASH + USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(updated)))
                .andDo(print())
                .andExpect(status().isNoContent());

        USER_MATCHER.assertMatch(repository.getExisted(USER_ID), getUpdated());
    }

    @Test
    @WithUserDetails(value = "admin")
    void createWithLocation() throws Exception {
        User newUser = getNew();
        ResultActions action = perform(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newUser)))
                .andExpect(status().isCreated());

        User created = USER_MATCHER.readFromJson(action);
        int newId = created.id();
        newUser.setId(newId);
        USER_MATCHER.assertMatch(created, newUser);
        USER_MATCHER.assertMatch(repository.getExisted(newId), newUser);
    }

    @Test
    @WithUserDetails(value = "admin")
    void getAll() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(USER_MATCHER.contentJson(admin, guest, user));
    }

    @Test
    @WithUserDetails(value = "admin")
    void enable() throws Exception {
        perform(MockMvcRequestBuilders.patch(REST_URL_SLASH + USER_ID)
                .param("enabled", "false")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        assertFalse(repository.getExisted(USER_ID).isEnabled());
    }

    @Test
    @WithUserDetails(value = "admin")
    void createInvalid() throws Exception {
        User invalid = new User(null, null, "", "NewLastName", Role.USER, Role.ADMIN);
        perform(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(invalid)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails(value = "admin")
    void updateInvalid() throws Exception {
        User invalid = new User(user);
        invalid.setName("");
        perform(MockMvcRequestBuilders.put(REST_URL_SLASH + USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(invalid)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails(value = "admin")
    void updateHtmlUnsafe() throws Exception {
        User updated = new User(user);
        updated.setName("<script>alert(123)</script>");
        perform(MockMvcRequestBuilders.put(REST_URL_SLASH + USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(updated)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails(value = "admin")
    void updateDuplicate() throws Exception {
        User updated = new User(user);
        updated.setEmail(ADMIN_MAIL);
        perform(MockMvcRequestBuilders.put(REST_URL_SLASH + USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(updated)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(containsString(EXCEPTION_DUPLICATE_EMAIL)));
    }

    @Test
    @WithUserDetails(value = "admin")
    void createDuplicate() throws Exception {
        User expected = new User(null, "New", USER_MAIL, "NewLastName", Role.USER, Role.ADMIN);
        perform(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(expected)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(containsString(EXCEPTION_DUPLICATE_EMAIL)));
    }
}
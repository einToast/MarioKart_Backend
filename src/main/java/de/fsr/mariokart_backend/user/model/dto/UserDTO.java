package de.fsr.mariokart_backend.user.model.dto;

import java.util.LinkedList;
import java.util.List;

import de.fsr.mariokart_backend.user.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO {
    private String username;
    private boolean isAdmin;
    private int ID;

    public UserDTO(User user) {
        setUsername(user.getUsername());
        this.isAdmin = user.isAdmin();
        setID(user.getID());
    }

    public static List<UserDTO> fromUserList(List<User> users) {
        List<UserDTO> userDTOS = new LinkedList<>();
        users.forEach(user -> userDTOS.add(new UserDTO(user)));
        return userDTOS;
    }
}

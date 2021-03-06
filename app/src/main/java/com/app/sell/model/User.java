package com.app.sell.model;

import com.google.firebase.auth.FirebaseUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String uid;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String location;
    private String image;
    private String email;
    private String messagingToken;

    public User(FirebaseUser firebaseUser) {
        this.setImage(String.valueOf(firebaseUser.getPhotoUrl()));
        String[] splitUsername;
        if (firebaseUser.getDisplayName() != null) {
            splitUsername = firebaseUser.getDisplayName().split(" ");
            this.setFirstName(splitUsername[0]);
            this.setLastName(splitUsername[splitUsername.length - 1]);
        }
        this.setUsername(firebaseUser.getDisplayName());
        this.setEmail(firebaseUser.getEmail());
        this.setUid(firebaseUser.getUid());
    }

    public String getPrettyLocation() {
        String city = getLocation().split(",")[2];
        String country = getLocation().split(",")[3];
        return city + "," + country;
    }
}

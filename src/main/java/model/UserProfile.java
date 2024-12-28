package model;

// UserProfile class to hold the profile information
public class UserProfile {
    private String username;
    private String profilePhoto;

    public UserProfile(String username, String profilePhoto) {
        this.username = username;
        this.profilePhoto = profilePhoto;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }
}

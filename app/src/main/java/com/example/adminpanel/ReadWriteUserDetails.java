package com.example.adminpanel;
public class ReadWriteUserDetails {
    public String email,username,name,gender,profileImageUrl;
    public ReadWriteUserDetails(){};
    public ReadWriteUserDetails(String email)
    {
        this.email = email;
        this.username=extractUsername(email);
        this.name="User";
        this.gender="Male";
        this.profileImageUrl = "https://example.com/user_default_profile_pic.jpg";
    }

    public static String extractUsername(String email) {
        if (email != null && email.contains("@")) {
            // Extract substring before the '@' character
            return email.substring(0, email.indexOf("@"));
        }
        return email; // Return the original email if '@' is not found
    }
}

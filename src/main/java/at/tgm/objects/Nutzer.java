package at.tgm.objects;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public abstract class Nutzer extends SendableObject{

    private String phoneNumber;
    private String firstName;
    private String lastName;
    private final String username;
    private String password;

    private String beschreibung;
    private int age;

    private String displayName;
    private NutzerStatus status;

    private final long createdAt;
    private final String uuid;
    private String email;
    private String profilePictureUrl;
    private long lastLoginTimestamp;
    private boolean isDeactivated;

    public Nutzer() {
        this.username = "";
        this.password = "";
        this.createdAt = System.currentTimeMillis();
        this.uuid = UUID.randomUUID().toString();
        this.status = NutzerStatus.OFFLINE;
        this.isDeactivated = false;
    }
    public Nutzer (String username, String password){
        this.username = username;
        this.password = password;
        this.createdAt = System.currentTimeMillis();
        this.status = NutzerStatus.OFFLINE;
        this.uuid = String.valueOf(UUID.randomUUID());
        this.isDeactivated = false;
    }



    public boolean checkPassword(String password){
        return this.password.equals(password);
    }
    @Override
    public boolean equals(Object o){
        if (o == null || o.getClass() != this.getClass()) return false;
        Nutzer n = (Nutzer) o;

        return n.getPassword().equals(getPassword()) && n.getUsername().equals(getUsername());
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }


    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public NutzerStatus getStatus() {
        return status;
    }

    public void setStatus(NutzerStatus status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getUuid() {
        return uuid;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public long getLastLoginTimestamp() {
        return lastLoginTimestamp;
    }

    public void setLastLoginTimestamp(long lastLoginTimestamp) {
        this.lastLoginTimestamp = lastLoginTimestamp;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isDeactivated() {
        return isDeactivated;
    }

    public void setDeactivated(boolean deactivated) {
        isDeactivated = deactivated;
    }
}

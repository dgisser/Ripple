package com.facebook.peepingtom.Models;

import android.content.Context;
import android.util.Log;

import com.facebook.peepingtom.R;
import com.facebook.peepingtom.UI.TimeFormatter;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by dgisser on 7/6/16.
 */
@Parcel
@IgnoreExtraProperties
public class User {
    //regions
    public enum Region {NONE, AFRICA, USACAN, CENTRALAMERICA, SOUTHAMERICA,
            WESTEUROPE, EASTEUROPE, OCEANIA, CENTRALASIA, EASTASIA, MIDDLEEAST,}
    //communitydensity
    public enum CommunityDensity {NONE, RURAL, SUBURBAN, CITY,}
    //gender
    public enum Gender {NONE, MALE, FEMALE, NONBINARY,}
    //sexualorientation
    public enum SexualOrientation {NONE, HETEROSEXUAL, HOMOSEXUAL, BISEXUAL, ASEXUAL,}
    //religion
    public enum Religion {NONE, ATHEIST,AGNOSTIC, CHRISTIAN, HINDU, MUSLIM, JEWISH, BUDDHIST, SIKH,
            JAIN, CONFUCIAN,}
    public enum Color {GREEN, BLUE, PURPLE, RED, ORANGE}
    public String uid;
    public String firstName;
    public String lastName;
    public String bio;
    public int age;
    public String profileUrl;
    public Date birthDay;
    public Date accountCreation;
    public ArrayList<String> interests;
    public HashMap<String, Boolean> followers;
    public HashMap<String, Boolean> following;
    public Region region;
    public CommunityDensity communityDensity;
    public Gender gender;
    public SexualOrientation sexualOrientation;
    public Religion religion;
    public Color color;

    public User() {

    }

    public static void initializeUser (User user) {
        if (user.gender == null) user.gender = Gender.NONE;
        if (user.religion == null) user.religion = Religion.NONE;
        if (user.region == null) user.region = Region.NONE;
        if (user.communityDensity == null) user.communityDensity = CommunityDensity.NONE;
        if (user.sexualOrientation == null) user.sexualOrientation = SexualOrientation.NONE;
        if (user.color == null) user.color = Color.PURPLE;

        if (user.firstName == null) user.firstName = "";
        if (user.lastName == null) user.lastName = "";
        if (user.bio == null) user.bio = "";
        if (user.followers == null) user.followers = new HashMap<>();
        if (user.following == null) user.following = new HashMap<>();
    }

    public User (String uid, String firstName, String lastName, String profileUrl, String bio, int age, Date birthDay,
                 Date accountCreation, ArrayList<String> interests, HashMap<String, Boolean> followers,
                 HashMap<String, Boolean> following, Region region, CommunityDensity communityDensity,
                 Gender gender, SexualOrientation sexualOrientation, Religion religion, Color color) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profileUrl = profileUrl;
        this.bio = bio;
        this.age = age;
        if(birthDay != null) this.setBirthDay(birthDay);
        this.accountCreation = accountCreation;
        this.interests = interests;
        // initializes empty array list if null followers passed in
        if (followers == null) this.followers = new HashMap<>();
        else this.followers = followers;

        // initializes empty array list if null following passed in
        if (following == null) this.following = new HashMap<>();
        else this.following = following;
        this.region = region;
        this.communityDensity = communityDensity;
        this.gender = gender;
        this.sexualOrientation = sexualOrientation;
        this.religion = religion;
        this.color = color;
    }

    public String getUid() { return uid; }

    public String getProfileUrl() { return profileUrl; }

    public String getFirstName() { return firstName; }

    public String getLastName() { return lastName; }

    public String getBio() { return bio; }

    public int getAge() { return age; }

    public Date getBirthDay() { return birthDay; }

    public Date getAccountCreation() { return accountCreation; }

    public ArrayList<String> getInterests() { return interests; }

    public HashMap<String, Boolean> getFollowers() { return followers; }//uid

    public HashMap<String, Boolean> getFollowing() { return following; }//uid

    //public void setReligion(int religion) { this.religion = Religion.values()[religion]; }

    //must be ordinal for firebase to be happy -- do not call to get variable
    public int getRegion() { return region.ordinal(); }

    //must be ordinal for firebase to be happy -- do not call to get variable
    public int getCommunityDensity() { return communityDensity.ordinal(); }

    //must be ordinal for firebase to be happy -- do not call to get variable
    public int getGender() { return gender.ordinal(); }

    //must be ordinal for firebase to be happy -- do not call to get variable
    public int getSexualOrientation() { return sexualOrientation.ordinal(); }

    //must be ordinal for firebase to be happy -- do not call to get variable
    public int getReligion() { return religion.ordinal(); }

    //must be ordinal for firebase to be happy -- do not call to get variable
    public int getColor(){ return color.ordinal(); }

    public void setGenderString(String gender) {
        if (Objects.equals(gender, "female")) this.gender = Gender.FEMALE;
        else if(Objects.equals(gender, "male")) this.gender = Gender.MALE;
        else if (gender == null) this.gender = Gender.NONBINARY;
        else Log.d("user","ERROR");
    }

    public void setRegion(int region) { this.region = Region.values()[region]; }

    public void setCommunityDensity (int density)
    { this.communityDensity = CommunityDensity.values()[density]; }

    public void setGender(int gender) { this.gender = Gender.values()[gender]; }

    public void setSexualOrientation(int orientation)
    { this.sexualOrientation = SexualOrientation.values()[orientation]; }

    public void setReligion(int religion) { this.religion = Religion.values()[religion]; }

    public void setColor(int color) { this.color = Color.values()[color]; }

    public void setFirstName(String firstName) { this.firstName = firstName; }

    public void setLastName(String lastName) { this.lastName = lastName; }

    public void setUid(String uid) { this.uid = uid; }

    public void setBio(String bio) { this.bio = bio; }

    public void setAge(int age) {
        if( this.birthDay == null) this.age = age; }

    public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }

    public void setBirthDay(Date birthDay) {
        this.birthDay = birthDay;
        age = (int) TimeFormatter.getAge(new Date(), birthDay);
    }

    public void setAccountCreation(Date accountCreation) { this.accountCreation = accountCreation; }

    public void setInterests(ArrayList<String> interests) {
        this.interests = interests;
    }

    public void setFollowers(HashMap<String, Boolean> followers) {
        this.followers = followers;
    }

    public void setFollowing(HashMap<String, Boolean> following) {
        this.following = following;
    }

    @Exclude
    public String getReligionString() {
            if (religion == User.Religion.NONE) {
                return "";
            } else {
                return religion.name().substring(0, 1) +
                        religion.name().substring(1).toLowerCase();
            }
    }

    @Exclude
    public String getRegionString(){
        switch (region) {
            case NONE: return "";
            case AFRICA: return "Africa";
            case USACAN: return "US/Canada";
            case CENTRALAMERICA: return "Central America";
            case SOUTHAMERICA: return "South America";
            case EASTEUROPE: return "East Europe";
            case OCEANIA: return "Oceania";
            case WESTEUROPE: return "West Europe";
            case CENTRALASIA: return "Central Asia";
            case EASTASIA: return "East Asia";
            case MIDDLEEAST: return "Middle East";
            default: return "";
        }
    }

    @Exclude
    public String getGenderString() {
        if (gender == Gender.NONE) return "";
        else {
            return gender.name().substring(0, 1) +
                   gender.name().substring(1).toLowerCase();
        }
    }

    @Exclude
    public int getColorHex(Context context) {
        switch (color) {
            case RED: return context.getColor(R.color.redPastel);
            case ORANGE: return context.getColor(R.color.orangePastel);
            case BLUE: return context.getColor(R.color.bluePastel);
            case GREEN: return context.getColor(R.color.greenPastel);
            case PURPLE: return context.getColor(R.color.purplePastel);
            default: return context.getColor(R.color.purplePastel);
        }

    }

    // converts enums to @color id values
    @Exclude
    public int getColorLocationFromEnum(User.Color color) {
        switch (color) {
            case RED: return R.color.redPastel;
            case ORANGE: return R.color.orangePastel;
            case BLUE: return R.color.bluePastel;
            case GREEN: return R.color.greenPastel;
            case PURPLE: return R.color.purplePastel;
            default: return R.color.purplePastel;
        }

    }

    @Exclude
    public int getColorHexDark(Context context) {
        switch (color) {
            case RED: return context.getColor(R.color.redAccent);
            case ORANGE: return context.getColor(R.color.orangeAccent);
            case BLUE: return context.getColor(R.color.blueAccent);
            case GREEN: return context.getColor(R.color.greenAccent);
            case PURPLE: return context.getColor(R.color.purpleAccent);
            default: return context.getColor(R.color.purpleAccent);
        }

    }

    @Exclude
    public int getColorHexAccent(Context context) {
        switch (color) {
            case RED: return context.getColor(R.color.blueAccent);
            case ORANGE: return context.getColor(R.color.yellowAccent);
            case BLUE: return context.getColor(R.color.redAccent);
            case GREEN: return context.getColor(R.color.purpleAccent);
            case PURPLE: return context.getColor(R.color.greenAccent);
            default: return context.getColor(R.color.greenAccent);
        }

    }
    @Exclude
    public int getColorTheme() {
        switch (color) {
            case RED: return R.style.RedTheme;
            case ORANGE: return R.style.OrangeTheme;
            case BLUE: return R.style.BlueTheme;
            case GREEN: return R.style.GreenTheme;
            case PURPLE: return R.style.AppTheme;
            default: return R.style.AppTheme;
        }

    }


    @Exclude
    public String getSexualOrientationString() {
        if (sexualOrientation == User.SexualOrientation.NONE) return "";
        else {
            return sexualOrientation.name().substring(0, 1) +
                    sexualOrientation.name().substring(1).toLowerCase();
        }
    }

    @Exclude
    public String getCommunityDensityString() {
        if (communityDensity == User.CommunityDensity.NONE) return "";
        else {
            return communityDensity.name().substring(0, 1) +
                    communityDensity.name().substring(1).toLowerCase();
        }
    }


}

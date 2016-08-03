package com.facebook.peepingtom.Database;

import com.facebook.peepingtom.Models.Story;
import com.facebook.peepingtom.Models.User;

import java.util.Date;

/**
 * Created by dgisser on 7/8/16.
 */
public class DatabaseTester {
    //TODO: fix this
    public static void test() {

        final User user = new User("", "David"," Gisser", "",  "I like pina coladas", 20, new Date(820472400),
                new Date(), null, null, null, User.Region.USACAN, User.CommunityDensity.CITY,
                User.Gender.MALE, User.SexualOrientation.HETEROSEXUAL, User.Religion.NONE, null);


        Story story = new Story("", "I love cats", "", new Date(),
                0, user, null);
        DatabaseLayer.submitStory(story);
    }
}

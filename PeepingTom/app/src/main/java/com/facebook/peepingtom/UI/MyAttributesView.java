package com.facebook.peepingtom.UI;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.peepingtom.Activities.AccountActivity;
import com.facebook.peepingtom.Adapters.StoryRecyclerAdapter;
import com.facebook.peepingtom.Database.DatabaseLayer;
import com.facebook.peepingtom.Fragments.ProfileStoriesFragment;
import com.facebook.peepingtom.Fragments.SearchFragment;
import com.facebook.peepingtom.GlobalVars;
import com.facebook.peepingtom.Models.Story;
import com.facebook.peepingtom.Models.User;
import com.facebook.peepingtom.R;
import com.sackcentury.shinebuttonlib.ShineButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by aespino on 7/14/16.
 */
public class MyAttributesView {
    View view;
    EditText etAge;
    EditText etBirthday;
    EditText etFirst;
    EditText etLast;
    Spinner spGender;
    Spinner spRegion;
    Spinner spReligion;
    Spinner spCommunityDensity;
    Spinner spSexualOrientation;
    LinearLayout llColorButtons;
    Button btnDone;
    Boolean colorChanged = false;

    User user;

    String gender[] = new String[0];
    ArrayAdapter genderAdapter;
    String region[];
    ArrayAdapter regionAdapter;
    String religion[];
    CustomAdapter religionAdapter;
    String communityDensity[];
    CustomAdapter communityDensityAdapter;
    String sexualOrientation[];
    CustomAdapter sexualOrientationAdapter;

    int age;
    String firstName;
    String lastName;
    Date birthday;
    public User.Gender genderChoice = User.Gender.NONE;
    public User.Religion religionChoice = User.Religion.NONE;
    public User.Region regionChoice = User.Region.NONE;
    public User.CommunityDensity densityChoice = User.CommunityDensity.NONE;
    public User.SexualOrientation orientationChoice = User.SexualOrientation.NONE;
    public User.Color colorChoice = User.Color.PURPLE;

    boolean isProfile;

    public MyAttributesView(boolean isProfile) {this.isProfile = isProfile;}

    /* sets up an the attributes view given an item view by the adapter*/
    public void setUpFromView(View view) {
        this.view = view;
        etAge = (EditText) view.findViewById(R.id.etAge);
        etBirthday = (EditText) view.findViewById(R.id.etBirthday);
        etFirst = (EditText) view.findViewById(R.id.etFirst);
        etLast = (EditText) view.findViewById(R.id.etLast);
        spReligion = (Spinner) view.findViewById(R.id.spReligion);
        spGender = (Spinner) view.findViewById(R.id.spGender);
        spRegion = (Spinner) view.findViewById(R.id.spRegion);
        spCommunityDensity = (Spinner) view.findViewById(R.id.spCommunityDensity);
        spSexualOrientation = (Spinner) view.findViewById(R.id.spSexualOrientation);
        btnDone = (Button) view.findViewById(R.id.btnDone);
        llColorButtons = (LinearLayout) view.findViewById(R.id.llColorButtons);
    }


    /* configures changes to the Attributes view*/

    public void configureMyAttributesView(final Fragment fragment) {
        setEnumArrays();
        if (isProfile) {
            user = ((GlobalVars) fragment.getActivity().getApplication()).getUser();
            genderChoice = user.gender;
            religionChoice = user.religion;
            regionChoice = user.region;
            densityChoice = user.communityDensity;
            orientationChoice = user.sexualOrientation;
            colorChoice = user.color;
            firstName = user.getFirstName();
            lastName = user.getLastName();
            birthday = user.getBirthDay();
            initalizeEditText();
            etFirst.setVisibility(View.VISIBLE);
            etLast.setVisibility(View.VISIBLE);
            llColorButtons.setVisibility(View.VISIBLE);
            createColorButtons(fragment);
            btnDone.setVisibility(View.VISIBLE);
            btnDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(user.color!= colorChoice){
                        colorChanged = true;
                    } else colorChanged = false;
                    ProfileStoriesFragment profileStoriesFragment = (ProfileStoriesFragment) fragment;
                    user = new User(user.uid, firstName, lastName, user.getProfileUrl(), user.getBio(),
                            age, birthday, user.getAccountCreation(), user.getInterests(), user.getFollowers(),
                            user.getFollowing(), regionChoice, densityChoice, genderChoice, orientationChoice,
                            religionChoice, colorChoice);
                    ((GlobalVars) fragment.getActivity().getApplication()).setUser(user);
                    DatabaseLayer.submitUser(user);
                    profileStoriesFragment.storyAdapter.inEditMode = false;
                   // if(colorChanged) profileStoriesFragment.storyAdapter.invalidate();
                   if(colorChanged) {
                       ((GradientDrawable)fragment.getResources().getDrawable(R.drawable.custom_button))
                               .setColor(user.getColorHex(fragment.getContext()));
                   }

                    profileStoriesFragment.storyAdapter.notifyDataSetChanged();
                }
            });
        } else {
            genderChoice = ((SearchFragment)fragment).gender;
            religionChoice = ((SearchFragment)fragment).religion;
            regionChoice = ((SearchFragment)fragment).region;
            densityChoice = ((SearchFragment)fragment).density;
            orientationChoice = ((SearchFragment)fragment).orientation;
            btnDone.setVisibility(View.GONE);
            etFirst.setVisibility(View.GONE);
            etLast.setVisibility(View.GONE);
            etBirthday.setVisibility(View.GONE);
            etAge.setVisibility(View.GONE);
            llColorButtons.setVisibility(View.GONE);
        }
        setETListeners(fragment.getContext());
        setSpinners(fragment.getContext());
        handleSpinnerClicks(fragment);
    }

    private void setETListeners(final Context context) {
        if (isProfile) {
            final Calendar myCalendar = Calendar.getInstance();
            final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    // TODO Auto-generated method stub
                    myCalendar.set(Calendar.YEAR, year);
                    myCalendar.set(Calendar.MONTH, monthOfYear);
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    if (user.birthDay != myCalendar.getTime()) {
                        birthday = myCalendar.getTime();
                        updateLabel(myCalendar.getTime());
                        etAge.setEnabled(false);
                    }
                }
            };

            etBirthday.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DatePickerDialog(context, date, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            });

            etAge.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_DONE) {
                        age = Integer.parseInt(etAge.getText().toString());
                        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        return true;
                    }
                    return true;
                }
            });
        }

        etLast.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    if (isProfile && !etLast.getText().toString().trim().isEmpty())
                        lastName = etLast.getText().toString();
                    InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    return true;
                }
                return true;
            }
        });

        etFirst.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    if (isProfile && !etFirst.getText().toString().trim().isEmpty())
                        firstName = etFirst.getText().toString();
                    InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    return true;
                }
                return true;
            }
        });
    }

    public void createColorButtons(Fragment fragment){
        // this is called everytime you open myattributes so clears all existing buttons
        if(llColorButtons.getChildCount() > 0) {
            llColorButtons.removeViewsInLayout(1, llColorButtons.getChildCount() - 1);
        }
            final Fragment mFragment = fragment;
        // list of enums
            final User.Color[] colorValues = User.Color.values();
            for (int i = 0; i < colorValues.length; i++) {
                final int j = i;
                final ShineButton button = new ShineButton(fragment.getContext());
                button.setBtnColor(mFragment.getContext().getColor(user.getColorLocationFromEnum(colorValues[j])));
                button.setBtnFillColor(mFragment.getContext().getColor(user.getColorLocationFromEnum(colorValues[j])));
                button.setShapeResource(R.drawable.ic_circle);
                button.setAllowRandomColor(false);

                // converts int (in this case 10) to dp
                Resources r = fragment.getContext().getResources();
                int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
                // add margins between buttons
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(125,125);
                params.setMargins(px, px, 0, 0);
                button.setLayoutParams(params);


                if( user.color == colorValues[i]){
                    button.setShapeResource(R.drawable.ic_circle_outline);
                    button.setBackground(fragment.getContext().getResources().getDrawable(R.drawable.rounded_button_selected));
                    ((GradientDrawable) button.getBackground()).setColorFilter(mFragment.getContext().getColor(user.getColorLocationFromEnum(colorValues[j])), PorterDuff.Mode.SRC_ATOP);
                    button.setSelected(true);
                }else{

                    button.setBackground(fragment.getContext().getResources().getDrawable(R.drawable.rounded_button_states));
                    ((StateListDrawable) button.getBackground()).setColorFilter(mFragment.getContext().getColor(user.getColorLocationFromEnum(colorValues[j])), PorterDuff.Mode.SRC_ATOP);
                    button.setSelected(false);
                }

                button.setTag(user.getColorLocationFromEnum(colorValues[j]));
                /*final Button button = new Button(fragment.getContext());
                // if the user's current color is this button make it selected

                button.setBackground(fragment.getContext().getResources().getDrawable(R.drawable.rounded_button_states));
                ((StateListDrawable) button.getBackground()).setColorFilter(mFragment.getContext().getColor(user.getColorLocationFromEnum(colorValues[j])), PorterDuff.Mode.SRC_ATOP);
                if (user.color == colorValues[i]){
                    button.setBackground(fragment.getContext().getResources().getDrawable(R.drawable.rounded_button_selected));
                    ((GradientDrawable) button.getBackground()).setColorFilter(mFragment.getContext().getColor(user.getColorLocationFromEnum(colorValues[j])), PorterDuff.Mode.SRC_ATOP);
                    button.setSelected(true);
                } else {
                    button.setSelected(false);
                }
                button.setVisibility(View.VISIBLE);
                // converts int (in this case 10) to dp
                Resources r = fragment.getContext().getResources();
                int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
                // add margins between buttons
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(px, px, 0, 0);
                button.setLayoutParams(params);
                // add tag of color to button so you can get color from it later
                button.setTag(user.getColorLocationFromEnum(colorValues[j]));*/
                llColorButtons.addView(button);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!button.isSelected()){
                            for (int i = 1; i < llColorButtons.getChildCount(); i++){
                                if (llColorButtons.getChildAt(i).isSelected()){
                                    llColorButtons.getChildAt(i).setSelected(false);
                                    ((ShineButton)llColorButtons.getChildAt(i)).setShapeResource(R.drawable.ic_circle);
                                    llColorButtons.getChildAt(i).setBackground(mFragment.getContext()
                                            .getResources().getDrawable(R.drawable.rounded_button_states));
                                    ((StateListDrawable) llColorButtons.getChildAt(i).getBackground()).setColorFilter(mFragment.getContext()
                                            .getColor(user.getColorLocationFromEnum(colorValues[i - 1])), PorterDuff.Mode.SRC_ATOP);
                                }
                            }
                            button.setSelected(true);
                            // get the color from the selected button
                            colorChoice = setColorFromId(((int) button.getTag()));
                            button.setBackground(mFragment.getContext().getResources()
                                    .getDrawable(R.drawable.rounded_button_selected));
                            ((GradientDrawable) button.getBackground()).setColorFilter(
                                    mFragment.getContext().getColor(user.getColorLocationFromEnum(colorValues[j])), PorterDuff.Mode.SRC_ATOP);
                            //button.setShapeResource(R.drawable.ic_circle_outline);

                        }
                    }
                });

            }
    }

    // converts enums to @color id values
    public User.Color setColorFromId(int thisColor) {
        switch (thisColor) {
            case R.color.redPastel: return User.Color.RED;
            case R.color.orangePastel: return User.Color.ORANGE;
            case R.color.bluePastel: return User.Color.BLUE;
            case R.color.greenPastel: return User.Color.GREEN;
            case R.color.purplePastel: return User.Color.PURPLE;
            default: return User.Color.PURPLE;
        }

    }

    /* sets up the options available for the region, religion, and gender spinners. if the user attribute is not
    none, it also sets up the spinner to automatically select the value of that user attribute*/
    public void setSpinners(final Context context) {
        genderAdapter = new CustomAdapter(context,
                android.R.layout.simple_spinner_dropdown_item, gender);
        regionAdapter = new CustomAdapter(context,
                android.R.layout.simple_spinner_dropdown_item, region);
        religionAdapter = new CustomAdapter(context,
                android.R.layout.simple_spinner_dropdown_item, religion);
        communityDensityAdapter = new CustomAdapter(context,
                android.R.layout.simple_spinner_dropdown_item, communityDensity);
        sexualOrientationAdapter = new CustomAdapter(context,
                android.R.layout.simple_spinner_dropdown_item, sexualOrientation);

        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(genderAdapter);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRegion.setAdapter(regionAdapter);
        religionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spReligion.setAdapter(religionAdapter);
        communityDensityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCommunityDensity.setAdapter(communityDensityAdapter);
        sexualOrientationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSexualOrientation.setAdapter(sexualOrientationAdapter);

        switch (orientationChoice) {
            case HETEROSEXUAL: spSexualOrientation.setSelection(sexualOrientationAdapter.getPosition("Heterosexual"));
                break;
            case HOMOSEXUAL: spSexualOrientation.setSelection(sexualOrientationAdapter.getPosition("Homosexual"));
                break;
            case ASEXUAL: spSexualOrientation.setSelection(sexualOrientationAdapter.getPosition("Asexual"));
                break;
            case BISEXUAL: spSexualOrientation.setSelection(sexualOrientationAdapter.getPosition("Bisexual"));
            default:
                break;
        }
        switch (genderChoice) {
            case MALE: spGender.setSelection(genderAdapter.getPosition("Male"));
                break;
            case FEMALE: spGender.setSelection(genderAdapter.getPosition("Female"));
                break;
            case NONBINARY: spGender.setSelection(genderAdapter.getPosition("Nonbinary"));
                break;
            default:
                break;
        }
        switch (religionChoice) {
            case AGNOSTIC: spReligion.setSelection(religionAdapter.getPosition("Agnostic"));
                break;
            case ATHEIST: spReligion.setSelection(religionAdapter.getPosition("Atheist"));
                break;
            case BUDDHIST: spReligion.setSelection(religionAdapter.getPosition("Buddhist"));
                break;
            case HINDU: spReligion.setSelection(religionAdapter.getPosition("Hindu"));
                break;
            case SIKH: spReligion.setSelection(religionAdapter.getPosition("Sikh"));
                break;
            case CHRISTIAN: spReligion.setSelection(religionAdapter.getPosition("Christian"));
                break;
            case JAIN: spReligion.setSelection(religionAdapter.getPosition("Jain"));
                break;
            case JEWISH: spReligion.setSelection(religionAdapter.getPosition("Jew"));
                break;
            case CONFUCIAN: spReligion.setSelection(religionAdapter.getPosition("Confucian"));
                break;
            case MUSLIM: spReligion.setSelection(religionAdapter.getPosition("Muslim"));
                break;
            default:
                break;
        }
        switch (regionChoice) {
            case AFRICA: spRegion.setSelection(regionAdapter.getPosition("Africa"));
                break;
            case CENTRALASIA: spRegion.setSelection(regionAdapter.getPosition("Central Asia"));
                break;
            case CENTRALAMERICA: spRegion.setSelection(regionAdapter.getPosition("Central America"));
                break;
            case EASTASIA: spRegion.setSelection(regionAdapter.getPosition("East Asia"));
                break;
            case EASTEUROPE: spRegion.setSelection(regionAdapter.getPosition("East Europe"));
                break;
            case MIDDLEEAST: spRegion.setSelection(regionAdapter.getPosition("Middle East"));
                break;
            case SOUTHAMERICA: spRegion.setSelection(regionAdapter.getPosition("South America"));
                break;
            case USACAN: spRegion.setSelection(regionAdapter.getPosition("US/Canada"));
                break;
            case WESTEUROPE: spRegion.setSelection(regionAdapter.getPosition("West Europe"));
                break;
            default:
                break;
        }
        switch (densityChoice) {
            case CITY: spCommunityDensity.setSelection(communityDensityAdapter.getPosition("City"));
                break;
            case SUBURBAN: spCommunityDensity.setSelection(communityDensityAdapter.getPosition("Suburban"));
                break;
            case RURAL: spCommunityDensity.setSelection(communityDensityAdapter.getPosition("Rural"));
                break;
            default:
                break;
        }
    }

    public void setEnumArrays() {
        User.SexualOrientation[] sexualOrientationValues = User.SexualOrientation.values();
        sexualOrientation = new String[sexualOrientationValues.length];
        for (int i = 0; i < sexualOrientationValues.length; i++) {
            if (sexualOrientationValues[i] == User.SexualOrientation.NONE)
                sexualOrientation[i] = "Sexual Orientaion +";
            else sexualOrientation[i] = sexualOrientationValues[i].name().substring(0, 1) +
                        sexualOrientationValues[i].name().substring(1).toLowerCase();
        }
        User.Gender[] genderValues = User.Gender.values();
        gender = new String[genderValues.length];
        for (int i = 0; i < genderValues.length; i++) {
            if (genderValues[i] == User.Gender.NONE) gender[i] = "Gender +";
            else gender[i] = genderValues[i].name().substring(0, 1) +
                        genderValues[i].name().substring(1).toLowerCase();
        }
        User.Region[] regionValues = User.Region.values();
        region = new String[regionValues.length];
        for (int i = 0; i < regionValues.length; i++) {
            switch (regionValues[i]) {
                case NONE: region[i] = "Region +";
                    break;
                case AFRICA: region[i] = "Africa";
                    break;
                case USACAN: region[i] = "US/Canada";
                    break;
                case CENTRALAMERICA: region[i] = "Central America";
                    break;
                case SOUTHAMERICA: region[i] = "South America";
                    break;
                case EASTEUROPE: region[i] = "East Europe";
                    break;
                case OCEANIA: region[i] = "Oceania";
                    break;
                case WESTEUROPE: region[i] = "West Europe";
                    break;
                case CENTRALASIA: region[i] = "Central Asia";
                    break;
                case EASTASIA: region[i] = "East Asia";
                    break;
                case MIDDLEEAST: region[i] = "Middle East";
                    break;
                default:
                    break;
            }
        }
        User.Religion[] religionValues = User.Religion.values();
        religion = new String[religionValues.length];
        for (int i = 0; i < religionValues.length; i++) {
            if (religionValues[i] == User.Religion.NONE) religion[i] = "Religion +";
            else religion[i] = religionValues[i].name().substring(0, 1) +
                        religionValues[i].name().substring(1).toLowerCase();
        }
        User.CommunityDensity[] communityDensityValues = User.CommunityDensity.values();
        communityDensity = new String[communityDensityValues.length];
        for (int i = 0; i < communityDensity.length; i++) {
            if (communityDensityValues[i] == User.CommunityDensity.NONE)
                communityDensity[i] = "Community Density +";
            else communityDensity[i] = communityDensityValues[i].name().substring(0, 1) +
                        communityDensityValues[i].name().substring(1).toLowerCase();
        }
    }

    /* handles user clicks on spinner objects */
    public void handleSpinnerClicks(final Fragment fragment) {
        final User.Gender[] genderValues = User.Gender.values();
        final User.Region[] regionValues = User.Region.values();
        final User.Religion[] religionValues = User.Religion.values();
        final User.CommunityDensity[] communityDensityValues = User.CommunityDensity.values();
        final User.SexualOrientation[] sexualOrientationValues = User.SexualOrientation.values();
        spGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {
                // On selecting a spinner item
                genderChoice = genderValues[position];
                if (!isProfile) {
                    ((SearchFragment) fragment).gender = genderChoice;
                    if (genderChoice != User.Gender.NONE)
                        resetSpinners(genderChoice, User.Religion.NONE, User.Region.NONE,
                            User.CommunityDensity.NONE, User.SexualOrientation.NONE);
                }

        }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
        spReligion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {
                // On selecting a spinner item
                religionChoice = religionValues[position];
                if (!isProfile) {
                    ((SearchFragment) fragment).religion = religionChoice;
                    if (religionChoice != User.Religion.NONE)
                        resetSpinners(User.Gender.NONE, religionChoice, User.Region.NONE,
                                User.CommunityDensity.NONE, User.SexualOrientation.NONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
        spRegion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {
                // On selecting a spinner item
                regionChoice = regionValues[position];
                if (!isProfile) {
                    ((SearchFragment) fragment).region = regionChoice;
                    if (regionChoice != User.Region.NONE)
                        resetSpinners(User.Gender.NONE, User.Religion.NONE, regionChoice,
                                User.CommunityDensity.NONE, User.SexualOrientation.NONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
        spCommunityDensity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {
                // On selecting a spinner item
                densityChoice = communityDensityValues[position];
                if (!isProfile) {
                    ((SearchFragment) fragment).density = densityChoice;
                    if (densityChoice != User.CommunityDensity.NONE)
                        resetSpinners(User.Gender.NONE, User.Religion.NONE, User.Region.NONE,
                                                     densityChoice, User.SexualOrientation.NONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
        spSexualOrientation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {
                // On selecting a spinner item
                orientationChoice = sexualOrientationValues[position];
                if (!isProfile) {
                    ((SearchFragment) fragment).orientation = orientationChoice;
                    if (genderChoice != User.Gender.NONE)
                        resetSpinners(User.Gender.NONE, User.Religion.NONE, User.Region.NONE,
                                                User.CommunityDensity.NONE, orientationChoice);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });

    }

    public void initalizeEditText() {
        etFirst.setText(user.getFirstName());
        etLast.setText(user.getLastName());
        etBirthday.setShowSoftInputOnFocus(false); //TODO fix this∂
        if (user.getAge() != 0) etAge.setText(String.valueOf(user.getAge()));
        if (user.birthDay != null) {
            updateLabel(user.birthDay);
            etAge.setEnabled(false);
        }


    }
    private void updateLabel(Date date) {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etBirthday.setText(sdf.format(date));
    }

    private void resetSpinners(User.Gender gender, User.Religion religion, User.Region region,
                               User.CommunityDensity density, User.SexualOrientation orientation) {
        spGender.setSelection(gender.ordinal());
        spReligion.setSelection(religion.ordinal());
        spRegion.setSelection(region.ordinal());
        spCommunityDensity.setSelection(density.ordinal());
        spSexualOrientation.setSelection(orientation.ordinal());
    }

    public class CustomAdapter extends ArrayAdapter<String> {
        public CustomAdapter(Context context, int textViewResourceId, String[] objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return super.getDropDownView(position, null, parent);
        }
    }
}
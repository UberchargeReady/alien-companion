package com.gDyejeekis.aliencompanion.activities;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.entity.Subreddit;
import com.gDyejeekis.aliencompanion.models.filters.DomainFilter;
import com.gDyejeekis.aliencompanion.models.filters.Filter;
import com.gDyejeekis.aliencompanion.models.filters.FilterProfile;
import com.gDyejeekis.aliencompanion.models.filters.FlairFilter;
import com.gDyejeekis.aliencompanion.models.filters.SelfTextFilter;
import com.gDyejeekis.aliencompanion.models.filters.SubredditFilter;
import com.gDyejeekis.aliencompanion.models.filters.TitleFilter;
import com.gDyejeekis.aliencompanion.models.filters.UserFilter;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.views.DelayAutoCompleteTextView;
import com.gDyejeekis.aliencompanion.views.adapters.RemovableItemListAdapter;
import com.gDyejeekis.aliencompanion.views.adapters.SubredditAutoCompleteAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 6/21/2017.
 */

public class EditFilterProfileActivity extends ToolbarActivity implements View.OnClickListener, DialogInterface.OnClickListener, TextView.OnEditorActionListener{

    private FilterProfile profile;
    private boolean isNewProfile;
    private List<Filter> originalFilters;
    private List<String> originalSubRestr;
    private List<String> originalMultiRestr;
    private EditText nameField;
    private EditText domainField;
    private EditText titleField;
    private EditText flairField;
    private EditText selfTextField;
    private DelayAutoCompleteTextView subredditField;
    private EditText userField;
    private DelayAutoCompleteTextView subRestrField;
    private EditText multiRestrField;
    private ListView domains;
    private ListView titles;
    private ListView flairs;
    private ListView selfTexts;
    private ListView subreddits;
    private ListView users;
    private ListView subRestrctions;
    private ListView multiRestrctions;

    @Override
    public void finish() {
        super.finish();
        MyApplication.setPendingTransitions(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_filter_profile);
        initToolbar();
        initFields();
        initProfile();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    private void initFields() {
        nameField = findViewById(R.id.editText_profile_name);
        domainField = findViewById(R.id.editText_domain_filter);
        titleField = findViewById(R.id.editText_title_filter);
        flairField = findViewById(R.id.editText_flair_filter);
        selfTextField = findViewById(R.id.editText_self_text_filter);
        subredditField = findViewById(R.id.editText_subreddit_filter);
        userField = findViewById(R.id.editText_user_filter);
        subRestrField = findViewById(R.id.editText_subreddit_restrction);
        multiRestrField = findViewById(R.id.editText_multireddit_restrction);
        domains = findViewById(R.id.listView_domain_filters);
        titles = findViewById(R.id.listView_title_filters);
        flairs = findViewById(R.id.listView_flair_filters);
        selfTexts = findViewById(R.id.listView_self_text_filters);
        subreddits = findViewById(R.id.listView_subreddit_filters);
        users = findViewById(R.id.listView_user_filters);
        subRestrctions = findViewById(R.id.listView_subreddit_restrictions);
        multiRestrctions = findViewById(R.id.listView_multireddit_restrictions);
        ImageView addDomain = findViewById(R.id.button_add_domain_filter);
        ImageView addTitle = findViewById(R.id.button_add_title_filter);
        ImageView addSelfText = findViewById(R.id.button_add_self_text_filter);
        ImageView addFlair = findViewById(R.id.button_add_flair_filter);
        ImageView addSubreddit = findViewById(R.id.button_add_subreddit_filter);
        ImageView addUser = findViewById(R.id.button_add_user_filter);
        ImageView addSubRestr = findViewById(R.id.button_add_subreddit_restrction);
        ImageView addMultiRestr = findViewById(R.id.button_add_multireddit_restrction);
        Button saveButton = findViewById(R.id.button_save_changes);

        styleAddImageView(addDomain);
        styleAddImageView(addTitle);
        styleAddImageView(addSelfText);
        styleAddImageView(addFlair);
        styleAddImageView(addSubreddit);
        styleAddImageView(addUser);
        styleAddImageView(addSubRestr);
        styleAddImageView(addMultiRestr);

        domainField.setOnEditorActionListener(this);
        titleField.setOnEditorActionListener(this);
        flairField.setOnEditorActionListener(this);
        selfTextField.setOnEditorActionListener(this);
        subredditField.setOnEditorActionListener(this);
        userField.setOnEditorActionListener(this);
        subRestrField.setOnEditorActionListener(this);
        multiRestrField.setOnEditorActionListener(this);

        addDomain.setOnClickListener(this);
        addTitle.setOnClickListener(this);
        addSelfText.setOnClickListener(this);
        addFlair.setOnClickListener(this);
        addSubreddit.setOnClickListener(this);
        addUser.setOnClickListener(this);
        addSubRestr.setOnClickListener(this);
        addMultiRestr.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        SubredditAutoCompleteAdapter autocompleteAdapter = new SubredditAutoCompleteAdapter(this);
        subredditField.setAdapter(autocompleteAdapter);
        subredditField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Subreddit subreddit = (Subreddit) adapterView.getItemAtPosition(i);
                String name = subreddit.getDisplayName();
                subredditField.setText(name);
                subredditField.setSelection(name.length());
            }
        });
        subRestrField.setAdapter(autocompleteAdapter);
        subRestrField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Subreddit subreddit = (Subreddit) adapterView.getItemAtPosition(i);
                String name = subreddit.getDisplayName();
                subRestrField.setText(name);
                subRestrField.setSelection(name.length());
            }
        });
    }

    private void styleAddImageView(ImageView imageView) {
        int drawable;
        float alpha;
        switch (MyApplication.currentBaseTheme) {
            case AppConstants.LIGHT_THEME:
                drawable = R.drawable.ic_add_circle_outline_black_24dp;
                alpha = 0.54f;
                break;
            case AppConstants.DARK_THEME_LOW_CONTRAST:
                drawable = R.drawable.ic_add_circle_outline_white_24dp;
                alpha = 0.6f;
                break;
            default:
                drawable = R.drawable.ic_add_circle_outline_white_24dp;
                alpha = 1f;
                break;
        }
        imageView.setImageResource(drawable);
        imageView.setAlpha(alpha);
    }

    private void initProfile() {
        FilterProfile originalProfile = (FilterProfile) getIntent().getSerializableExtra("profile");
        isNewProfile = (originalProfile==null);

        if (isNewProfile) {
            this.profile = new FilterProfile();
            getSupportActionBar().setTitle("Create filter profile");
            nameField.requestFocus();
        } else {
            this.profile = originalProfile;
            getSupportActionBar().setTitle("Edit filter profile");
            nameField.setText(originalProfile.getName());

            // not ideal, check same method in EditSyncProfileActivity
            try {
                originalFilters = new ArrayList<>(originalProfile.getFilters());
                originalSubRestr = new ArrayList<>(originalProfile.getSubredditRestrictions());
                originalMultiRestr = new ArrayList<>(originalProfile.getMultiredditRestrictions());
            } catch (Exception e) { // in case of null original profile lists
                e.printStackTrace();
            }
        }
        refreshFilters();
        refreshSubredditRestrctions();
        refreshMultiredditRestrctions();
    }

    private void refreshFilters() {
        refreshDomainFilters();
        refreshTitleFilters();
        refreshFlairFilters();
        refreshSelfTextFilters();
        refreshSubredditFilters();
        refreshUserFilters();
    }

    private void refreshFilters(Class<? extends Filter> cls) {
        if (cls == DomainFilter.class) {
            refreshDomainFilters();
        } else if (cls == TitleFilter.class) {
            refreshTitleFilters();
        } else if (cls == FlairFilter.class) {
            refreshFlairFilters();
        } else if (cls == SelfTextFilter.class) {
            refreshSelfTextFilters();
        } else if (cls == SubredditFilter.class) {
            refreshSubredditFilters();
        } else if (cls == UserFilter.class) {
            refreshUserFilters();
        }
    }

    private void refreshFilterList(Class<? extends Filter> cls, ListView listView) {
        List<String> filters = profile.getFilterStrings(cls);
        if (filters == null || filters.isEmpty()) {
            listView.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.VISIBLE);
            listView.setAdapter(new RemovableItemListAdapter(this, filters, RemovableItemListAdapter.FILTERS));
            GeneralUtils.setListViewHeightBasedOnChildren(listView);
        }
    }

    private void refreshDomainFilters() {
        refreshFilterList(DomainFilter.class, domains);
    }

    private void refreshFlairFilters() {
        refreshFilterList(FlairFilter.class, flairs);
    }

    private void refreshTitleFilters() {
        refreshFilterList(TitleFilter.class, titles);
    }

    private void refreshSelfTextFilters() {
        refreshFilterList(SelfTextFilter.class, selfTexts);
    }

    private void refreshSubredditFilters() {
        refreshFilterList(SubredditFilter.class, subreddits);
    }

    private void refreshUserFilters() {
        refreshFilterList(UserFilter.class, users);
    }

    private void refreshSubredditRestrctions() {
        if (profile.getSubredditRestrictions() == null || profile.getSubredditRestrictions().isEmpty()) {
            subRestrctions.setVisibility(View.GONE);
        } else {
            subRestrctions.setVisibility(View.VISIBLE);
            subRestrctions.setAdapter(new RemovableItemListAdapter(this, profile.getSubredditRestrictions(), RemovableItemListAdapter.SUBREDDIT_RESTRICTIONS));
            GeneralUtils.setListViewHeightBasedOnChildren(subRestrctions);
        }
    }

    private void refreshMultiredditRestrctions() {
        if (profile.getMultiredditRestrictions() == null || profile.getMultiredditRestrictions().isEmpty()) {
            multiRestrctions.setVisibility(View.GONE);
        } else {
            multiRestrctions.setVisibility(View.VISIBLE);
            multiRestrctions.setAdapter(new RemovableItemListAdapter(this, profile.getMultiredditRestrictions(), RemovableItemListAdapter.MULTIREDDIT_RESTRCTIONS));
            GeneralUtils.setListViewHeightBasedOnChildren(multiRestrctions);
        }
    }

    private void addFilter(Class<? extends Filter> cls, EditText field, String hint, String warning) {
        String filterText = field.getText().toString();
        if (filterText.trim().isEmpty()) {
            field.setText("");
            field.setHint(warning);
            field.setHintTextColor(Color.RED);
        } else if (profile.containsFilter(cls, filterText)) {
            GeneralUtils.clearField(field, hint);
            ToastUtils.showSnackbarOverToast(this, "Filter already in list");
        } else {
            GeneralUtils.clearField(field, hint);
            Filter filter = Filter.newInstance(cls, filterText);
            boolean added = profile.addFilter(filter);
            if (added) {
                refreshFilters(cls);
            } else {
                String message;
                if (!filter.isValid()) {
                    message = filter.getTextRequirements();
                } else {
                    message = "Failed to add filter";
                }
                ToastUtils.showSnackbarOverToast(this, message);
            }
        }
    }

    private void addDomainFilter() {
        addFilter(DomainFilter.class, domainField, "domain", "enter domain");
    }

    private void addFlairFilter() {
        addFilter(FlairFilter.class, flairField, "flair", "enter flair");
    }

    private void addTitleFilter() {
        addFilter(TitleFilter.class, titleField, "title keyword / phrase", "enter a keyword or phrase");
    }

    private void addSelfTextFilter() {
        addFilter(SelfTextFilter.class, selfTextField, "self-text keyword / phrase", "enter a keyword or phrase");
    }

    private void addSubredditFilter() {
        addFilter(SubredditFilter.class, subredditField, "subreddit", "enter subreddit");
    }

    private void addUserFilter() {
        addFilter(UserFilter.class, userField, "user", "enter user");
    }

    private void addSubredditRestrction() {
        String restriction = subRestrField.getText().toString();
        restriction = restriction.replaceAll("\\s","");
        if (restriction.isEmpty()) {
            GeneralUtils.clearField(subRestrField, "enter subreddit", Color.RED);
        } else if (profile.containsSubredditRestriction(restriction)) {
            GeneralUtils.clearField(subRestrField, "subreddit");
            ToastUtils.showSnackbarOverToast(this, "Subreddit already in list");
        } else if (!GeneralUtils.isValidSubreddit(restriction)) {
            GeneralUtils.clearField(subRestrField, "subreddit");
            ToastUtils.showSnackbarOverToast(this, "Subreddit can contain only alphanumeric characters (a-z,0-9) and underscores (_)");
        } else {
            GeneralUtils.clearField(subRestrField, "subreddit");
            profile.addSubredditRestriction(restriction);
            refreshSubredditRestrctions();
        }
    }

    private void addMultiredditRestrction() {
        String restriction = multiRestrField.getText().toString();
        restriction = restriction.replaceAll("\\s","");
        if (restriction.isEmpty()) {
            GeneralUtils.clearField(multiRestrField, "enter multireddit", Color.RED);
        } else if (profile.containsMultiredditRestrction(restriction)) {
            GeneralUtils.clearField(multiRestrField, "multireddit");
            ToastUtils.showSnackbarOverToast(this, "Multireddit already in list");
        } else if (!GeneralUtils.isValidSubreddit(restriction)) {
            GeneralUtils.clearField(multiRestrField, "multireddit");
            ToastUtils.showSnackbarOverToast(this, "Multireddit can contain only alphanumeric characters (a-z,0-9) and underscores (_)");
        } else {
            GeneralUtils.clearField(multiRestrField, "multireddit");
            profile.addMultiredditRestriction(restriction);
            refreshMultiredditRestrctions();
        }
    }

    public void removeFilter(int index) {
        Filter filter = profile.getFilters().get(index);
        profile.removeFilter(filter);
        refreshFilters(filter.getClass());
    }

    public void removeSubRestriction(int index) {
        profile.removeSubredditRestrction(index);
        refreshSubredditRestrctions();
    }

    public void removeMultiRestriction(int index) {
        profile.removeMultiredditRestrction(index);
        refreshMultiredditRestrctions();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_add_domain_filter:
                addDomainFilter();
                break;
            case R.id.button_add_title_filter:
                addTitleFilter();
                break;
            case R.id.button_add_flair_filter:
                addFlairFilter();
                break;
            case R.id.button_add_self_text_filter:
                addSelfTextFilter();
                break;
            case R.id.button_add_subreddit_filter:
                addSubredditFilter();
                break;
            case R.id.button_add_user_filter:
                addUserFilter();
                break;
            case R.id.button_add_subreddit_restrction:
                addSubredditRestrction();
                break;
            case R.id.button_add_multireddit_restrction:
                addMultiredditRestrction();
                break;
            case R.id.button_save_changes:
                saveProfile();
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (v.getId()) {
            case R.id.editText_domain_filter:
                addDomainFilter();
                return true;
            case R.id.editText_title_filter:
                addTitleFilter();
                return true;
            case R.id.editText_flair_filter:
                addFlairFilter();
                return true;
            case R.id.editText_self_text_filter:
                addSelfTextFilter();
                return true;
            case R.id.editText_subreddit_filter:
                addSubredditFilter();
                return true;
            case R.id.editText_user_filter:
                addUserFilter();
                return true;
            case R.id.editText_subreddit_restrction:
                addSubredditRestrction();
                return true;
            case R.id.editText_multireddit_restrction:
                addMultiredditRestrction();
                return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save_profile) {
            saveProfile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProfile() {
        String name = nameField.getText().toString();
        if (name.trim().isEmpty()) {
            if (isNewProfile) {
                profile.setName(getIntent().getStringExtra("defaultName"));
            }
        } else {
            profile.setName(name);
        }

        if (isNewProfile && profile.hasFilters()) {
            profile.setActive(true);
        }

        profile.save(this, isNewProfile);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (changesMade()) {
            showSaveChangesDialog();
        } else {
            super.onBackPressed();
        }
    }

    private boolean changesMade() {
        try {
            String nameFieldString = nameField.getText().toString();
            if (isNewProfile) {
                if (!nameFieldString.trim().isEmpty()) return true;
                if (!profile.getFilters().isEmpty()) return true;
                if (!profile.getSubredditRestrictions().isEmpty()) return true;
                if (!profile.getMultiredditRestrictions().isEmpty()) return true;
            } else {
                if (!nameFieldString.equals(profile.getName())) return true;
                if (!originalFilters.equals(profile.getFilters())) return true;
                if (!originalSubRestr.equals(profile.getSubredditRestrictions())) return true;
                if (!originalMultiRestr.equals(profile.getMultiredditRestrictions())) return true;
            }
        } catch (Exception e) { // in case of null profile lists
            e.printStackTrace();
            return true;
        }
        return false;
    }

    private void showSaveChangesDialog() {
        String message = isNewProfile ? "Save profile?" : "Save changes?";
        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MyAlertDialogStyle))
                .setMessage(message)
                .setPositiveButton("Yes", this)
                .setNegativeButton("No", this)
                .show();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                saveProfile();
                super.onBackPressed();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                super.onBackPressed();
                break;
        }
    }

}

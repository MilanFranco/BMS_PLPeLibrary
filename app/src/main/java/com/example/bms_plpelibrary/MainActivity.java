package com.example.bms_plpelibrary;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.plp.elibrary.fragments.AdminFragment;
import com.plp.elibrary.fragments.BookmarksFragment;
import com.plp.elibrary.fragments.ExploreFragment;
import com.plp.elibrary.fragments.HomeFragment;
import com.plp.elibrary.fragments.ProfileFragment;
import com.plp.elibrary.fragments.UploadFragment;
import com.plp.elibrary.fragments.VerifiedEbooksFragment;
import com.plp.elibrary.models.User;
import com.plp.elibrary.utils.Constants;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private User currentUser;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase instances
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize UI elements
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Setup Navigation Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Setup Bottom Navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.nav_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.nav_explore:
                    selectedFragment = new ExploreFragment();
                    break;
                case R.id.nav_upload:
                    selectedFragment = new UploadFragment();
                    break;
                case R.id.nav_bookmarks:
                    selectedFragment = new BookmarksFragment();
                    break;
                case R.id.nav_profile:
                    selectedFragment = new ProfileFragment();
                    break;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        // Load user data and check role
        loadUserData();

        // Default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            db.collection(Constants.USERS_COLLECTION)
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            currentUser = documentSnapshot.toObject(User.class);

                            // Check if user is admin
                            if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
                                isAdmin = true;
                                showAdminFeatures();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle error
                    });
        }
    }

    private void showAdminFeatures() {
        // Show admin menu items
        navigationView.getMenu().findItem(R.id.nav_admin).setVisible(true);

        // Add admin fragment to navigation drawer menu
        navigationView.getMenu().findItem(R.id.nav_admin).setOnMenuItemClickListener(item -> {
            // Load admin fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AdminFragment())
                    .commit();

            // Close drawer
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        switch (item.getItemId()) {
            case R.id.nav_verified_ebooks:
                selectedFragment = new VerifiedEbooksFragment();
                break;
            case R.id.nav_settings:
                // Handle settings
                break;
            case R.id.nav_help:
                // Handle help
                break;
            case R.id.nav_logout:
                // Handle logout
                firebaseAuth.signOut();
                finish();
                return true;
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
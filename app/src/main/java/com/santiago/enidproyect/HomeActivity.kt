package com.santiago.enidproyect

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.santiago.enidproyect.databinding.ActivityHomeBinding
import com.santiago.enidproyect.ui.chat.DashboardFragment
import com.santiago.enidproyect.ui.chat.User
import com.santiago.enidproyect.ui.chat.userAdapter
import com.santiago.enidproyect.ui.notifications.NotificationsFragment

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var search_btn: ImageView
    private lateinit var search_bar: EditText
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: userAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()
        userList = ArrayList()

        adapter = userAdapter(this, userList)


        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Set up the action bar
        val toolbar: Toolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(toolbar)
        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        if (mAuth.currentUser == null) {
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
            finish()
        }
        search_btn = findViewById(R.id.search_btn)
        search_bar = findViewById(R.id.search_bar)
        search_bar.visibility = View.GONE
        search_btn.visibility = View.GONE

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.navigation_dashboard) {
                search_btn.visibility = View.VISIBLE
            } else {
                search_btn.visibility = View.GONE
            }
        }

        //Aqui tengo que añadir la logica para
        mDbRef.child("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (postsnapshot in snapshot.children) {
                    val currentUser = postsnapshot.getValue(User::class.java)
                    if (mAuth.currentUser?.uid != currentUser?.uid) {
                        userList.add(currentUser!!)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })


    }

}
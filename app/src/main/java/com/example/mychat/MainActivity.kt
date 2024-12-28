package com.example.mychat

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mychat.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var myRef: DatabaseReference // Firebase reference
    private val messageList = mutableListOf<String>() // List for storing messages

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase Realtime Database
        val database = FirebaseDatabase.getInstance()
        myRef = database.getReference("messages") // Reference to "messages" node in Firebase

        // Bind layout using ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set Toolbar as ActionBar
        setSupportActionBar(binding.toolbar)

        // Add avatar image in Toolbar
        setUpActionBar()

        // Set up RecyclerView
        setupRecyclerView()

        // Add ValueEventListener to fetch messages from Firebase
        addMessagesListener()

        // Handle send button click
        binding.btnSend.setOnClickListener {
            val message = binding.edMessage.text.toString()
            if (message.isNotBlank()) {
                // Push the message to Firebase
                myRef.push().setValue(message)
                binding.edMessage.text.clear() // Clear input after sending
                Log.d("MainActivity", "Message sent: $message")
            } else {
                Log.d("MainActivity", "Message is empty")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.sign_out){
            auth.signOut()
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpActionBar() {
        val ab = supportActionBar
        val photoUrl = auth.currentUser?.photoUrl

        if (photoUrl != null) {
            Thread {
                try {
                    val bMap = Picasso.get().load(photoUrl).get()
                    val dIcon = BitmapDrawable(resources, bMap)

                    runOnUiThread {
                        ab?.setDisplayHomeAsUpEnabled(true)
                        ab?.setHomeAsUpIndicator(dIcon)
                        ab?.title = auth.currentUser?.displayName
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("MainActivity", "Error loading profile image: ${e.message}")
                }
            }.start()
        } else {
            Log.w("MainActivity", "No profile image found.")
        }
    }

    private fun setupRecyclerView() {
        binding.rcView.layoutManager = LinearLayoutManager(this)
        binding.rcView.adapter = MessageAdapter(messageList) // Set up the adapter
    }

    private fun addMessagesListener() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Fetch all messages and update UI
                messageList.clear() // Clear the existing messages
                for (data in snapshot.children) {
                    val message = data.getValue(String::class.java)
                    message?.let {
                        messageList.add(it) // Add message to the list
                    }
                }
                binding.rcView.adapter?.notifyDataSetChanged() // Notify adapter to refresh the list
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MainActivity", "Failed to read value.", error.toException())
            }
        })
    }
}

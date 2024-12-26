package com.example.mychat

// Importerar nödvändiga Android- och Firebase-bibliotek
import android.app.Activity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mychat.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// Huvudaktivitet för appen
class MainActivity : AppCompatActivity() {
    // Deklarerar en binding-variabel för att koppla till layouten
    lateinit var binding: ActivityMainBinding

    // Körs när aktiviteten skapas
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialiserar binding med layoutinflation
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge() // Aktiverar stöd för kant-till-kant UI
        setContentView(binding.root) // Sätter layoutens rotvy

        // Hanterar systemfält (status- och navigeringsfält) för att justera layoutens marginaler
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets // Returnerar modifierade insets
        }

        // Initialiserar Firebase-databasen
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("message") // Referens till "message"-noden

        // Lyssnar på klickhändelser på skicka-knappen
        binding.btnSend.setOnClickListener {
            val message = binding.edMessage.text.toString()
            if (message.isNotBlank()) { // Kontroll för att förhindra tomma meddelanden
                myRef.setValue(message) // Skickar meddelandet till Firebase
                binding.edMessage.text.clear() // Rensar textfältet efter skickat meddelande
            }
        }

        // Anropar funktion för att lyssna på förändringar i databasen
        onChangeListener(myRef)
    }

    // Lyssnar på förändringar i databasen
    private fun onChangeListener(dRef: DatabaseReference) {
        dRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Uppdaterar UI när data i Firebase ändras
                binding.apply {
                    rcView.append("\n") // Lägger till en ny rad
                    rcView.append("Jonas: ${snapshot.value.toString()}") // Visar meddelandet
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Hanterar fel vid databasåtkomst
                // Här kan du lägga till loggning eller visa ett felmeddelande
                // Exempel: Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

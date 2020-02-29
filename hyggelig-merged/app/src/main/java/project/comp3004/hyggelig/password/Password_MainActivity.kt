package project.comp3004.hyggelig.password
import project.comp3004.hyggelig.R

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent

class Password_MainActivity : AppCompatActivity() {

    lateinit var pwGenButton: Button
    lateinit var pwManButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.password_main)


        pwGenButton = findViewById(R.id.passwordGenerator) as Button
        pwManButton = findViewById(R.id.passwordManager) as Button
        pwGenButton.setOnClickListener {
            val intent = Intent(this, PasswordGenerator::class.java)
            // start your next activity
            startActivity(intent)
        }

        pwManButton.setOnClickListener {
            val intent = Intent(this, PasswordManager::class.java)
            // start your next activity
            startActivity(intent)
        }


    }
}

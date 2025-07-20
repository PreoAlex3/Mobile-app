package com.example.petshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.petshop.data.database.DatabaseProvider
import com.example.petshop.ui.theme.PetShopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //deleteDatabase("petshop_database")
        DatabaseProvider.initializeDatabase(this)
        enableEdgeToEdge()
        setContent {
            PetShopTheme {
                PetShopApp()
            }
        }
    }
}





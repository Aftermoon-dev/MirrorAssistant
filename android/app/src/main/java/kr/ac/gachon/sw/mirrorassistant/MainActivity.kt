package kr.ac.gachon.sw.mirrorassistant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kr.ac.gachon.sw.mirrorassistant.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
    }
}
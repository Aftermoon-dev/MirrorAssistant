package kr.ac.gachon.sw.mirrorassistant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kr.ac.gachon.sw.mirrorassistant.databinding.ActivityConnectBinding

class ConnectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConnectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityConnectBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
    }
}
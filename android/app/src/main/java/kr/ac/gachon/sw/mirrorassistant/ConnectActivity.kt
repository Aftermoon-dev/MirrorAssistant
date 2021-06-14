package kr.ac.gachon.sw.mirrorassistant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kr.ac.gachon.sw.mirrorassistant.databinding.ActivityConnectBinding
import kr.ac.gachon.sw.mirrorassistant.network.APICall
import kr.ac.gachon.sw.mirrorassistant.network.BaseResponse
import kr.ac.gachon.sw.mirrorassistant.network.RetrofitClient
import kr.ac.gachon.sw.mirrorassistant.util.LoadingDialog
import kr.ac.gachon.sw.mirrorassistant.util.Preferences
import kr.ac.gachon.sw.mirrorassistant.util.Util
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.ConnectException

class ConnectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConnectBinding
    private lateinit var prefs: Preferences
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityConnectBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        prefs = Preferences(this)
        loadingDialog = LoadingDialog(this)

        if(!prefs.lastIP.isNullOrEmpty()) {
            binding.etAddress.setText(prefs.lastIP)
            checkMirrorAssistant(prefs.lastIP)
        }

        binding.btnConnect.setOnClickListener {
            if(binding.etAddress.text.trim().isNotBlank() && binding.etAddress.text.trim().isNotEmpty() && Util.checkUrl(binding.etAddress.text.toString().trim())) {
                checkMirrorAssistant(binding.etAddress.text.toString().trim())
            }
            else {
                Toast.makeText(this, getString(R.string.connect_notvalidurl), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkMirrorAssistant(url: String) {
        val retrofitClient = RetrofitClient.getNewRetrofitClient(url)
        val apiCall: APICall = retrofitClient!!.create(APICall::class.java)
        val callResponse: Call<BaseResponse> = apiCall.validCheck()

        loadingDialog.show()

        callResponse.enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                loadingDialog.dismiss()
                val body = response.body()
                if(body != null) {
                    Log.d("ConnectActivity", "${response.code()} - ${body.message}")
                    if(body.message == "OK") {
                        prefs.lastIP = binding.etAddress.text.toString().trim()
                        Toast.makeText(this@ConnectActivity, getString(R.string.connected), Toast.LENGTH_SHORT).show();
                        startActivity(Intent(this@ConnectActivity, MainActivity::class.java))
                        finish()
                    }
                }
                else {
                    Toast.makeText(this@ConnectActivity, getString(R.string.connect_notvalidserver), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                loadingDialog.dismiss()
                Log.e("ConnectActivity", "Error!", t)
                if(t is ConnectException) {
                    Toast.makeText(this@ConnectActivity, getString(R.string.connect_notvalidurl), Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this@ConnectActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                }

            }
        })
    }
}
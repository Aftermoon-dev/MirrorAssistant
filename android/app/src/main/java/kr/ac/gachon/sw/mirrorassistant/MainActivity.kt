package kr.ac.gachon.sw.mirrorassistant

import android.app.Activity
import android.content.Intent
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.utils.MDUtil.getStringArray
import com.github.dhaval2404.imagepicker.ImagePicker
import kr.ac.gachon.sw.mirrorassistant.databinding.ActivityMainBinding
import kr.ac.gachon.sw.mirrorassistant.network.*
import kr.ac.gachon.sw.mirrorassistant.util.LoadingDialog
import kr.ac.gachon.sw.mirrorassistant.util.Preferences
import kr.ac.gachon.sw.mirrorassistant.util.Util
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var prefs: Preferences
    private var retrofitClient: Retrofit? = null
    private var apiCall: APICall? = null
    private var faceList: List<FaceList>? = null
    private var userSpinnerAdapter: ArrayAdapter<String>? = null
    private var newsAdapter: ArrayAdapter<String>? = null
    private var emptyStringArray: Array<String>? = null
    private var layoutSpinnerAdapter: ArrayAdapter<String>? = null
    private val layoutSpinnerSelect: HashMap<Spinner, Int> = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        retrofitClient = RetrofitClient.getCurrentRetrofitClient()
        if(retrofitClient == null) {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
            finish()
        }

        loadingDialog = LoadingDialog(this)
        prefs = Preferences(this)
        apiCall = retrofitClient!!.create(APICall::class.java)

        verifyPermission()
        getUserList()

        binding.btnRemoveuser.setOnClickListener {
            if(!faceList.isNullOrEmpty()) {
                removeUser(binding.spinnerUser.selectedItemId.toInt())
            }
        }

        binding.btnNewface.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }

        setNewsSpinner()
        setLayoutSpinner()

        binding.btnNewssave.setOnClickListener {
            // 얼굴 목록이 비어있지 않으면
            if(!faceList.isNullOrEmpty()) {
                // 이미 해당 News가 설정 값이 아니라면
                if(faceList!![binding.spinnerUser.selectedItemPosition].newsid != binding.spinnerNewscategory.selectedItemPosition) {
                    Log.d("MainActivity", "ID ${faceList!![binding.spinnerUser.selectedItemPosition].id} - News ID ${binding.spinnerNewscategory.selectedItemPosition}")
                    setNews(faceList!![binding.spinnerUser.selectedItemPosition].id, binding.spinnerNewscategory.selectedItemPosition)
                }
                else {
                    Toast.makeText(this@MainActivity, R.string.menu_alreadyset, Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Toast.makeText(this@MainActivity, R.string.menu_nouser_error, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLayoutsave.setOnClickListener {
            if(!faceList.isNullOrEmpty()) {
                Log.d("MainActivity", "ID ${faceList!![binding.spinnerUser.selectedItemPosition].id} Layout")
                setLayout(faceList!![binding.spinnerUser.selectedItemPosition].id,
                    binding.spinnerClock.selectedItemPosition,
                    binding.spinnerNews.selectedItemPosition,
                    binding.spinnerWeather.selectedItemPosition,
                    binding.spinnerNoti.selectedItemPosition)
            }
            else {
                Toast.makeText(this@MainActivity, R.string.menu_nouser_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 퍼미션 체크
     */
    private fun verifyPermission() {
        setNotiSwitch()

        if(!Util.checkNotificationPermission(applicationContext)) {
            MaterialDialog(this).show {
                title(R.string.noti_permission_title)
                message(R.string.noti_permission_msg)
                positiveButton(android.R.string.ok) {
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                }
                negativeButton(android.R.string.cancel) {
                    prefs.enableNoti = false
                }
            }
        }
        else {
            if(prefs.enableNoti) Util.toggleNotificationListenerService(applicationContext)
        }
    }

    /**
     * 알림 전송 활성화 설정
     */
    private fun setNotiSwitch() {
        Log.d("MainActivity", "Switch ${prefs.enableNoti}")
        binding.switchNotienable.isChecked = prefs.enableNoti

        binding.switchNotienable.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            prefs.enableNoti = isChecked
            Log.d("MainActivity", "Switch ${prefs.enableNoti}")
            if(isChecked){
                Log.d("MainActivity", "NotiService Request Restart")
                Util.toggleNotificationListenerService(applicationContext)
            }
        })
    }

    /**
     * News 선택 Spinner
     */
    private fun setNewsSpinner() {
        newsAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, getStringArray(R.array.news))
        newsAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerNewscategory.adapter = newsAdapter
    }

    /**
     * 레이아웃 설정 Spinner
     */
    private fun setLayoutSpinner() {
        layoutSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, getStringArray(R.array.layout))
        layoutSpinnerAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerClock.adapter = layoutSpinnerAdapter
        binding.spinnerNews.adapter = layoutSpinnerAdapter
        binding.spinnerWeather.adapter = layoutSpinnerAdapter
        binding.spinnerNoti.adapter = layoutSpinnerAdapter

        val layoutSpinnerListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                checkLayoutSpinner(parent as Spinner, position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerClock.onItemSelectedListener = layoutSpinnerListener
        binding.spinnerNews.onItemSelectedListener = layoutSpinnerListener
        binding.spinnerWeather.onItemSelectedListener = layoutSpinnerListener
        binding.spinnerNoti.onItemSelectedListener = layoutSpinnerListener
    }

    /**
     * 데이터 기반 스피너 설정
     */
    private fun setUserSpinnerEvent() {
        binding.spinnerUser.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.spinnerClock.setSelection(faceList!![position].clock)
                binding.spinnerNews.setSelection(faceList!![position].news)
                binding.spinnerWeather.setSelection(faceList!![position].weather)
                binding.spinnerNoti.setSelection(faceList!![position].noti)

                layoutSpinnerSelect[binding.spinnerClock] = binding.spinnerClock.selectedItemPosition
                layoutSpinnerSelect[binding.spinnerNews] = binding.spinnerNews.selectedItemPosition
                layoutSpinnerSelect[binding.spinnerWeather] = binding.spinnerWeather.selectedItemPosition
                layoutSpinnerSelect[binding.spinnerNoti] = binding.spinnerNoti.selectedItemPosition

                binding.spinnerNewscategory.setSelection(faceList!![position].newsid)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    /**
     * 스피너 중복 체크
     */
    private fun checkLayoutSpinner(currentSpinner: Spinner, selectedId: Int) {
        if(layoutSpinnerSelect[currentSpinner] != null) {
            val spinnerList = arrayListOf(binding.spinnerClock, binding.spinnerNews, binding.spinnerWeather, binding.spinnerNoti)

            // 선택 값이 이전 값과 동일하지 않으면
            if (layoutSpinnerSelect[currentSpinner] != selectedId) {
                // 전체 스피너 탐색
                for (spinner in spinnerList) {
                    // 체크하려는 스피너가 동일하지 않으면
                    if (spinner != currentSpinner) {
                        // 해당 스피너와 현재 선택 값이 같다면
                        if (layoutSpinnerSelect[spinner] == selectedId) {
                            Log.d("MainActivity", "Detecting Same Layout Spinner")
                            //  체크하려는 스피너의 값을 현재 스피너의 이전 선택 값으로
                            spinner.setSelection(layoutSpinnerSelect[currentSpinner]!!)
                            layoutSpinnerSelect[spinner] = spinner.selectedItemPosition
                        }
                    }
                }
                layoutSpinnerSelect[currentSpinner] = selectedId
            }
        }
    }

    /**
     * 등록된 사용자 목록 가져오기
     */
    private fun getUserList() {
        val callResponse: Call<FaceListResponse> = apiCall!!.getFaceList()

        callResponse.enqueue(object : Callback<FaceListResponse> {
            override fun onResponse(call: Call<FaceListResponse>, response: Response<FaceListResponse>) {
                loadingDialog.dismiss()
                val body = response.body()
                if(body != null) {
                    if(body.message == "OK") {
                        Log.d("MainActivity", "$faceList")

                        if(body.faceList.isEmpty()) {
                            emptyStringArray = arrayOf(getString(R.string.menu_user_nodata))
                            userSpinnerAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, emptyStringArray!!)
                            userSpinnerAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            binding.spinnerUser.adapter = userSpinnerAdapter
                            return
                        }

                        val userList: ArrayList<String> = arrayListOf()
                        faceList = body.faceList

                        Log.d("MainActivity", "List $faceList")

                        for(user: FaceList in faceList!!) {
                            userList.add("${user.name} (${user.create})")
                        }

                        var lastItem = 0
                        if(userSpinnerAdapter != null)
                            lastItem = binding.spinnerUser.selectedItemPosition

                        userSpinnerAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, userList)
                        userSpinnerAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerUser.adapter = userSpinnerAdapter
                        binding.spinnerUser.setSelection(lastItem)
                        setUserSpinnerEvent()
                    }
                }
                else {
                    Toast.makeText(this@MainActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FaceListResponse>, t: Throwable) {
                loadingDialog.dismiss()
                Log.e("MainActivity", "Error!", t)
                Toast.makeText(this@MainActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    /**
     * 해당하는 ID의 사용자 삭제하기
     */
    private fun removeUser(id: Int) {
        loadingDialog.show()
        val callResponse: Call<BaseResponse> = apiCall!!.removeFace(id)

        callResponse.enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                loadingDialog.dismiss()
                val body = response.body()
                if(body != null) {
                    if(body.code == 200) {
                        Toast.makeText(this@MainActivity, getString(R.string.menu_user_deleted), Toast.LENGTH_SHORT).show()
                        getUserList()
                    }
                    else {
                        Log.i("MainActivity", "${body.code} ${body.message}")
                        Toast.makeText(this@MainActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    Log.i("MainActivity", "Body NULL")
                    Toast.makeText(this@MainActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                loadingDialog.dismiss()
                Log.e("MainActivity", "Error!", t)
                Toast.makeText(this@MainActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * 새 이미지 업로드
     */
    private fun addNewUser(name: String, fileUri: Uri) {
        val file = File(fileUri.path)
        val namePart = MultipartBody.Part.createFormData("name", name)
        val photoPart = MultipartBody.Part.createFormData("photoFile", "photo.jpg", RequestBody.create(MediaType.parse("image/jpeg"), file))

        val callResponse: Call<BaseResponse> = apiCall!!.addNewFace(photoPart, namePart)

        loadingDialog.show()
        callResponse.enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                loadingDialog.dismiss()
                val body = response.body()
                if(body != null) {
                    if(body.code == 200) {
                        Toast.makeText(this@MainActivity, getString(R.string.menu_user_added), Toast.LENGTH_SHORT).show()
                        getUserList()
                    }
                    else {
                        Log.d("MainActivity", "Code ${body.code} Msg ${body.message}")
                        Toast.makeText(this@MainActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    Log.d("MainActivity", "Body NULL")
                    Toast.makeText(this@MainActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                loadingDialog.dismiss()
                Log.e("MainActivity", "Error!", t)
                Toast.makeText(this@MainActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * 레이아웃 설정
     */
    private fun setLayout(id: Int, clock: Int, news: Int, weather: Int, noti: Int) {
        loadingDialog.show()
        val callResponse: Call<BaseResponse> = apiCall!!.setFaceLayout(id, clock, news, weather, noti)

        callResponse.enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                loadingDialog.dismiss()
                val body = response.body()
                if(body != null) {
                    if(body.code == 200) {
                        Toast.makeText(this@MainActivity, getString(R.string.menu_setcomplete), Toast.LENGTH_SHORT).show()
                        getUserList()
                    }
                    else {
                        Log.i("MainActivity", "${body.code} ${body.message}")
                        Toast.makeText(this@MainActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    Log.i("MainActivity", "Body NULL")
                    Toast.makeText(this@MainActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                loadingDialog.dismiss()
                Log.e("MainActivity", "Error!", t)
                Toast.makeText(this@MainActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * 뉴스 설정
     */
    private fun setNews(id: Int, newsId: Int) {
        val callResponse: Call<BaseResponse> = apiCall!!.setNews(id, newsId)

        loadingDialog.show()
        callResponse.enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                loadingDialog.dismiss()
                val body = response.body()
                if(body != null) {
                    if(body.code == 200) {
                        Toast.makeText(this@MainActivity, getString(R.string.menu_news_setcomplete), Toast.LENGTH_SHORT).show()
                        getUserList()
                    }
                    else {
                        Log.i("MainActivity", "${body.code} ${body.message}")
                        Toast.makeText(this@MainActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    Log.i("MainActivity", "Body NULL")
                    Toast.makeText(this@MainActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                loadingDialog.dismiss()
                Log.e("MainActivity", "Error!", t)
                Toast.makeText(this@MainActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == ImagePicker.REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                val uri: Uri = data?.data!!
                MaterialDialog(this).show {
                    title(R.string.menu_user_add_title)
                    message(R.string.menu_user_add_msg)
                    input()
                    positiveButton(android.R.string.ok) {
                        if(!it.getInputField().text.isNullOrEmpty())
                            addNewUser(it.getInputField().text.toString(), uri)
                        else
                            it.show()
                    }.show()
                }
            }
            else if(resultCode == ImagePicker.RESULT_ERROR) {
                Log.i("MainActivity", "ImagePicker Error\n{${ImagePicker.getError(data)}")
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            }
            else {
                Log.i("MainActivity", "ImagePicker Error - Result ${resultCode}")
            }
        }
    }
}
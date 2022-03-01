package com.example.app

import Prefs
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.pusher.pushnotifications.PushNotifications
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class MainActivity : AppCompatActivity() {
    //defining textviews.
    val bitcoinValue = findViewById<TextView>(R.id.bitcoinValue)
    val etherumValue = findViewById<TextView>(R.id.etherumValue)


    private var prefs: Prefs? = null

    //Create HTTP connection.
    val retrofit: ApiService by lazy {
        val httpClient = OkHttpClient.Builder()
        val builder = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:9000/") //emulator url
            .addConverterFactory(ScalarsConverterFactory.create())

        val retrofit = builder
            .client(httpClient.build())
            .build()
        retrofit.create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fetchCurrentPrice()
        setupPushNotifications()
        setupClickListeners()
    }
    //get current BTC and ETH prices.
    private fun fetchCurrentPrice() {
        retrofit.getValues().enqueue(object: Callback<String> {
            override fun onResponse(call: Call<String>?, response: Response<String>?) {
                val jsonObject = JSONObject(response!!.body())
                bitcoinValue.text = "1 BTC = $"+ jsonObject.getJSONObject("BTC").getString("USD")
                etherumValue.text = "1 ETH = $"+ jsonObject.getJSONObject("ETH").getString("USD")
            }

            override fun onFailure(call: Call<String>?, t: Throwable?) {
                Log.e("MainActivity",t!!.localizedMessage)
            }
        })
    }
    //Send push notifications using Pusher Beam via Firebase.
    private fun setupPushNotifications() {
        PushNotifications.start(applicationContext, "PUSHER_BEAMS_INSTANCE_ID")
        val fmt = "%s_%s_changed"
        PushNotifications.subscribe(java.lang.String.format(fmt, deviceUuid(), "BTC"))
        PushNotifications.subscribe(java.lang.String.format(fmt, deviceUuid(), "ETH"))
    }

    private fun setupClickListeners() {
        bitcoinValue.setOnClickListener {
            createDialog("BTC")
        }
        etherumValue.setOnClickListener {
            createDialog("ETH")
        }
    }
    //creating dialog to get BTC and ETH limits.
    //App sends the notifications when the currency changes
    //upon the limits.
    private fun createDialog(source:String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.alert_layout,null)

        builder.setTitle("Set limits")
            .setMessage("")
            .setView(view)

        val dialog = builder.create()
        val minEditText: EditText = view.findViewById(R.id.minimumValue)
        val maxEditText: EditText = view.findViewById(R.id.maximumValue)

        view.findViewById<Button>(R.id.save).setOnClickListener {
            if (source == "BTC"){
                saveBTCPref(minEditText.text.toString(), maxEditText.text.toString())
            } else {
                saveETHPref(minEditText.text.toString(), maxEditText.text.toString())
            }
            dialog.dismiss()
        }
        dialog.show()
    }
    //save the btc preferences
    private fun saveBTCPref(min:String, max:String){
        val jsonObject = JSONObject()
        jsonObject.put("minBTC", min)
        jsonObject.put("maxBTC", max)
        jsonObject.put("uuid", deviceUuid())

        val body = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObject.toString()
        )

        retrofit.saveBTCLimit(body).enqueue(object: Callback<String> {
            override fun onResponse(call: Call<String>?, response: Response<String>?) {}
            override fun onFailure(call: Call<String>?, t: Throwable?) {}
        })
    }
    //save the eth preferences
    private fun saveETHPref(min:String, max:String){
        val jsonObject = JSONObject()
        jsonObject.put("minETH",min)
        jsonObject.put("maxETH",max)
        jsonObject.put("uuid", deviceUuid())

        val body = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObject.toString()
        )

        retrofit.saveETHLimit(body).enqueue(object: Callback<String> {
            override fun onResponse(call: Call<String>?, response: Response<String>?) {}
            override fun onFailure(call: Call<String>?, t: Throwable?) {}
        })
    }
    //get the device uuid.
    private fun deviceUuid() : String? {
        prefs = Prefs(this)
        var uuid: String? = prefs!!.deviceUuid

        if (uuid == "") {
            uuid = java.util.UUID.randomUUID().toString().replace("-", "_")
            prefs!!.deviceUuid = uuid
        }

        return uuid
    }
}

package da.arpan.delivery.ui.auth

import android.app.Dialog
import core.arpan.delivery.models.User
import android.content.ClipData
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.shashank.sony.fancytoastlib.FancyToast
import core.arpan.delivery.utils.LiveDataUtil
import core.arpan.delivery.utils.createProgressDialog
import core.arpan.delivery.utils.generateSignatureOTP
import core.arpan.delivery.utils.showToast
import da.arpan.delivery.R
import da.arpan.delivery.ui.home.HomeActivity
import da.arpan.delivery.viewModels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val TAG = "PhoneAuthActivity"
    private var phoneNumberGlobal = ""
    private var daAgent = User()
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var progressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLanguageDefault()
        setContentView(R.layout.activity_main)
        initLogics()
        launchAnimation()
    }


    private fun launchAnimation() {
        val mTimer1 = Timer()
        val mTt1 = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    mainLinearView.animate().alpha(1f).duration = 500
                }
            }
        }
        mTimer1.schedule(mTt1, 1500)
        val mTimer2 = Timer()
        val mTt2 = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    icon_arpan_icon.animate().translationY(-100f).duration = 1500
                }
            }
        }
        mTimer2.schedule(mTt2, 500)
    }

    private fun setLanguageDefault() {
        val language = getSharedPreferences("APP_SETTINGS", MODE_PRIVATE)
            .getString("lang", "bn")
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale); } else {
            config.locale = locale; }
        baseContext.resources.updateConfiguration(
            config,
            baseContext.resources.displayMetrics
        )
    }

    private fun initLogics() {
        progressDialog = createProgressDialog()

        progressDialog.show()
        LiveDataUtil.observeOnce(viewModel.getRefreshResponse()) {
            runOnUiThread {
                progressDialog.dismiss()
            }
            viewModel.switchActivity(it, HomeActivity::class.java as Class<Any?>)
        }

        buttonSendOtpFinal.setOnClickListener {
            val phoneNumber = edt_phone_number.text.toString()
            sendCode(phoneNumber)
        }
    }

    private fun sendCode(phoneNumber: String) {
        if (phoneNumber.isNotEmpty()) {
            progressDialog.show()
            LiveDataUtil.observeOnce(
                viewModel.getSendOTPResponse(
                    edt_phone_number.text.toString(),
                    generateSignatureOTP()
                )
            ) {
                runOnUiThread {
                    progressDialog.dismiss()
                }
                if (it.error == true) {
                    Toast.makeText(this, "Try again...", Toast.LENGTH_SHORT).show()
                    buttonSendOtpFinal.isEnabled = true
                } else {
                    progressSendOtpFinal.visibility = View.VISIBLE
                    buttonSendOtpFinal.visibility = View.GONE
                    phoneNumberGlobal = phoneNumber
                    implementCodeSentLogic()
                }
            }
        }
    }

    private fun implementCodeSentLogic() {
        sendCodeLinear.visibility = View.GONE
        enterCodeLinear.visibility = View.VISIBLE
        otpDidntReciveText.visibility = View.VISIBLE
        buttonEnterOtpFinal.isEnabled = false
        otp_view.setText("")
        otp_view.setOtpCompletionListener {
            val codeSent = otp_view.text.toString()
            if (codeSent.isEmpty()) {
                this.showToast(
                    getString(R.string.enter_otp_failed_sent_to_your_device),
                    FancyToast.ERROR
                )
            } else {
                progressEnterOtpFinal.visibility = View.VISIBLE
                buttonEnterOtpFinal.visibility = View.GONE
                checkCodeAndLogin(codeSent)
            }
        }
        object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                buttonEnterOtpFinal.text = "( " + millisUntilFinished / 1000 + " )"
            }

            override fun onFinish() {
                buttonEnterOtpFinal.isEnabled = true
                buttonEnterOtpFinal.text = "Paste"
            }
        }.start()
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                otpDidntReciveText.text =
                    getString(R.string.try_again_after) + " " + millisUntilFinished / 1000 + " " + getString(
                        R.string.this_many_seconds
                    )
            }

            override fun onFinish() {
                otpDidntReciveText.visibility = View.GONE
                buttonResendOtpFinal.visibility = View.VISIBLE
                buttonResendOtpFinal.setOnClickListener {
                    sendCode(phoneNumberGlobal)
                }
            }
        }.start()
    }

    fun onSubmitCodeButtonClick(view: View) {
        val clipboard: ClipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        var pasteData = ""

        // If it does contain data, decide if you can handle the data.

        // If it does contain data, decide if you can handle the data.
        if (!clipboard.hasPrimaryClip()) {
        } else if (!clipboard.getPrimaryClipDescription()!!.hasMimeType(
                MIMETYPE_TEXT_PLAIN
            )
        ) {

            // since the clipboard has data but it is not plain text
        } else {

            //since the clipboard contains plain text.
            val item: ClipData.Item = clipboard.getPrimaryClip()!!.getItemAt(0)

            // Gets the clipboard as text.
            pasteData = item.text.toString()
            otp_view.setText(pasteData)
        }

    }

    private fun checkCodeAndLogin(codeSent: String) {
        if (codeSent.isEmpty()) {
            progressEnterOtpFinal.visibility = View.GONE
            buttonEnterOtpFinal.visibility = View.VISIBLE
            this.showToast(getString(R.string.try_again_to_send_code_error), FancyToast.ERROR)
        } else {
            progressDialog.show()
            LiveDataUtil.observeOnce(
                viewModel.getLoginResponse(
                    phoneNumberGlobal,
                    codeSent
                )
            ) {
                runOnUiThread {
                    progressDialog.dismiss()
                }
                val b = viewModel.switchActivity(it, HomeActivity::class.java as Class<Any?>)
                if(!b){
                    sendCodeLinear.visibility = View.VISIBLE
                    enterCodeLinear.visibility = View.GONE
                    progressSendOtpFinal.visibility = View.GONE
                    buttonSendOtpFinal.visibility = View.VISIBLE
                    otpDidntReciveText.visibility = View.VISIBLE
                    buttonResendOtpFinal.visibility = View.GONE
                }
            }
        }
    }

}
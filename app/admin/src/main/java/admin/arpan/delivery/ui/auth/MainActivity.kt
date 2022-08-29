package admin.arpan.delivery.ui.auth

import admin.arpan.delivery.R
import admin.arpan.delivery.ui.home.HomeActivityMain
import core.arpan.delivery.utils.LiveDataUtil
import core.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.viewModels.AuthViewModel
import android.app.Dialog
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private lateinit var progressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressDialog = createProgressDialog()

        progressDialog.show()
        LiveDataUtil.observeOnce(viewModel.getRefreshResponse()) {
            runOnUiThread {
                progressDialog.dismiss()
            }
            viewModel.switchActivity(it, HomeActivityMain::class.java as Class<Any?>)
        }

        buttonDone.setOnClickListener {
            if (editTextTextPassword.text.isNotEmpty() && editTextTextPersonName.text.isNotEmpty()) {
                progressDialog.show()
                LiveDataUtil.observeOnce(
                    viewModel.getLoginResponse(
                        editTextTextPersonName.text.toString(),
                        editTextTextPassword.text.toString()
                    )
                ) {
                    runOnUiThread {
                        progressDialog.dismiss()
                    }
                    viewModel.switchActivity(it, HomeActivityMain::class.java as Class<Any?>)
                }
            }
        }
    }

}


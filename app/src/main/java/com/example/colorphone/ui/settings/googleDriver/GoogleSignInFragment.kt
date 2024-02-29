package com.example.colorphone.ui.settings.googleDriver

import android.content.Intent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.colorphone.R
import com.example.colorphone.model.EmailUser
import com.example.colorphone.util.PrefUtil
import com.example.colorphone.util.ext.showDialogLoginSuccess
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Collections
import javax.inject.Inject

@AndroidEntryPoint
abstract class GoogleSignInFragment : Fragment() {

    @Inject
    lateinit var prefUtil: PrefUtil

    open fun onGoogleDriveSignedInSuccess(driveApi: Drive?) {}

    open fun onGoogleDriveSignedInFailed(exception: ApiException?) {}

    private fun onGoogleSignedInSuccess(signInAccount: GoogleSignInAccount?) {
        lifecycleScope.launch {
            handeInfoUser(signInAccount)
            delay(300)
            initializeDriveClient(signInAccount)
        }
    }

    private fun onGoogleSignedInFailed(exception: ApiException?) {
        onGoogleDriveSignedInFailed(exception)
    }

    var googleSignInClient: GoogleSignInClient? = null

    fun startGoogleDriveSignIn() {
        val signInIntent: Intent? = googleSignInClient?.signInIntent
        launcher.launch(signInIntent)
    }


    val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            onGoogleSignedInSuccess(account)
        } catch (e: ApiException) {
            onGoogleSignedInFailed(e)
        }
    }

    fun initializeDriveClient(signInAccount: GoogleSignInAccount?) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            Collections.singleton(DriveScopes.DRIVE_FILE)
        )
        credential?.selectedAccount = signInAccount?.account
        val builder = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
        val appName = getString(R.string.app_name)
        val driveApi = builder
                .setApplicationName(appName)
                .build()
        onGoogleDriveSignedInSuccess(driveApi)
    }

    private fun handeInfoUser(signInAccount: GoogleSignInAccount?) {
        val name = signInAccount?.displayName
        val email = signInAccount?.email
        val avatar = signInAccount?.photoUrl
        prefUtil.statusEmailUser = EmailUser(name, email, avatar.toString())
        context?.showDialogLoginSuccess()
    }

    fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    protected fun showMessage(@StringRes res: Int) {
        showMessage(getString(res))
    }
}

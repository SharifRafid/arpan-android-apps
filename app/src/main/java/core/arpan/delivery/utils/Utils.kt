package core.arpan.delivery.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64.*
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.type.DateTime
import com.shashank.sony.fancytoastlib.FancyToast
import core.arpan.delivery.R
import core.arpan.delivery.utils.Constants.Companion.numStrConvConst
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours


fun Context.showToast(message: String, status: Int){
    FancyToast.makeText(this, message, FancyToast.LENGTH_SHORT, status, false).show()
}

fun Context.createProgressDialog() : Dialog {
    val dialog = AlertDialog.Builder(this)
        .setView(LayoutInflater.from(this).inflate(R.layout.dialog_progress_layout_main, null))
        .create()
    dialog.setCancelable(false)
    dialog.setCanceledOnTouchOutside(false)
    dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
    return dialog
}

fun EditText.getNumValue() : Int {
    var value = 0
    if(text.isNullOrEmpty()){
        value = 0
    }else{
        try{
            value = text.toString().trim().toInt()
        }catch (e : Exception){
            value = 0
        }
    }
    return value
}

fun callPermissionCheck(context: Context, activity: Activity): Boolean {
    return if(ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.CALL_PHONE
            )
        ) {
            val mDialog = AlertDialog.Builder(activity)
                .setTitle(context.getString(R.string.give_call_permission))
                .setMessage(context.getString(R.string.call_permission_for_first_time))
                .setCancelable(false)
                .setPositiveButton(
                    context.getString(R.string.yes_ok)
                ) { diaInt, _ ->
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.CALL_PHONE),
                        0
                    )
                    diaInt.dismiss()
                }
                .setNegativeButton(
                    context.getString(R.string.no_its_ok)
                ) { dialogInterface, _ -> dialogInterface.dismiss() }
                .create()
            mDialog.show()
        } else {
            ActivityCompat.requestPermissions(activity,arrayOf(Manifest.permission.CALL_PHONE),
                0)
        }
        false
    } else {
        true
    }
}

fun getDate(milliSeconds: Long, dateFormat: String?): String? {
    // Create a DateFormatter object for displaying date in specified format.
    val formatter = SimpleDateFormat(dateFormat, Locale.ENGLISH)
    // Create a calendar object that will convert the date and time value in milliseconds to date.
    val calendar: Calendar = Calendar.getInstance()
    calendar.timeInMillis = milliSeconds
    return formatter.format(calendar.time)
}
fun parseDate(date: String, dateFormat: String?): Long {
    // Create a DateFormatter object for displaying date in specified format.
    val formatter = SimpleDateFormat(dateFormat, Locale.ENGLISH)
    return formatter.parse(date)!!.time
}
private var gson: Gson? = null
fun getGsonParser(): Gson? {
    if (null == gson) {
        val builder = GsonBuilder()
        gson = builder.create()
    }
    return gson
}

fun orderNumberToString(num : String) :String {
    var newString = ""
    for(char in num) {
        newString += numStrConvConst[char.toString().toInt()];
    }
    return newString
}

fun generateSignatureOTP() : String{
    var publicKey = "-----BEGIN PUBLIC KEY-----\nMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAz6mdnQcEo77evfR3BcTD\nN3cKDsbfuwqrdu9lLrr/CAxZn39yFNgK9RGuGNqh9/Mgr1RHexlARkprWEgV5Fm1\nV0pffsIH3+G85rfoCjhMXXR/U9MaFCm90FWQsXC/oITOtu4YyaCLpsQQrqjw6o6L\nciTO09lg7baW97QGIuRj6Di6Iomw9xLcSkKKYnH+AS12ZSZvL+sGbqdkP4O+EKE8\nbi8hRcweHQsm9zpoM078sgaHytIdC7MVYiYusx8aBAToH2OuRd3ebOttZ6zgx92W\nPzNkV7E1vVvYKHZ7vxkFVlta+mEhRaD2ad70XyhQqLn47M09z9wEbxysU62qD7LY\ncEYkSzvXQthVX+GZiX4EGEjV+5ioACTp7v0MCO8yznh3coBX+DVnEgiElRHZfOZ9\nnpcRRZ0D1o3r3a5pVscgUeeCLXiwwNTNxNaixCl8Zln2bCex8/XkO5/tgpbmopXo\nG1llook1jFiQF9nyRs6MA252YOIo/qIeTNVwmZrP+oNAgzPib9TrfFvRXrEQZy3m\n/EGGvrkDhFILJrzDccK8f/Hn4zhc6RKuA+GFMd+43IAoiOgOpie/k12F/ZuNbtlb\nNMi03cYQpuBRmseikUg85Y+LrChwevcpUVnD/uuxkcmpk5IPSEvVAzx4rM9Xf3H/\nN9UsX0QlZBicu7lZZAMer10CAwEAAQ==\n-----END PUBLIC KEY-----\n"
    publicKey = publicKey  .replace("\\r".toRegex(), "")
        .replace("\\n".toRegex(), "")
        .replace(System.lineSeparator().toRegex(), "")
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
    val part1: String = getRandString(10)
    val part2: String = getRandString(10)
    val part3: String = getRandString(10)
    val part4: String = getRandString(10)
    val date: Date = Date()
    val data = enccriptData("${part1}___${part2}___${part3}___${part4}___${date.time.days}___${date.time.hours}", publicKey).toString()
    Log.e("SIGNATURE", data)
    return data
}

fun getRandString(size: Int): String {
    val source = "A1BCDEF4G0H8IJKLM7NOPQ3RST9UVWX52YZab1cd60ef2ghij3klmn49opq5rst6uvw7xyz8"
    return (source).map { it }.shuffled().subList(0, size).joinToString("")
}

fun enccriptData(txt: String, pk: String): String? {
    var encoded = ""
    var encrypted: ByteArray? = null
    try {
        val publicBytes: ByteArray = decode(pk, DEFAULT_BUFFER_SIZE)
        val keySpec = X509EncodedKeySpec(publicBytes)
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        val pubKey: PublicKey = keyFactory.generatePublic(keySpec)
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, pubKey)
        encrypted = cipher.doFinal(txt.toByteArray())
        encoded = encodeToString(encrypted, DEFAULT_BUFFER_SIZE)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return encoded
}

fun String?.toNotNull(): String{
    return this?.toString() ?: ""
}
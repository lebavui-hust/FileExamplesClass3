package vn.edu.hust.fileexamples

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import vn.edu.hust.fileexamples.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    val MY_SETTINGS = "my_settings"
    val BACKGROUND_COLOR = "BACKGROUND_COLOR"

    var backgroundColor = Color.WHITE

    lateinit var db: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonRed.setOnClickListener {
            backgroundColor = Color.RED
            binding.root.setBackgroundColor(backgroundColor)
        }

        binding.buttonGreen.setOnClickListener {
            backgroundColor = Color.GREEN
            binding.root.setBackgroundColor(backgroundColor)
        }

        binding.buttonBlue.setOnClickListener {
            backgroundColor = Color.BLUE
            binding.root.setBackgroundColor(backgroundColor)
        }

        binding.buttonReset.setOnClickListener {
            backgroundColor = Color.WHITE
            binding.root.setBackgroundColor(backgroundColor)
        }

        val prefs = getSharedPreferences(MY_SETTINGS, MODE_PRIVATE)
        backgroundColor = prefs.getInt(BACKGROUND_COLOR, Color.WHITE)
        binding.root.setBackgroundColor(backgroundColor)

        binding.buttonReadResource.setOnClickListener {
            val inputStream = resources.openRawResource(R.raw.test)
            val reader = inputStream.reader()
            val content = reader.readText()
            reader.close()

            binding.textResource.text = content
        }

        binding.buttonReadInternal.setOnClickListener {
            val inputStream = openFileInput("data.txt")
            val reader = inputStream.reader()
            val content = reader.readText()
            reader.close()

            binding.editText.setText(content)
        }

        binding.buttonWriteInternal.setOnClickListener {
            val outputStream = openFileOutput("data.txt", MODE_PRIVATE)
            val writer = outputStream.writer()
            writer.write(binding.editText.text.toString())
            writer.close()
        }

        binding.buttonClear.setOnClickListener {
            binding.editText.setText("")
        }

        binding.buttonDeleteInternal.setOnClickListener {
            val filePath = filesDir.path + "/data.txt"
            val file = File(filePath)
            if (file.exists()) {
                if (file.delete()) {
                    Toast.makeText(this, "File deleted successfully", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "File deleted failed", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "File not existed", Toast.LENGTH_LONG).show()
            }
        }

        binding.buttonReadExternal.setOnClickListener {
            val filePath = Environment.getExternalStorageDirectory().path + "/data.txt"
            val file = File(filePath)
            val inputStream = file.inputStream()
            val reader = inputStream.reader()
            val content = reader.readText()
            reader.close()

            binding.editText.setText(content)
        }

        binding.buttonWriteExternal.setOnClickListener {
            val filePath = Environment.getExternalStorageDirectory().path + "/data.txt"
            val file = File(filePath)
            val outputStream = file.outputStream()
            val writer = outputStream.writer()
            writer.write(binding.editText.text.toString())
            writer.close()
        }

        if (Build.VERSION.SDK_INT < 30) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                Log.v("TAG", "Permission Denied")
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1234)
            } else
                Log.v("TAG", "Permission Granted")
        } else {
            if (!Environment.isExternalStorageManager()) {
                Log.v("TAG", "Permission Denied")
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            } else {
                Log.v("TAG", "Permission Granted")
            }
        }

        db = SQLiteDatabase.openDatabase(filesDir.path + "/mydb",
            null, SQLiteDatabase.CREATE_IF_NECESSARY)

        // createTable()

        // query all records
        val cs = db.rawQuery("select * from tblAMIGO", null)
        Log.v("TAG", "Num of records: ${cs.count}")
        cs.moveToFirst()
        do {
            val recID = cs.getInt(0)
            val name = cs.getString(1)
            val phoneIndex = cs.getColumnIndex("phone")
            val phone = if (phoneIndex > -1) cs.getString(phoneIndex) else ""
            Log.v("TAG", "$recID - $name - $phone")
        } while (cs.moveToNext())
        cs.close()
    }

    fun createTable() {
        db.beginTransaction()
        try {
            db.execSQL("create table tblAMIGO(" +
                    "recID integer primary key autoincrement," +
                    "name text," +
                    "phone text)")
            db.execSQL("insert into tblAMIGO(name, phone) values ('AAA','555-1111')")
            db.execSQL("insert into tblAMIGO(name, phone) values ('BBB','555-2222')")
            db.execSQL("insert into tblAMIGO(name, phone) values ('CCC','555-3333')")

            db.setTransactionSuccessful()
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("TAG", "Permission Granted")
        } else {
            Log.v("TAG", "Permission Denied")
        }
    }

    override fun onStop() {
        val prefs = getSharedPreferences(MY_SETTINGS, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt(BACKGROUND_COLOR, backgroundColor)
        editor.apply()

        db.close()

        super.onStop()
    }
}
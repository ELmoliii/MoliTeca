package moliteca.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import moliteca.view.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    // Request codes
    private val REQUEST_CODE_OPEN_DIRECTORY = 1
    private val REQUEST_CODE_READ_PERMISSION = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Open directory picker when activity starts
        openDirectoryPicker()
    }

    private fun openDirectoryPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_OPEN_DIRECTORY -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        // Grant persistent access to the selected directory
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                        listFilesInDirectory(uri)
                    }
                }
            }
        }
    }

    private fun listFilesInDirectory(uri: Uri) {
        val documentUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            uri,
            DocumentsContract.getTreeDocumentId(uri)
        )

        val projection = arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_DOCUMENT_ID)
        contentResolver.query(documentUri, projection, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val fileNames = mutableListOf<String>()

            if (cursor.moveToFirst()) {
                do {
                    val fileName = cursor.getString(nameIndex)
                    fileNames.add(fileName)
                } while (cursor.moveToNext())

                binding.textprueba.text = fileNames.joinToString(",")
            } else {
                binding.textprueba.text = "No files found."
            }
        }
    }
}
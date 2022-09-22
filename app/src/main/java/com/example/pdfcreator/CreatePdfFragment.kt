package com.example.pdfcreator

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import java.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pdfcreator.databinding.FragmentCreatePdfBinding
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.FileOutputStream
import java.util.*


class CreatePdfFragment : Fragment(R.layout.fragment_create_pdf) {

    private lateinit var binding: FragmentCreatePdfBinding
    private val STORAGE_PERMISSION_CODE: Int = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreatePdfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnToPdf.setOnClickListener {
            if (checkPermission()) {
                Log.d(TAG, "onViewCreated: Permission already granted, create folder")
                savePdf()
            } else {
                Log.d(TAG, "onViewCreated: Permission was not granted, request")
                requestPermission()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (write && read) {
                    Log.d(TAG, "onRequestPermissionsResult: External Storage Permission granted")
                    savePdf()
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: External Storage Permission denied...")
                    toast("External Storage Permission denied...")
                }
            }
        }
    }



//===============================================================================================================================================================================================
//===============================================================================================================================================================================================
//===============================================================================================================================================================================================

    private fun savePdf() {
        //create object of Document class
        val mDoc = Document()
        //pdf file name
        val mFileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        //pdf file path
        val mFilePath = Environment.getExternalStorageDirectory().toString() + "/" + mFileName +".pdf"
        try {
            //create instance of PdfWriter class
            PdfWriter.getInstance(mDoc, FileOutputStream(mFilePath))

            //open the document for writing
            mDoc.open()

            //get text from EditText i.e. textEt
            val mText = binding.etPdfText.text.toString()

            //add author of the document (metadata)
            mDoc.addAuthor("Atif Pervaiz")

            //add paragraph to the document
            mDoc.add(Paragraph(mText))

            //close document
            mDoc.close()

            //show file saved message with file name and path
            toast("$mFileName.pdf\nis saved to\n$mFilePath")
        }
        catch (e: Exception){
            //if anything goes wrong causing exception, get and show exception message
            toast(e.message.toString())
        }

    }

    private val storageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        Log.d(TAG, "storageActivityResultLauncher: ")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Log.d(TAG, "storageActivityResultLauncher: ")
                savePdf()
            } else {
                Log.d(TAG, "storageActivityResultLauncher: Manage External Storage Permission is denied...")
                toast("Manage External Storage Permission is denied...")
            }
        } else {

        }
    }


    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Log.d(TAG, "requestPermission: try")
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this@CreatePdfFragment.requireActivity().packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                Log.d(TAG, "requestPermission: ", e)
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            ActivityCompat.requestPermissions(this@CreatePdfFragment.requireActivity(),
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE)
        }
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val write = ContextCompat.checkSelfPermission(this@CreatePdfFragment.requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this@CreatePdfFragment.requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }


    private fun toast(message: String) {
        Toast.makeText(this@CreatePdfFragment.requireActivity(), message, Toast.LENGTH_SHORT).show()
    }



}
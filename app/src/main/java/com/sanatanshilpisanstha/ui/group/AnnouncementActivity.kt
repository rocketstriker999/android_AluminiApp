package com.sanatanshilpisanstha.ui.group

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewpager.widget.ViewPager
import com.sanatanshilpisanstha.data.entity.group.announcement.Announcement
import com.sanatanshilpisanstha.data.entity.group.announcement.Attachment
import com.sanatanshilpisanstha.data.entity.group.announcement.Document
import com.sanatanshilpisanstha.data.enum.FailureActions
import com.sanatanshilpisanstha.databinding.ActivityAnnouncementBinding
import com.sanatanshilpisanstha.callbackListener.CameraPickerCallback
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.GroupRepository
import com.sanatanshilpisanstha.ui.adapter.ViewPagerAdapter
import com.sanatanshilpisanstha.utility.CameraPicker
import com.sanatanshilpisanstha.utility.Constant.REQUEST_CODE_AUDIO
import com.sanatanshilpisanstha.utility.Constant.REQUEST_CODE_DOCUMENT
import com.sanatanshilpisanstha.utility.Extra
import com.sanatanshilpisanstha.utility.FileUtils
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext


class AnnouncementActivity : AppCompatActivity(), View.OnClickListener,
    AnnouncementBottomDialogFragment.ItemClickListener {
    private lateinit var binding: ActivityAnnouncementBinding
    lateinit var viewPagerAdapter: ViewPagerAdapter
    lateinit var viewPager: ViewPager
    var cameraPicker: CameraPicker? = null
    val TAG = "AnnouncementActivity"
    var groupId = 0
    var groupName=""
    var groupBanner =""
    var imageList: ArrayList<Uri> = arrayListOf()
    private lateinit var groupRepository: GroupRepository
    private var announcement: Announcement = Announcement(
        Attachment(
            "", Document("", ""),
            arrayListOf()
        ), "", 0, ""
    )
    private val parentJob = Job()

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityAnnouncementBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        groupRepository = GroupRepository(this)

        // initializing variables
        // of below line with their id.
        viewPager = binding.viewPagerMain
        viewPagerAdapter = ViewPagerAdapter(this, arrayListOf())
        viewPager.adapter = viewPagerAdapter
        init()
    }

    private fun init() {

        addListener()
        val intent = intent
        groupId = intent.getIntExtra(Extra.GROUP_ID, 0)
        groupName = intent.getStringExtra(Extra.GROUP_NAME).toString()
        groupBanner = intent.getStringExtra(Extra.GROUP_BANNER).toString()
        announcement.group_id = groupId
    }

    private fun addListener() {
        binding.ivAddOption.setOnClickListener(this)
        binding.btnSend.setOnClickListener(this)
        binding.ivDocClose.setOnClickListener(this)
        binding.ivAucClose.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.ivLeft.setOnClickListener(this)
        binding.ivRight.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.ivAddOption -> {
                showBottomSheet()

            }
            binding.btnSend -> {
                if (checkValidation()) {
                    announcement.title = binding.editTextTextPersonName4.text.toString().trim()
                    announcement.description = binding.etDesc.text.toString().trim()
                    postAnnouncement()
                }

            }
            binding.ivDocClose -> {
                binding.clDoc.visibility = View.GONE
                announcement.attachment.document = null
            }
            binding.ivAucClose -> {
                binding.clDoc.visibility = View.GONE
                announcement.attachment.audio = null
            }
            binding.ivBack -> {
                finish()
            }
            binding.ivLeft -> {
                var tab = viewPager.currentItem
                if (tab > 0) {
                    tab--
                    viewPager.currentItem = tab
                } else if (tab == 0) {
                    viewPager.currentItem = tab
                }
            }
            binding.ivRight -> {
                var tab = viewPager.currentItem
                tab++
                viewPager.currentItem = tab
            }
        }
    }

    private fun showBottomSheet() {
        val bottomDialogFragment: AnnouncementBottomDialogFragment =
            AnnouncementBottomDialogFragment()
        bottomDialogFragment.show(
            supportFragmentManager,
            "tab"
        )
    }


    private fun checkValidation(): Boolean {
        return if (binding.editTextTextPersonName4.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Title is required.", Toast.LENGTH_LONG).show()
            false
        } else if (binding.etDesc.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Description is required.", Toast.LENGTH_LONG).show()
            false
        } /*else if (announcement.attachment.audio.isEmpty() && announcement.attachment.photos.isEmpty() && announcement.attachment.document.ext.isEmpty()) {
            Toast.makeText(this, "Attachment is required.", Toast.LENGTH_LONG).show()
            false
        }*/ else {
            true
        }
    }

    override fun onItemClick(item: String?) {
        when (item) {

            "ivAudio" -> {
                val intentAudio = Intent()
                intentAudio.type = "audio/*"
                intentAudio.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intentAudio, REQUEST_CODE_AUDIO)
            }
            "ivPhotos" -> {
                getGallery()
            }
            "ivCam" -> {
                photoCameraSelection()
            }
            "ivDocument" -> {
                val intent = Intent()
                val mimeTypes: Array<String> = FileUtils.getMimeTypes()!!
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.type = java.lang.String.join("|", *mimeTypes)
                } else {
                    intent.type = TextUtils.join("|", mimeTypes)
                }
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.action = "android.intent.action.GET_CONTENT"
                intent.addCategory("android.intent.category.OPENABLE")
                intent.putExtra("android.intent.extra.MIME_TYPES", mimeTypes)
                startActivityForResult(
                    Intent.createChooser(intent, "Complete action using"),
                    REQUEST_CODE_DOCUMENT
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_AUDIO -> {
                if (resultCode == RESULT_OK) {
                    //the selected audio.
                    val uri: Uri? = data?.data
                    Log.e("files==", uri.toString())
                    val filePath: String =
                        FileUtils.getFilePathForURI(this, uri)!!
                    binding.claud.visibility = View.VISIBLE
                    binding.tvAudName.text = uri?.path
                    announcement.attachment.audio = Utilities.encodeAudio(filePath)

                }
            }
            REQUEST_CODE_DOCUMENT -> {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        var selectedFile: Uri? = null
                        if (data.clipData != null) {
                            for (i in 0 until data.clipData!!.itemCount) {
                                selectedFile = data.clipData!!.getItemAt(i).uri
                                val filePath: String =
                                    FileUtils.getFilePathForURI(this, selectedFile)!!
                                val name: String =
                                    FileUtils.getFileName(this, selectedFile)!!
                                val mimeType: String =
                                    FileUtils.getMimeTypes(this, selectedFile)!!
                                announcement.attachment.document = Document(".$mimeType", Utilities.encodeAudio(filePath))
                                binding.clDoc.visibility = View.VISIBLE
                                binding.tvDocName.text = name
                                Log.e("files==1", name + "," + mimeType + "," + filePath)

                            }
                        } else {
                            selectedFile = data.data
                            val filePath: String =
                                FileUtils.getFilePathForURI(this, selectedFile)!!
                            val name: String = FileUtils.getFileName(this, selectedFile!!)!!
                            val mimeType: String =
                                FileUtils.getMimeTypes(this, selectedFile)!!
                            announcement.attachment.document = Document(".$mimeType", name)
                            binding.clDoc.visibility = View.VISIBLE
                            binding.tvDocName.text = name
                            Log.e("files==2", name + "," + mimeType + "," + filePath)
                        }
                    }
                }
            }
            else -> {
                cameraPicker?.onActivityResult(requestCode, resultCode, data)
            }


        }
    }

    private fun photoCameraSelection() {
        cameraPicker = CameraPicker(this).setResultCallback(object : CameraPickerCallback {
            override fun onCameraPickSuccess(file: File) {
                // TODO:Step-24 get click file
                var cameraSelectedFile = file
                cameraPicker = null
                Log.e("file==", cameraSelectedFile.toString())
                Log.e("file==>", file.toString())
//                ivShowCase.setImageURI(Uri.fromFile(file))
                binding.viewPagerMain.visibility = View.VISIBLE
                binding.ivLeft.visibility = View.VISIBLE
                binding.ivRight.visibility = View.VISIBLE
                announcement.attachment.photos.add("" + Utilities.getFileToByte(file.path))
                imageList.add(Uri.fromFile(file))
                viewPagerAdapter.updateList(imageList)
            }

            // TODO:Step-25 get Error
            override fun onCameraPickFail(enum: Enum<FailureActions>?) {
                Toast.makeText(applicationContext, enum!!.name, Toast.LENGTH_SHORT).show()
            }

        }).cameraIntent()
    }


    fun getGallery() {
        cameraPicker = CameraPicker(this).setResultCallback(object : CameraPickerCallback {
            override fun onCameraPickSuccess(file: File) {
                // TODO:Step-29 get click file

                cameraPicker = null
                Log.e("file==>", file.toString())
                file.path
                val image64 = Utilities.getFileToByte(file.path)
                Log.i(TAG, "updatePhotoIdView: $image64")
                binding.viewPagerMain.visibility = View.VISIBLE
                binding.ivLeft.visibility = View.VISIBLE
                binding.ivRight.visibility = View.VISIBLE
                announcement.attachment.photos.add("" + Utilities.getFileToByte(file.path))
                imageList.add(Uri.fromFile(file))
                viewPagerAdapter.updateList(imageList)
//                ivShowCase.setImageURI(Uri.fromFile(file))
            }

            // TODO:Step-30 get Error
            override fun onCameraPickFail(enum: Enum<FailureActions>?) {
//                Toast.makeText(applicationContext, enum!!.name, Toast.LENGTH_SHORT).show()
            }

        }).galleryIntent()
    }


    private fun postAnnouncement() {
        scope.launch {
            groupRepository.postAnnouncement(announcement) {
                when (it) {
                    is APIResult.Success -> {
                        Log.d("Success", it.data)
                        binding.btnSend.text = "Send";
                        binding.progressBar.visibility = View.GONE
                        val i = Intent(this@AnnouncementActivity, ChatActivity::class.java)
                        i.putExtra(Extra.GROUP_ID, groupId)
                        i.putExtra(Extra.GROUP_NAME, groupName)
                        i.putExtra(Extra.GROUP_BANNER, groupBanner)
                        startActivity(i)
                        finishAffinity()
                    }

                    is APIResult.Failure -> {
                        Log.d("Failure", it.message.toString())
                        binding.btnSend.text = "Send";
                        binding.progressBar.visibility = View.GONE
                    }

                    APIResult.InProgress -> {
                        binding.btnSend.text = "";
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    else -> {

                    }
                }
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraPicker?.onRequestPermissionsResult(requestCode,permissions,grantResults)
    }
}
package com.sanatanshilpisanstha.ui.adapter

import android.R.attr.bitmap
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.group.ChatDateItem
import com.sanatanshilpisanstha.data.entity.group.ChatListItem
import com.sanatanshilpisanstha.data.entity.group.message.MMessage
import com.sanatanshilpisanstha.data.enum.MessageCode
import com.sanatanshilpisanstha.databinding.UnitChatDateBinding
import com.sanatanshilpisanstha.databinding.UnitChatMsgBinding
import com.sanatanshilpisanstha.ui.group.LikesCommentActivity
import com.sanatanshilpisanstha.utility.Constant
import com.sanatanshilpisanstha.utility.Constant.MessageId
import com.sanatanshilpisanstha.utility.Utilities


class DirectoryChatAdapter(
    val context: Context,
    var messageList: ArrayList<ChatListItem>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    /*TODO Step 4 : add recyclerview adapter */
    private var docSelectionListener: AppointedDocSelectionListener? = null
    var bitmap: Bitmap? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ChatListItem.TYPE_DATE) {
            val binding =
                UnitChatDateBinding.inflate(LayoutInflater.from(context), parent, false)
            DateVh(binding)
        } else {
            val binding = UnitChatMsgBinding.inflate(LayoutInflater.from(context), parent, false)
            MessagVh(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ChatListItem.TYPE_GENERAL -> {
                if (holder is MessagVh) {
                    holder.setData(message = messageList[position] as MMessage,position)
                }
            }
            ChatListItem.TYPE_DATE -> {
                if (holder is DateVh) {
                    holder.setData(date = messageList[position] as ChatDateItem,position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }


    fun updateList(messageList: ArrayList<ChatListItem>) {
        this.messageList = messageList
        notifyDataSetChanged()
    }

    inner class MessagVh(val binding: UnitChatMsgBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(message: MMessage, position: Int) {

            if(message.mLikes!=0){
                binding.likeCount.text = message.mLikes.toString()
            }
            if(message.mComments!=0){
                binding.CommentCount.text = message.mComments.toString()
            }

            binding.cvMVideoLocation.setOnClickListener{
                if (docSelectionListener != null) {
                    docSelectionListener!!.VideoPlayListner(message, position)
                }

            }
            binding.MAudioFileLayout.setOnClickListener{
                if (docSelectionListener != null) {
                    docSelectionListener!!.AudioPlayListner(message, position)
                }

            }
            binding.likeLinear.setOnClickListener {
                if (docSelectionListener != null) {
                    docSelectionListener!!.LikeListener(message, position)
                }

            }
            binding.commentlinear.setOnClickListener {
                if (docSelectionListener != null) {
                    docSelectionListener!!.CommentListener(message, position)
                }
            }


            binding.likeLinear.setOnLongClickListener{
                val i = Intent(context, LikesCommentActivity::class.java)
                i.putExtra(MessageId,message.messageID.toString())
                context.startActivity(i)
                return@setOnLongClickListener true
            }

            binding.commentlinear.setOnLongClickListener{
                val i = Intent(context, LikesCommentActivity::class.java)
                i.putExtra(MessageId,message.messageID.toString())
                context.startActivity(i)
                return@setOnLongClickListener true
            }


            binding.cvMText2.setOnClickListener{
                Log.e("mBy====>",message.mBy.toString())
                if(message.mBy.toString().equals("false")) {
                    if (docSelectionListener != null) {
                        docSelectionListener!!.DeleteMsgListner(message, position)
                    }
                }
            }


            setAlignment(binding, message)

            when (message.mtype) {
                MessageCode.ANNOUNCEMENT.type -> {
                    setAnnouncement(binding, message)
                    binding.cvAnnouncement.visibility = View.VISIBLE
                    binding.cvMJob.visibility = View.GONE
                    binding.cvMPhotoLocation.visibility = View.GONE
                    binding.cvMSurvey.visibility = View.GONE
                    binding.cvMQA.visibility = View.GONE
                    binding.cvMQuickPoll.visibility = View.GONE
                    binding.cvMText.visibility = View.GONE
                    binding.cvMText2.visibility = View.GONE
                    binding.cvMDocument.visibility = View.GONE
                    binding.cvMVideoLocation.visibility = View.GONE
                    binding.MAudioFileLayout.visibility = View.GONE
                    binding.cvMContactLocation.visibility = View.GONE
                }
                MessageCode.JOB.type -> {
                    setJob(binding, message)
                    binding.cvAnnouncement.visibility = View.GONE
                    binding.cvMJob.visibility = View.VISIBLE
                    binding.cvMPhotoLocation.visibility = View.GONE
                    binding.cvMSurvey.visibility = View.GONE
                    binding.cvMQA.visibility = View.GONE
                    binding.cvMQuickPoll.visibility = View.GONE
                    binding.cvMText.visibility = View.GONE
                    binding.cvMText2.visibility = View.GONE
                    binding.cvMDocument.visibility = View.GONE
                    binding.cvMVideoLocation.visibility = View.GONE
                    binding.MAudioFileLayout.visibility = View.GONE
                    binding.cvMContactLocation.visibility = View.GONE

                }
                MessageCode.PHOTO_LOCATION.type -> {
                    setPhotoLocation(binding, message)
                    binding.cvAnnouncement.visibility = View.GONE
                    binding.cvMJob.visibility = View.GONE
                    binding.cvMPhotoLocation.visibility = View.VISIBLE
                    binding.cvMSurvey.visibility = View.GONE
                    binding.cvMQA.visibility = View.GONE
                    binding.cvMQuickPoll.visibility = View.GONE
                    binding.cvMText.visibility = View.GONE
                    binding.cvMText2.visibility = View.GONE
                    binding.cvMDocument.visibility = View.GONE
                    binding.cvMVideoLocation.visibility = View.GONE
                    binding.MAudioFileLayout.visibility = View.GONE
                    binding.cvMContactLocation.visibility = View.GONE
                }
                MessageCode.SURVEY.type -> {
                    setSurvey(binding, message)
                    binding.cvAnnouncement.visibility = View.GONE
                    binding.cvMJob.visibility = View.GONE
                    binding.cvMPhotoLocation.visibility = View.GONE
                    binding.cvMSurvey.visibility = View.VISIBLE
                    binding.cvMQA.visibility = View.GONE
                    binding.cvMQuickPoll.visibility = View.GONE
                    binding.cvMText.visibility = View.GONE
                    binding.cvMText2.visibility = View.GONE
                    binding.cvMDocument.visibility = View.GONE
                    binding.cvMVideoLocation.visibility = View.GONE
                    binding.MAudioFileLayout.visibility = View.GONE
                    binding.cvMContactLocation.visibility = View.GONE

                }
                MessageCode.QUICK_POLL.type -> {
                    setQuickPoll(binding, message)
                    binding.cvAnnouncement.visibility = View.GONE
                    binding.cvMJob.visibility = View.GONE
                    binding.cvMPhotoLocation.visibility = View.GONE
                    binding.cvMSurvey.visibility = View.GONE
                    binding.cvMQA.visibility = View.GONE
                    binding.cvMQuickPoll.visibility = View.VISIBLE
                    binding.cvMText.visibility = View.GONE
                    binding.cvMText2.visibility = View.GONE
                    binding.cvMDocument.visibility = View.GONE
                    binding.cvMVideoLocation.visibility = View.GONE
                    binding.MAudioFileLayout.visibility = View.GONE
                    binding.cvMContactLocation.visibility = View.GONE
                }
                MessageCode.QA.type -> {
                    setQA(binding, message)
                    binding.cvAnnouncement.visibility = View.GONE
                    binding.cvMJob.visibility = View.GONE
                    binding.cvMPhotoLocation.visibility = View.GONE
                    binding.cvMSurvey.visibility = View.GONE
                    binding.cvMQA.visibility = View.VISIBLE
                    binding.cvMQuickPoll.visibility = View.GONE
                    binding.cvMText.visibility = View.GONE
                    binding.cvMText2.visibility = View.GONE
                    binding.cvMDocument.visibility = View.GONE
                    binding.cvMVideoLocation.visibility = View.GONE
                    binding.MAudioFileLayout.visibility = View.GONE

                }
                MessageCode.MESSAGE.type -> {
                    setText(binding, message)
                    binding.cvAnnouncement.visibility = View.GONE
                    binding.cvMJob.visibility = View.GONE
                    binding.cvMPhotoLocation.visibility = View.GONE
                    binding.cvMSurvey.visibility = View.GONE
                    binding.cvMQA.visibility = View.GONE
                    binding.cvMQuickPoll.visibility = View.GONE
                    binding.cvMText.visibility = View.GONE
                    binding.cvMText2.visibility = View.VISIBLE
                    binding.cvMDocument.visibility = View.GONE
                    binding.cvMVideoLocation.visibility = View.GONE
                    binding.MAudioFileLayout.visibility = View.GONE
                    binding.cvMContactLocation.visibility = View.GONE
                }
                MessageCode.DOCUMENT.type -> {
                    setDocument(binding, message)
                    binding.cvAnnouncement.visibility = View.GONE
                    binding.cvMJob.visibility = View.GONE
                    binding.cvMPhotoLocation.visibility = View.GONE
                    binding.cvMSurvey.visibility = View.GONE
                    binding.cvMQA.visibility = View.GONE
                    binding.cvMQuickPoll.visibility = View.GONE
                    binding.cvMText.visibility = View.GONE
                    binding.cvMText2.visibility = View.GONE
                    binding.cvMDocument.visibility = View.VISIBLE
                    binding.cvMVideoLocation.visibility = View.GONE
                    binding.MAudioFileLayout.visibility = View.GONE
                    binding.cvMContactLocation.visibility = View.GONE
                }
                MessageCode.VIDEO.type -> {
                    setVideoLocation(binding, message)
                    binding.cvAnnouncement.visibility = View.GONE
                    binding.cvMJob.visibility = View.GONE
                    binding.cvMPhotoLocation.visibility = View.GONE
                    binding.cvMSurvey.visibility = View.GONE
                    binding.cvMQA.visibility = View.GONE
                    binding.cvMQuickPoll.visibility = View.GONE
                    binding.cvMText.visibility = View.GONE
                    binding.cvMText2.visibility = View.GONE
                    binding.cvMDocument.visibility = View.GONE
                    binding.cvMVideoLocation.visibility = View.VISIBLE
                    binding.MAudioFileLayout.visibility = View.GONE
                    binding.cvMContactLocation.visibility = View.GONE
                }
                MessageCode.AUDIO.type -> {
                    setAudioFile(binding, message)
                    binding.cvAnnouncement.visibility = View.GONE
                    binding.cvMJob.visibility = View.GONE
                    binding.cvMPhotoLocation.visibility = View.GONE
                    binding.cvMSurvey.visibility = View.GONE
                    binding.cvMQA.visibility = View.GONE
                    binding.cvMQuickPoll.visibility = View.GONE
                    binding.cvMText.visibility = View.GONE
                    binding.cvMText2.visibility = View.GONE
                    binding.cvMDocument.visibility = View.GONE
                    binding.cvMVideoLocation.visibility = View.GONE
                    binding.MAudioFileLayout.visibility = View.VISIBLE
                    binding.cvMContactLocation.visibility = View.GONE

                }
                MessageCode.CONTACT.type -> {
                    setContact(binding, message)
                    binding.cvAnnouncement.visibility = View.GONE
                    binding.cvMJob.visibility = View.GONE
                    binding.cvMPhotoLocation.visibility = View.GONE
                    binding.cvMSurvey.visibility = View.GONE
                    binding.cvMQA.visibility = View.GONE
                    binding.cvMQuickPoll.visibility = View.GONE
                    binding.cvMText.visibility = View.GONE
                    binding.cvMText2.visibility = View.GONE
                    binding.cvMDocument.visibility = View.GONE
                    binding.cvMVideoLocation.visibility = View.GONE
                    binding.MAudioFileLayout.visibility = View.GONE
                    binding.cvMContactLocation.visibility = View.VISIBLE
                }


                else -> {
                    binding.cvAnnouncement.visibility = View.GONE
                    binding.cvMJob.visibility = View.GONE
                    binding.cvMPhotoLocation.visibility = View.GONE
                    binding.cvMSurvey.visibility = View.GONE
                    binding.cvMQA.visibility = View.GONE
                    binding.cvMText.visibility = View.GONE
                    binding.cvMText2.visibility = View.GONE
                    binding.cvMDocument.visibility = View.GONE
                    binding.cvMContactLocation.visibility = View.GONE

                }
            }



        }

    }

    private fun setContact(binding: UnitChatMsgBinding, message: MMessage) {
        val message = message.mContactFile!!
        binding.tvMContactLocationBy.text = message.username
        binding.contactName.text = Utilities.decodeContact(message.contact.toString())

        binding.tvMContactTime.text =   Utilities.formatDate(
            "" + Constant.SERVER_DATE_FORMAT,
            "" + Constant.CHAT_TIME_FORMAT,
            message.createdAt!!
        ).toString()

    }

    private fun setAudioFile(binding: UnitChatMsgBinding, message: MMessage) {
        val message = message.mAudioFile!!
       binding.tvAudioBy.text = message.username
        binding.tvAudiFileTime.text =   Utilities.formatDate(
                "" + Constant.SERVER_DATE_FORMAT,
        "" + Constant.CHAT_TIME_FORMAT,
        message.createdAt!!
        ).toString()
    }

    private fun setJob(binding: UnitChatMsgBinding, mMessage: MMessage) {
        val message = mMessage.mJob!!
        binding.cvMJob.visibility = View.VISIBLE
        binding.cvAnnouncement.visibility = View.GONE
        binding.cvMPhotoLocation.visibility = View.GONE
        binding.tvMJobDesc.text = message.description
        binding.tvMJobBy.text = message.username
        binding.tvMJobType.text = "Job"
        binding.tvMJobAction.text = "View More"
        binding.tvMJobAssign.text = message.assignedTo.toString()
        binding.tvMJobTime.text = message.createdAt!!.split("\\s".toRegex())[1]
        if (!mMessage.mBy) {
            binding.tvMJobBy.visibility = View.GONE
        } else {
            binding.tvMJobBy.visibility = View.VISIBLE
        }
        if (!message.photo.isNullOrBlank()) {
            binding.ivMJobBanner.visibility = View.VISIBLE
            binding.ivMJobBanner.load(message.photo) {
                 crossfade(true)
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }
        } else {
            binding.ivMJobBanner.visibility = View.GONE
        }

    }

    private fun setQA(binding: UnitChatMsgBinding, mMessage: MMessage) {
        val message = mMessage.mQa!!

        binding.tvMQABy.text = message.username
        binding.tvMQATittle.text = message.title
        binding.tvMQAType.text = "Q&A"
        binding.tvMQAAction.text = "View More"
        binding.tvMQATime.text =  Utilities.formatDate(
            "" + Constant.SERVER_DATE_FORMAT,
            "" + Constant.CHAT_TIME_FORMAT,
            message.createdAt!!
        ).toString()
        binding.tvMQATime.text =  Utilities.formatDate(
            "" + Constant.SERVER_DATE_FORMAT,
            "" + Constant.CHAT_TIME_FORMAT,
            message.createdAt!!
        ).toString()
        if (!mMessage.mBy) {
            binding.tvMQABy.visibility = View.GONE
        } else {
            binding.tvMQABy.visibility = View.VISIBLE
        }
        if (!message.photo.isNullOrBlank()) {
            binding.ivMQABanner.visibility = View.VISIBLE
            binding.ivMQABanner.load(message.photo) {
                 crossfade(true)
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }
        } else {
            binding.ivMQABanner.visibility = View.GONE
        }

    }

    private fun setText(binding: UnitChatMsgBinding, mMessage: MMessage) {
        val message = mMessage.mText!!

        binding.tvMTextBy2.text = message.username
        binding.tvMTextTittle2.text = message.message

        binding.tvMTextTime2.text =  Utilities.formatDate(
            "" + Constant.SERVER_DATE_FORMAT,
            "" + Constant.CHAT_TIME_FORMAT,
            message.createdAt!!
        ).toString()


    }

    private fun setDocument(binding: UnitChatMsgBinding, mMessage: MMessage) {
        val message = mMessage.mDocument!!

        binding.tvMDocumentBy.text = message.username

        binding.tvMDocumentTime.text =  Utilities.formatDate(
            "" + Constant.SERVER_DATE_FORMAT,
            "" + Constant.CHAT_TIME_FORMAT,
            message.createdAt!!
        ).toString()
        if (!mMessage.mBy) {
            binding.tvMDocumentBy.visibility = View.GONE
        } else {
            binding.tvMDocumentBy.visibility = View.VISIBLE
        }
        if (!message.file.isNullOrBlank()) {
            binding.ivMDocument.visibility = View.VISIBLE
            binding.ivMDocument.load(message.file) {
                 crossfade(true)
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }
        } else {
            binding.ivMJobBanner.visibility = View.GONE
        }

    }

    private fun setSurvey(binding: UnitChatMsgBinding, mMessage: MMessage) {
        val message = mMessage.mSurvey!!

        binding.tvMSurveyTittle.text = message.title
        binding.tvMSurveyDesc.text = message.description
        binding.tvMSurveyBy.text = message.username
        binding.tvMSurveyQuestion.text = "Questions"
        binding.tvMSurveyAction.text = "Take  Survey"
        binding.tvMSurveyQCountNumber.text = message.questionCount
        binding.tvMSurveyResponseCount.text = message.respond?.total ?: ""
        binding.tvMSurveyType.text = "Survey"

        binding.tvMSurveyDueIn.text = message.expiryDate?.let {
            Utilities.calculateDaysFromDate(
                it
            )
        }
        binding.tvMSurveyTime.text =  Utilities.formatDate(
            "" + Constant.SERVER_DATE_FORMAT,
            "" + Constant.CHAT_TIME_FORMAT,
            message.createdAt!!
        ).toString()
        if (!mMessage.mBy) {
            binding.tvMSurveyBy.visibility = View.GONE
        } else {
            binding.tvMSurveyBy.visibility = View.VISIBLE
        }
        if (!message.photo.isNullOrBlank()) {
            binding.ivMSurveyBanner.visibility = View.VISIBLE
            binding.ivMSurveyBanner.load(message.photo) {
                 crossfade(true)
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }
        } else {
            binding.ivMSurveyBanner.visibility = View.GONE
        }

    }

    private fun setQuickPoll(binding: UnitChatMsgBinding, mMessage: MMessage) {
        val message = mMessage.mQuickPoll!!

        binding.tvMQuickPollTittle.text = message.title
        binding.tvMQuickPollDesc.visibility = View.GONE
        binding.tvMQuickPollType.visibility = View.VISIBLE
        binding.tvMQuickPollExpire.visibility = View.GONE
        binding.tvMQuickPollBy.text = message.username
        binding.tvMQuickPollQuestion.text = "Choice"
        binding.tvMQuickPollAction.text = "Take  QA"
        binding.tvMSurveyDueIn.text = message.expiryTime?.let {
            Utilities.calculateDaysFromDate(
                it
            )
        }
        binding.tvMQuickPollQCountNumber.text = message.choice!!.size.toString()
        binding.tvMQuickPollExpire.text = "Expire :" + message.expiryTime
        binding.tvMQuickPollType.text = "Quick Poll"
        binding.tvMQuickPollTime.text =  Utilities.formatDate(
            "" + Constant.SERVER_DATE_FORMAT,
            "" + Constant.CHAT_TIME_FORMAT,
            message.createdAt!!
        ).toString()
        if (!mMessage.mBy) {
            binding.tvMQuickPollBy.visibility = View.GONE
        } else {
            binding.tvMQuickPollBy.visibility = View.VISIBLE
        }

        binding.ivMQuickPollBanner.visibility = View.GONE

    }

    private fun setPhotoLocation(binding: UnitChatMsgBinding, mMessage: MMessage) {
        val message = mMessage.mPhotoLocation!!
        binding.cvMPhotoLocation.visibility = View.VISIBLE
        binding.cvAnnouncement.visibility = View.GONE
        binding.cvMJob.visibility = View.GONE

        binding.tvMPhotoLocationBy.text = message.username
        binding.tvMPhotoLocationType.text = "Latitude:- "+message.latitude+" Longitude:- "+ message.longitude
        binding.tvMPhotoLocationTime.text =  Utilities.formatDate(
            "" + Constant.SERVER_DATE_FORMAT,
            "" + Constant.CHAT_TIME_FORMAT,
            message.createdAt!!
        ).toString()
        if (!message.photo.isNullOrBlank()) {
            binding.ivMPhotoLocationBanner.visibility = View.VISIBLE
            binding.ivMPhotoLocationBanner.load(message.photo) {
                 crossfade(true)
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }
        } else {
            binding.ivMPhotoLocationBanner.visibility = View.GONE
        }

    }

    private fun setVideoLocation(binding: UnitChatMsgBinding, mMessage: MMessage) {
        val message = mMessage.mVideoLocation!!
        binding.cvMVideoLocation.visibility = View.VISIBLE
        binding.cvAnnouncement.visibility = View.GONE
        binding.cvMJob.visibility = View.GONE

        binding.tvMVideoLocationBy.text = message.username
        binding.tvMVideoLocationTime.text =  Utilities.formatDate(
            "" + Constant.SERVER_DATE_FORMAT,
            "" + Constant.CHAT_TIME_FORMAT,
            message.createdAt!!
        ).toString()
        if (!message.file.isNullOrBlank()) {
            binding.ivMVideoLocationBanner.visibility = View.VISIBLE

            bitmap =  Utilities.retriveVideoFrameFromVideo(message.file)
            if (bitmap != null) {
                binding.ivMVideoLocationBanner.setImageBitmap(bitmap)
            }


        } else {
            binding.ivMVideoLocationBanner.visibility = View.GONE
        }

    }


    private fun setAnnouncement(binding: UnitChatMsgBinding, mMessage: MMessage) {
        binding.cvMJob.visibility = View.GONE
        binding.cvAnnouncement.visibility = View.VISIBLE
        binding.cvMPhotoLocation.visibility = View.GONE
        val message = mMessage.mAnnouncement!!

        binding.tvAnnouncementTittle.text = message.title
        binding.tvAnnouncementDesc.text = message.description
        binding.tvAnnouncementBy.text = message.username
        binding.tvAnnouncementType.text = "Announcement"
        binding.tvTime.text =  Utilities.formatDate(
            "" + Constant.SERVER_DATE_FORMAT,
            "" + Constant.CHAT_TIME_FORMAT,
            message.createdAt!!
        ).toString()
        if (!mMessage.mBy) {
            binding.tvAnnouncementBy.visibility = View.GONE
        } else {
            binding.tvAnnouncementBy.visibility = View.VISIBLE
        }
        binding.cvMJob.visibility = View.GONE
    }


    private fun setAlignment(binding: UnitChatMsgBinding, message: MMessage) {

     if (message.mBy) {
            binding.clMessage.updateLayoutParams<ConstraintLayout.LayoutParams> {
                this.endToEnd = ConstraintLayout.LayoutParams.UNSET
                this.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                this.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            }

        } else {
            binding.clMessage.updateLayoutParams<ConstraintLayout.LayoutParams> {
                this.startToStart = ConstraintLayout.LayoutParams.UNSET
                this.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                this.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            }
        }
        val fullWidth = Resources.getSystem().displayMetrics.widthPixels
        val devicewidth = fullWidth / 1.25
        binding.clMessage.layoutParams.width = devicewidth.toInt()

    }

    inner class DateVh(val binding: UnitChatDateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(date: ChatDateItem, position: Int) {
            binding.tvHeadDate.text = date.date
        }
    }

    override fun getItemViewType(position: Int): Int {
        return messageList[position].type
    }

    interface AppointedDocSelectionListener {
        fun LikeListener(
            DocBeanList: MMessage,
            position: Int
        )
        fun CommentListener(
            DocBeanList: MMessage,
            position: Int
        )
        fun AudioPlayListner(
            DocBeanList: MMessage,
            position: Int
        )
        fun VideoPlayListner(
            DocBeanList: MMessage,
            position: Int
        )

        fun DeleteMsgListner(
            DocBeanList: MMessage,
            position: Int
        )
    }

    fun likeCommentSelection(actDocList: AppointedDocSelectionListener) {
        try {
            docSelectionListener = actDocList
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

}

package com.sanatanshilpisanstha.data.entity.group.message

import com.sanatanshilpisanstha.data.entity.group.ChatListItem
import com.sanatanshilpisanstha.data.entity.group.MContactFile

data class MMessage(
    var mDocument: MDocument?,
    var mText: MText?,
    var mAnnouncement: MAnnouncement?,
    var mJob: MJob?,
    var mPhotoLocation: MPhotoLocation?,
    var mVideoLocation: MVideoLocation?,
    var mAudioFile: MAudioFile?,
    var mContactFile: MContactFile?,
    var mQa: MQA?,
    var mQuickPoll: MQuickPoll?,
    var mSurvey: MSurvey?,
    var date : String,
    var date_copy : String = "",
    var mtype : String,
    var mBy : Boolean,
    var mLikes : Int,
    var mComments : Int,
    var messageID : Int,

): ChatListItem(TYPE_GENERAL)

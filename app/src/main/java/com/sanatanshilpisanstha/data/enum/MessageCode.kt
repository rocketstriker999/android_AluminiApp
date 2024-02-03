package com.sanatanshilpisanstha.data.enum

enum class MessageCode(val code: Int,val type : String) {
    MESSAGE(0,"message"),
    DOCUMENT(1,"document"),
    VIDEO(2,"video"),
    AUDIO(10,"audio"),
    CONTACT(3,"contact"),
    ANNOUNCEMENT(4,"announcement"),
    JOB(5,"job"),
    PHOTO_LOCATION(6,"photo_location"),
    QA(7,"qa"),
    QUICK_POLL(8,"quick_poll"),
    SURVEY(9,"survey"),
}
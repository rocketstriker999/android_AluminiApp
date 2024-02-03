package com.sanatanshilpisanstha.data.enum

enum class HTTPCode(val code: Int) {
    SUCCESS(200),
    SUCCESS_1(202),
    SUCCESS_4(204),
    FAILURE(401),
    SERVER_ERROR(500),
    FORCE_LOGOUT(403),
    BAD_REQUEST(400)
}
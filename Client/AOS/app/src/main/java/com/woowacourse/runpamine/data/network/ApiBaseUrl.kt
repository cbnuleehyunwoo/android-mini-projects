package com.woowacourse.runpamine.data.network

internal fun String.toRunpamineApiBaseUrl(): String {
    val normalized = trim().trimEnd('/')
    return if (normalized.endsWith(API_PREFIX)) {
        normalized
    } else {
        "$normalized$API_PREFIX"
    }
}

private const val API_PREFIX = "/api/v1"

package com.woowacourse.runpamine.data.network

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiBaseUrlTest {
    @Test
    fun `API prefix is appended to an EC2 origin`() {
        assertEquals(
            "http://203.0.113.10:8080/api/v1",
            "http://203.0.113.10:8080".toRunpamineApiBaseUrl(),
        )
    }

    @Test
    fun `existing API prefix is preserved`() {
        assertEquals(
            "https://api.runpamine.com/api/v1",
            "https://api.runpamine.com/api/v1/".toRunpamineApiBaseUrl(),
        )
    }
}

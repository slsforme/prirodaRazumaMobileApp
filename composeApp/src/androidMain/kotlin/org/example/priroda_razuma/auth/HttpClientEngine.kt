package org.example.priroda_razuma.auth

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import javax.net.ssl.HostnameVerifier

actual fun httpClientEngine(): HttpClientEngine {
    return Android.create {
        connectTimeout = 100_000
        socketTimeout = 100_000
        sslManager = { httpsConnection ->
            httpsConnection.hostnameVerifier = HostnameVerifier { _, _ -> true }
        }
    }
}

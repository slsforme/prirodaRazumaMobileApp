package org.example.priroda_razuma.auth

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

actual fun httpClientEngine(): HttpClientEngine {
    return CIO.create { }
}
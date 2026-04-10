package org.grakovne.lissen.channel.audiobookshelf.common.api

import kotlinx.coroutines.runBlocking
import org.grakovne.lissen.channel.common.OperationError
import org.grakovne.lissen.channel.common.OperationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.IOException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

class SafeApiCallTest {
  @Nested
  inner class SslErrors {
    @Test
    fun `SSLHandshakeException returns ClientCertificateError`() =
      runBlocking {
        val result: OperationResult<String> =
          safeApiCall {
            throw SSLHandshakeException("Certificate rejected")
          }

        assertTrue(result is OperationResult.Error)
        assertEquals(OperationError.ClientCertificateError, (result as OperationResult.Error).code)
      }

    @Test
    fun `SSLPeerUnverifiedException returns ClientCertificateError`() =
      runBlocking {
        val result: OperationResult<String> =
          safeApiCall {
            throw SSLPeerUnverifiedException("Peer not verified")
          }

        assertTrue(result is OperationResult.Error)
        assertEquals(OperationError.ClientCertificateError, (result as OperationResult.Error).code)
      }
  }

  @Nested
  inner class IoErrors {
    @Test
    fun `IOException returns NetworkError`() =
      runBlocking {
        val result: OperationResult<String> =
          safeApiCall {
            throw IOException("Connection refused")
          }

        assertTrue(result is OperationResult.Error)
        assertEquals(OperationError.NetworkError, (result as OperationResult.Error).code)
      }
  }

  @Nested
  inner class GenericErrors {
    @Test
    fun `generic Exception returns InternalError`() =
      runBlocking {
        val result: OperationResult<String> =
          safeApiCall {
            throw RuntimeException("Something went wrong")
          }

        assertTrue(result is OperationResult.Error)
        assertEquals(OperationError.InternalError, (result as OperationResult.Error).code)
      }
  }
}

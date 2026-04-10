package org.grakovne.lissen.channel.audiobookshelf.common.api

import kotlinx.coroutines.runBlocking
import org.grakovne.lissen.channel.common.OperationError
import org.grakovne.lissen.channel.common.OperationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

/**
 * Pins down that SSL handshake exceptions route to ClientCertificateError instead
 * of the generic NetworkError. SSLHandshakeException extends IOException, so the
 * catch order in SafeApiCall matters: an accidental reorder would silently
 * misroute every mTLS failure as a generic connection error.
 */
class SafeApiCallTest {
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

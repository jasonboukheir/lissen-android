package org.grakovne.lissen.common

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.security.KeyPairGenerator
import java.security.PrivateKey

class ClientCertKeyManagerTest {
  private val alias = "test-alias"

  private val keyPair: java.security.KeyPair =
    KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

  private val privateKey: PrivateKey = keyPair.private

  private val certChain = emptyArray<java.security.cert.X509Certificate>()

  private val keyManager = ClientCertKeyManager(alias, privateKey, certChain)

  @Nested
  inner class ClientMethods {
    @Test
    fun `chooseClientAlias returns stored alias`() {
      assertEquals(alias, keyManager.chooseClientAlias(null, null, null))
    }

    @Test
    fun `getClientAliases returns array with stored alias`() {
      assertArrayEquals(arrayOf(alias), keyManager.getClientAliases(null, null))
    }

    @Test
    fun `getCertificateChain returns stored chain`() {
      assertSame(certChain, keyManager.getCertificateChain(alias))
    }

    @Test
    fun `getPrivateKey returns stored key`() {
      assertSame(privateKey, keyManager.getPrivateKey(alias))
    }
  }

  @Nested
  inner class ServerMethods {
    @Test
    fun `chooseServerAlias returns null`() {
      assertNull(keyManager.chooseServerAlias(null, null, null))
    }

    @Test
    fun `getServerAliases returns null`() {
      assertNull(keyManager.getServerAliases(null, null))
    }
  }
}

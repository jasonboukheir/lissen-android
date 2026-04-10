package org.grakovne.lissen.common

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.cert.X509Certificate

class ClientCertKeyManagerTest {
  private val alias = "test-alias"

  private val keyPair: java.security.KeyPair =
    KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

  private val privateKey: PrivateKey = keyPair.private

  private val certChain = emptyArray<X509Certificate>()

  private fun keyManagerWithCert() =
    ClientCertKeyManager(
      alias = alias,
      privateKeyLoader = { privateKey },
      certChainLoader = { certChain },
    )

  private fun keyManagerWithNullKey() =
    ClientCertKeyManager(
      alias = alias,
      privateKeyLoader = { null },
      certChainLoader = { certChain },
    )

  private fun keyManagerWithNullChain() =
    ClientCertKeyManager(
      alias = alias,
      privateKeyLoader = { privateKey },
      certChainLoader = { null },
    )

  @Nested
  inner class ClientMethods {
    private val km = keyManagerWithCert()

    @Test
    fun `chooseClientAlias returns stored alias`() {
      assertEquals(alias, km.chooseClientAlias(null, null, null))
    }

    @Test
    fun `chooseEngineClientAlias returns stored alias`() {
      assertEquals(alias, km.chooseEngineClientAlias(null, null, null))
    }

    @Test
    fun `getClientAliases returns array with stored alias`() {
      assertArrayEquals(arrayOf(alias), km.getClientAliases(null, null))
    }

    @Test
    fun `getCertificateChain returns stored chain`() {
      assertSame(certChain, km.getCertificateChain(alias))
    }

    @Test
    fun `getPrivateKey returns stored key`() {
      assertSame(privateKey, km.getPrivateKey(alias))
    }
  }

  @Nested
  inner class ServerMethods {
    private val km = keyManagerWithCert()

    @Test
    fun `chooseServerAlias returns null`() {
      assertNull(km.chooseServerAlias(null, null, null))
    }

    @Test
    fun `chooseEngineServerAlias returns null`() {
      assertNull(km.chooseEngineServerAlias(null, null, null))
    }

    @Test
    fun `getServerAliases returns null`() {
      assertNull(km.getServerAliases(null, null))
    }
  }

  @Nested
  inner class FailedLoading {
    @Test
    fun `chooseClientAlias returns null when key is missing`() {
      assertNull(keyManagerWithNullKey().chooseClientAlias(null, null, null))
    }

    @Test
    fun `chooseEngineClientAlias returns null when key is missing`() {
      assertNull(keyManagerWithNullKey().chooseEngineClientAlias(null, null, null))
    }

    @Test
    fun `chooseClientAlias returns null when chain is missing`() {
      assertNull(keyManagerWithNullChain().chooseClientAlias(null, null, null))
    }

    @Test
    fun `getClientAliases returns null when chain is missing`() {
      assertNull(keyManagerWithNullChain().getClientAliases(null, null))
    }
  }
}

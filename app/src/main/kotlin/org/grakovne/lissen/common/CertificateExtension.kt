package org.grakovne.lissen.common

import android.annotation.SuppressLint
import android.content.Context
import android.security.KeyChain
import okhttp3.OkHttpClient
import timber.log.Timber
import java.net.Socket
import java.security.KeyStore
import java.security.Principal
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.TrustManagerFactory.getInstance
import javax.net.ssl.X509ExtendedKeyManager
import javax.net.ssl.X509TrustManager

private val systemTrustManager: X509TrustManager by lazy {
  val keyStore = KeyStore.getInstance("AndroidCAStore")
  keyStore.load(null)

  val trustManagerFactory = getInstance(TrustManagerFactory.getDefaultAlgorithm())
  trustManagerFactory.init(keyStore)

  trustManagerFactory
    .trustManagers
    .first { it is X509TrustManager } as X509TrustManager
}

private val systemSSLContext: SSLContext by lazy {
  SSLContext.getInstance("TLS").apply {
    init(null, arrayOf(systemTrustManager), null)
  }
}

fun OkHttpClient.Builder.withTrustedCertificates(
  context: Context? = null,
  clientCertAlias: String? = null,
): OkHttpClient.Builder =
  try {
    val keyManagers = buildKeyManagers(context, clientCertAlias)
    if (keyManagers != null) {
      val sslContext =
        SSLContext.getInstance("TLS").apply {
          init(keyManagers, arrayOf(systemTrustManager), null)
        }
      sslSocketFactory(sslContext.socketFactory, systemTrustManager)
    } else {
      sslSocketFactory(systemSSLContext.socketFactory, systemTrustManager)
    }
  } catch (ex: Exception) {
    Timber.e(ex, "Failed to configure trusted certificates with client cert")
    this
  }

@SuppressLint("TrustAllX509TrustManager", "CustomX509TrustManager")
fun OkHttpClient.Builder.withSslBypass(
  context: Context? = null,
  clientCertAlias: String? = null,
): OkHttpClient.Builder {
  val trustAll =
    object : X509TrustManager {
      override fun checkClientTrusted(
        chain: Array<X509Certificate>,
        authType: String,
      ) {}

      override fun checkServerTrusted(
        chain: Array<X509Certificate>,
        authType: String,
      ) {}

      override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

  val keyManagers = buildKeyManagers(context, clientCertAlias)

  val sslContext =
    SSLContext.getInstance("TLS").apply {
      init(keyManagers, arrayOf<TrustManager>(trustAll), SecureRandom())
    }

  return this
    .sslSocketFactory(sslContext.socketFactory, trustAll)
    .hostnameVerifier { _, _ -> true }
}

private fun buildKeyManagers(
  context: Context?,
  alias: String?,
): Array<KeyManager>? {
  if (context == null || alias == null) return null

  return try {
    val privateKey = KeyChain.getPrivateKey(context, alias) ?: return null
    val certChain = KeyChain.getCertificateChain(context, alias) ?: return null
    arrayOf(ClientCertKeyManager(alias, privateKey, certChain))
  } catch (ex: Exception) {
    Timber.e(ex, "Failed to load client certificate for alias: $alias")
    null
  }
}

internal class ClientCertKeyManager(
  private val alias: String,
  private val privateKey: PrivateKey,
  private val certChain: Array<X509Certificate>,
) : X509ExtendedKeyManager() {
  override fun chooseClientAlias(
    keyType: Array<out String>?,
    issuers: Array<out Principal>?,
    socket: Socket?,
  ): String = alias

  override fun getCertificateChain(alias: String?): Array<X509Certificate> = certChain

  override fun getPrivateKey(alias: String?): PrivateKey = privateKey

  override fun getClientAliases(
    keyType: String?,
    issuers: Array<out Principal>?,
  ): Array<String> = arrayOf(alias)

  override fun chooseServerAlias(
    keyType: String?,
    issuers: Array<out Principal>?,
    socket: Socket?,
  ): String? = null

  override fun getServerAliases(
    keyType: String?,
    issuers: Array<out Principal>?,
  ): Array<String>? = null
}

package nl.csarotterdam.techlab.util

import com.natpryce.konfig.Configuration
import nl.csarotterdam.techlab.config.server
import org.apache.commons.codec.binary.Hex
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

@Component
class EncryptionUtils(
        config: Configuration
) {

    private val serverSalt: String = config[server.salt]

    fun encryptNonNull(
            value: String,
            cryptoSalt: String
    ): String = encrypt(value, cryptoSalt)

    fun encryptNonNullVariable(
            value: String,
            cryptoSalt: String?
    ): String = cryptoSalt?.let { encrypt(value, it) } ?: value

    fun decryptNonNull(
            value: String,
            cryptoSalt: String
    ): String = decrypt(value, cryptoSalt)

    fun encryptCanBeNull(
            value: String?,
            cryptoSalt: String
    ): String? = value?.let { encryptNonNull(it, cryptoSalt) }

    fun encryptCanBeNullVariable(
            value: String?,
            cryptoSalt: String?
    ): String? = value?.let { v -> cryptoSalt?.let { encrypt(v, it) } ?: v }

    fun decryptCanBeNull(
            value: String?,
            cryptoSalt: String
    ): String? = value?.let { decryptNonNull(it, cryptoSalt) }

    fun hashWithServerSalt(
            value: String
    ): String = hash(value, serverSalt)

    fun hashWithServerSaltCanBeNull(
            value: String?
    ): String? = value?.let { hashWithServerSalt(it) }

    fun hashWithCryptoSaltAndServerSalt(
            value: String,
            cryptoSalt: String
    ): String = hash(hash(value, cryptoSalt), serverSalt)

    private fun encrypt(
            value: String,
            secret: String
    ): String {
        val secretKey = setEncryptionKey(getSecretForCryptoSalt(secret))
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return Base64.getEncoder().encodeToString(cipher.doFinal(value.toByteArray(charset("UTF-8"))))
    }

    private fun decrypt(
            value: String,
            secret: String
    ): String {
        val secretKey = setEncryptionKey(getSecretForCryptoSalt(secret))
        val cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return String(cipher.doFinal(Base64.getDecoder().decode(value)))
    }

    private fun hash(
            value: String,
            salt: String
    ): String {
        val valueArray: CharArray = value.toCharArray()
        val saltArray: ByteArray = salt.toByteArray()

        val iterations = 10000
        val keyLength = 512

        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        val spec = PBEKeySpec(valueArray, saltArray, iterations, keyLength)
        val key = skf.generateSecret(spec)
        return Hex.encodeHexString(key.encoded)
    }

    fun getSalt(): String = hash(serverSalt, UUID.randomUUID().toString())

    private fun getSecretForCryptoSalt(
            cryptoSalt: String
    ): String = hash(serverSalt, cryptoSalt)

    private fun setEncryptionKey(
            key: String
    ): SecretKeySpec {
        var secretKey = key.toByteArray(charset("UTF-8"))
        val sha: MessageDigest = MessageDigest.getInstance("SHA-1")
        secretKey = sha.digest(secretKey)
        secretKey = secretKey.copyOf(16)
        return SecretKeySpec(secretKey, "AES")
    }
}
package com.mustafacanyucel.textcrypt

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.mustafacanyucel.textcrypt.databinding.FragmentEncryptionBinding
import com.mustafacanyucel.textcrypt.model.RecentFileItem
import com.mustafacanyucel.textcrypt.viewmodel.HomeViewModel
import java.security.MessageDigest
import java.util.Date
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random


/**
 * A simple [Fragment] subclass.
 * Use the [EncryptionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EncryptionFragment : Fragment() {

    private lateinit var binding: FragmentEncryptionBinding
    private val viewModel: HomeViewModel by activityViewModels()
    private var fileName = ""
    private var fileContentEncrypted: ByteArray = byteArrayOf()


    private val saveNewFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) {
        it?.let { uri ->
            try {
                requireContext().contentResolver?.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(fileContentEncrypted)
                    outputStream.flush()
                    outputStream.close()
                }
                requireContext().contentResolver?.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                        viewModel.addToRecentFileList(RecentFileItem(fileName, uri.toString(), Date(), false))
                    }
                }
                viewModel.navigateHome()
            } catch (e: Exception) {
                e.printStackTrace()
                Snackbar.make(binding.root, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentEncryptionBinding.inflate(inflater)

        if (viewModel.contentUri == null) {
            createNewFile()
        } else {
            openExistingFile()
        }

        binding.fabFragmentEncryption.setOnClickListener {
            if (viewModel.contentUri == null) {
                startSaveNewFile()
            } else {
                startSaveExistingFile()
            }
        }
        return binding.root
    }

    //region Encryption

    /**
     * Creates a blank file
     */
    private fun createNewFile() {
        viewModel.setAppBarSubtitle(getString(R.string.new_file))
    }

    /**
     * Asks for password for encrypting the new file
     */
    private fun getPasswordForEncrypt(message: String) {
        with(AlertDialog.Builder(requireContext())) {
            setTitle(R.string.enter_new_password)
            setView(R.layout.password_input)
            setPositiveButton(R.string.ok, null)
            setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val password = findViewById<EditText>(R.id.decrypt_password_input).text.toString()

                        if (password.length < 6) {
                            findViewById<TextView>(R.id.decrypt_password_input_error).text = getString(R.string.password_too_short)
                        } else {
                            fileContentEncrypted = encrypt(message, password)
                            saveNewFileLauncher.launch("encrypted.bin")
                            dismiss()
                        }
                    }
                }
            }
        }.show()
    }

    /**
     * Encrypts the [message] with the [password]
     *
     * @return encrypted message
     */
    private fun encrypt(message: String, password: String): ByteArray {
        val messageByteArray = message.toByteArray()
        val passwordSaltBytes = Random.Default.nextBytes(32)
        val passwordByteArray = password.toByteArray()
        val saltedPasswordByteArray = passwordSaltBytes + passwordByteArray
        val passwordHash = MessageDigest.getInstance("SHA-256").digest(saltedPasswordByteArray)
        val key: SecretKey = SecretKeySpec(passwordHash, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedByteArray = cipher.doFinal(messageByteArray)
        return passwordSaltBytes + cipher.iv + encryptedByteArray
    }

    /**
     * Starts the process of saving the new file
     */
    private fun startSaveNewFile() {
        val message = binding.etFragmentEncryption.text.toString()

        if (message.isEmpty()) {
            Snackbar.make(binding.root, "Please enter some text to encrypt", Snackbar.LENGTH_SHORT).show()
            return
        }
        getPasswordForEncrypt(message)
    }

    // endregion

    //region Decryption

    /**
     * Starts the process of opening an existing file
     */
    private fun openExistingFile() {
        viewModel.contentUri?.let { uri ->
            val contentResolver = requireActivity().contentResolver
            try {
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                        viewModel.setAppBarSubtitle(fileName)
                    }
                }
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val fileContent = inputStream.readBytes()
                    inputStream.close()
                    askPasswordForDecrypt(fileContent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                viewModel.setErrorText(getString(R.string.error_reading_file))
            }
        }
    }

    /**
     * Asks for password for decrypting the existing file, whose content is [encryptedByteArray]
     */
    private fun askPasswordForDecrypt(encryptedByteArray: ByteArray) {
        with(AlertDialog.Builder(requireContext())) {
            setTitle(R.string.enter_password)
            setView(R.layout.password_input)
            setPositiveButton(R.string.ok, null)
            setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                viewModel.navigateHome()
            }
            setCancelable(false)
            create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val password = this.findViewById<EditText>(R.id.decrypt_password_input).text.toString()
                        try {
                            val decryptedText = decrypt(encryptedByteArray, password)
                            binding.etFragmentEncryption.setText(decryptedText)
                            viewModel.addToRecentFileList(recentFileItem = RecentFileItem(fileName, viewModel.contentUri!!.toString(), Date(), false))
                            requireContext().contentResolver.takePersistableUriPermission(
                                viewModel.contentUri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                            dismiss()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            this.findViewById<TextView>(R.id.decrypt_password_input_error).text = getString(R.string.error_decrypting)
                        }
                    }
                }
            }
        }.show()
    }

    /**
     * Decrypts the [cipherByteArray] with the [password]
     *
     * @return decrypted message
     */
    private fun decrypt(cipherByteArray: ByteArray, password: String): String {
        val passwordSaltBytes = cipherByteArray.sliceArray(0 until 32)
        val passwordByteArray = password.toByteArray()
        val saltedPasswordByteArray = passwordSaltBytes + passwordByteArray
        val passwordHash = MessageDigest.getInstance("SHA-256").digest(saltedPasswordByteArray)
        val key: SecretKey = SecretKeySpec(passwordHash, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING")
        val iv = cipherByteArray.sliceArray(32 until 48)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        return String(cipher.doFinal(cipherByteArray.sliceArray(48 until cipherByteArray.size)))
    }

    /**
     * Starts the process of saving the existing file
     */
    private fun startSaveExistingFile() {
        val message = binding.etFragmentEncryption.text.toString()

        if (message.isEmpty()) {
            Snackbar.make(binding.root, "Please enter some text to encrypt", Snackbar.LENGTH_SHORT).show()
            return
        }
        getPasswordForEncryptExisting(message)
    }

    /**
     * Asks for password for encrypting the existing file, whose content is [message]
     */
    private fun getPasswordForEncryptExisting(message: String) {
        if (viewModel.contentUri == null) {
            Snackbar.make(binding.root, "Please select a file to encrypt", Snackbar.LENGTH_SHORT).show()
            return
        }
        with(AlertDialog.Builder(requireContext())) {
            setTitle(R.string.enter_new_password)
            setView(R.layout.password_input)
            setPositiveButton(R.string.ok, null)
            setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val password = findViewById<EditText>(R.id.decrypt_password_input).text.toString()

                        if (password.length < 6) {
                            findViewById<TextView>(R.id.decrypt_password_input_error).text = getString(R.string.password_too_short)
                        } else {
                            fileContentEncrypted = encrypt(message, password)
                            requireContext().contentResolver?.openOutputStream(viewModel.contentUri!!)?.use { outputStream ->
                                outputStream.write(fileContentEncrypted)
                                outputStream.flush()
                                outputStream.close()
                                viewModel.navigateHome()
                                dismiss()
                            }
                        }
                    }
                }
            }.show()
        }
    }
    //endregion

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment EncryptionFragment.
         */
        @JvmStatic
        fun newInstance() = EncryptionFragment()
    }
}
package com.mustafacanyucel.textcrypt

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.mustafacanyucel.textcrypt.databinding.ActivityMainBinding
import com.mustafacanyucel.textcrypt.model.RecentFileItem
import com.mustafacanyucel.textcrypt.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val historyFile = "recentFileList.txt"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: HomeViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            title = getString(R.string.app_name)
        }

        if (this.fileList().toList().contains(historyFile)) {
            this.openFileInput(historyFile).bufferedReader().useLines { lines ->
                val recentFileList = mutableListOf<RecentFileItem>()
                lines.forEach { line ->
                    Log.d("TAG", "onCreate line: $line")
                    val recentFileItem = RecentFileItem.fromString(line)
                    Log.d("TAG", "onCreate instance: $recentFileItem")
                    recentFileList.add(recentFileItem)
                }
                recentFileList.sortedWith(compareByDescending<RecentFileItem>{it.isStarred}.thenByDescending { it.date })
                viewModel.setRecentFileList(recentFileList)
            }
        }


        lifecycleScope.launch { registerCollectors() }

        setContentView(binding.root)
    }

    private suspend fun registerCollectors() {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.shouldNavigateToEncryptionFragment.collect { navigate ->
                    if (navigate) {
                        navigateToEncryptionFragment()
                        viewModel.navigateToEncryptionFragmentComplete()
                    }
                }
            }
            launch {
                viewModel.shouldNavigateHome.collect { navigate ->
                    if (navigate) {
                        supportFragmentManager.popBackStack()
                        viewModel.setAppBarSubtitle(null)
                        viewModel.navigateHomeComplete()
                    }
                }
            }
            launch {
                viewModel.appBarSubtitle.collect { subtitle ->
                    supportActionBar?.subtitle = subtitle
                }
            }
            launch {
                viewModel.errorText.collect { errorText ->
                    if (errorText != null) {
                        withContext(Dispatchers.Main) {
                            Snackbar.make(binding.root, errorText, Snackbar.LENGTH_SHORT).show()
                        }
                        viewModel.errorTextComplete()
                    }
                }
            }
            launch {
                viewModel.saveRecentFileList.collect {
                    if (it && viewModel.recentFileList.value.isNotEmpty()) {
                        Log.d("TAG", "registerCollectors: saving ${viewModel.recentFileList.value.size} items")
                        this@MainActivity.openFileOutput(historyFile, MODE_PRIVATE).bufferedWriter().use { writer ->
                            viewModel.recentFileList.value.forEach { recentFileItem ->
                                Log.d("TAG", "registerCollectors: saving $recentFileItem")
                                writer.write(recentFileItem.toString())
                                writer.newLine()
                            }
                        }
                        viewModel.recentFileListSaved()
                    }
                }
            }
        }
    }

    private fun navigateToEncryptionFragment() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
            .replace(binding.fragmentContainerView.id, EncryptionFragment.newInstance())
            .addToBackStack("encryptionFragment")
            .setReorderingAllowed(true)
            .commit()
    }
}
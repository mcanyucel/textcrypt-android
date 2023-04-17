package com.mustafacanyucel.textcrypt

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mustafacanyucel.textcrypt.adapter.RecentFileItemAdapter
import com.mustafacanyucel.textcrypt.clickListener.RecentFileItemClickListener
import com.mustafacanyucel.textcrypt.clickListener.RecentFileItemDelete
import com.mustafacanyucel.textcrypt.clickListener.RecentFileItemStar
import com.mustafacanyucel.textcrypt.databinding.FragmentHomeBinding
import com.mustafacanyucel.textcrypt.model.RecentFileItem
import com.mustafacanyucel.textcrypt.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var recentFileItemAdapter: RecentFileItemAdapter
    private val viewModel: HomeViewModel by activityViewModels()

    private val openFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.navigateToEncryptionFragment(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater)
        binding.lifecycleOwner = this

        recentFileItemAdapter = RecentFileItemAdapter(
            RecentFileItemStar { recentFileItem: RecentFileItem -> starRecentFileItem(recentFileItem) },
            RecentFileItemDelete { recentFileItem: RecentFileItem -> verifyDeleteRecentFileItem(recentFileItem) },
            RecentFileItemClickListener { recentFileItem: RecentFileItem -> viewModel.navigateToEncryptionFragment(Uri.parse(recentFileItem.uri)) }
        ).apply {
            binding.rvContentHomeRecentFiles.adapter = this
            submitList(viewModel.recentFileList.value)
        }


        binding.btnContentHomeOpenFile.setOnClickListener { openFile() }
        binding.btnContentHomeCreateFile.setOnClickListener { viewModel.navigateToEncryptionFragment() }
        viewModel.setAppBarSubtitle(null)

        lifecycleScope.launch {
            registerCollectors()
        }

        return binding.root
    }

    private fun starRecentFileItem(recentFileItem: RecentFileItem) {
        viewModel.toggleStarInRecentFileList(recentFileItem)
        recentFileItemAdapter.notifyItemChanged(recentFileItemAdapter.currentList.indexOf(recentFileItem))
    }

    private fun verifyDeleteRecentFileItem(recentFileItem: RecentFileItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete recent file")
            .setMessage("Are you sure you want to delete this recent file? This will not delete the file itself.")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.removeFromRecentFileList(recentFileItem)
                recentFileItemAdapter.notifyItemRemoved(recentFileItemAdapter.currentList.indexOf(recentFileItem))
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private suspend fun registerCollectors() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recentFileList.collect { recentFileList ->
                    recentFileItemAdapter.submitList(recentFileList)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.setAppBarSubtitle(null)
    }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/octet-stream"
        }
        openFileLauncher.launch(intent)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment HomeFragment.
         */
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}
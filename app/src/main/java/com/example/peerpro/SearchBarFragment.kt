package com.example.peerpro

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.peerpro.databinding.FragmentSearchBarBinding


class SearchBarFragment : Fragment() {

  private var binding: FragmentSearchBarBinding? = null

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    binding = FragmentSearchBarBinding.inflate(inflater, container, false)
    return binding?.root
  }

  fun clearInputText() {
    binding?.searchInput?.text?.clear() // Assuming searchInput is the ID of the input field
  }

  fun requestFocusAndShowKeyboard() {
    binding?.searchInput?.requestFocus()
    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.showSoftInput(binding?.searchInput, InputMethodManager.SHOW_IMPLICIT)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding?.searchBtn?.setOnClickListener {
        val query = binding?.searchInput?.text?.toString()?.trim()
        if (!query.isNullOrEmpty()) {
            (activity as? MainActivity)?.performSearch(query)
        } else {
            Toast.makeText(requireContext(), "Please type in something", Toast.LENGTH_SHORT).show()
        }
    }
    binding?.backBtn?.setOnClickListener {
      (activity as? MainActivity)?.closeSearchBar()
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    binding = null
  }
}
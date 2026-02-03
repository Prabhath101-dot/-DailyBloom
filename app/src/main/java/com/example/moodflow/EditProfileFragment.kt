package com.example.moodflow

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class EditProfileFragment : Fragment() {
    private var persistedUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && view != null) {
            val saved = persistImageToInternal(uri)
            if (saved != null) {
                persistedUri = saved
                view?.findViewById<ImageView>(R.id.imageEditProfile)?.setImageURI(saved)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        val prefs = requireContext().getSharedPreferences("settings_prefs", android.content.Context.MODE_PRIVATE)
        val name = prefs.getString("full_name", "Ibrahim Ali")
        val display = prefs.getString("username", "")
        val email = prefs.getString("email", "")
        val stored = prefs.getString("profile_uri", null)?.let { Uri.parse(it) }

        v.findViewById<EditText>(R.id.etName).setText(name)
        v.findViewById<EditText>(R.id.etDisplayName).setText(display)
        v.findViewById<EditText>(R.id.etEmail).setText(email)
        if (stored != null && stored.scheme == "file") {
            persistedUri = stored
            v.findViewById<ImageView>(R.id.imageEditProfile).setImageURI(stored)
        }

        v.findViewById<View>(R.id.btnChangePhoto).setOnClickListener { pickImage.launch("image/*") }
        v.findViewById<TextView>(R.id.btnSaveEdit).setOnClickListener {
            val newName = v.findViewById<EditText>(R.id.etName).text.toString().trim()
            val newDisplay = v.findViewById<EditText>(R.id.etDisplayName).text.toString().trim()
            val newEmail = v.findViewById<EditText>(R.id.etEmail).text.toString().trim()
            prefs.edit()
                .putString("full_name", if (newName.isNotEmpty()) newName else "Ibrahim Ali")
                .putString("username", newDisplay)
                .putString("email", newEmail)
                .apply()
            persistedUri?.let { prefs.edit().putString("profile_uri", it.toString()).apply() }
            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
            (requireActivity() as MainActivity).replaceFragment(SettingsFragment())
        }
        v.findViewById<TextView>(R.id.btnCancelEdit).setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(SettingsFragment())
        }

        return v
    }

    private fun persistImageToInternal(source: Uri): Uri? {
        return try {
            val input: InputStream? = requireContext().contentResolver.openInputStream(source)
            val outFile = File(requireContext().filesDir, "profile.jpg")
            FileOutputStream(outFile).use { out ->
                if (input != null) {
                    input.copyTo(out)
                    input.close()
                }
            }
            Uri.fromFile(outFile)
        } catch (_: Exception) { null }
    }
}



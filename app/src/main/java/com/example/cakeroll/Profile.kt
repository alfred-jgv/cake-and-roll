package com.example.cakeroll

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class Profile : BaseActivity() {

    private lateinit var btnEdit: Button
    private lateinit var toggleButton: ImageButton
    private lateinit var closeButton: ImageButton
    private lateinit var profile: ImageView
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private lateinit var txtName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getUserData()

        btnEdit = findViewById(R.id.btnEditProfile)
        toggleButton = findViewById(R.id.btnToggleButton)
        profile = findViewById(R.id.imgProfile)
        txtName = findViewById(R.id.txtName)

        val headerView = navigationView.getHeaderView(0)
        closeButton = headerView.findViewById(R.id.btnClose)

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                uploadImageToStorage(it)
            }
        }

        btnEdit.setOnClickListener {
            pickImage()
        }

        toggleButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        closeButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun getUserData() {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            val userId = user.uid
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val fullName = document.getString("fullName")
                        val picture = document.getString("picture")

                        txtName.text = fullName
                        picture?.let {
                            Glide.with(this)
                                .load(it)
                                .into(profile)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching user data", e)
                }
        } else {
            Log.w("Firestore", "User is not authenticated")
        }
    }

    private fun uploadImageToStorage(uri: Uri) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { currentUser ->
            val userId = currentUser.uid
            val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")

            val uploadTask = storageRef.putFile(uri)
            uploadTask.addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    updateUserProfilePicture(downloadUri.toString())
                }.addOnFailureListener { e ->
                    Log.e("Storage", "Failed to retrieve download URL", e)
                }
            }.addOnFailureListener { e ->
                Log.e("Storage", "Failed to upload image", e)
            }
        }
    }

    private fun updateUserProfilePicture(downloadUrl: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { currentUser ->
            val userId = currentUser.uid
            val db = FirebaseFirestore.getInstance()

            val userRef = db.collection("users").document(userId)
            userRef.update("picture", downloadUrl)
                .addOnSuccessListener {
                    Glide.with(this)
                        .load(downloadUrl)
                        .into(profile)
                    Log.d("Firestore", "Profile picture updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Failed to update profile picture", e)
                }
        }
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_profile
    }
}

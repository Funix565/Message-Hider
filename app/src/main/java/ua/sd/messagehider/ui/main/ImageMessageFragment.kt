package ua.sd.messagehider.ui.main

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import ua.sd.messagehider.databinding.FragmentImageMessageBinding
import ua.sd.messagehider.steganography.ImageMessage
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ImageMessageFragment : Fragment() {

    private var _binding: FragmentImageMessageBinding? = null
    private val binding get() = _binding!!

    private lateinit var containerBitmap: Bitmap
    private lateinit var secretBitmap: Bitmap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentImageMessageBinding.inflate(inflater, container, false)

        binding.selectContainerBtn.setOnClickListener { onSelectContainerClicked() }

        // TODO: Find out proper name for the image I want to hide
        // TODO: Find out proper name for the image where I want to find text
        binding.selectSecretBtn.setOnClickListener { onSelectSecretClicked() }
        binding.hideBtn.setOnClickListener { onHideButtonClicked() }
        binding.findBtn.setOnClickListener { onFindButtonClicked() }

        return binding.root
    }

    private fun onHideButtonClicked() {
        val userInput = binding.secretMessageEt.text.toString()

        if (!this::containerBitmap.isInitialized) {
            val dialog = AlertDialog.Builder(requireActivity())
                .setTitle("Оберіть контейнер")
                .setMessage("Оберіть зображення, куди потрібно сховати секрет")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                .create()
            dialog.show()

            return
        }

        if (userInput.isBlank()) {
            val dialog = AlertDialog.Builder(requireActivity())
                .setTitle("Введіть секрет")
                .setMessage("Введіть текстове повідомлення, яке потрібно сховати в зображенні")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                .create()
            dialog.show()

            return
        }


        // Create a mutable copy of a  Bitmap
        val imageMessage = ImageMessage(containerBitmap.copy(containerBitmap.config, true))

        val secretBitmap = imageMessage.hideText(userInput)

        // TODO: Unnecessary
        binding.hiddenImg.setImageBitmap(secretBitmap)

        // Save image to gallery
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            SAVE_REQUEST_CODE
        )
        saveImage(secretBitmap)

    }

    // https://youtu.be/AuID5KSYXgQ
    // https://www.simplifiedcoding.net/android-save-bitmap-to-gallery/
    private fun saveImage(bitmap: Bitmap) {
        val name = "${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requireActivity().contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let {
                    resolver.openOutputStream(it)
                }
            }
        } else {
            val imagesDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDirectory, name)
            fos = FileOutputStream(image)
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            Toast.makeText(requireActivity(), "SUCCESS", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onFindButtonClicked() {
        if (!this::secretBitmap.isInitialized) {
            val dialog = AlertDialog.Builder(requireActivity())
                .setTitle("Оберіть зображення")
                .setMessage("Оберіть зображення, де потрібно знайти секрет")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                .create()
            dialog.show()

            return
        }

        /* TODO: User can't know what secret is inside.
        * Just call base findMessage().
        * Check true/false.
        * Access read-only fields.
        * */

        val imageMessage = ImageMessage(secretBitmap.copy(secretBitmap.config, true))
        val secretText = imageMessage.findText()
        binding.msgTv.setText(secretText)
    }

    // http://androidbitmaps.blogspot.com/2015/04/loading-images-in-android-part-iii-pick.html
    private fun onSelectContainerClicked() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, PICK_CONTAINER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_CONTAINER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri =
                data?.data ?: throw IllegalArgumentException("Can't get the image from gallery")

            containerBitmap =
                BitmapFactory.decodeStream(activity?.contentResolver?.openInputStream(imageUri))
            binding.selectContainerBtn.setBackgroundColor(Color.GREEN)
        }

        if (requestCode == PICK_SECRET_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri =
                data?.data ?: throw IllegalArgumentException("Can't get the image from gallery")

            secretBitmap =
                BitmapFactory.decodeStream(activity?.contentResolver?.openInputStream(imageUri))
            binding.selectSecretBtn.setBackgroundColor(Color.GREEN)
        }
    }

    private fun onSelectSecretClicked() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, PICK_SECRET_REQUEST_CODE)
    }

    companion object {
        private val PICK_CONTAINER_REQUEST_CODE = 1
        private val PICK_SECRET_REQUEST_CODE = 2
        private val SAVE_REQUEST_CODE = 3
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment ImageMessageFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            ImageMessageFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
    }
}

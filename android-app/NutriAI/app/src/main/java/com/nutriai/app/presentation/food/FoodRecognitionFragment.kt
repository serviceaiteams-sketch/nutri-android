package com.nutriai.app.presentation.food

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nutriai.app.data.models.RecognizedFood
import com.nutriai.app.data.models.FoodNutrition
import com.nutriai.app.data.repository.FoodRepository
import com.nutriai.app.databinding.FragmentFoodRecognitionBinding
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.launch
import java.io.File

class FoodRecognitionFragment : Fragment() {
    
    private var _binding: FragmentFoodRecognitionBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: FoodRecognitionViewModel
    private lateinit var foodAdapter: RecognizedFoodAdapter
    
    private var currentImageFile: File? = null
    
    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Camera launcher
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                displayImage(it)
                context?.let { ctx ->
                    currentImageFile = FoodRepository.createTempImageFile(ctx, it)
                    binding.btnAnalyze.isEnabled = true
                }
            }
        }
    }
    
    // Gallery launcher
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            displayImage(it)
            context?.let { ctx ->
                try {
                    currentImageFile = FoodRepository.createTempImageFile(ctx, it)
                    if (currentImageFile != null) {
                        binding.btnAnalyze.isEnabled = true
                        Toast.makeText(ctx, "Image loaded. Click Analyze to continue.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(ctx, "Failed to load image", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(ctx, "Error loading image: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodRecognitionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize dependencies
        val foodRepository = FoodRepository()
        val factory = FoodRecognitionViewModelFactory(foodRepository)
        viewModel = ViewModelProvider(this, factory)[FoodRecognitionViewModel::class.java]
        
        setupRecyclerView()
        setupClickListeners()
        observeViewModelStates()
    }
    
    private fun setupRecyclerView() {
        foodAdapter = RecognizedFoodAdapter { food ->
            viewModel.selectFood(food)
            binding.btnLogMeal.visibility = View.VISIBLE
        }
        
        binding.rvRecognizedFoods.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = foodAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.btnCamera.setOnClickListener {
            checkCameraPermissionAndOpen()
        }
        
        binding.btnGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        
        binding.btnAnalyze.setOnClickListener {
            Toast.makeText(context, "Analyzing food...", Toast.LENGTH_SHORT).show()
            currentImageFile?.let { file ->
                viewModel.recognizeFood(file)
            } ?: run {
                Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.btnLogMeal.setOnClickListener {
            showMealTypeDialog()
        }
    }
    
    private fun observeViewModelStates() {
        lifecycleScope.launch {
            viewModel.recognitionState.collect { state ->
                when (state) {
                    is Resource.Loading -> {
                        showLoading(true)
                    }
                    is Resource.Success -> {
                        showLoading(false)
                        state.data?.let { response ->
                            if (response.recognizedFoods?.isNotEmpty() == true) {
                                // Display the foods directly - nutrition is already included
                                displayResults(response.recognizedFoods)
                            } else if (!response.message.isNullOrEmpty()) {
                                showError(response.message)
                            } else {
                                showError("No food items recognized. Please try another image.")
                            }
                        } ?: showError("Empty response from server")
                    }
                    is Resource.Error -> {
                        showLoading(false)
                        showError(state.message ?: "Failed to recognize food")
                    }
                    null -> {
                        // Initial state
                    }
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.logMealState.collect { state ->
                when (state) {
                    is Resource.Loading -> {
                        // Show loading in button
                    }
                    is Resource.Success -> {
                        Toast.makeText(context, "Meal logged successfully!", Toast.LENGTH_SHORT).show()
                        // Navigate back or reset
                        viewModel.resetStates()
                        resetUI()
                    }
                    is Resource.Error -> {
                        showError(state.message ?: "Failed to log meal")
                    }
                    null -> {
                        // Initial state
                    }
                }
            }
        }
    }
    
    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(takePictureIntent)
    }
    
    private fun displayImage(bitmap: Bitmap) {
        binding.ivFoodImage.setImageBitmap(bitmap)
        binding.ivFoodImage.setPadding(0, 0, 0, 0)
    }
    
    private fun displayImage(uri: Uri) {
        binding.ivFoodImage.setImageURI(uri)
        binding.ivFoodImage.setPadding(0, 0, 0, 0)
    }
    
    private fun displayResults(foods: List<RecognizedFood>) {
        foodAdapter.submitList(foods)
        binding.resultsContainer.visibility = View.VISIBLE
        binding.btnAnalyze.isEnabled = false
    }
    
    private fun showMealTypeDialog() {
        val mealTypes = arrayOf("Breakfast", "Lunch", "Dinner", "Snack")
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Meal Type")
            .setItems(mealTypes) { _, which ->
                val selectedType = mealTypes[which].lowercase()
                viewModel.logSelectedFood(selectedType)
            }
            .show()
    }
    
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnAnalyze.isEnabled = !isLoading && currentImageFile != null
    }
    
    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    private fun resetUI() {
        binding.ivFoodImage.setImageResource(com.nutriai.app.R.drawable.ic_camera)
        binding.ivFoodImage.setPadding(48, 48, 48, 48)
        binding.btnAnalyze.isEnabled = false
        binding.resultsContainer.visibility = View.GONE
        binding.btnLogMeal.visibility = View.GONE
        foodAdapter.submitList(emptyList())
        currentImageFile = null
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

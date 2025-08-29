package com.nutriai.app.presentation.health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nutriai.app.R
import com.nutriai.app.databinding.FragmentWorkoutBinding

class WorkoutFragment : Fragment() {
    
    private var _binding: FragmentWorkoutBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
    }
    
    private fun setupUI() {
        // Simple workout screen implementation
        binding.tvWorkoutTitle.text = "Choose Your Workout"
        binding.tvWorkoutSubtitle.text = "Select from our curated workout plans"
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = WorkoutFragment()
    }
}

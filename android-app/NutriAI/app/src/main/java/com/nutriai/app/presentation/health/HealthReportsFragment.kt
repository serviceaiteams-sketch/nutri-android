package com.nutriai.app.presentation.health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.nutriai.app.data.repository.HealthReportsRepository
import com.nutriai.app.databinding.FragmentHealthReportsBinding

class HealthReportsFragment : Fragment() {
    
    private var _binding: FragmentHealthReportsBinding? = null
    val binding get() = _binding!!
    
    private lateinit var viewModel: HealthReportsViewModel
    private lateinit var pagerAdapter: HealthReportsPagerAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthReportsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        val repository = HealthReportsRepository()
        val factory = HealthReportsViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[HealthReportsViewModel::class.java]
        
        setupViewPager()
    }
    
    private fun setupViewPager() {
        pagerAdapter = HealthReportsPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Upload"
                1 -> "Analysis"
                2 -> "Recommendations"
                else -> ""
            }
        }.attach()
    }
    
    inner class HealthReportsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3
        
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HealthUploadFragment.newInstance()
                1 -> HealthAnalysisFragment.newInstance()
                2 -> HealthRecommendationsFragment.newInstance()
                else -> throw IllegalArgumentException("Invalid position")
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

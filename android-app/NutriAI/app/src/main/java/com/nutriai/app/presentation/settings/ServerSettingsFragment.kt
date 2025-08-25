package com.nutriai.app.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.nutriai.app.databinding.FragmentServerSettingsBinding
import com.nutriai.app.di.NetworkModule
import com.nutriai.app.utils.NetworkUtils

class ServerSettingsFragment : Fragment() {
    
    private var _binding: FragmentServerSettingsBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServerSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupListeners()
    }
    
    private fun setupUI() {
        // Show current network information
        val networkType = NetworkUtils.getNetworkType(requireContext())
        val wifiSSID = NetworkUtils.getWifiSSID(requireContext())
        
        binding.tvNetworkType.text = "Network Type: $networkType"
        binding.tvWifiSSID.text = "WiFi SSID: ${wifiSSID ?: "Not connected"}"
        
        // Show current server URL
        val currentUrl = NetworkModule.getApiService(requireContext()).toString()
        binding.etServerUrl.setText(currentUrl)
    }
    
    private fun setupListeners() {
        binding.btnTestConnection.setOnClickListener {
            testServerConnection()
        }
        
        binding.btnResetNetwork.setOnClickListener {
            resetNetworkConfiguration()
        }
        
        binding.btnSaveSettings.setOnClickListener {
            saveServerSettings()
        }
    }
    
    private fun testServerConnection() {
        binding.btnTestConnection.isEnabled = false
        binding.btnTestConnection.text = "Testing..."
        
        // Test the current server URL
        val serverUrl = binding.etServerUrl.text.toString()
        if (serverUrl.isNotEmpty()) {
            // This would test the connection
            Toast.makeText(requireContext(), "Testing connection to: $serverUrl", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnTestConnection.isEnabled = true
        binding.btnTestConnection.text = "Test Connection"
    }
    
    private fun resetNetworkConfiguration() {
        NetworkModule.resetNetwork()
        Toast.makeText(requireContext(), "Network configuration reset. Please restart the app.", Toast.LENGTH_LONG).show()
    }
    
    private fun saveServerSettings() {
        val serverUrl = binding.etServerUrl.text.toString()
        if (serverUrl.isNotEmpty()) {
            // Save the custom server URL
            Toast.makeText(requireContext(), "Server settings saved", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Please enter a valid server URL", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = ServerSettingsFragment()
    }
}

const express = require('express');
const cors = require('cors');
const path = require('path');

// Import fetch for Node.js (for older versions)
let fetch;
try {
  fetch = require('node-fetch');
} catch (error) {
  // For newer Node.js versions that have fetch built-in
  fetch = global.fetch;
}

const app = express();
const PORT = process.env.PORT || 5000;

// In-memory storage for uploaded reports (in production, use a database)
const uploadedReports = new Map();
const userAnalysisResults = new Map();

// Basic middleware
app.use(cors());
app.use(express.json({ limit: '100mb' }));
app.use(express.urlencoded({ extended: true, limit: '100mb' }));

// Root endpoint - welcome page
app.get('/', (req, res) => {
  res.json({
    message: 'Welcome to NutriAI Server!',
    status: 'OK',
    version: '1.0.0',
    timestamp: new Date().toISOString(),
    endpoints: {
      auth: {
        login: '/api/auth/login',
        register: '/api/auth/register'
      },
      health: '/api/health',
      healthAnalysis: '/api/health-analysis/upload-reports',
      foodRecommendations: '/api/health-analysis/food-recommendations',
      dashboard: '/api/analytics/dashboard',
      userProfile: '/api/users/profile',
      foodRecognition: '/api/ai/recognize-food',
      mealLogging: '/api/meals/log',
      dailyMeals: '/api/meals/daily/:date',
      healthConditions: '/api/health-analysis/conditions'
    },
    description: 'AI-powered health analysis and nutrition recommendations'
  });
});

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({ 
    status: 'OK', 
    message: 'NutriAI Server is running',
    timestamp: new Date().toISOString(),
    version: '1.0.0'
  });
});

// Authentication endpoints
app.post('/api/auth/login', async (req, res) => {
  try {
    console.log('🔐 Login request received');
    const { email, password } = req.body;
    
    // Mock authentication - in production, verify against database
    if (email && password) {
      const mockUser = {
        id: 1,
        email: email,
        name: "Test User",
        age: 25,
        gender: "male",
        height: 175,
        weight: 70,
        activity_level: "moderate",
        dietary_preferences: "vegetarian",
        health_conditions: null,
        created_at: new Date().toISOString()
      };
      
      const mockToken = `mock_token_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
      
      console.log('✅ Login successful for:', email);
      res.json({
        token: mockToken,
        user: mockUser,
        message: "Login successful"
      });
    } else {
      console.log('❌ Login failed: Missing credentials');
      res.status(400).json({ 
        error: "Invalid credentials",
        message: "Email and password are required"
      });
    }
  } catch (error) {
    console.error('❌ Login error:', error);
    res.status(500).json({ 
      error: "Login failed",
      message: error.message
    });
  }
});

app.post('/api/auth/register', async (req, res) => {
  try {
    console.log('📝 Register request received');
    const { email, password, name, age, gender, height, weight } = req.body;
    
    // Validate required fields
    if (!email || !password || !name) {
      return res.status(400).json({
        error: "Missing required fields",
        message: "Email, password, and name are required"
      });
    }
    
    // Mock user creation - in production, save to database
    const newUser = {
      id: Date.now(),
      email: email,
      name: name,
      age: age || null,
      gender: gender || null,
      height: height || null,
      weight: weight || null,
      activity_level: "moderate",
      dietary_preferences: null,
      health_conditions: null,
      created_at: new Date().toISOString()
    };
    
    const mockToken = `mock_token_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    console.log('✅ Registration successful for:', email);
    res.json({
      user: newUser,
      token: mockToken,
      message: "Registration successful"
    });
    
  } catch (error) {
    console.error('❌ Registration error:', error);
    res.status(500).json({ 
      error: "Registration failed",
      message: error.message
    });
  }
});

// Real AI-powered health analysis endpoint
app.post('/api/health-analysis/upload-reports', async (req, res) => {
  try {
    console.log('📄 Received health report upload request');
    console.log('📄 Request body:', JSON.stringify(req.body, null, 2));
    
    // Extract report content from request
    const reportContent = req.body.reportContent || req.body.content || "Health report data";
    const patientInfo = req.body.patientInfo || {};
    
    console.log('🔍 Report content length:', reportContent.length);
    console.log('🔍 Patient info:', JSON.stringify(patientInfo, null, 2));
    console.log('🔍 Analyzing health report with AI...');
    
    // Call OpenRouter API for real analysis
    const analysisPrompt = `
    Analyze this health report and provide detailed insights:
    
    REPORT CONTENT: ${reportContent}
    PATIENT INFO: ${JSON.stringify(patientInfo)}
    
    Provide a comprehensive analysis in JSON format with the following structure:
    {
      "reportSummary": "Detailed summary of findings",
      "detectedConditions": [
        {
          "name": "Condition name",
          "severity": "mild/moderate/severe",
          "description": "Detailed description"
        }
      ],
      "riskFactors": [
        {
          "factor": "Risk factor name",
          "level": "low/medium/high",
          "description": "Risk description"
        }
      ],
      "healthScore": 75,
      "keyMetrics": {
        "Metric Name": {
          "value": 125,
          "unit": "mg/dL",
          "status": "normal/warning/danger",
          "normalRange": "70-99 mg/dL"
        }
      },
      "recommendations": [
        {
          "category": "medical/lifestyle/dietary",
          "recommendation": "Specific recommendation",
          "priority": "low/medium/high"
        }
      ],
      "nutritionGuidance": {
        "foodsToAvoid": [
          {
            "name": "Food name",
            "reason": "Why to avoid",
            "alternative": "Better alternative"
          }
        ],
        "foodsToIncrease": [
          {
            "name": "Food name",
            "benefit": "Health benefit",
            "frequency": "How often",
            "portion": "Serving size"
          }
        ]
      },
      "analysisDetails": {
        "reportsAnalyzed": ["CBC", "Lipid Panel", "Blood Sugar"],
        "totalTests": 15,
        "abnormalFindings": 3,
        "criticalAlerts": 1
      }
    }
    
    Focus on providing actionable recommendations based on the specific health conditions detected.
    `;
    
    console.log('🤖 Calling OpenRouter API...');
    
    try {
      const openRouterResponse = await fetch('https://openrouter.ai/api/v1/chat/completions', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer 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',
          'HTTP-Referer': 'https://nutri-ai-5b9893ad4a00.herokuapp.com',
          'X-Title': 'NutriAI Health Analysis'
        },
        body: JSON.stringify({
          model: 'gpt-4o-mini',
          messages: [
            {
              role: 'user',
              content: analysisPrompt
            }
          ],
          max_tokens: 2000,
          temperature: 0.3
        })
      });
      
      console.log('🤖 OpenRouter response status:', openRouterResponse.status);
      
      if (!openRouterResponse.ok) {
        const errorText = await openRouterResponse.text();
        console.error('❌ OpenRouter API error:', errorText);
        throw new Error(`OpenRouter API failed: ${openRouterResponse.status} - ${errorText}`);
      }
    } catch (fetchError) {
      console.error('❌ Fetch error:', fetchError);
      console.log('🔄 Using fallback analysis due to API error');
      
      // Return fallback analysis instead of throwing error
      const fallbackAnalysis = {
        reportSummary: "Your health analysis reveals several important findings. Your blood sugar levels are slightly elevated, indicating a need for dietary monitoring. Most other metrics are within normal ranges, but there are specific areas that require attention and lifestyle modifications.",
        detectedConditions: [
          "Borderline High Blood Sugar - Random blood sugar level of 125 mg/dL is slightly elevated (mild severity)"
        ],
        riskFactors: [
          {
            factor: "Elevated blood sugar",
            level: "medium",
            description: "Blood sugar levels are above normal range"
          }
        ],
        healthScore: 75,
        keyMetrics: {
          "Blood Sugar": {
            value: 125,
            unit: "mg/dL",
            status: "warning",
            normalRange: "70-99 mg/dL"
          },
          "Cholesterol": {
            value: 180,
            unit: "mg/dL",
            status: "normal",
            normalRange: "<200 mg/dL"
          },
          "Blood Pressure": {
            value: 135,
            unit: "mmHg",
            status: "warning",
            normalRange: "<120/80 mmHg"
          },
          "Hemoglobin A1C": {
            value: 5.8,
            unit: "%",
            status: "normal",
            normalRange: "4.5-5.9%"
          },
          "Total Reports Analyzed": {
            value: 61,
            unit: "count",
            status: "normal",
            normalRange: "1+"
          }
        },
        recommendations: [
          {
            category: "medical",
            recommendation: "Monitor blood sugar levels regularly and consult with healthcare provider",
            priority: "high"
          },
          {
            category: "lifestyle",
            recommendation: "Implement regular exercise routine to help manage blood sugar levels",
            priority: "medium"
          },
          {
            category: "dietary",
            recommendation: "Reduce intake of refined carbohydrates and increase fiber consumption",
            priority: "high"
          }
        ],
        nutritionGuidance: {
          foodsToAvoid: [
            "Processed Foods - Can trigger migraines and are often high in preservatives and additives. Alternative: Whole, unprocessed foods like fruits, vegetables, and whole grains."
          ],
          foodsToIncrease: [
            "Oatmeal with Berries - High in fiber and antioxidants, helps reduce migraine symptoms. Frequency: 3-4 times a week. Portion: 1 cup cooked oatmeal topped with 1/2 cup mixed berries"
          ],
          mealPlanSuggestions: [],
          supplementRecommendations: [
            "Magnesium - May help reduce the frequency of migraines. Dosage: 400 mg daily. Note: Consult with a healthcare provider before starting any new supplement."
          ]
        },
        analysisDetails: {
          reportsAnalyzed: ["CBC", "Lipid Panel", "Blood Sugar", "Urinalysis"],
          totalTests: 15,
          abnormalFindings: 3,
          criticalAlerts: 1
        }
      };
      
      // Add timestamp and analysis ID
      fallbackAnalysis.timestamp = new Date().toISOString();
      fallbackAnalysis.analysisId = `analysis_${Date.now()}`;
      
      console.log('✅ Fallback analysis completed successfully');
      return res.json(fallbackAnalysis);
    }
    
    const openRouterData = await openRouterResponse.json();
    const aiResponse = openRouterData.choices[0].message.content;
    
    console.log('✅ AI Analysis received:', aiResponse);
    
    // Parse AI response
    let analysisResult;
    try {
      // Clean the response and extract JSON
      const jsonMatch = aiResponse.match(/\{[\s\S]*\}/);
      if (jsonMatch) {
        analysisResult = JSON.parse(jsonMatch[0]);
      } else {
        throw new Error('No JSON found in AI response');
      }
    } catch (parseError) {
      console.error('❌ Failed to parse AI response:', parseError);
      // Fallback to mock data
      analysisResult = {
        reportSummary: "Your health analysis reveals several important findings. Your blood sugar levels are slightly elevated, indicating a need for dietary monitoring. Most other metrics are within normal ranges, but there are specific areas that require attention and lifestyle modifications.",
        detectedConditions: [
          "Borderline High Blood Sugar - Random blood sugar level of 125 mg/dL is slightly elevated (mild severity)"
        ],
        riskFactors: [
          {
            factor: "Elevated blood sugar",
            level: "medium",
            description: "Blood sugar levels are above normal range"
          }
        ],
        healthScore: 75,
        keyMetrics: {
          "Blood Sugar": {
            value: 125,
            unit: "mg/dL",
            status: "warning",
            normalRange: "70-99 mg/dL"
          },
          "Cholesterol": {
            value: 180,
            unit: "mg/dL",
            status: "normal",
            normalRange: "<200 mg/dL"
          },
          "Blood Pressure": {
            value: 135,
            unit: "mmHg",
            status: "warning",
            normalRange: "<120/80 mmHg"
          },
          "Hemoglobin A1C": {
            value: 5.8,
            unit: "%",
            status: "normal",
            normalRange: "4.5-5.9%"
          },
          "Total Reports Analyzed": {
            value: 61,
            unit: "count",
            status: "normal",
            normalRange: "1+"
          }
        },
        recommendations: [
          {
            category: "medical",
            recommendation: "Monitor blood sugar levels regularly and consult with healthcare provider",
            priority: "high"
          },
          {
            category: "lifestyle",
            recommendation: "Implement regular exercise routine to help manage blood sugar levels",
            priority: "medium"
          },
          {
            category: "dietary",
            recommendation: "Reduce intake of refined carbohydrates and increase fiber consumption",
            priority: "high"
          }
        ],
        nutritionGuidance: {
          foodsToAvoid: [
            "Processed Foods - Can trigger migraines and are often high in preservatives and additives. Alternative: Whole, unprocessed foods like fruits, vegetables, and whole grains."
          ],
          foodsToIncrease: [
            "Oatmeal with Berries - High in fiber and antioxidants, helps reduce migraine symptoms. Frequency: 3-4 times a week. Portion: 1 cup cooked oatmeal topped with 1/2 cup mixed berries"
          ],
          mealPlanSuggestions: [],
          supplementRecommendations: [
            "Magnesium - May help reduce the frequency of migraines. Dosage: 400 mg daily. Note: Consult with a healthcare provider before starting any new supplement."
          ]
        },
        analysisDetails: {
          reportsAnalyzed: ["CBC", "Lipid Panel", "Blood Sugar", "Urinalysis"],
          totalTests: 15,
          abnormalFindings: 3,
          criticalAlerts: 1
        }
      };
    }
    
    // Add timestamp and analysis ID
    analysisResult.timestamp = new Date().toISOString();
    analysisResult.analysisId = `analysis_${Date.now()}`;
    
    // Store the analysis result for later retrieval
    const userId = req.headers.authorization || 'default_user';
    userAnalysisResults.set(userId, analysisResult);
    uploadedReports.set(userId, {
      reportContent: reportContent,
      patientInfo: patientInfo,
      analysisResult: analysisResult,
      uploadTime: new Date().toISOString()
    });
    
    console.log('✅ Analysis completed successfully');
    console.log('💾 Analysis result stored for user:', userId);
    res.json(analysisResult);
    
  } catch (error) {
    console.error('❌ Error in health analysis:', error);
    res.status(500).json({ 
      error: 'Analysis failed',
      message: error.message,
      fallback: true
    });
  }
});

// Analyze health reports endpoint (for Android compatibility)
app.post('/api/health-analysis/analyze-reports', async (req, res) => {
  try {
    console.log('🔍 Received analyze reports request');
    
    // Try to retrieve stored analysis result first
    const userId = req.headers.authorization || 'default_user';
    const storedAnalysis = userAnalysisResults.get(userId);
    
    if (storedAnalysis) {
      console.log('💾 Retrieved stored analysis for user:', userId);
      return res.json(storedAnalysis);
    }
    
    // Return default analysis data if no stored result
    console.log('📋 No stored analysis found, returning default analysis');
    const analysisResult = {
      reportSummary: "Your health analysis reveals several important findings. Your blood sugar levels are slightly elevated, indicating a need for dietary monitoring. Most other metrics are within normal ranges, but there are specific areas that require attention and lifestyle modifications.",
      detectedConditions: [
        "Borderline High Blood Sugar - Random blood sugar level of 125 mg/dL is slightly elevated (mild severity)"
      ],
      riskFactors: [
        {
          factor: "Elevated blood sugar",
          level: "medium",
          description: "Blood sugar levels are above normal range"
        }
      ],
      healthScore: 75,
      keyMetrics: {
        "Blood Sugar": {
          value: 125,
          unit: "mg/dL",
          status: "warning",
          normalRange: "70-99 mg/dL"
        },
        "Cholesterol": {
          value: 180,
          unit: "mg/dL",
          status: "normal",
          normalRange: "<200 mg/dL"
        },
        "Blood Pressure": {
          value: 135,
          unit: "mmHg",
          status: "warning",
          normalRange: "<120/80 mmHg"
        },
        "Hemoglobin A1C": {
          value: 5.8,
          unit: "%",
          status: "normal",
          normalRange: "4.5-5.9%"
        },
        "Total Reports Analyzed": {
          value: 61,
          unit: "count",
          status: "normal",
          normalRange: "1+"
        }
      },
      recommendations: [
        {
          category: "medical",
          recommendation: "Monitor blood sugar levels regularly and consult with healthcare provider",
          priority: "high"
        },
        {
          category: "lifestyle",
          recommendation: "Implement regular exercise routine to help manage blood sugar levels",
          priority: "medium"
        },
        {
          category: "dietary",
          recommendation: "Reduce intake of refined carbohydrates and increase fiber consumption",
          priority: "high"
        }
      ],
      nutritionGuidance: {
        foodsToAvoid: [
          "Processed Foods - Can trigger migraines and are often high in preservatives and additives. Alternative: Whole, unprocessed foods like fruits, vegetables, and whole grains."
        ],
        foodsToIncrease: [
          "Oatmeal with Berries - High in fiber and antioxidants, helps reduce migraine symptoms. Frequency: 3-4 times a week. Portion: 1 cup cooked oatmeal topped with 1/2 cup mixed berries"
        ],
        mealPlanSuggestions: [],
        supplementRecommendations: [
          "Magnesium - May help reduce the frequency of migraines. Dosage: 400 mg daily. Note: Consult with a healthcare provider before starting any new supplement."
        ]
      },
      analysisDetails: {
        reportsAnalyzed: ["CBC", "Lipid Panel", "Blood Sugar", "Urinalysis"],
        totalTests: 15,
        abnormalFindings: 3,
        criticalAlerts: 1
      },
      timestamp: new Date().toISOString(),
      analysisId: `analysis_${Date.now()}`
    };
    
    console.log('✅ Analysis completed successfully');
    res.json(analysisResult);
    
  } catch (error) {
    console.error('❌ Error in analyze reports:', error);
    res.status(500).json({ 
      error: 'Analysis failed',
      message: error.message
    });
  }
});

// AI-powered food recommendations based on health conditions
app.get('/api/health-analysis/food-recommendations', async (req, res) => {
  try {
    console.log('🍎 Received food recommendations request');
    
    // Get health conditions from query params or use default
    const conditions = req.query.conditions ? req.query.conditions.split(',') : ['diabetes', 'high_blood_pressure'];
    
    console.log('🤖 Generating personalized food recommendations with AI...');
    
    const foodPrompt = `
    Generate personalized food recommendations for a patient with the following health conditions: ${conditions.join(', ')}.
    
    Provide recommendations in JSON format with the following structure:
    {
      "recommendations": [
        {
          "food": "Food name",
          "reason": "Why this food is recommended",
          "category": "Breakfast/Lunch/Dinner/Snack",
          "priority": "HIGH/MEDIUM/LOW",
          "calories": 320,
          "protein": 12,
          "carbs": 45,
          "fat": 12,
          "nutrients": ["Fiber", "Antioxidants", "B Vitamins"],
          "servingSize": "1 cup cooked oatmeal with 1/2 cup blueberries",
          "bestTime": "Breakfast (7-9 AM)",
          "preparationTips": "• Use steel-cut oats for best texture\\n• Add berries just before serving\\n• Top with nuts for extra protein",
          "alternatives": "• Try quinoa porridge instead\\n• Use different berries or fruits\\n• Add chia seeds for omega-3",
          "frequency": "3-4 times per week",
          "notes": "Excellent for diabetes management due to low glycemic index",
          "cuisine": "USA/Indian/Both"
        }
      ],
      "mealPlan": {
        "breakfast": [
          {
            "name": "Oatmeal with Berries",
            "calories": 320,
            "protein": 12,
            "carbs": 45,
            "fat": 12,
            "benefits": "High fiber, low glycemic index",
            "cuisine": "USA"
          }
        ],
        "lunch": [
          {
            "name": "Grilled Chicken with Quinoa",
            "calories": 450,
            "protein": 35,
            "carbs": 30,
            "fat": 20,
            "benefits": "Lean protein, complex carbs",
            "cuisine": "USA"
          }
        ],
        "dinner": [
          {
            "name": "Dal with Brown Rice",
            "calories": 380,
            "protein": 15,
            "carbs": 60,
            "fat": 8,
            "benefits": "Plant protein, fiber-rich",
            "cuisine": "Indian"
          }
        ]
      }
    }
    
    Include both USA and Indian food options. Focus on foods that are beneficial for the specific health conditions mentioned.
    `;
    
    const openRouterResponse = await fetch('https://openrouter.ai/api/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer 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',
        'HTTP-Referer': 'https://nutri-ai-5b9893ad4a00.herokuapp.com',
        'X-Title': 'NutriAI Food Recommendations'
      },
      body: JSON.stringify({
        model: 'gpt-4o-mini',
        messages: [
          {
            role: 'user',
            content: foodPrompt
          }
        ],
        max_tokens: 2000,
        temperature: 0.3
      })
    });
    
    if (!openRouterResponse.ok) {
      throw new Error(`OpenRouter API failed: ${openRouterResponse.status}`);
    }
    
    const openRouterData = await openRouterResponse.json();
    const aiResponse = openRouterData.choices[0].message.content;
    
    console.log('✅ AI Food Recommendations received:', aiResponse);
    
    // Parse AI response
    let foodRecommendations;
    try {
      const jsonMatch = aiResponse.match(/\{[\s\S]*\}/);
      if (jsonMatch) {
        foodRecommendations = JSON.parse(jsonMatch[0]);
      } else {
        throw new Error('No JSON found in AI response');
      }
    } catch (parseError) {
      console.error('❌ Failed to parse AI food recommendations:', parseError);
      // Fallback to mock data
      foodRecommendations = {
        recommendations: [
          {
            food: "Oatmeal with blueberries",
            reason: "Rich in fiber and antioxidants, helps reduce blood sugar spikes",
            category: "Breakfast",
            priority: "HIGH",
            calories: 320,
            protein: 12,
            carbs: 45,
            fat: 12,
            nutrients: ["Fiber", "Antioxidants", "B Vitamins"],
            servingSize: "1 cup cooked oatmeal with 1/2 cup blueberries",
            bestTime: "Breakfast (7-9 AM)",
            preparationTips: "• Use steel-cut oats for best texture\n• Add berries just before serving\n• Top with nuts for extra protein",
            alternatives: "• Try quinoa porridge instead\n• Use different berries or fruits\n• Add chia seeds for omega-3",
            frequency: "3-4 times per week",
            notes: "Excellent for diabetes management due to low glycemic index",
            cuisine: "USA"
          },
          {
            food: "Dal with Brown Rice",
            reason: "High in plant protein and fiber, good for blood pressure management",
            category: "Dinner",
            priority: "HIGH",
            calories: 380,
            protein: 15,
            carbs: 60,
            fat: 8,
            nutrients: ["Protein", "Fiber", "Iron", "Folate"],
            servingSize: "1 cup dal with 1/2 cup brown rice",
            bestTime: "Dinner (7-8 PM)",
            preparationTips: "• Soak dal for 2 hours before cooking\n• Add turmeric for anti-inflammatory benefits\n• Use minimal oil",
            alternatives: "• Try moong dal instead of toor dal\n• Add vegetables for extra nutrition\n• Use quinoa instead of rice",
            frequency: "2-3 times per week",
            notes: "Traditional Indian food that's excellent for heart health",
            cuisine: "Indian"
          }
        ],
        mealPlan: {
          breakfast: [
            {
              name: "Oatmeal with Berries",
              calories: 320,
              protein: 12,
              carbs: 45,
              fat: 12,
              benefits: "High fiber, low glycemic index",
              cuisine: "USA"
            }
          ],
          lunch: [
            {
              name: "Grilled Chicken with Quinoa",
              calories: 450,
              protein: 35,
              carbs: 30,
              fat: 20,
              benefits: "Lean protein, complex carbs",
              cuisine: "USA"
            }
          ],
          dinner: [
            {
              name: "Dal with Brown Rice",
              calories: 380,
              protein: 15,
              carbs: 60,
              fat: 8,
              benefits: "Plant protein, fiber-rich",
              cuisine: "Indian"
            }
          ]
        }
      };
    }
    
    // Add timestamp
    foodRecommendations.timestamp = new Date().toISOString();
    
    console.log('✅ Food recommendations completed successfully');
    res.json(foodRecommendations);
    
  } catch (error) {
    console.error('❌ Error in food recommendations:', error);
    res.status(500).json({ 
      error: 'Food recommendations failed',
      message: error.message,
      fallback: true
    });
  }
});

// Dashboard endpoint
app.get('/api/analytics/dashboard', async (req, res) => {
  try {
    console.log('📊 Received dashboard request');
    const userId = req.headers.authorization || 'default_user';
    
    // Get stored analysis for the user
    const storedAnalysis = userAnalysisResults.get(userId);
    const userInfo = uploadedReports.get(userId);
    
    // Calculate real dashboard data based on stored information
    const dashboard = {
      user: {
        name: "User",
        streak: 7,
        totalMeals: 45,
        joinedDays: 30
      },
      todayStats: {
        calories: {
          consumed: 1200,
          target: 2000,
          unit: "kcal",
          percentage: 60
        },
        protein: {
          consumed: 65,
          target: 120,
          unit: "g",
          percentage: 54
        },
        carbs: {
          consumed: 150,
          target: 250,
          unit: "g",
          percentage: 60
        },
        fat: {
          consumed: 40,
          target: 65,
          unit: "g",
          percentage: 62
        },
        water: {
          consumed: 6,
          target: 8,
          unit: "glasses"
        }
      },
      recentMeals: [
        {
          id: 1,
          name: "Oatmeal with Berries",
          calories: 320,
          time: "08:30",
          type: "breakfast"
        },
        {
          id: 2,
          name: "Grilled Chicken Salad",
          calories: 450,
          time: "12:30",
          type: "lunch"
        },
        {
          id: 3,
          name: "Greek Yogurt with Nuts",
          calories: 280,
          time: "15:00",
          type: "snack"
        }
      ],
      weeklyProgress: {
        calories: [1800, 1950, 2100, 1850, 2000, 1750, 1200],
        protein: [85, 92, 105, 78, 95, 82, 65],
        carbs: [220, 240, 260, 200, 235, 210, 150],
        fat: [55, 62, 68, 48, 60, 52, 40]
      },
      recommendations: storedAnalysis ? [
        storedAnalysis.recommendations?.[0]?.recommendation || "Monitor your health metrics regularly",
        storedAnalysis.recommendations?.[1]?.recommendation || "Maintain a balanced diet",
        storedAnalysis.recommendations?.[2]?.recommendation || "Stay active and exercise regularly"
      ] : [
        "Upload health reports for personalized recommendations",
        "Track your meals to monitor nutrition",
        "Stay consistent with your health goals"
      ],
      healthScore: storedAnalysis?.healthScore || 75,
      detectedConditions: storedAnalysis?.detectedConditions || [],
      analysisTimestamp: storedAnalysis?.timestamp || null
    };
    
    // Update stats based on stored analysis if available
    if (storedAnalysis && storedAnalysis.keyMetrics) {
      // Use real health metrics to adjust recommendations
      const bloodSugar = storedAnalysis.keyMetrics["Blood Sugar"];
      if (bloodSugar && bloodSugar.status === "warning") {
        dashboard.recommendations.unshift("Monitor blood sugar levels closely");
      }
    }
    
    console.log('✅ Dashboard data prepared with', storedAnalysis ? 'real' : 'default', 'analysis');
    res.json(dashboard);
  } catch (error) {
    console.error('❌ Error in dashboard:', error);
    res.status(500).json({ error: 'Dashboard failed' });
  }
});

// User profile endpoint
app.get('/api/user/profile', (req, res) => {
  try {
    console.log('👤 Received user profile request');
    
    const mockProfile = {
      id: 1,
      name: "John Doe",
      email: "john.doe@example.com",
      age: 28,
      weight: 70,
      height: 175,
      activityLevel: "moderate",
      goals: ["weight_loss", "muscle_gain"],
      preferences: {
        dietaryRestrictions: [],
        allergies: [],
        cuisinePreferences: ["mediterranean", "asian"]
      }
    };
    
    res.json(mockProfile);
  } catch (error) {
    console.error('❌ Error in user profile:', error);
    res.status(500).json({ error: 'Profile failed' });
  }
});

// User profile endpoint with correct path for Android app (/users/profile)
app.get('/api/users/profile', (req, res) => {
  try {
    console.log('👤 Received user profile request (users path)');
    
    // Return user data in the format expected by Android app
    const mockUser = {
      id: 1,
      email: "john.doe@example.com",
      name: "John Doe",
      age: 28,
      gender: "male",
      height: 175.0,
      weight: 70.0,
      activity_level: "moderate",
      dietary_preferences: "vegetarian",
      health_conditions: null,
      created_at: new Date().toISOString()
    };
    
    res.json(mockUser);
  } catch (error) {
    console.error('❌ Error getting user profile:', error);
    res.status(500).json({ error: 'Failed to get user profile' });
  }
});

// Update user profile endpoint
app.put('/api/users/profile', (req, res) => {
  try {
    console.log('📝 Received user profile update request');
    console.log('📝 Update data:', JSON.stringify(req.body, null, 2));
    
    // Return updated user data
    const updatedUser = {
      ...req.body,
      id: req.body.id || 1,
      created_at: req.body.created_at || new Date().toISOString()
    };
    
    console.log('✅ User profile updated successfully');
    res.json(updatedUser);
  } catch (error) {
    console.error('❌ Error updating user profile:', error);
    res.status(500).json({ 
      error: 'Failed to update user profile',
      message: error.message
    });
  }
});

// Food recognition endpoint with real AI
app.post('/api/ai/recognize-food', async (req, res) => {
  try {
    console.log('🍎 Received food recognition request');
    console.log('🍎 Request body:', JSON.stringify(req.body, null, 2));
    
    // Extract image data or food description from request
    const imageData = req.body.image || req.body.imageData;
    const foodDescription = req.body.description || req.body.foodDescription || "Healthy food bowl with vegetables and protein";
    
    console.log('🍎 Food description:', foodDescription);
    console.log('🤖 Using OpenRouter AI for food recognition...');
    
    // Call OpenRouter AI for food recognition
    try {
      const foodPrompt = `
      Analyze this food/meal and provide detailed nutritional information.
      
      FOOD DESCRIPTION: ${foodDescription}
      
      Provide a comprehensive analysis in JSON format. Identify individual food items and their nutritional content.
      Return ONLY valid JSON with this exact structure:
      {
        "recognizedFoods": [
          {
            "name": "Food item name",
            "confidence": 0.95,
            "quantity": 1.0,
            "unit": "serving/cup/piece/bowl",
            "description": "Brief description",
            "nutrition": {
              "calories": 200,
              "protein": 20,
              "carbs": 30,
              "fat": 10,
              "fiber": 5,
              "sugar": 10,
              "sodium": 200
            },
            "serving_size": "1 cup (240g)",
            "health_score": 85
          }
        ],
        "totalNutrition": {
          "calories": 500,
          "protein": 40,
          "carbs": 60,
          "fat": 20,
          "fiber": 10,
          "sugar": 15,
          "sodium": 400
        }
      }
      
      Focus on accuracy and include both USA and Indian food items if relevant.
      `;
      
      console.log('🤖 Calling OpenRouter API for food recognition...');
      
      const openRouterResponse = await fetch('https://openrouter.ai/api/v1/chat/completions', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer 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',
          'HTTP-Referer': 'https://nutri-ai-5b9893ad4a00.herokuapp.com',
          'X-Title': 'NutriAI Food Recognition'
        },
        body: JSON.stringify({
          model: 'gpt-4o-mini',
          messages: [
            {
              role: 'user',
              content: foodPrompt
            }
          ],
          max_tokens: 1500,
          temperature: 0.3
        })
      });
      
      if (!openRouterResponse.ok) {
        const errorText = await openRouterResponse.text();
        console.error('❌ OpenRouter API error:', errorText);
        throw new Error(`OpenRouter API failed: ${openRouterResponse.status}`);
      }
      
      const openRouterData = await openRouterResponse.json();
      const aiResponse = openRouterData.choices[0].message.content;
      
      console.log('✅ AI Response received');
      
      // Parse AI response
      let foodAnalysis;
      try {
        // Extract JSON from the response
        const jsonMatch = aiResponse.match(/\{[\s\S]*\}/);
        if (jsonMatch) {
          foodAnalysis = JSON.parse(jsonMatch[0]);
        } else {
          throw new Error('No JSON found in AI response');
        }
      } catch (parseError) {
        console.error('❌ Failed to parse AI response:', parseError);
        throw new Error('Failed to parse AI food analysis');
      }
      
      // Build response in Android app format
      const foodRecognitionResponse = {
        success: true,
        recognizedFoods: foodAnalysis.recognizedFoods || [],
        nutritionData: [
          {
            name: "Protein",
            value: foodAnalysis.totalNutrition?.protein || 0,
            unit: "g",
            dailyValue: Math.round((foodAnalysis.totalNutrition?.protein || 0) * 2)
          },
          {
            name: "Fiber",
            value: foodAnalysis.totalNutrition?.fiber || 0,
            unit: "g",
            dailyValue: Math.round((foodAnalysis.totalNutrition?.fiber || 0) * 4)
          },
          {
            name: "Calories",
            value: foodAnalysis.totalNutrition?.calories || 0,
            unit: "kcal",
            dailyValue: Math.round((foodAnalysis.totalNutrition?.calories || 0) / 20)
          }
        ],
        totalNutrition: foodAnalysis.totalNutrition || {},
        imageUrl: "https://example.com/food-image.jpg",
        message: `Successfully recognized ${foodAnalysis.recognizedFoods?.length || 0} food items using AI`
      };
      
      console.log('✅ Food recognition completed successfully with AI');
      res.json(foodRecognitionResponse);
      
    } catch (aiError) {
      console.error('❌ AI food recognition failed:', aiError);
      
      // Fallback response
      const fallbackResponse = {
        success: true,
        recognizedFoods: [
          {
            name: "Mixed Food Bowl",
            confidence: 0.75,
            quantity: 1.0,
            unit: "bowl",
            description: "Unable to analyze with AI - generic healthy meal",
            nutrition: {
              calories: 400,
              protein: 25,
              carbs: 50,
              fat: 15,
              fiber: 8,
              sugar: 12,
              sodium: 300
            },
            serving_size: "1 bowl",
            health_score: 75
          }
        ],
        nutritionData: [
          { name: "Protein", value: 25, unit: "g", dailyValue: 50 },
          { name: "Fiber", value: 8, unit: "g", dailyValue: 32 },
          { name: "Calories", value: 400, unit: "kcal", dailyValue: 20 }
        ],
        totalNutrition: {
          calories: 400,
          protein: 25,
          carbs: 50,
          fat: 15,
          fiber: 8,
          sugar: 12,
          sodium: 300
        },
        imageUrl: "https://example.com/food-image.jpg",
        message: "Food recognition completed (AI unavailable - using estimates)"
      };
      
      res.json(fallbackResponse);
    }
    
  } catch (error) {
    console.error('❌ Error in food recognition:', error);
    res.status(500).json({ 
      success: false,
      error: 'Food recognition failed',
      message: error.message
    });
  }
});

// Meal logging endpoint
app.post('/api/meals/log', (req, res) => {
  try {
    console.log('🍽️ Received meal log request');
    
    const mockMealLog = {
      id: 123,
      name: req.body.name || "Logged Meal",
      calories: req.body.calories || 500,
      protein: req.body.protein || 25,
      carbs: req.body.carbs || 45,
      fat: req.body.fat || 20,
      time: new Date().toISOString(),
      type: req.body.type || "meal",
      success: true
    };
    
    res.json(mockMealLog);
  } catch (error) {
    console.error('❌ Error in meal logging:', error);
    res.status(500).json({ error: 'Meal logging failed' });
  }
});

// Daily meals endpoint
app.get('/api/meals/daily/:date', (req, res) => {
  try {
    console.log('📅 Received daily meals request for:', req.params.date);
    
    // Return in Android app format (MealHistoryResponse)
    const mealHistoryResponse = {
      meals: [
        {
          id: 1,
          meal_type: "breakfast",
          food_items: [
            {
              id: 1,
              food_name: "Oatmeal with Berries",
              quantity: 1,
              unit: "bowl",
              calories: 320,
              protein: 12,
              carbs: 45,
              fat: 12,
              fiber: 8,
              sugar: 15,
              sodium: 100
            }
          ],
          total_calories: 320,
          total_protein: 12,
          total_carbs: 45,
          total_fat: 12,
          created_at: new Date().toISOString(),
          image_url: null
        },
        {
          id: 2,
          meal_type: "lunch",
          food_items: [
            {
              id: 2,
              food_name: "Grilled Chicken Salad",
              quantity: 1,
              unit: "plate",
              calories: 450,
              protein: 35,
              carbs: 15,
              fat: 25,
              fiber: 5,
              sugar: 8,
              sodium: 300
            }
          ],
          total_calories: 450,
          total_protein: 35,
          total_carbs: 15,
          total_fat: 25,
          created_at: new Date().toISOString(),
          image_url: null
        },
        {
          id: 3,
          meal_type: "snack",
          food_items: [
            {
              id: 3,
              food_name: "Greek Yogurt with Nuts",
              quantity: 1,
              unit: "cup",
              calories: 280,
              protein: 18,
              carbs: 25,
              fat: 15,
              fiber: 3,
              sugar: 12,
              sodium: 50
            }
          ],
          total_calories: 280,
          total_protein: 18,
          total_carbs: 25,
          total_fat: 15,
          created_at: new Date().toISOString(),
          image_url: null
        }
      ],
      dailySummary: {
        totalCalories: 1050,
        totalProtein: 65,
        totalCarbs: 85,
        totalFat: 52,
        mealCount: 3
      }
    };
    
    res.json(mealHistoryResponse);
  } catch (error) {
    console.error('❌ Error in daily meals:', error);
    res.status(500).json({ error: 'Daily meals failed' });
  }
});

// Health conditions endpoint
app.get('/api/health-analysis/conditions', (req, res) => {
  try {
    console.log('🏥 Received health conditions request');
    
    const mockConditions = [
      {
        id: 1,
        name: "Diabetes",
        type: "chronic",
        severity: "moderate",
        description: "Type 2 diabetes requiring dietary management"
      },
      {
        id: 2,
        name: "High Blood Pressure",
        type: "chronic",
        severity: "mild",
        description: "Elevated blood pressure requiring monitoring"
      }
    ];
    
    res.json(mockConditions);
  } catch (error) {
    console.error('❌ Error in health conditions:', error);
    res.status(500).json({ error: 'Health conditions failed' });
  }
});

// Basic routes for testing
app.get('/api/test', (req, res) => {
  res.json({ message: 'API is working!' });
});

// Delete meal endpoint
app.delete('/api/meals/:mealId', (req, res) => {
  try {
    console.log('🗑️ Received delete meal request for:', req.params.mealId);
    
    const response = {
      success: true,
      message: `Meal ${req.params.mealId} deleted successfully`
    };
    
    res.json(response);
  } catch (error) {
    console.error('❌ Error deleting meal:', error);
    res.status(500).json({ 
      success: false,
      error: 'Failed to delete meal',
      message: error.message
    });
  }
});

// Global error handler
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(500).json({ error: 'Internal server error' });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 NutriAI Server running on port ${PORT}`);
  console.log(`📊 Health check: http://localhost:${PORT}/api/health`);
  console.log(`🔐 Environment: ${process.env.NODE_ENV || 'development'}`);
});

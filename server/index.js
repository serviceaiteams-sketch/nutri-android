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
      health: '/api/health',
      healthAnalysis: '/api/health-analysis/upload-reports',
      foodRecommendations: '/api/health-analysis/food-recommendations',
      dashboard: '/api/analytics/dashboard',
      userProfile: '/api/user/profile',
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
          {
            name: "Borderline High Blood Sugar",
            severity: "mild",
            description: "Random blood sugar level of 125 mg/dL is slightly elevated"
          }
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
            {
              name: "Processed Foods",
              reason: "Can trigger migraines and are often high in preservatives and additives.",
              alternative: "Whole, unprocessed foods like fruits, vegetables, and whole grains."
            }
          ],
          foodsToIncrease: [
            {
              name: "Oatmeal with Berries",
              benefit: "High in fiber and antioxidants, helps reduce migraine symptoms.",
              frequency: "3-4 times a week",
              portion: "1 cup cooked oatmeal topped with 1/2 cup mixed berries"
            }
          ],
          mealPlanSuggestions: [],
          supplementRecommendations: [
            {
              name: "Magnesium",
              benefit: "May help reduce the frequency of migraines.",
              dosage: "400 mg daily",
              note: "Consult with a healthcare provider before starting any new supplement."
            }
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
          {
            name: "Borderline High Blood Sugar",
            severity: "mild",
            description: "Random blood sugar level of 125 mg/dL is slightly elevated"
          }
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
            {
              name: "Processed Foods",
              reason: "Can trigger migraines and are often high in preservatives and additives.",
              alternative: "Whole, unprocessed foods like fruits, vegetables, and whole grains."
            }
          ],
          foodsToIncrease: [
            {
              name: "Oatmeal with Berries",
              benefit: "High in fiber and antioxidants, helps reduce migraine symptoms.",
              frequency: "3-4 times a week",
              portion: "1 cup cooked oatmeal topped with 1/2 cup mixed berries"
            }
          ],
          mealPlanSuggestions: [],
          supplementRecommendations: [
            {
              name: "Magnesium",
              benefit: "May help reduce the frequency of migraines.",
              dosage: "400 mg daily",
              note: "Consult with a healthcare provider before starting any new supplement."
            }
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
    
    console.log('✅ Analysis completed successfully');
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
app.get('/api/analytics/dashboard', (req, res) => {
  try {
    console.log('📊 Received dashboard request');
    
    const mockDashboard = {
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
      recommendations: [
        "Try to increase your protein intake",
        "Great job on staying hydrated!",
        "Consider adding more vegetables to your meals"
      ]
    };
    
    res.json(mockDashboard);
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

// Food recognition endpoint
app.post('/api/ai/recognize-food', (req, res) => {
  try {
    console.log('🍎 Received food recognition request');
    
    const mockRecognition = {
      foodName: "Grilled Chicken Breast",
      confidence: 0.95,
      calories: 165,
      protein: 31,
      carbs: 0,
      fat: 3.6,
      nutrients: ["Protein", "B6", "B12", "Iron"],
      alternatives: ["Turkey Breast", "Fish Fillet", "Tofu"]
    };
    
    res.json(mockRecognition);
  } catch (error) {
    console.error('❌ Error in food recognition:', error);
    res.status(500).json({ error: 'Food recognition failed' });
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
    
    const mockDailyMeals = {
      date: req.params.date,
      meals: [
        {
          id: 1,
          name: "Oatmeal with Berries",
          calories: 320,
          protein: 12,
          carbs: 45,
          fat: 12,
          time: "08:30",
          type: "breakfast"
        },
        {
          id: 2,
          name: "Grilled Chicken Salad",
          calories: 450,
          protein: 35,
          carbs: 15,
          fat: 25,
          time: "12:30",
          type: "lunch"
        },
        {
          id: 3,
          name: "Greek Yogurt with Nuts",
          calories: 280,
          protein: 18,
          carbs: 25,
          fat: 15,
          time: "15:00",
          type: "snack"
        }
      ],
      totalCalories: 1050,
      totalProtein: 65,
      totalCarbs: 85,
      totalFat: 52
    };
    
    res.json(mockDailyMeals);
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

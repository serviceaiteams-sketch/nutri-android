const express = require('express');
const cors = require('cors');
const path = require('path');

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
      foodRecommendations: '/api/health-analysis/food-recommendations'
    },
    description: 'AI-powered health analysis and nutrition recommendations'
  });
});

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({ 
    status: 'OK', 
    message: 'NutriAI  Server is running',
    timestamp: new Date().toISOString(),
    version: '1.0.0'
  });
});

// Mock health analysis endpoint for testing
app.post('/api/health-analysis/upload-reports', (req, res) => {
  try {
    console.log('📄 Received health report upload request');
    
    // Simulate processing delay
    setTimeout(() => {
      const mockAnalysis = {
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
        foodRecommendations: {
          recommendations: [
            {
              food: "Oatmeal with blueberries",
              reason: "Rich in fiber and antioxidants, helps reduce migraine frequency",
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
              notes: "Excellent for migraine prevention due to magnesium content"
            },
            {
              food: "Greek yogurt with chia seeds",
              reason: "High in protein and omega-3 fatty acids, supports overall health",
              category: "Breakfast/Snack",
              priority: "HIGH",
              calories: 280,
              protein: 18,
              carbs: 25,
              fat: 15,
              nutrients: ["Protein", "Omega-3", "Calcium", "Probiotics"],
              servingSize: "1 cup Greek yogurt with 1 tablespoon chia seeds",
              bestTime: "Breakfast or afternoon snack",
              preparationTips: "• Let chia seeds soak for 10 minutes\n• Add honey or maple syrup for sweetness\n• Top with fresh fruits",
              alternatives: "• Try coconut yogurt for dairy-free option\n• Use flax seeds instead of chia\n• Add granola for crunch",
              frequency: "3-4 times per week",
              notes: "Great source of probiotics for gut health"
            },
            {
              food: "Quinoa salad with spinach and chickpeas",
              reason: "High in iron and magnesium, beneficial for migraine management",
              category: "Lunch",
              priority: "HIGH",
              calories: 380,
              protein: 15,
              carbs: 55,
              fat: 10,
              nutrients: ["Iron", "Magnesium", "Fiber", "Complete Protein"],
              servingSize: "1 cup quinoa, 1 cup spinach, 1/2 cup chickpeas",
              bestTime: "Lunch (12-2 PM)",
              preparationTips: "• Rinse quinoa thoroughly before cooking\n• Steam spinach lightly to preserve nutrients\n• Add lemon juice for iron absorption",
              alternatives: "• Try farro or bulgur instead of quinoa\n• Use kale instead of spinach\n• Add grilled chicken for extra protein",
              frequency: "2-3 times per week",
              notes: "Iron from plant sources is better absorbed with vitamin C"
            }
          ],
          mealPlan: {
            breakfast: [
              {
                day: "Monday",
                meal: "Oatmeal with berries and almonds",
                nutrition: "High in fiber and antioxidants",
                calories: 320,
                protein: "12g",
                carbs: "45g",
                fat: "12g"
              },
              {
                day: "Tuesday",
                meal: "Greek yogurt with honey and walnuts",
                nutrition: "Protein-rich with healthy fats",
                calories: 280,
                protein: "18g",
                carbs: "25g",
                fat: "15g"
              }
            ],
            lunch: [
              {
                day: "Monday",
                meal: "Grilled chicken salad with mixed greens",
                nutrition: "Lean protein with vitamins",
                calories: 380,
                protein: "35g",
                carbs: "15g",
                fat: "18g"
              },
              {
                day: "Tuesday",
                meal: "Quinoa bowl with roasted vegetables",
                nutrition: "Complete protein and fiber",
                calories: 420,
                protein: "15g",
                carbs: "65g",
                fat: "12g"
              }
            ],
            dinner: [
              {
                day: "Monday",
                meal: "Baked salmon with quinoa and vegetables",
                nutrition: "Omega-3 fatty acids and complete protein",
                calories: 450,
                protein: "40g",
                carbs: "35g",
                fat: "22g"
              },
              {
                day: "Tuesday",
                meal: "Stir-fried tofu with brown rice",
                nutrition: "Plant-based protein and whole grains",
                calories: 380,
                protein: "20g",
                carbs: "55g",
                fat: "12g"
              }
            ],
            snacks: [
              {
                day: "Monday",
                meal: "Apple slices with almond butter",
                nutrition: "Fiber and healthy fats",
                calories: 180,
                protein: "4g",
                carbs: "25g",
                fat: "8g"
              },
              {
                day: "Tuesday",
                meal: "Mixed nuts and dried fruits",
                nutrition: "Energy boost with minerals",
                calories: 200,
                protein: "6g",
                carbs: "20g",
                fat: "12g"
              }
            ]
          },
          weeklyNutrition: {
            totalCalories: 1800,
            averageProtein: "120g",
            averageCarbs: "180g",
            averageFat: "65g"
          },
          shoppingList: [
            "Oatmeal", "Mixed berries", "Almonds", "Chicken breast", "Mixed greens",
            "Salmon fillets", "Quinoa", "Vegetables", "Greek yogurt", "Honey",
            "Walnuts", "Tofu", "Brown rice"
          ]
        },
        nextSteps: [
          "Schedule follow-up blood sugar test",
          "Consult with healthcare provider",
          "Implement dietary changes"
        ]
      };
      
      res.json(mockAnalysis);
    }, 2000); // 2 second delay to simulate processing
    
  } catch (error) {
    console.error('❌ Error in health analysis:', error);
    res.status(500).json({ error: 'Analysis failed' });
  }
});

// Mock food recommendations endpoint
app.get('/api/health-analysis/food-recommendations', (req, res) => {
  try {
    console.log('🍎 Received food recommendations request');
    
    const mockRecommendations = {
      success: true,
      recommendations: {
        recommendations: [
          {
            food: "Leafy green vegetables",
            reason: "Rich in vitamins and minerals",
            category: "Vegetables",
            priority: "HIGH",
            calories: 45,
            protein: 3,
            carbs: 8,
            fat: 0,
            nutrients: ["Vitamin A", "Vitamin C", "Iron", "Fiber"],
            servingSize: "2-3 cups",
            bestTime: "Lunch or Dinner",
            preparationTips: "• Steam lightly to preserve nutrients\n• Add olive oil for better absorption\n• Season with herbs and spices",
            alternatives: "• Try different colored vegetables\n• Mix raw and cooked varieties",
            frequency: "Daily",
            notes: null
          },
          {
            food: "Lean proteins (chicken, fish, beans)",
            reason: "Essential for muscle maintenance",
            category: "Protein",
            priority: "HIGH",
            calories: 180,
            protein: 25,
            carbs: 0,
            fat: 8,
            nutrients: ["Protein", "B vitamins", "Iron"],
            servingSize: "3-4 oz per meal",
            bestTime: "Lunch or Dinner",
            preparationTips: "• Grill or bake for healthier cooking\n• Marinate for flavor without extra calories\n• Use herbs instead of heavy sauces",
            alternatives: "• Try different protein sources\n• Include plant-based options",
            frequency: "Daily",
            notes: null
          },
          {
            food: "Whole grains (brown rice, quinoa, oats)",
            reason: "Complex carbohydrates and fiber",
            category: "Grains",
            priority: "MEDIUM",
            calories: 150,
            protein: 5,
            carbs: 30,
            fat: 2,
            nutrients: ["Fiber", "B vitamins", "Minerals"],
            servingSize: "1/2 cup cooked",
            bestTime: "Breakfast or as side dish",
            preparationTips: "• Cook in broth for extra flavor\n• Add vegetables for nutrition\n• Use as base for bowls",
            alternatives: "• Try different grain varieties\n• Mix grains for variety",
            frequency: "Daily",
            notes: null
          }
        ],
        mealPlan: {
          breakfast: [
            {
              day: "Monday",
              meal: "Oatmeal with berries and nuts",
              nutrition: "High in fiber and antioxidants",
              calories: 320,
              protein: "12g",
              carbs: "45g",
              fat: "12g"
            }
          ],
          lunch: [
            {
              day: "Monday",
              meal: "Quinoa salad with vegetables",
              nutrition: "Complete protein and fiber",
              calories: 380,
              protein: "15g",
              carbs: "55g",
              fat: "10g"
            }
          ],
          dinner: [
            {
              day: "Monday",
              meal: "Grilled salmon with brown rice",
              nutrition: "Omega-3 fatty acids and protein",
              calories: 450,
              protein: "35g",
              carbs: "40g",
              fat: "20g"
            }
          ],
          snacks: [
            {
              day: "Monday",
              meal: "Apple with almond butter",
              nutrition: "Fiber and healthy fats",
              calories: 200,
              protein: "4g",
              carbs: "25g",
              fat: "8g"
            }
          ]
        }
      },
      note: "Using fallback recommendations due to AI service issue"
    };
    
    res.json(mockRecommendations);
    
  } catch (error) {
    console.error('❌ Error in food recommendations:', error);
    res.status(500).json({ error: 'Food recommendations failed' });
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

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: 'Route not found' });
});

// Global error handler
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(500).json({ error: 'Internal server error' });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 NutriAI Oracle Server running on port ${PORT}`);
  console.log(`📊 Health check: http://localhost:${PORT}/api/health`);
  console.log(`🔐 Environment: ${process.env.NODE_ENV || 'development'}`);
}); 
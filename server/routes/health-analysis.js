const express = require('express');
const router = express.Router();
const multer = require('multer');
const path = require('path');
const { authenticateToken } = require('../middleware/auth');
const { runQuery, getAll, getRow } = require('../config/database');

// Configure multer for file uploads
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, path.join(__dirname, '../uploads/health-reports/'));
  },
  filename: function (req, file, cb) {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    cb(null, file.fieldname + '-' + uniqueSuffix + path.extname(file.originalname));
  }
});

const upload = multer({
  storage: storage,
  limits: {
    fileSize: 10 * 1024 * 1024 // 10MB limit
  },
  fileFilter: (req, file, cb) => {
    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'];
    if (allowedTypes.includes(file.mimetype)) {
      cb(null, true);
    } else {
      cb(new Error('Invalid file type. Only PDF, JPG, and PNG files are allowed.'), false);
    }
  }
});

// Create uploads directory if it doesn't exist
const fs = require('fs');
const uploadDir = path.join(__dirname, '../uploads/health-reports/');
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

// Initialize health analysis tables
const initializeHealthTables = async () => {
  try {
    // Health reports table
    await runQuery(`
      CREATE TABLE IF NOT EXISTS health_reports (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        filename TEXT NOT NULL,
        file_path TEXT NOT NULL,
        file_type TEXT NOT NULL,
        file_size INTEGER NOT NULL,
        uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        analyzed_at DATETIME,
        analysis_result TEXT,
        FOREIGN KEY (user_id) REFERENCES users (id)
      )
    `);

    // Health conditions table
    await runQuery(`
      CREATE TABLE IF NOT EXISTS health_conditions (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        condition_name TEXT NOT NULL,
        diagnosed_date DATE,
        severity TEXT DEFAULT 'mild',
        medications TEXT,
        symptoms TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users (id)
      )
    `);

    // Health metrics table
    await runQuery(`
      CREATE TABLE IF NOT EXISTS health_metrics (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        metric_name TEXT NOT NULL,
        metric_value REAL NOT NULL,
        metric_unit TEXT,
        status TEXT DEFAULT 'normal',
        recorded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users (id)
      )
    `);

    // Health alerts table
    await runQuery(`
      CREATE TABLE IF NOT EXISTS health_alerts (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        alert_type TEXT NOT NULL,
        message TEXT NOT NULL,
        metric_name TEXT,
        severity TEXT DEFAULT 'medium',
        is_read BOOLEAN DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users (id)
      )
    `);

    console.log('✅ Health analysis tables initialized');
    
    // Migration: Add missing columns to existing tables
    try {
      // Check if health_conditions table has symptoms column
      const healthConditionsColumns = await getAll("PRAGMA table_info(health_conditions)");
      const hasSymptoms = healthConditionsColumns.some(c => c.name === 'symptoms');
      if (!hasSymptoms) {
        await runQuery('ALTER TABLE health_conditions ADD COLUMN symptoms TEXT');
        console.log('🛠️ Added missing health_conditions.symptoms column');
      }
    } catch (error) {
      console.log('Migration completed (some columns may already exist)');
    }
  } catch (error) {
    console.error('❌ Error initializing health tables:', error);
  }
};

initializeHealthTables();

// Upload health reports
router.post('/upload-reports', authenticateToken, upload.array('reports', 10), async (req, res) => {
  try {
    const userId = req.user.id;
    const files = req.files;
    const healthConditions = JSON.parse(req.body.healthConditions || '[]');

    if (!files || files.length === 0) {
      return res.status(400).json({ error: 'No files uploaded' });
    }

    const uploadedReports = [];

    for (const file of files) {
      const result = await runQuery(
        'INSERT INTO health_reports (user_id, filename, file_path, file_type, file_size) VALUES (?, ?, ?, ?, ?)',
        [userId, file.originalname, file.path, file.mimetype, file.size]
      );

      uploadedReports.push({
        id: result.lastID,
        filename: file.originalname,
        fileType: file.mimetype,
        fileSize: file.size,
        uploadedAt: new Date().toISOString()
      });
    }

    // Save health conditions
    for (const condition of healthConditions) {
      if (condition.condition) {
        await runQuery(
          'INSERT INTO health_conditions (user_id, condition_name, diagnosed_date, severity, medications) VALUES (?, ?, ?, ?, ?)',
          [
            userId,
            condition.condition,
            condition.diagnosedDate,
            condition.severity,
            JSON.stringify(condition.medications || [])
          ]
        );
      }
    }

    res.json({
      success: true,
      message: `${files.length} report(s) uploaded successfully`,
      reports: uploadedReports
    });
  } catch (error) {
    console.error('Error uploading health reports:', error);
    res.status(500).json({ error: 'Failed to upload health reports' });
  }
});

// Analyze health reports with AI
router.post('/analyze-reports', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    
    // Get user's uploaded reports
    const reports = await getAll(
      'SELECT * FROM health_reports WHERE user_id = ? ORDER BY uploaded_at DESC',
      [userId]
    );

    if (reports.length === 0) {
      return res.status(400).json({ error: 'No health reports found for analysis' });
    }

    // Get user's health conditions
    const conditions = await getAll(
      'SELECT * FROM health_conditions WHERE user_id = ?',
      [userId]
    );

    // Simulate AI analysis (in a real implementation, this would call an AI service)
    const analysisResult = await performAIAnalysis(reports, conditions);

    // Update reports with analysis results
    for (const report of reports) {
      await runQuery(
        'UPDATE health_reports SET analyzed_at = ?, analysis_result = ? WHERE id = ?',
        [new Date().toISOString(), JSON.stringify(analysisResult), report.id]
      );
    }

    res.json(analysisResult);
  } catch (error) {
    console.error('Error analyzing health reports:', error);
    res.status(500).json({ error: 'Failed to analyze health reports' });
  }
});

// ------------------- Health Conditions APIs -------------------

// List current user's health conditions
router.get('/conditions', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const rows = await getAll(
      `SELECT id, condition_name as condition, diagnosed_date as diagnosedDate, severity, medications, symptoms, created_at as createdAt
       FROM health_conditions WHERE user_id = ? ORDER BY created_at DESC`,
      [userId]
    );
    const parsed = rows.map(r => ({
      ...r,
      medications: r.medications ? JSON.parse(r.medications) : [],
      symptoms: r.symptoms ? JSON.parse(r.symptoms) : []
    }));
    res.json({ success: true, conditions: parsed });
  } catch (error) {
    console.error('Error fetching conditions:', error);
    res.status(500).json({ error: 'Failed to fetch conditions' });
  }
});

// Replace all conditions for the user (bulk upsert via replace strategy)
router.post('/conditions/bulk', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const { conditions = [] } = req.body;
    await runQuery('DELETE FROM health_conditions WHERE user_id = ?', [userId]);
    for (const c of conditions) {
      if (!c || !c.condition) continue;
      await runQuery(
        `INSERT INTO health_conditions (user_id, condition_name, diagnosed_date, severity, medications, symptoms)
         VALUES (?, ?, ?, ?, ?, ?)`,
        [
          userId,
          c.condition,
          c.diagnosedDate || null,
          c.severity || 'mild',
          JSON.stringify(c.medications || []),
          JSON.stringify(c.symptoms || [])
        ]
      );
    }
    const rows = await getAll(
      `SELECT id, condition_name as condition, diagnosed_date as diagnosedDate, severity, medications, symptoms, created_at as createdAt
       FROM health_conditions WHERE user_id = ? ORDER BY created_at DESC`,
      [userId]
    );
    const parsed = rows.map(r => ({
      ...r,
      medications: r.medications ? JSON.parse(r.medications) : [],
      symptoms: r.symptoms ? JSON.parse(r.symptoms) : []
    }));
    res.json({ success: true, conditions: parsed });
  } catch (error) {
    console.error('Error saving conditions:', error);
    res.status(500).json({ error: 'Failed to save conditions' });
  }
});

// Generate recommendations (foods and exercises) for given conditions
router.post('/conditions/recommendations', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const { conditions } = req.body;
    let list = conditions;
    if (!Array.isArray(list)) {
      list = await getAll(
        `SELECT condition_name as condition, severity FROM health_conditions WHERE user_id = ?`,
        [userId]
      );
    }
    const recs = await generateRecommendations(list || []);
    const normalized = Array.isArray(recs) ? recs : (recs && typeof recs === 'object' ? [recs] : []);
    res.json({ success: true, recommendations: normalized });
  } catch (error) {
    console.error('Error creating recommendations:', error);
    res.status(500).json({ error: 'Failed to generate recommendations' });
  }
});

// Get health metrics
router.get('/metrics', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    
    const metrics = await getAll(
      'SELECT * FROM health_metrics WHERE user_id = ? ORDER BY recorded_at DESC LIMIT 50',
      [userId]
    );

    res.json({ metrics });
  } catch (error) {
    console.error('Error fetching health metrics:', error);
    res.status(500).json({ error: 'Failed to fetch health metrics' });
  }
});

// Add health metric
router.post('/metrics', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const { metricName, metricValue, metricUnit, status } = req.body;

    const result = await runQuery(
      'INSERT INTO health_metrics (user_id, metric_name, metric_value, metric_unit, status) VALUES (?, ?, ?, ?, ?)',
      [userId, metricName, metricValue, metricUnit, status]
    );

    res.json({
      success: true,
      message: 'Health metric added successfully',
      metricId: result.lastID
    });
  } catch (error) {
    console.error('Error adding health metric:', error);
    res.status(500).json({ error: 'Failed to add health metric' });
  }
});

// Get health alerts
router.get('/alerts', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    
    const alerts = await getAll(
      'SELECT * FROM health_alerts WHERE user_id = ? ORDER BY created_at DESC LIMIT 20',
      [userId]
    );

    res.json({ alerts });
  } catch (error) {
    console.error('Error fetching health alerts:', error);
    res.status(500).json({ error: 'Failed to fetch health alerts' });
  }
});

// Mark alert as read
router.put('/alerts/:alertId/read', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const alertId = req.params.alertId;

    await runQuery(
      'UPDATE health_alerts SET is_read = 1 WHERE id = ? AND user_id = ?',
      [alertId, userId]
    );

    res.json({ success: true, message: 'Alert marked as read' });
  } catch (error) {
    console.error('Error marking alert as read:', error);
    res.status(500).json({ error: 'Failed to mark alert as read' });
  }
});

// Get health trends
router.get('/trends', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const { period = '30' } = req.query;

    const trends = await getAll(
      `SELECT 
        metric_name,
        metric_value,
        metric_unit,
        status,
        recorded_at
       FROM health_metrics 
       WHERE user_id = ? 
       AND recorded_at >= datetime('now', '-${period} days')
       ORDER BY recorded_at DESC`,
      [userId]
    );

    res.json({ trends });
  } catch (error) {
    console.error('Error fetching health trends:', error);
    res.status(500).json({ error: 'Failed to fetch health trends' });
  }
});

// Get health recommendations for user
router.get('/recommendations', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    
    // Get user's health conditions from database
    const conditions = await getAll(
      `SELECT condition_name as condition, severity FROM health_conditions WHERE user_id = ?`,
      [userId]
    );

    if (conditions.length === 0) {
      return res.json({
        recommendations: [],
        message: 'No health conditions found. Add health conditions to get personalized recommendations.'
      });
    }

    // Generate recommendations using AI or fallback
    const recommendations = await generateRecommendations(conditions);
    
    res.json({
      recommendations,
      conditions: conditions.map(c => ({ condition: c.condition, severity: c.severity })),
      message: 'Health recommendations generated successfully'
    });
  } catch (error) {
    console.error('Error generating health recommendations:', error);
    res.status(500).json({ error: 'Failed to generate health recommendations' });
  }
});

// Generate mock analysis when OpenAI is not available
function generateMockAnalysis(reports, conditions) {
  // Generate a realistic mock response
  const hasHighBloodPressure = conditions.some(c => 
    c.condition_name?.toLowerCase().includes('pressure') || 
    c.condition_name?.toLowerCase().includes('hypertension')
  );
  
  const hasDiabetes = conditions.some(c => 
    c.condition_name?.toLowerCase().includes('diabetes')
  );
  
  const healthScore = 75 - (conditions.length * 5);
  
  return {
    reportSummary: "Analysis of your uploaded health reports indicates overall good health with some areas that need attention. Your vital signs are mostly within normal ranges, though we've identified some markers that require monitoring.",
    
    detectedConditions: conditions.map(c => c.condition_name || c.condition),
    
    riskFactors: [
      ...(hasHighBloodPressure ? [{
        factor: "Elevated Blood Pressure",
        level: "medium",
        description: "Your blood pressure readings show mild elevation. Lifestyle modifications recommended."
      }] : []),
      ...(hasDiabetes ? [{
        factor: "Blood Sugar Levels",
        level: "high",
        description: "Your glucose levels require careful monitoring and dietary management."
      }] : []),
      {
        factor: "Cholesterol",
        level: "low",
        description: "Your cholesterol levels are within acceptable range."
      }
    ],
    
    healthScore: Math.max(40, Math.min(95, healthScore)),
    
    keyMetrics: {
      "Blood Pressure": {
        value: hasHighBloodPressure ? 135 : 120,
        unit: "mmHg",
        status: hasHighBloodPressure ? "high" : "normal",
        normalRange: "90-120 mmHg"
      },
      "Blood Sugar": {
        value: hasDiabetes ? 145 : 95,
        unit: "mg/dL",
        status: hasDiabetes ? "high" : "normal",
        normalRange: "70-100 mg/dL"
      },
      "Cholesterol": {
        value: 180,
        unit: "mg/dL",
        status: "normal",
        normalRange: "< 200 mg/dL"
      },
      "BMI": {
        value: 24.5,
        unit: "kg/m²",
        status: "normal",
        normalRange: "18.5-24.9"
      }
    },
    
    recommendations: [
      {
        category: "diet",
        recommendation: hasHighBloodPressure ? 
          "Reduce sodium intake to less than 2,300mg per day. Focus on DASH diet principles." :
          "Maintain a balanced diet rich in fruits, vegetables, and whole grains.",
        priority: hasHighBloodPressure ? "high" : "medium"
      },
      {
        category: "exercise",
        recommendation: "Engage in at least 150 minutes of moderate aerobic activity per week.",
        priority: "medium"
      },
      {
        category: "lifestyle",
        recommendation: "Ensure 7-8 hours of quality sleep daily and manage stress levels.",
        priority: "medium"
      },
      ...(conditions.length > 0 ? [{
        category: "medical",
        recommendation: "Continue regular check-ups with your healthcare provider to monitor existing conditions.",
        priority: "high"
      }] : [])
    ],
    
    nutritionGuidance: {
      foodsToAvoid: [
        ...(hasHighBloodPressure ? ["High-sodium foods", "Processed meats", "Canned soups"] : []),
        ...(hasDiabetes ? ["Sugary beverages", "White bread", "Processed snacks"] : []),
        "Trans fats", "Excessive alcohol"
      ],
      foodsToIncrease: [
        "Leafy greens", "Berries", "Fatty fish", "Nuts and seeds", "Whole grains"
      ],
      mealPlanSuggestions: [
        "Start your day with oatmeal topped with berries and nuts",
        "Include a colorful salad with every lunch",
        "Choose lean proteins like grilled chicken or fish for dinner"
      ],
      supplementRecommendations: [
        "Vitamin D (if deficient)",
        "Omega-3 fatty acids",
        ...(conditions.length > 0 ? ["Consult your doctor before starting any supplements"] : [])
      ]
    },
    
    nextSteps: [
      "Schedule a follow-up appointment with your primary care physician",
      "Start tracking your daily food intake and physical activity",
      "Monitor your vital signs regularly at home",
      "Consider joining a wellness program for additional support"
    ]
  };
}

// AI analysis function using ChatGPT API
async function performAIAnalysis(reports, conditions) {
  try {
    const OPENAI_API_KEY = process.env.OPENAI_API_KEY;
    if (!OPENAI_API_KEY) {
      console.log('⚠️ OpenAI API key not configured - returning mock analysis');
      return generateMockAnalysis(reports, conditions);
    }

    const axios = require('axios');
    
    // Use OpenRouter API
    const apiBase = process.env.OPENAI_API_BASE || 'https://api.openai.com/v1';
    
    // Prepare report data for analysis with content
    const reportData = [];
    for (const report of reports) {
      try {
        // Read file content if it exists
        const fs = require('fs');
        const path = require('path');
        let content = '';
        
        if (fs.existsSync(report.file_path)) {
          if (report.file_type === 'application/pdf') {
            // For PDFs, we'll extract text content
            const pdf = require('pdf-parse');
            const dataBuffer = fs.readFileSync(report.file_path);
            const pdfData = await pdf(dataBuffer);
            content = pdfData.text;
          } else if (report.file_type.startsWith('image/')) {
            // For images, we'll describe the file
            content = `Image file: ${report.filename}`;
          } else {
            // For other files, try to read as text
            content = fs.readFileSync(report.file_path, 'utf8');
          }
        }
        
        reportData.push({
          filename: report.filename,
          fileType: report.file_type,
          uploadedAt: report.uploaded_at,
          content: content.substring(0, 5000) // Limit content length
        });
      } catch (error) {
        console.log(`Could not read content for ${report.filename}:`, error.message);
        reportData.push({
          filename: report.filename,
          fileType: report.file_type,
          uploadedAt: report.uploaded_at,
          content: 'Content could not be extracted'
        });
      }
    }

    // Create a comprehensive prompt for health report analysis
    const prompt = `You are an expert medical AI assistant analyzing health reports. 

PATIENT CONTEXT:
- Health Conditions: ${conditions.map(c => `${c.condition_name} (${c.severity || 'mild'})`).join(', ') || 'None specified'}

REPORTS TO ANALYZE:
${reportData.map((report, index) => `
REPORT ${index + 1}: ${report.filename}
Type: ${report.fileType}
Content:
${report.content}
`).join('\n')}

TASK: Analyze the actual content of the uploaded health reports and provide a comprehensive medical analysis.

IMPORTANT GUIDELINES:
1. Analyze the ACTUAL LAB VALUES and results shown in the report content
2. Compare values to reference ranges when provided
3. If values are within normal ranges, clearly state "NORMAL" or "WITHIN NORMAL RANGE"
4. If a report shows normal values, do NOT diagnose conditions - state the results are normal
5. Only identify issues if values are clearly outside normal ranges
6. Be conservative - when in doubt, recommend consulting a healthcare provider
7. Focus on the actual lab results, not the patient's existing conditions

CRITICAL: Return ONLY pure JSON. Do NOT include any markdown formatting, backticks, or prose text.

Return this exact JSON structure:
{
  "totalReports": ${reports.length},
  "normalMetrics": number,
  "attentionNeeded": number,
  "findings": [
    {
      "type": "success|warning|danger|info",
      "title": "Finding title",
      "description": "Detailed description of the finding"
    }
  ],
  "recommendations": [
    {
      "title": "Recommendation title",
      "description": "Detailed recommendation",
      "action": "Suggested action"
    }
  ],
  "riskFactors": ["risk factor 1", "risk factor 2"],
  "nextSteps": ["next step 1", "next step 2"],
  "analysisSummary": "Overall summary of the analysis",
  "confidence": "high|medium|low",
  "limitations": ["limitation 1", "limitation 2"]
}

Remember: NO markdown, NO backticks, NO prose - ONLY the JSON object above.`;

    let response;
    try {
      response = await axios.post(`${apiBase}/chat/completions`, {
      model: 'gpt-4o-mini',
      messages: [{ role: 'user', content: prompt }],
      max_tokens: 2000,
      temperature: 0.2
    }, {
      headers: {
        'Authorization': `Bearer ${OPENAI_API_KEY}`,
          'Content-Type': 'application/json',
          'HTTP-Referer': 'http://localhost:5000', // Required for OpenRouter
          'X-Title': 'NutriAI Health Analysis' // Required for OpenRouter
        }
      });
    } catch (apiError) {
      console.error('❌ OpenRouter API call failed:', apiError.response?.status, apiError.message);
      if (apiError.response?.status === 402) {
        console.log('⚠️ Payment required (402) - using mock analysis as fallback');
        return generateMockAnalysis(reports, conditions);
      }
      throw apiError;
    }

    const content = response.data.choices[0].message.content;
    
    // Clean markdown formatting from AI response - handle all possible formats
    let cleanContent = content;
    
    // Remove all possible markdown code block formats
    if (content.includes('```json')) {
      cleanContent = content.replace(/```json\s*/, '').replace(/\s*```/, '');
    } else if (content.includes('```')) {
      cleanContent = content.replace(/```\s*/, '').replace(/\s*```/, '');
    }
    
    // Remove any remaining markdown artifacts
    cleanContent = cleanContent
      .replace(/^```\s*/, '')  // Remove leading ```
      .replace(/\s*```$/, '')  // Remove trailing ```
      .replace(/^`\s*/, '')    // Remove leading `
      .replace(/\s*`$/, '')    // Remove trailing `
      .trim();
    
    console.log('🔍 Raw AI response:', content.substring(0, 200) + '...');
    console.log('🧹 Cleaned content:', cleanContent.substring(0, 200) + '...');
    
    let aiAnalysis;
    try {
      aiAnalysis = JSON.parse(cleanContent);
      console.log('✅ JSON parsed successfully');
    } catch (parseError) {
      console.error('❌ AI analysis JSON parsing failed:', parseError.message);
      console.log('Raw AI response:', content);
      console.log('Cleaned content:', cleanContent);
      
      // Try to extract JSON from the middle if it's wrapped in other text
      try {
        const jsonMatch = content.match(/\{[\s\S]*\}/);
        if (jsonMatch) {
          console.log('🔄 Trying to extract JSON from middle...');
          const extractedJson = jsonMatch[0];
          aiAnalysis = JSON.parse(extractedJson);
          console.log('✅ JSON extracted and parsed successfully');
        } else {
          throw new Error('No JSON found in response');
        }
      } catch (extractError) {
        console.error('❌ JSON extraction also failed:', extractError.message);
        throw new Error('Invalid JSON response from AI - could not parse or extract');
      }
    }
    
    // Transform to match Android app expectations
    // Generate food recommendations based on the analysis
    let foodRecommendations = null;
    try {
      foodRecommendations = await generateFoodRecommendations(reports, conditions);
    } catch (error) {
      console.log('⚠️ Food recommendations failed, using fallback:', error.message);
      foodRecommendations = generateMockFoodRecommendations(conditions);
    }

    const analysis = {
      reportSummary: aiAnalysis.analysisSummary || 'Health analysis completed successfully. Review the detailed metrics and recommendations below.',
      detectedConditions: aiAnalysis.findings?.filter(f => f.type === 'danger').map(f => f.title) || [],
      riskFactors: aiAnalysis.riskFactors?.map(factor => ({
        factor: factor,
        level: 'high', // Set to high for risk factors
        description: `Identified risk factor: ${factor}`
      })) || [],
      healthScore: calculateHealthScore(aiAnalysis),
      keyMetrics: generateKeyMetrics(aiAnalysis),
      recommendations: aiAnalysis.recommendations?.map(rec => ({
        category: 'medical',
        recommendation: rec.description || rec.title || 'General health recommendation',
        priority: aiAnalysis.confidence === 'high' ? 'high' : 'medium'
      })) || [],
      nutritionGuidance: {
        foodsToAvoid: foodRecommendations?.foodsToAvoid?.map(item => 
          typeof item === 'string' ? item : item.name || item.food || 'Unknown food'
        ) || [],
        foodsToIncrease: foodRecommendations?.foodsToIncrease?.map(item => 
          typeof item === 'string' ? item : item.name || item.food || 'Unknown food'
        ) || [],
        mealPlanSuggestions: foodRecommendations?.mealPlanSuggestions || [],
        supplementRecommendations: foodRecommendations?.supplementRecommendations?.map(item => 
          typeof item === 'string' ? item : item.name || item.supplement || 'Unknown supplement'
        ) || []
      },
      foodRecommendations: {
        recommendations: foodRecommendations?.recommendations?.flatMap(category => 
          category.foods?.map(food => ({
            food: food.name || food.food || 'Unknown food',
            reason: food.benefit || food.reason || 'General health benefit',
            nutrients: food.nutrients || [],
            servingSize: food.portion || food.servingSize || 'Standard serving',
            frequency: food.frequency || 'As recommended'
          })) || []
        ) || [],
        mealPlan: {
          breakfast: foodRecommendations?.recommendations?.find(cat => cat.category === 'breakfast')?.foods?.map(f => f.name || f.food) || [],
          lunch: foodRecommendations?.recommendations?.find(cat => cat.category === 'lunch')?.foods?.map(f => f.name || f.food) || [],
          dinner: foodRecommendations?.recommendations?.find(cat => cat.category === 'dinner')?.foods?.map(f => f.name || f.food) || [],
          snacks: foodRecommendations?.recommendations?.find(cat => cat.category === 'snacks')?.foods?.map(f => f.name || f.food) || []
        }
      },
      nextSteps: aiAnalysis.nextSteps || ['Review the analysis results', 'Consult with healthcare provider if needed'],
      timestamp: new Date().toISOString(), // Add timestamp to force fresh data
      analysisId: `analysis_${Date.now()}` // Add unique ID to prevent caching
    };
    
    console.log('✅ AI analysis completed successfully');
    console.log('📤 Sending to Android:', JSON.stringify(analysis, null, 2));
    return analysis;
    
  } catch (error) {
    console.error('❌ AI analysis failed:', error.message);
    
    // Fallback to basic analysis in correct format
    return {
      reportSummary: 'Analysis could not be completed. Please consult your healthcare provider.',
      detectedConditions: [],
      riskFactors: [],
      healthScore: 50,
      keyMetrics: {
        'Analysis Status': {
          value: 0,
          unit: 'status',
          status: 'high',
          normalRange: 'Completed'
        }
      },
      recommendations: [
        {
          category: 'medical',
          recommendation: 'Please discuss your lab results with your doctor for proper interpretation.',
          priority: 'high'
        }
      ],
      nutritionGuidance: {
        foodsToAvoid: [],
        foodsToIncrease: [],
        mealPlanSuggestions: [],
        supplementRecommendations: []
      },
      nextSteps: ['Consult with healthcare provider']
    };
  }
}

// Generate health recommendations based on conditions
async function generateRecommendations(conditions) {
  try {
    // Try AI recommendations first
    const aiRecommendations = await generateAIRecommendations(conditions);
    if (aiRecommendations && aiRecommendations.length > 0) {
      return aiRecommendations;
    }
  } catch (error) {
    console.log('AI recommendations failed, using fallback:', error.message);
  }
  
  // Fallback to rule-based recommendations
  return generateFallbackRecommendations(conditions);
}

// Generate AI-based recommendations using OpenAI
async function generateAIRecommendations(conditions) {
  try {
    const OPENAI_API_KEY = process.env.OPENAI_API_KEY;
    if (!OPENAI_API_KEY) {
      throw new Error('OpenAI API key not configured');
    }

    const axios = require('axios');
    const prompt = `You are an expert medical AI assistant specializing in personalized health recommendations. 

PATIENT CONDITIONS: ${conditions.map(c => `${c.condition} (${c.severity || 'mild'})`).join(', ') || 'No specific conditions'}

TASK: Provide comprehensive, evidence-based recommendations for each condition.

IMPORTANT GUIDELINES:
1. Be conservative and safety-focused
2. Always recommend consulting healthcare providers for medical decisions
3. Provide practical, actionable advice
4. Consider interactions between multiple conditions
5. Focus on lifestyle modifications and preventive care

Return STRICT JSON in this exact schema (no prose outside JSON):
[
  {
    "condition": "condition name",
    "severity": "severity level",
    "treatment": ["clinical advice", "when to see doctor", "important tests"],
    "diet": { 
      "include": ["foods to include"], 
      "avoid": ["foods to limit/avoid"] 
    },
    "care": ["self-care steps", "monitoring reminders", "lifestyle changes"],
    "exerciseRecommendations": ["exercise recommendations"],
    "monitoringMetrics": ["key metrics to track"],
    "safetyConsiderations": ["safety warnings", "red flags to watch for"]
  }
]`;

    // Use OpenRouter API
    const apiBase = process.env.OPENAI_API_BASE || 'https://api.openai.com/v1';
    
    const response = await axios.post(`${apiBase}/chat/completions`, {
      model: 'gpt-4o-mini',
      messages: [{ role: 'user', content: prompt }],
      max_tokens: 1000,
      temperature: 0.3
    }, {
      headers: {
        'Authorization': `Bearer ${OPENAI_API_KEY}`,
        'Content-Type': 'application/json',
        'HTTP-Referer': 'http://localhost:5000', // Required for OpenRouter
        'X-Title': 'NutriAI Health Recommendations' // Required for OpenRouter
      }
    });

    const content = response.data.choices[0].message.content;
    
    // Clean markdown formatting from AI response - handle all possible formats
    let cleanContent = content;
    
    // Remove all possible markdown code block formats
    if (content.includes('```json')) {
      cleanContent = content.replace(/```json\s*/, '').replace(/\s*```/, '');
    } else if (content.includes('```')) {
      cleanContent = content.replace(/```\s*/, '').replace(/\s*```/, '');
    }
    
    // Remove any remaining markdown artifacts
    cleanContent = cleanContent
      .replace(/^```\s*/, '')  // Remove leading ```
      .replace(/\s*```$/, '')  // Remove trailing ```
      .replace(/^`\s*/, '')    // Remove leading `
      .replace(/\s*`$/, '')    // Remove trailing `
      .trim();
    
    console.log('🔍 Raw AI recommendation response:', content.substring(0, 200) + '...');
    console.log('🧹 Cleaned recommendation content:', cleanContent.substring(0, 200) + '...');
    
    try {
      const result = JSON.parse(cleanContent);
      console.log('✅ AI recommendation JSON parsed successfully');
      return result;
    } catch (parseError) {
      console.error('❌ AI recommendation JSON parsing failed:', parseError.message);
      console.log('Raw AI response:', content);
      console.log('Cleaned content:', cleanContent);
      
      // Try to extract JSON from the middle if it's wrapped in other text
      try {
        const jsonMatch = content.match(/\[[\s\S]*\]/);
        if (jsonMatch) {
          console.log('🔄 Trying to extract JSON array from middle...');
          const extractedJson = jsonMatch[0];
          const result = JSON.parse(extractedJson);
          console.log('✅ AI recommendation JSON extracted and parsed successfully');
          return result;
        } else {
          throw new Error('No JSON array found in response');
        }
      } catch (extractError) {
        console.error('❌ AI recommendation JSON extraction also failed:', extractError.message);
        throw new Error('Invalid JSON response from AI recommendations - could not parse or extract');
      }
    }
  } catch (error) {
    console.error('AI recommendation generation failed:', error);
    throw error;
  }
}

// Generate fallback rule-based recommendations
function generateFallbackRecommendations(conditions) {
  const recommendations = [];
  
  for (const condition of conditions) {
    const conditionName = (condition.condition || '').toLowerCase();
    const severity = condition.severity || 'mild';
    
    const recommendation = {
      condition: condition.condition,
      severity: severity,
      // keep old fields for backward compatibility
      foodRecommendations: [],
      exerciseRecommendations: [],
      supplementRecommendations: [],
      lifestyleRecommendations: [],
      monitoringMetrics: [],
      safetyConsiderations: [],
      // new rich schema
      treatment: [],
      diet: { include: [], avoid: [] },
      care: []
    };

    if (conditionName.includes('diabetes')) {
      recommendation.treatment = [
        'Consult your physician for individualized targets; continue prescribed medications',
        'Aim for HbA1c < 7% unless advised otherwise',
        'Annual eye, foot, and kidney screening'
      ];
      recommendation.diet.include = [
        'Low glycemic index carbohydrates (millets, oats, legumes)',
        'High-fiber vegetables and salads',
        'Lean protein: eggs, fish, tofu, dals'
      ];
      recommendation.diet.avoid = [
        'Sugary drinks and sweets',
        'Refined flour snacks, deep-fried foods',
        'Large late-night meals'
      ];
      recommendation.care = [
        'Monitor fasting and post-meal glucose routinely',
        'Walk 10–20 minutes after meals',
        'Maintain sleep 7–8 hours and manage stress'
      ];
      recommendation.exerciseRecommendations = [
        '150 minutes/week brisk walking or cycling',
        '2–3 days/week resistance training'
      ];
      recommendation.monitoringMetrics = ['Fasting glucose', 'Post-prandial glucose', 'HbA1c', 'Blood pressure'];
    } else if (conditionName.includes('hypertension') || conditionName.includes('high blood pressure')) {
      recommendation.treatment = [
        'Check BP at home (validated device); record morning/evening',
        'Continue prescribed antihypertensives; do not stop abruptly',
        'Review with clinician if BP persistently >140/90'
      ];
      recommendation.diet.include = ['DASH-style meals', 'Potassium-rich foods (banana, coconut water, leafy greens)', 'Unsalted nuts, seeds'];
      recommendation.diet.avoid = ['High-salt pickles, papads, chips', 'Processed/instant foods', 'Excessive caffeine and alcohol'];
      recommendation.care = ['30–45 min moderate exercise most days', 'Weight management and stress reduction', 'Limit added salt < 5 g/day'];
      recommendation.exerciseRecommendations = ['Brisk walking', 'Yoga/meditation, breathing exercises'];
      recommendation.monitoringMetrics = ['Daily blood pressure', 'Sodium intake'];
    } else if (conditionName.includes('wound')) {
      recommendation.treatment = [
        'Keep the wound clean and dry; follow dressing protocol',
        'Seek medical care for signs of infection (increasing pain, redness, pus, fever)',
        'Tetanus prophylaxis if indicated'
      ];
      recommendation.diet.include = ['High-protein foods (eggs, paneer, fish, dals)', 'Vitamin C sources (amla, citrus, capsicum)', 'Zinc sources (seeds, nuts, whole grains)', 'Adequate hydration'];
      recommendation.diet.avoid = ['Excess sugar and ultra-processed snacks', 'Alcohol and smoking'];
      recommendation.care = ['Gentle movement to promote circulation as advised', 'Proper wound dressing hygiene', 'Adequate sleep for recovery'];
      recommendation.exerciseRecommendations = ['Light mobility as tolerated', 'Avoid strain on affected area'];
      recommendation.monitoringMetrics = ['Temperature', 'Redness/swelling increase', 'Pain scale'];
      recommendation.supplementRecommendations = ['Vitamin C 500 mg/day if dietary intake is low (consult clinician)', 'Zinc 15–30 mg/day short-term if deficient'];
    } else {
      // General wellness default
      recommendation.treatment = ['Annual preventive check-up', 'Address deficiencies if detected'];
      recommendation.diet.include = ['Fruits and vegetables (5+ servings/day)', 'Adequate protein each meal', 'Healthy fats (nuts, seeds, olive/mustard oil)'];
      recommendation.diet.avoid = ['Sugary beverages', 'Ultra-processed snacks', 'Excess salt'];
      recommendation.care = ['7–9 hours sleep', 'Stress management', 'Regular activity 30+ min/day'];
      recommendation.exerciseRecommendations = ['150 minutes moderate exercise/week', '2–3 sessions strength training/week'];
      recommendation.monitoringMetrics = ['Weight/BMI trend', 'Waist circumference'];
    }

    // maintain backward compatibility fields
    recommendation.foodRecommendations = recommendation.diet.include.slice(0, 6);
    recommendation.lifestyleRecommendations = recommendation.care.slice(0, 6);

    recommendations.push(recommendation);
  }
  
  return recommendations;
}

// Generate food recommendations based on health reports
router.get('/food-recommendations', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    
    // Get user's health reports and conditions
    const reports = await getAll(
      'SELECT * FROM health_reports WHERE user_id = ? ORDER BY uploaded_at DESC',
      [userId]
    );
    
    const conditions = await getAll(
      'SELECT * FROM health_conditions WHERE user_id = ?',
      [userId]
    );

    // Set a timeout for the AI request
    const timeoutPromise = new Promise((_, reject) => {
      setTimeout(() => reject(new Error('Request timeout')), 30000); // 30 seconds
    });

    // Generate AI-powered food recommendations with timeout
    const foodRecommendationsPromise = generateFoodRecommendations(reports, conditions);
    const foodRecommendations = await Promise.race([foodRecommendationsPromise, timeoutPromise]);
    
    // Transform the data structure to match Android expectations
    const transformedRecommendations = {
      recommendations: foodRecommendations.recommendations?.flatMap(category => 
        category.foods?.map(food => ({
          food: food.name || food.food || 'Unknown food',
          reason: food.benefit || food.reason || 'General health benefit',
          nutrients: food.nutrients || [],
          servingSize: food.portion || food.servingSize || 'Standard serving',
          frequency: food.frequency || 'As recommended'
        })) || []
      ) || [],
      mealPlan: {
        breakfast: foodRecommendations.recommendations?.find(cat => cat.category === 'breakfast')?.foods?.map(f => f.name || f.food) || [],
        lunch: foodRecommendations.recommendations?.find(cat => cat.category === 'lunch')?.foods?.map(f => f.name || f.food) || [],
        dinner: foodRecommendations.recommendations?.find(cat => cat.category === 'dinner')?.foods?.map(f => f.name || f.food) || [],
        snacks: foodRecommendations.recommendations?.find(cat => cat.category === 'snacks')?.foods?.map(f => f.name || f.food) || []
      }
    };
    
    res.json({
      success: true,
      recommendations: transformedRecommendations
    });
  } catch (error) {
    console.error('Error generating food recommendations:', error);
    
    // Return fallback recommendations if AI fails
    const fallbackRecommendations = {
      recommendations: [
        {
          food: "Leafy green vegetables",
          reason: "Rich in vitamins and minerals",
          nutrients: ["Vitamin A", "Vitamin C", "Iron", "Fiber"],
          servingSize: "2-3 cups",
          frequency: "Daily"
        },
        {
          food: "Lean proteins (chicken, fish, beans)",
          reason: "Essential for muscle maintenance",
          nutrients: ["Protein", "B vitamins", "Iron"],
          servingSize: "3-4 oz per meal",
          frequency: "Daily"
        },
        {
          food: "Whole grains (brown rice, quinoa, oats)",
          reason: "Complex carbohydrates and fiber",
          nutrients: ["Fiber", "B vitamins", "Minerals"],
          servingSize: "1/2 cup cooked",
          frequency: "Daily"
        }
      ],
      mealPlan: {
        breakfast: ["Oatmeal with berries", "Greek yogurt with nuts"],
        lunch: ["Quinoa salad", "Grilled chicken with vegetables"],
        dinner: ["Salmon with brown rice", "Lentil soup"],
        snacks: ["Apple with peanut butter", "Carrot sticks with hummus"]
      }
    };
    
    res.json({
      success: true,
      recommendations: fallbackRecommendations,
      note: "Using fallback recommendations due to AI service issue"
    });
  }
});

// Generate mock food recommendations when AI is not available
function generateMockFoodRecommendations(conditions) {
  const hasHighBloodPressure = conditions.some(c => 
    c.condition_name?.toLowerCase().includes('pressure') || 
    c.condition_name?.toLowerCase().includes('hypertension')
  );
  
  const hasDiabetes = conditions.some(c => 
    c.condition_name?.toLowerCase().includes('diabetes')
  );
  
  return {
    recommendations: [
      {
        food: "Leafy Greens (Spinach, Kale)",
        reason: hasHighBloodPressure ? 
          "Rich in potassium and magnesium, helps lower blood pressure naturally" :
          "Packed with vitamins, minerals, and antioxidants for overall health",
        nutrients: ["Vitamin K", "Folate", "Iron", "Calcium"],
        servingSize: "2 cups raw or 1 cup cooked",
        frequency: "Daily"
      },
      {
        food: hasDiabetes ? "Quinoa" : "Brown Rice",
        reason: hasDiabetes ?
          "Low glycemic index grain that helps stabilize blood sugar" :
          "Whole grain providing sustained energy and fiber",
        nutrients: ["Fiber", "Protein", "B Vitamins"],
        servingSize: "1/2 cup cooked",
        frequency: "3-4 times per week"
      },
      {
        food: "Fatty Fish (Salmon, Mackerel)",
        reason: "Omega-3 fatty acids support heart health and reduce inflammation",
        nutrients: ["Omega-3", "Vitamin D", "Protein"],
        servingSize: "3-4 oz",
        frequency: "2-3 times per week"
      },
      {
        food: "Berries (Blueberries, Strawberries)",
        reason: "Low in sugar, high in antioxidants and fiber",
        nutrients: ["Antioxidants", "Vitamin C", "Fiber"],
        servingSize: "1 cup",
        frequency: "Daily"
      },
      {
        food: "Nuts and Seeds",
        reason: "Healthy fats and protein for heart health",
        nutrients: ["Healthy Fats", "Protein", "Magnesium"],
        servingSize: "1 oz (handful)",
        frequency: "Daily"
      }
    ],
    mealPlan: {
      breakfast: [
        "Oatmeal with berries and nuts",
        "Greek yogurt with seeds and fruit",
        "Whole grain toast with avocado"
      ],
      lunch: [
        "Quinoa salad with mixed vegetables",
        "Grilled chicken with leafy greens",
        "Lentil soup with whole grain bread"
      ],
      dinner: [
        "Baked salmon with steamed vegetables",
        "Stir-fried tofu with brown rice",
        "Grilled chicken with sweet potato"
      ],
      snacks: [
        "Apple slices with almond butter",
        "Mixed nuts and seeds",
        "Vegetable sticks with hummus"
      ]
    }
  };
}

// Generate AI-powered food recommendations
async function generateFoodRecommendations(reports, conditions) {
  try {
    const OPENAI_API_KEY = process.env.OPENAI_API_KEY;
    if (!OPENAI_API_KEY) {
      console.log('⚠️ OpenAI API key not configured - returning mock food recommendations');
      return generateMockFoodRecommendations(conditions);
    }

    const axios = require('axios');
    
    // Create a simple prompt based on report count and conditions
    const reportCount = reports.length;
    const conditionText = conditions.map(c => `${c.condition_name} (${c.severity || 'mild'})`).join(', ') || 'None specified';
    
    const prompt = `You are an expert nutritionist and dietitian. Generate personalized food recommendations based on the following information:

PATIENT CONTEXT:
- Health Conditions: ${conditionText}
- Number of Health Reports: ${reportCount}

TASK: Generate personalized food recommendations that support overall health and address any specific conditions.

IMPORTANT GUIDELINES:
1. Focus on foods that support the specific health conditions identified
2. Recommend foods for general health and prevention
3. Include specific foods, not just general categories
4. Consider cultural preferences and practical meal planning
5. Be specific about portion sizes and frequency
6. Focus on iron-rich foods if iron levels are mentioned

Return STRICT JSON in this exact schema (no prose outside JSON):
{
  "recommendations": [
    {
      "category": "breakfast",
      "foods": [
        {
          "name": "food name",
          "benefit": "specific health benefit",
          "frequency": "how often to consume",
          "portion": "recommended portion size"
        }
      ]
    }
  ],
  "generalGuidelines": [
    "general dietary guideline 1",
    "general dietary guideline 2"
  ],
  "foodsToLimit": [
    {
      "name": "food name",
      "reason": "why to limit",
      "alternative": "healthier alternative"
    }
  ],
  "supplements": [
    {
      "name": "supplement name",
      "benefit": "health benefit",
      "dosage": "recommended dosage",
      "note": "important note"
    }
  ]
}`;

    console.log('🚀 Calling OpenRouter API for food recommendations...');
    
    // Use OpenRouter API
    const apiBase = process.env.OPENAI_API_BASE || 'https://api.openai.com/v1';
    
    const response = await axios.post(`${apiBase}/chat/completions`, {
      model: 'gpt-4o-mini',
      messages: [{ role: 'user', content: prompt }],
      max_tokens: 2000,
      temperature: 0.3
    }, {
      headers: {
        'Authorization': `Bearer ${OPENAI_API_KEY}`,
        'Content-Type': 'application/json',
        'HTTP-Referer': 'http://localhost:5000', // Required for OpenRouter
        'X-Title': 'NutriAI Food Recommendations' // Required for OpenRouter
      },
      timeout: 10000 // 10 second timeout
    });

    console.log('✅ OpenRouter API response received');
    const content = response.data.choices[0].message.content;
    
    // Try to extract JSON from markdown code blocks if present
    let jsonContent = content;
    if (content.includes('```json')) {
      const jsonMatch = content.match(/```json\s*([\s\S]*?)\s*```/);
      if (jsonMatch) {
        jsonContent = jsonMatch[1];
      }
    } else if (content.includes('```')) {
      const jsonMatch = content.match(/```\s*([\s\S]*?)\s*```/);
      if (jsonMatch) {
        jsonContent = jsonMatch[1];
      }
    }
    
    try {
      const result = JSON.parse(jsonContent);
      console.log('✅ Food recommendations parsed successfully');
      return result;
    } catch (parseError) {
      console.error('❌ Failed to parse AI response as JSON:', parseError);
      console.log('Raw AI response:', content);
      throw new Error('Invalid JSON response from AI');
    }
    
  } catch (error) {
    console.error('❌ AI food recommendation generation failed:', error.message);
    
    // Fallback recommendations
    return {
      recommendations: [
        {
          category: "general",
          foods: [
            {
              name: "Leafy green vegetables",
              benefit: "Rich in vitamins and minerals",
              frequency: "Daily",
              portion: "2-3 cups"
            },
            {
              name: "Lean proteins (chicken, fish, beans)",
              benefit: "Essential amino acids for muscle health",
              frequency: "Daily",
              portion: "3-4 oz per meal"
            },
            {
              name: "Whole grains (brown rice, quinoa, oats)",
              benefit: "Complex carbohydrates and fiber",
              frequency: "Daily",
              portion: "1/2 cup cooked"
            }
          ]
        }
      ],
      generalGuidelines: [
        "Eat a balanced diet with plenty of fruits and vegetables",
        "Stay hydrated with water throughout the day",
        "Limit processed foods and added sugars",
        "Include protein with every meal"
      ],
      foodsToLimit: [
        {
          name: "Processed foods",
          reason: "High in sodium and unhealthy fats",
          alternative: "Fresh, whole foods"
        },
        {
          name: "Added sugars",
          reason: "Can contribute to weight gain and diabetes",
          alternative: "Natural sweeteners like honey or fruit"
        }
      ],
      supplements: [
        {
          name: "Multivitamin",
          benefit: "General health support",
          dosage: "1 tablet daily",
          note: "Consult with healthcare provider"
        }
      ]
    };
  }
}

// Helper function to calculate health score based on analysis
function calculateHealthScore(aiAnalysis) {
  try {
    console.log('🔍 Calculating health score for:', JSON.stringify(aiAnalysis, null, 2));
    
    // Count findings by type
    let normalCount = 0;
    let warningCount = 0;
    let dangerCount = 0;
    
    if (aiAnalysis.findings && Array.isArray(aiAnalysis.findings)) {
      aiAnalysis.findings.forEach(finding => {
        switch (finding.type) {
          case 'success':
            normalCount++;
            break;
          case 'warning':
            warningCount++;
            break;
          case 'danger':
            dangerCount++;
            break;
          default:
            normalCount++; // Default to normal
        }
      });
    }
    
    // Count risk factors
    const riskFactorCount = aiAnalysis.riskFactors ? aiAnalysis.riskFactors.length : 0;
    
    // Calculate total issues (warnings + dangers + risk factors)
    const totalIssues = warningCount + dangerCount + riskFactorCount;
    const totalMetrics = normalCount + totalIssues;
    
    console.log(`📊 Health Score Calculation:
      - Normal findings: ${normalCount}
      - Warning findings: ${warningCount}
      - Danger findings: ${dangerCount}
      - Risk factors: ${riskFactorCount}
      - Total issues: ${totalIssues}
      - Total metrics: ${totalMetrics}`);
    
    if (totalMetrics === 0) {
      console.log('⚠️ No metrics found, returning default score: 50');
      return 50; // Default score
    }
    
    // Calculate base score (higher score = better health)
    let baseScore = Math.round((normalCount / totalMetrics) * 100);
    
    // Penalize for issues
    if (dangerCount > 0) {
      baseScore = Math.max(0, baseScore - (dangerCount * 20)); // -20 points per danger
    }
    if (warningCount > 0) {
      baseScore = Math.max(0, baseScore - (warningCount * 10)); // -10 points per warning
    }
    if (riskFactorCount > 0) {
      baseScore = Math.max(0, baseScore - (riskFactorCount * 15)); // -15 points per risk factor
    }
    
    // Adjust based on confidence
    if (aiAnalysis.confidence === 'high') {
      baseScore = Math.min(100, baseScore + 5);
    } else if (aiAnalysis.confidence === 'low') {
      baseScore = Math.max(0, baseScore - 5);
    }
    
    const finalScore = Math.max(0, Math.min(100, baseScore));
    console.log(`🎯 Final health score: ${finalScore}`);
    
    return finalScore;
  } catch (error) {
    console.error('❌ Error calculating health score:', error);
    return 50; // Default score
  }
}

// Get food recommendations for the current user
router.get('/food-recommendations', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    
    // Get user's uploaded reports
    const reports = await getAll(
      'SELECT * FROM health_reports WHERE user_id = ? ORDER BY uploaded_at DESC',
      [userId]
    );

    if (reports.length === 0) {
      return res.status(400).json({ error: 'No health reports found for recommendations' });
    }

    // Get user's health conditions
    const conditions = await getAll(
      'SELECT * FROM health_conditions WHERE user_id = ?',
      [userId]
    );

    // Generate food recommendations
    const foodRecommendations = await generateFoodRecommendations(reports, conditions);
    
    res.json(foodRecommendations);
  } catch (error) {
    console.error('Error getting food recommendations:', error);
    res.status(500).json({ error: 'Failed to get food recommendations' });
  }
});

// Helper function to generate key metrics from analysis
function generateKeyMetrics(aiAnalysis) {
  try {
    console.log('🔍 Generating key metrics for:', JSON.stringify(aiAnalysis, null, 2));
    const metrics = {};
    
    // Add findings as metrics
    if (aiAnalysis.findings && Array.isArray(aiAnalysis.findings)) {
      aiAnalysis.findings.forEach((finding, index) => {
        const metricName = finding.title || `Finding ${index + 1}`;
        let status = 'normal';
        let value = 1;
        
        switch (finding.type) {
          case 'success':
            status = 'normal';
            value = 1;
            break;
          case 'warning':
            status = 'warning';
            // Extract numeric value from description if available
            const numericMatch = finding.description?.match(/(\d+\.?\d*)/);
            value = numericMatch ? parseFloat(numericMatch[1]) : 1;
            break;
          case 'danger':
            status = 'high';
            // Extract numeric value from description if available
            const dangerMatch = finding.description?.match(/(\d+\.?\d*)/);
            value = dangerMatch ? parseFloat(dangerMatch[1]) : 1;
            break;
          default:
            status = 'normal';
            value = 1;
        }
        
        // Determine unit and normal range based on metric type
        let unit = 'status';
        let normalRange = 'Normal';
        
        if (metricName.toLowerCase().includes('glucose') || metricName.toLowerCase().includes('sugar')) {
          unit = 'mg/dL';
          normalRange = '70-99 mg/dL';
        } else if (metricName.toLowerCase().includes('iron') || metricName.toLowerCase().includes('ferritin')) {
          unit = 'ng/mL';
          normalRange = '15-247 ng/mL';
        } else if (metricName.toLowerCase().includes('creatinine') || metricName.toLowerCase().includes('acr')) {
          unit = 'mg/g';
          normalRange = '0-29 mg/g';
        } else if (metricName.toLowerCase().includes('protein') || metricName.toLowerCase().includes('albumin')) {
          unit = 'g/L';
          normalRange = '<0.15 g/L';
        }
        
        metrics[metricName] = {
          value: value,
          unit: unit,
          status: status,
          normalRange: normalRange
        };
      });
    }
    
    // Add risk factors as metrics
    if (aiAnalysis.riskFactors && Array.isArray(aiAnalysis.riskFactors)) {
      aiAnalysis.riskFactors.forEach((factor, index) => {
        const metricName = `Risk Factor: ${factor}`;
        metrics[metricName] = {
          value: 1,
          unit: 'presence',
          status: 'high',
          normalRange: 'None'
        };
      });
    }
    
    // Add summary metrics
    if (aiAnalysis.totalReports) {
      metrics['Total Reports Analyzed'] = {
        value: aiAnalysis.totalReports,
        unit: 'count',
        status: 'normal',
        normalRange: '1+'
      };
    }
    
    console.log('📊 Generated metrics:', Object.keys(metrics));
    return metrics;
  } catch (error) {
    console.error('❌ Error generating key metrics:', error);
    return {};
  }
}

module.exports = router; 
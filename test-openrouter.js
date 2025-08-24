const axios = require('axios');
require('dotenv').config();

const OPENAI_API_KEY = process.env.OPENAI_API_KEY;
const OPENAI_API_BASE = process.env.OPENAI_API_BASE || 'https://api.openai.com/v1';

console.log('🔍 Testing OpenRouter Integration');
console.log('==================================');
console.log('API Key:', OPENAI_API_KEY ? `Present (${OPENAI_API_KEY.length} chars)` : 'MISSING!');
console.log('API Base:', OPENAI_API_BASE);

if (!OPENAI_API_KEY) {
  console.error('\n❌ ERROR: No API key found!');
  console.log('\nPlease create a .env file with:');
  console.log('OPENAI_API_KEY=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');
  process.exit(1);
}

async function testOpenRouter() {
  try {
    console.log('\n📡 Testing connection to OpenRouter...');
    
    const response = await axios.post(
      `${OPENAI_API_BASE}/chat/completions`,
      {
        model: 'gpt-4o-mini',
        messages: [
          {
            role: 'user',
            content: 'Say "Hello from OpenRouter!" if you can read this.'
          }
        ],
        temperature: 0.1
      },
      {
        headers: {
          'Authorization': `Bearer ${OPENAI_API_KEY}`,
          'Content-Type': 'application/json',
          'HTTP-Referer': 'http://localhost:5000',
          'X-Title': 'NutriAI Oracle Test'
        }
      }
    );

    console.log('✅ Success! OpenRouter is working.');
    console.log('Response:', response.data.choices[0].message.content);
    
  } catch (error) {
    console.error('❌ Failed to connect to OpenRouter:');
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Error:', error.response.data);
    } else {
      console.error('Error:', error.message);
    }
  }
}

testOpenRouter();

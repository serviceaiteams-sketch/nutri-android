const axios = require('axios');

async function testHerokuDeployment() {
  const herokuUrl = 'https://nutri-ai-5b9893ad4a00.herokuapp.com/api/health';
  
  try {
    console.log('🔍 Testing Heroku deployment...');
    console.log(`📡 URL: ${herokuUrl}`);
    
    const response = await axios.get(herokuUrl);
    
    console.log('✅ Success!');
    console.log('📊 Status:', response.status);
    console.log('📄 Response:', response.data);
    
  } catch (error) {
    console.log('❌ Error:', error.message);
    if (error.response) {
      console.log('📊 Status:', error.response.status);
      console.log('📄 Response:', error.response.data);
    }
  }
}

testHerokuDeployment();

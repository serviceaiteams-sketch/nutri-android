const https = require('https');

// Your OpenRouter API key (replace with your actual key)
const OPENROUTER_API_KEY = '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'; // Replace with your actual key

async function testOpenRouterAPI() {
    console.log('🔍 Testing OpenRouter API Key...');
    
    const data = JSON.stringify({
        model: 'gpt-4o-mini',
        messages: [
            {
                role: 'user',
                content: 'Say "Hello! OpenRouter API is working!" if you can read this message.'
            }
        ],
        max_tokens: 50
    });

    const options = {
        hostname: 'openrouter.ai',
        port: 443,
        path: '/api/v1/chat/completions',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Content-Length': data.length,
            'Authorization': `Bearer ${OPENROUTER_API_KEY}`,
            'HTTP-Referer': 'https://nutri-ai-5b9893ad4a00.herokuapp.com',
            'X-Title': 'NutriAI Test'
        }
    };

    return new Promise((resolve, reject) => {
        const req = https.request(options, (res) => {
            let responseData = '';
            
            res.on('data', (chunk) => {
                responseData += chunk;
            });
            
            res.on('end', () => {
                console.log('📡 Response Status:', res.statusCode);
                console.log('📡 Response Headers:', res.headers);
                
                if (res.statusCode === 200) {
                    try {
                        const response = JSON.parse(responseData);
                        console.log('✅ OpenRouter API is working!');
                        console.log('🤖 AI Response:', response.choices[0].message.content);
                        resolve(true);
                    } catch (error) {
                        console.log('❌ Failed to parse response:', error.message);
                        console.log('📄 Raw response:', responseData);
                        reject(error);
                    }
                } else if (res.statusCode === 401) {
                    console.log('❌ API Key is invalid or expired');
                    console.log('📄 Response:', responseData);
                    reject(new Error('Invalid API key'));
                } else if (res.statusCode === 402) {
                    console.log('❌ Payment required - API key needs credits');
                    console.log('📄 Response:', responseData);
                    reject(new Error('Payment required'));
                } else {
                    console.log('❌ API call failed with status:', res.statusCode);
                    console.log('📄 Response:', responseData);
                    reject(new Error(`HTTP ${res.statusCode}`));
                }
            });
        });

        req.on('error', (error) => {
            console.log('❌ Request failed:', error.message);
            reject(error);
        });

        req.write(data);
        req.end();
    });
}

// Run the test
testOpenRouterAPI()
    .then(() => {
        console.log('🎉 OpenRouter API test completed successfully!');
        process.exit(0);
    })
    .catch((error) => {
        console.log('💥 OpenRouter API test failed:', error.message);
        process.exit(1);
    });

require('dotenv').config();

console.log('🔍 Environment Variables Check:');
console.log('================================');
console.log('PORT:', process.env.PORT || 'NOT SET');
console.log('NODE_ENV:', process.env.NODE_ENV || 'NOT SET');
console.log('JWT_SECRET:', process.env.JWT_SECRET ? '✓ SET (' + process.env.JWT_SECRET.length + ' chars)' : '✗ NOT SET');
console.log('OPENAI_API_KEY:', process.env.OPENAI_API_KEY ? '✓ SET (' + process.env.OPENAI_API_KEY.length + ' chars)' : '✗ NOT SET');
console.log('OPENAI_API_BASE:', process.env.OPENAI_API_BASE || 'NOT SET');

if (process.env.OPENAI_API_KEY) {
  console.log('\nAPI Key Preview:', process.env.OPENAI_API_KEY.substring(0, 15) + '...');
}

console.log('\n📁 .env file exists:', require('fs').existsSync('.env') ? 'YES' : 'NO');

if (require('fs').existsSync('.env')) {
  console.log('\n📄 .env file contents:');
  console.log('--------------------');
  console.log(require('fs').readFileSync('.env', 'utf8'));
}

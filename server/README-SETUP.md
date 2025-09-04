# NutriAI Server Setup

## Quick Start

1. **Copy the environment template:**
   ```bash
   cp env.template .env
   ```

2. **Edit the .env file and add your OpenAI API key:**
   - Replace `your-openai-api-key-here` with your actual OpenAI API key
   - Get your API key from: https://platform.openai.com/account/api-keys

3. **Install dependencies:**
   ```bash
   npm install
   ```

4. **Start the server:**
   ```bash
   npm start
   ```

## Environment Variables

- `NODE_ENV`: Set to 'development' or 'production'
- `PORT`: Server port (default: 5000)
- `CLIENT_URL`: Frontend URL
- `JWT_SECRET`: Secret key for JWT tokens
- `OPENAI_API_KEY`: Your OpenAI API key (required for AI features)

## Security Note

Never commit your `.env` file to version control. It contains sensitive information like API keys.

# Environment Variables Setup

This project uses environment variables to manage sensitive configuration data securely.

## Setup Instructions

### 1. Create your `.env` file

Copy the `.env.example` file to create your own `.env` file:

```bash
cp .env.example .env
```

### 2. Update the values

Open the `.env` file and replace the placeholder values with your actual credentials:

- **Database Configuration**: Update `DB_USERNAME` and `DB_PASSWORD` with your MySQL credentials
- **JWT Secret**: Generate a secure random string (at least 256 bits) for `JWT_SECRET`
- **Hugging Face API**: Add your actual API key for `HUGGINGFACE_API_KEY`
- **Email Configuration**: Add your SMTP credentials for `MAIL_USERNAME` and `MAIL_PASSWORD`
- **Razorpay**: Add your Razorpay API credentials for `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET`
- **Redis**: Update if using different host/port

### 3. Security Notes

- ✅ The `.env` file is already added to `.gitignore` and will NOT be committed to version control
- ✅ Never commit sensitive credentials to the repository
- ✅ Share the `.env.example` file with your team, not the `.env` file
- ✅ Each developer should create their own `.env` file with their credentials

### 4. How it works

The application loads environment variables from the `.env` file at startup using the `dotenv-java` library. These variables are then referenced in `application.properties` using the `${VARIABLE_NAME}` syntax.

### 5. Production Deployment

For production environments, set environment variables directly in your hosting platform (e.g., AWS, Heroku, Docker) instead of using a `.env` file.

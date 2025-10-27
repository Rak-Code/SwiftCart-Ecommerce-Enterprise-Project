

## 🧠 SwiftCart ChatBot Backend (Spring Boot + Groq API)

This project integrates a **Spring Boot backend** with the **Groq LLM API** (OpenAI-compatible).
It provides an `/api/chat` endpoint that sends user messages to Groq’s free-tier models and returns AI-generated responses.

---

### 🚀 Features

* Connects to Groq’s OpenAI-style endpoint
* Free-tier friendly (no paid Grok/xAI credits needed)
* Simple JSON-based request/response format
* Compatible with React frontend (`localhost:5173`)

---

### 🧩 Requirements

* **Java 17+**
* **Spring Boot 3.x**
* **Internet access**
* **Groq API key** (Free to create)

---

### 🔑 Get Your API Key

1. Go to [https://console.groq.com/](https://console.groq.com/)
2. Sign up (it’s free).
3. Navigate to **API Keys → Create New Key**.
4. Copy your key — you’ll need it for the backend setup.

---

### ⚙️ Environment Setup

#### 1️⃣ Set your API key

You can use environment variables or a `.env` file.

For example:

**Windows (PowerShell):**

```powershell
$env:GORK_API_KEY="your_groq_api_key_here"
```

**Linux/Mac:**

```bash
export GORK_API_KEY=your_groq_api_key_here
```

#### 2️⃣ Configure `application.properties`

```properties
server.port=8080

# Groq Configuration
groq.api.key=${GORK_API_KEY}
groq.model=llama-3.1-8b-instant
```

---

### 💬 API Endpoint

**POST** → `http://localhost:8080/api/chat`

**Request JSON:**

```json
{
  "message": "Hello, how are you?"
}
```

**Response JSON:**

```json
{
  "response": "I'm great! How can I help you today?"
}
```

---

### 🧱 Project Structure

```
src/
 ├─ main/java/com/example/backend/
 │   ├─ controller/ChatController.java     → REST API layer
 │   ├─ services/GroqChatService.java      → Service calling Groq API
 │   └─ model/
 │        ├─ ChatRequest.java              → Request DTO
 │        └─ ChatResponse.java             → Response DTO
 └─ resources/
     └─ application.properties
```

---

### ⚡ How It Works

1. The frontend sends a POST request to `/api/chat`.
2. `ChatController` passes the user message to `GroqChatService`.
3. The service sends it to `https://api.groq.com/openai/v1/chat/completions`.
4. Groq returns the model’s generated message.
5. The backend wraps it in a `ChatResponse` and returns it to the frontend.

---

### ✅ Example Run

**Request:**

```bash
curl -X POST http://localhost:8080/api/chat \
     -H "Content-Type: application/json" \
     -d '{"message": "Tell me a fun fact!"}'
```

**Response:**

```json
{"response": "Honey never spoils — archaeologists found 3,000-year-old honey still edible!"}
```

---

### 🧾 Notes

* Uses the **Groq Free Tier** — no billing needed.
* Supported models are listed at [https://console.groq.com/docs/models](https://console.groq.com/docs/models).
* Replace `llama-3.1-8b-instant` with another model if needed.
* If you get `429 Too Many Requests`, you’ve hit the rate limit; try again after cooldown.


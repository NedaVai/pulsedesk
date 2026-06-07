# PulseDesk

PulseDesk is a Spring BOOT REST API that accepts user comments, uses Hugging Face AI API to analyse them and then creates a support ticket if a real issue is described.

## Built with:

- Java 25 and Spring Boot 4.0.6
- Spring Data JPA and a H2 in-memory database
- Hugging Face Inference API (meta-llama/Llama-3.1-8B-Instruct via nscale provider)
- Simple frontend with HTML, CSS and JavaScript.

## Setup Instructions

1. Clone the repository

2. Ensure prerequisites are installed:
   - Java 25
   - Maven

3. Get a Hugging Face API token (if you don't have one):
   - Go to huggingface.co and create a free account
   - Go to Settings, then Access Tokens and  New Token
   - Select Read, give it a name and copy the token

4. Configure environment variable in IntelliJ:
   Open: Run -> Edit Configurations -> PulsedeskApplication
   In **Environment Variables**:
   - Click the browse button
   - Click **+**
   - Add:
     - Name: HUGGINGFACE_API_KEY
     - Value: your actual Hugging Face API token
   - Click **Apply** and **OK**

5. Run the application:
   Run PulsedeskApplication.java directly from IntelliJ

6. Open the application:
   Visit http://localhost:8080

> Note: These instructions are for IntelliJ IDEA. If you're using a different IDE,
> first ensure the HUGGINGFACE_API_KEY environment variable is set before running.

## Notes

- API keys are managed via environment variables.
- H2 console is enabled for review purposes and available at http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:pulsedeskdb, username: sa, no password)
- The database is in memory and resets on every restart.
- Getters and setters in code were written explicitly for clarity and readability.
- hf-inference provider no longer supports mistralai/Mistral-7B-Instruct, so meta-llama/Llama-3.1-8B-Instruct was used instead.

## Known Limitations
- System.out.println was used for reponse logging instead of SLF4J. It was kept intentionally for visibility during review.
- Ai response parsing uses String searching rather than a JSON library. It works reliably for the current response format, but could still be improved.

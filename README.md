# Resume Analyser - Run Instructions

## Prerequisites
1.  **Java JDK 17+**: Ensure Java is installed and `JAVA_HOME` is set.
2.  **Maven**: Required to build and run the project.
3.  **MySQL Database**:
    - Ensure MySQL is running on `localhost:3306`.
    - Create a database named `resume_db`:
      ```sql
      CREATE DATABASE resume_db;
      ```
    - Default credentials in `application.properties`:
        - Username: `root`
        - Password: `2001`
    - Update `src/main/resources/application.properties` if your credentials differ.

## Configuration
Before running, ensuring you have set your Gemini API key:
- Open `src/main/resources/application.properties`
- Replace `YOUR_GEMINI_API_KEY_HERE` with your actual key.

## How to Run

### Option 1: Using Maven (Command Line)
Navigate to the project root directory and run:
```bash
mvn spring-boot:run
```

### Option 2: Using IDE (IntelliJ / Eclipse / VS Code)
1.  Open `src/main/java/com/resume/analyser/ResumeAnalyserApplication.java`.
2.  Click the **Run** button (usually a green play icon) next to the `main` method.

## Access the Application
Once the application starts, the backend will be available at:
`http://localhost:8080`

### Frontend
The frontend is a React + Vite application located in the `frontend` directory.

1.  Navigate to the frontend directory:
    ```bash
    cd frontend
    ```
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Run the development server:
    ```bash
    npm run dev
    ```
4.  Access the frontend at: `http://localhost:5173`

# Slant Game - Algorithmic Project

Welcome to the Slant Game, a Java implementation of the popular logic puzzle from Simon Tatham's Puzzle Collection. This project demonstrates the application of key algorithm design paradigms including **Divide & Conquer**, **Greedy Algorithms**, and **Sorting**.

## 🎮 How to Play
The objective is to fill a grid with diagonal lines (slants) such that:
1.  Every clue number (0-4) has exactly that many lines connecting to it.
2.  There are no closed loops.

### Controls
- **Left Click**: Place a forward slant (`/`)
- **Right Click**: Place a backward slant (`\`)
- **Click Again**: Toggle or remove slant.

---

## 🚀 How to Run

### Prerequisites
- Java JDK 8 or higher installed.

### 1. Compile the Project
Open your terminal in the project root folder and run:
```bash
cd Slant
javac -d out -sourcepath src/main/java src/main/java/slant/Main.java
```

### 2. Run the Game
```bash
java -cp out slant.Main
```

### 3. Run Tests (Optional)
To verify the logic and algorithms:
```bash
java -cp out slant.SlantTest
```

---

## 🧠 Algorithmic Implementation

This project implements the CPU logic using two distinct algorithm design paradigms as per the course requirements.

### 1. Review 2: Divide & Conquer (Primary Strategy)
The CPU primarily uses a **Divide & Conquer** strategy to determine its moves.
- **Divide**: The board is recursively split into 4 quadrants (Top-Left, Top-Right, Bottom-Left, Bottom-Right).
- **Conquer**: When a region is small enough (Base Case), the CPU evaluates valid moves.
- **Combine**: The results from sub-regions are merged together to form a ranked list of best moves.

### 2. Review 1: Greedy Algorithm (Fallback Strategy)
A **Greedy Algorithm** is also implemented as a robust fallback mechanism.
- **Logic**: It performs a linear scan of the entire board and greedily picks the cell with the most adjacent constraints (clues), without looking ahead.
- **Role**: This strategy kicks in if the D&C recursion hits an edge case or fails to find a move, ensuring the CPU never freezes.

### 3. Sorting (Custom Implementation)
- **Algorithm**: **Merge Sort**
- **Implementation**: A custom Merge Sort algorithm is implemented from scratch (not using `Collections.sort`) to rank the candidate moves.
- **Usage**: Used during the "Combine" phase of the Divide & Conquer strategy to ensure the CPU always picks the optimal move globally.

---

## 📂 Project Structure
- `src/main/java/slant/Main.java`: Entry point of the application.
- `src/main/java/slant/controller/SlantController.java`: Contains the **Game Logic**, **CPU AI**, **Divide & Conquer**, and **Merge Sort** implementations.
- `src/main/java/slant/model/SlantModel.java`: Data structure for the grid, clues, and loop detection (DSU).
- `src/main/java/slant/view`: GUI components using Java Swing.
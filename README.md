# CHARUSAT FTE - Object-Oriented Programming (CEUE203)
## Part B: MiniBank Console Project (All 14 Practicals + Complete Merged Application)

This repository contains clean, standalone, standard Java implementations of all **Part B (MiniBank)** practicals from the course curriculum, plus the complete integrated capstone application.

---

### Project Files Overview

| Lab # | Source File | Core Topic Covered |
|---|---|---|
| **Lab 01** | `Lab01_MiniBank.java` | Application Header Record, `MenuOption` Enum, Interactive Switch Expression |
| **Lab 02** | `Lab02_MiniBank.java` | `Customer` & `Account` Encapsulation, Static ID Counters, Constructor Chaining |
| **Lab 03** | `Lab03_MiniBank.java` | Object Methods (`toString`, `equals`, `hashCode`, `clone`), Static Nested `Address` |
| **Lab 04** | `Lab04_MiniBank.java` | Regex `Validator` (Mobile, Email, PAN, IFSC), `Command` Parser, `StringBuilder` Statement |
| **Lab 05** | `Lab05_MiniBank.java` | Abstract `Account`, Subclasses (`SavingsAccount`, `CurrentAccount`, `FixedDepositAccount`), Polymorphism |
| **Lab 06** | `Lab06_MiniBank.java` | `Transactable` & `InterestBearing` Interfaces, Functional Interface `WithdrawRule` (Lambda), Marker `Premium` |
| **Lab 07** | `Lab07_MiniBank.java` | Custom Annotations (`@Id`, `@Positive`, `@MaxLength`), Reflection Validator (`AnnotationValidator`) |
| **Lab 08** | `Lab08_MiniBank.java` | Checked Exception Hierarchy (`BankException`, `InsufficientFundsException`, etc.), `try-with-resources` |
| **Lab 09** | `Lab09_MiniBank.java` | Multithreading with `Runnable`, Demonstrating Race Condition vs `synchronized` Thread-Safety |
| **Lab 10** | `Lab10_MiniBank.java` | `ExecutorService` Thread Pool, Producer-Consumer (`wait`/`notify`), Deadlock Prevention Lock Ordering |
| **Lab 11** | `Lab11_MiniBank.java` | `Serializable` Persistence (`ObjectOutputStream`), NIO Audit Logging & End-of-Day Report Generator |
| **Lab 12** | `Lab12_MiniBank.java` | Collections Framework (`HashMap`, `ArrayList`), `Comparator` Sorting, `Comparable` Natural Order |
| **Lab 13** | `Lab13_MiniBank.java` | Generic `Repository<T>` with Reflection on `@Id`, `@SafeVarargs`, Bounded Generic Method `<N extends Number>` |
| **Lab 14** | `Lab14_MiniBank.java` | Capstone Assembly (`BankService` layered architecture, interactive CLI, automated test assertions) |
| **Lab 15** | `MiniBank_Complete.java` | **Fully Unified Capstone Application** merging all concepts from Lab 01 through Lab 14 into one master app |

---

### How to Compile & Run

#### Option 1: Compile all files at once
```bash
javac *.java
```

#### Option 2: Run any specific practical
```bash
# Example: Run Lab 1
java Lab01_MiniBank

# Example: Run Lab 5 (Polymorphism)
java Lab05_MiniBank

# Example: Run Lab 10 (Deadlock prevention & Thread pool)
java Lab10_MiniBank

# Example: Run Lab 14 (Capstone Service & Interactive CLI)
java Lab14_MiniBank

# Example: Run the Complete Unified Application (File 15)
java MiniBank_Complete
```

---
*Created for CHARUSAT FTE CEUE203 Coursework.*

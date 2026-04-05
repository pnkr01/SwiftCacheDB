# 🛰️ SwiftCache: Agentic Memory & Orchestration Engine

SwiftCache is a high-performance, in-memory data store purpose-built for **Agentic AI workflows**. It acts as a dedicated **“Neural Memory Layer”**, enabling agents to efficiently store, retrieve, and manage long-term context and complex reasoning graphs — all with sub-millisecond latency.

---

## 🚀 Vision

SwiftCache aims to become the **default memory + orchestration layer for Agentic AI systems**, bridging the gap between:

- Stateless LLM execution
- Persistent contextual intelligence
- Real-time observability

A true foundation for building **autonomous, scalable, and intelligent agent ecosystems**.

---

## 🏗️ System Architecture

SwiftCache follows a **dual-interface architecture**, cleanly separating high-speed data operations from system observability:

- **⚡ TCP Engine (Port 7777)**  
  A custom binary protocol built on Java NIO for ultra-fast, low-overhead communication between agents.

- **📊 REST Dashboard API (Port 8080)**  
  A Spring Boot-based management layer providing real-time metrics, system insights, and memory introspection for frontend dashboards.

---

## ☕ Backend: High-Performance Java Engine

The core engine is built using **Java 17** and **Spring Boot**, with a strong focus on **thread safety**, **low latency**, and **memory efficiency**.

### 🚀 Key Features

#### 🧠 Specialized Data Structures
- `SemanticList` → Sliding window buffer for managing LLM context efficiently
- `ReasoningGraph` → Structured representation for tracking multi-step agent logic

#### 💾 Durable Persistence (AOF)
- Append-Only File (AOF) ensures data durability across restarts
- Built-in **AOF Sweeper** compacts logs and removes expired entries automatically

#### 📊 Real-time Telemetry
- Tracks CPU usage, heap memory, and thread metrics
- Powered by `com.sun.management` for deep JVM insights

#### 🔐 Security
- TCP session authentication via `AUTH` command

---

## 🚦 Setup & Running (Backend)

### Prerequisites
- JDK 17+
- Maven

### Configuration
Update the authentication password in:

```
src/main/resources/application.properties
```

### Run the Server

```bash
mvn clean install
java -jar target/swiftcache-0.0.1.jar
```

---

## ⌨️ TCP Protocol Reference

| Command         | Parameters                    | Description                                      |
|----------------|------------------------------|--------------------------------------------------|
| `AUTH`         | `<password>`                 | Authenticates session (required first command)   |
| `PUSH`         | `<id> <message>`             | Append message to a context                      |
| `GRAPH_ADD`    | `<id> <t_id> <desc>`         | Add a task node to reasoning graph               |
| `GRAPH_UPDATE` | `<id> <t_id> <STATUS>`       | Update task status (COMPLETED / FAILED / PENDING)|
| `GET_CONTEXT`  | `<id>`                       | Retrieve full context messages                   |
| `GRAPH_GET`    | `<id>`                       | Retrieve reasoning graph                         |
| `PING`         | —                            | Health check → returns `PONG`                    |

---

## 🧪 Example Commands (Quick Start)

```bash
# 1. Authenticate first (Crucial!)
AUTH supersecret

# 2. Check if the engine is breathing
PING

# 3. Create a Chat Agent (SemanticList)
PUSH agent:001 SYSTEM: You are an autonomous researcher.
PUSH agent:001 USER: Analyze the latest market trends.

# 4. Create a Task Workflow (ReasoningGraph)
GRAPH_ADD task:99 setup_env Initialize Docker containers
GRAPH_ADD task:99 fetch_data Scrape competitor pricing

# 5. Update a task status
GRAPH_UPDATE task:99 setup_env COMPLETED

# 6. Retrieve the state
GET_CONTEXT agent:001
GRAPH_GET task:99
```

---

## 🎨 Frontend: Studio-Grade Dashboard

SwiftCache includes a modern observability dashboard designed as a **“War Room for Agents”**, emphasizing clarity, density, and real-time feedback.

### 🚀 Key Features

#### ⚡ Neural Telemetry
- Live tracking of **Operations Per Second (OPS)** and system throughput

#### 🧩 Bento Registry
- Searchable grid view of all active memory objects in RAM

#### 🔍 Agent Inspector
- Deep inspection panel for analyzing agent states and reasoning flows

#### ✨ Glassmorphic UI
- Dark-mode interface with blur effects, neon accents, and micro-interactions

#### 🖥️ Responsive Design
- Optimized for ultra-wide displays and high-density monitoring

---

## 🛠️ Frontend Tech Stack

- **Framework:** React 18 + Vite
- **Styling:** Tailwind CSS
- **Animations:** Framer Motion
- **Charts:** Recharts
- **Icons:** Lucide React

---

## 🚦 Setup & Running (Frontend)

### Prerequisites
- Node.js 18+

### Installation & Run

```bash
npm install
npm run dev
```

### Configuration Note

Ensure API proxy is configured in `vite.config.js`:

```js
server: {
  proxy: {
    '/api/v1': 'http://localhost:8080'
  }
}
```

---

## 📂 Maintenance & Logging

### 📁 `agentmemory-aof.log`
- Binary-safe log of all memory operations
- Deleting this file resets system state

### 📁 `agentmemory-audit.log`
- Human-readable audit log
- Contains timestamps, commands, and authentication attempts


## 📸 Screenshots

> Screenshot of frontend

### 🧠 Dashboard Overview
![Dashboard](https://github.com/user-attachments/assets/7e165568-3a6f-4827-bf8f-32d4742a575e)

### 📊 Neural Telemetry (OPS View)
![Telemetry](https://github.com/user-attachments/assets/1bb9336c-a305-488c-b1b5-13648f87495b)

### 🔍 Agent Inspector
![Inspector](https://github.com/user-attachments/assets/c992295e-026c-474b-aae5-755402e7c3ca)

---
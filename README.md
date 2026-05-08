# SARVYA — AI-Powered Personalized Learning Ecosystem

SARVYA is a next-generation AI-powered personalized learning ecosystem designed to adapt to each learner’s pace, style, environment, and accessibility needs in real time.

Built for the theme **“AI for Personalized Learning”**, SARVYA combines:

- Adaptive AI learning
- Gamified education
- Real-time IoT integration
- Accessibility-first learning
- Digital Twin analytics
- Multilingual interaction
- Context-aware personalization

Unlike traditional learning platforms that only react to answers, SARVYA also understands the learner’s behavior and environment using hardware sensors and AI-driven adaptation.

---

# Problem Statement

Most existing learning systems assume all students learn the same way.

They:
- Focus only on correctness
- Ignore environment and engagement
- Lack accessibility support
- Provide static learning experiences
- Do not adapt deeply to the learner

This problem is especially critical in rural and semi-urban environments where students face:
- Poor lighting conditions
- Distractions
- Language barriers
- Limited personalized guidance
- Accessibility challenges

---

# Our Solution

SARVYA is a fully connected ecosystem where:

- The app teaches
- The game engages
- The hardware senses
- The AI adapts

The system continuously analyzes:
- Performance
- Behavior
- Engagement
- Accessibility needs
- Real-world environmental signals

and dynamically personalizes the learning experience in real time.

---

# Core Components

## 1. SARVYA Control Center (Web Platform)

Central hub of the ecosystem featuring:
- Digital Twin dashboard
- Interactive knowledge graphs
- Real-time hardware monitoring
- AI tutor
- Session replay
- Learning analytics
- APK download center
- CareerOS integration

### Tech Stack
- Next.js
- TypeScript
- Tailwind CSS
- Framer Motion
- React Flow
- Recharts
- Zustand

---

## 2. Mobile Learning Application

Adaptive AI learning application featuring:
- Personalized learning paths
- AI tutor
- Adaptive quizzes
- Voice interaction
- Accessibility-first UI
- Multilingual support

### Features
- Explain-It-My-Way engine
- One-click accessibility transformer
- Real-time adaptation
- AI learning twin

---

## 3. SARVYA QUEST (Game Layer)

A gamified RPG-style educational experience where:
- Questions power combat
- Boss battles represent concept mastery
- AI adapts difficulty dynamically
- Wrong answers trigger learning mode

### Learning Loop
If a learner answers incorrectly:
- The game generates explanations
- Shows diagrams and visuals
- Uses voice narration
- Re-tests concepts

This ensures learning is reinforced instead of guessed.

---

## 4. Hardware Intelligence Layer

ESP32-based rover integrated with:
- Light sensor
- Tilt sensor
- Shock sensor
- Microphone module
- Interaction buttons

### Purpose
To capture real-world learning conditions such as:
- Lighting
- Movement
- Focus
- Disturbance
- Engagement

### Real-Time Pipeline
ESP32 → MQTT → Backend → Supabase → Dashboard/App/Game

---

# AI Personalization Engine

At the core of SARVYA is the Digital Twin system.

The AI continuously tracks:
- Accuracy
- Response time
- Weak topics
- Engagement
- Sensor data
- Learning patterns

The system dynamically adapts:
- Difficulty
- Content format
- Explanation style
- Learning pace
- Gameplay behavior

---

# Accessibility-First Learning

SARVYA is designed to support inclusive education.

## Features
- Voice-first interaction
- Screen reader compatibility
- High contrast mode
- Large text mode
- Simplified explanations
- Audio learning
- Multilingual support

## One-Click Accessibility Transformer
Convert any content into:
- Audio
- Simplified text
- Visual explanation

---

# Supported Learning Levels

## FOUNDATION
(Class 1–10)
- Story-based learning
- Visual explanations
- Voice guidance

## ADVANCED
(Class 11–12 + Competitive Exams)
- Timed tests
- Chapter-based progression
- Exam-focused analytics

## PROFESSIONAL
(College / STEM / Courses)
- Skill-based learning
- Domain progression
- Real-world problem solving

---

# Unique Features

- AI Digital Twin
- Context-aware learning
- Real-time hardware adaptation
- Gamified reinforcement learning
- Accessibility-first architecture
- Interactive knowledge graph
- Multilingual AI tutor
- MQTT-powered real-time ecosystem

---

# Architecture

```text
Mobile App / Game / CLI
           ↓
      AI Backend
           ↓
       Supabase
           ↑
ESP32 Rover → MQTT
           ↓
 SARVYA Control Center

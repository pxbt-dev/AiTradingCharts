# AI Cryptocurrency Analysis Platform 🤖

A _experimenal_ AI-powered cryptocurrency analysis and predeiction platform that uses real-time market data, technical analysis, and machine learning predictions for 4 cryptocurrencies - BTC, SOL, TAO and WIF. A work in progress.

![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-6DB33F?logo=springboot&logoColor=white)
![HTML5](https://img.shields.io/badge/HTML5-E34F26?logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?logo=css3&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?logo=javascript&logoColor=black)
![Railway](https://img.shields.io/badge/Deploy-Railway-0B0D0E?logo=railway&logoColor=white)
![Weka](https://img.shields.io/badge/Weka-Machine%20Learning-007EC6?logo=weka&logoColor=white)


✨ Features 
🤖 AI-Powered Predictions: Machine learning price forecasts across multiple timeframes (1H, 4H, 1D, 1W)

📊 Real-time Market Data: Live cryptocurrency prices from Binance WebSocket feeds

🎯 Technical Analysis: Automated pattern recognition (support/resistance, chart patterns, Fibonacci)

📈 Interactive Charts: Candlestick charts with zoom/pan capabilities and timeframe selection

⚡ Real-time Updates: WebSocket-based live data streaming and analysis

🔍 Multi-timeframe Analysis: Simultaneous analysis across 1H, 4H, 1D, and 1W timeframes

Live Demo 🚀
🌐: [Deployment URL - Add your deployment link here]

Tech Stack 🛠️
Backend
Java 17 - Core programming language

Spring Boot 3.0 - Application framework

Weka Machine Learning - AI/ML model implementation

WebSocket - Real-time communication

WebFlux - Reactive programming for API calls

Maven - Dependency management

Frontend
HTML5/CSS3/JavaScript - User interface

Chart.js with Financial Charts - Interactive candlestick charts

WebSocket API - Real-time data updates

Moment.js - Time formatting and manipulation

External Services
Binance API - Market data provider

WebSocket Streams - Real-time price feeds

AI/ML Implementation 🧠
Machine Learning Models
Our platform implements multiple machine learning algorithms through the Weka library:

🔍 Model Types
Linear Regression - Baseline predictions and trend analysis

Support Vector Machines (SMOreg) - Pattern recognition and classification

Random Forest - Ensemble learning for improved accuracy

📊 Feature Engineering
15+ technical indicators used for model training:

Simple Moving Averages (SMA 5, 20, 50, 200)

Exponential Moving Averages (EMA 12, 26)

Relative Strength Index (RSI 14, 21)

MACD (Moving Average Convergence Divergence)

Price volatility and momentum indicators

Volume-price trends and strength indicators

Bollinger Bands positioning

Statistical Z-scores and trend strength

🎯 Prediction System
Multi-timeframe Forecasting: Separate models for 1H, 4H, 1D, 1W predictions

Confidence Scoring: Each prediction includes reliability metrics (0.1-0.95 scale)

Feature Extraction: Real-time technical indicator calculation

Model Evaluation: R² scores, RMSE, and MAE tracking

Technical Analysis Engine
Chart Pattern Detection: Head & Shoulders, Double Top/Bottom, Triangles

Candlestick Patterns: Engulfing, Doji, Hammer formations

Fibonacci Analysis: Time zones and price level projections

Support/Resistance: Dynamic level identification using swing points

🏗️ Architecture Overview 
Frontend (HTML/JS) → API Controllers → Orchestration Services → Specialized Services → AI Models → External APIs

Core Components
🎮 Controllers
ApiDataController - Main REST API for chart data and analysis

HistoricalDataController - OHLC historical data provider

HtmlController - Dashboard interface serving

🔧 Services
Orchestration Layer:

🎯 TradingAnalysisService  - Main coordinator combining all analysis components

Specialized Analysis:

PricePredictionService - AI-powered price forecasting

AIModelService - Core machine learning engine (Weka integration)

ChartPatternService - Technical pattern recognition

FibonacciTimeZoneService - Fibonacci-based projections

Data Management:

BinanceHistoricalService - Binance API data provider

MarketDataService - Internal data cache management

RealTimeDataService - WebSocket data processing

TrainingDataService - AI model training data preparation

🌐 Gateway & Handlers
BinanceGateway - External API communication layer

CryptoWebSocketHandler - Real-time client communication manager

📊 Data Models
AIAnalysisResult - Complete analysis container

PricePrediction - Individual timeframe forecasts

ChartPattern - Technical pattern identifications

FibonacciTimeZone - Time and price projections

📡 API Endpoints 
REST Endpoints
GET /api/chart/data?symbol=BTC&timeframe=1d - Complete chart + AI analysis

GET /api/historical/BTC?timeframe=1d&limit=100 - Historical OHLC data

GET / - Main dashboard interface

⚙️ WebSocket Endpoints
WS /ws - Real-time market data and analysis broadcasts

Message Types: price_update, analysis, welcome

⚙️ Installation & Setup 
Prerequisites
Java 17 or higher

Maven 3.6+

Binance API access (public endpoints used)

-- 1. Enhance Courses Table
ALTER TABLE courses ADD COLUMN IF NOT EXISTS category TEXT DEFAULT 'General Engineering';
ALTER TABLE courses ADD COLUMN IF NOT EXISTS youtube_url TEXT;

-- 2. Create Enrollments Table
CREATE TABLE IF NOT EXISTS enrollments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id TEXT NOT NULL,
  course_id UUID REFERENCES courses(id) ON DELETE CASCADE,
  enrolled_at TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE(user_id, course_id)
);

-- 3. Create Progress Table
CREATE TABLE IF NOT EXISTS progress (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id TEXT NOT NULL,
  course_id UUID REFERENCES courses(id) ON DELETE CASCADE,
  completed BOOLEAN DEFAULT FALSE,
  completed_at TIMESTAMPTZ,
  UNIQUE(user_id, course_id)
);

-- 4. Enable RLS
ALTER TABLE enrollments ENABLE ROW LEVEL SECURITY;
ALTER TABLE progress ENABLE ROW LEVEL SECURITY;

-- 5. RLS Policies
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Courses are viewable by everyone') THEN
    CREATE POLICY "Courses are viewable by everyone" ON courses FOR SELECT USING (true);
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Users can manage own enrollments') THEN
    CREATE POLICY "Users can manage own enrollments" ON enrollments
      FOR ALL USING (user_id = auth.jwt() ->> 'sub');
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Users can manage own progress') THEN
    CREATE POLICY "Users can manage own progress" ON progress
      FOR ALL USING (user_id = auth.jwt() ->> 'sub');
  END IF;
END $$;

-- 6. Seed Data — 55 High Quality Engineering Courses
INSERT INTO courses (title, description, thumbnail_url, instructor, category, level, youtube_url, youtube_playlist_id) VALUES
-- Web Development
('Full Stack Web Development 2024', 'Complete roadmap for modern full stack development covering HTML, CSS, JS, React and Node.', 'https://img.youtube.com/vi/zJSY8tJY_67/maxresdefault.jpg', 'FreeCodeCamp', 'Web Development', 'Beginner', 'https://www.youtube.com/playlist?list=PLWKjhJtqVAbnSe1qUNGi745qnLkoWahuL', 'PLWKjhJtqVAbnSe1qUNGi745qnLkoWahuL'),
('The Complete JavaScript Course 2024', 'From zero to expert — the most complete JS course on the internet.', 'https://img.youtube.com/vi/W6NZfCO5SIk/maxresdefault.jpg', 'Jonas Schmedtmann', 'Web Development', 'Beginner', 'https://www.youtube.com/playlist?list=PLd3UqWTnYXOljTCRFgBkFMFJBFMFJBFMF', 'PLd3UqWTnYXOljTCRFgBkFMFJBFMFJBFMF'),
('Next.js 14 App Router Masterclass', 'Build production-ready apps with Next.js 14 App Router, Server Actions and more.', 'https://img.youtube.com/vi/wm5gMKuwSYk/maxresdefault.jpg', 'Lee Robinson', 'Web Development', 'Intermediate', 'https://www.youtube.com/playlist?list=PLC3y8-rFHvwjOKd6gdf4QtV1uYNiQnruI', 'PLC3y8-rFHvwjOKd6gdf4QtV1uYNiQnruI'),
('TypeScript Full Course', 'Master TypeScript from basics to advanced generics and utility types.', 'https://img.youtube.com/vi/30LWjhZzg50/maxresdefault.jpg', 'Matt Pocock', 'Web Development', 'Intermediate', 'https://www.youtube.com/playlist?list=PLNqp92_EXZBKYFrpEzdO2EapvU0GOJ09n', 'PLNqp92_EXZBKYFrpEzdO2EapvU0GOJ09n'),
('HTML & CSS Full Course', 'Build beautiful, responsive websites from scratch with HTML5 and CSS3.', 'https://img.youtube.com/vi/mU6anWqZJcc/maxresdefault.jpg', 'FreeCodeCamp', 'Web Development', 'Beginner', 'https://www.youtube.com/playlist?list=PLWKjhJtqVAbmMuZ3saqRIBimAKIMXml0n', 'PLWKjhJtqVAbmMuZ3saqRIBimAKIMXml0n'),
-- Frontend
('Mastering React Patterns', 'In-depth guide to React design patterns, hooks, and performance optimization.', 'https://img.youtube.com/vi/SqcY0GlETPk/maxresdefault.jpg', 'Jack Herrington', 'Frontend', 'Intermediate', 'https://www.youtube.com/playlist?list=PLNqp92_EXZBJYFrpEzdO2EapvU0GOJ09n', 'PLNqp92_EXZBJYFrpEzdO2EapvU0GOJ09n'),
('React 19 Complete Guide', 'Everything new in React 19 — Server Components, Actions, use() hook and more.', 'https://img.youtube.com/vi/T8TZQ6k4SLE/maxresdefault.jpg', 'Academind', 'Frontend', 'Intermediate', 'https://www.youtube.com/playlist?list=PL55RiY5tL51oyA8euSROLjzlW-epR6T9Z', 'PL55RiY5tL51oyA8euSROLjzlW-epR6T9Z'),
('Vue.js 3 Complete Course', 'Build modern SPAs with Vue 3, Composition API, Pinia and Vue Router.', 'https://img.youtube.com/vi/VeNfHj6MhgA/maxresdefault.jpg', 'Traversy Media', 'Frontend', 'Intermediate', 'https://www.youtube.com/playlist?list=PLillGF-RfqbYeckUaD1z6nviTp31GLTH8', 'PLillGF-RfqbYeckUaD1z6nviTp31GLTH8'),
('Tailwind CSS From Scratch', 'Master utility-first CSS with Tailwind — from basics to custom design systems.', 'https://img.youtube.com/vi/lCxcTsOHrjo/maxresdefault.jpg', 'Traversy Media', 'Frontend', 'Beginner', 'https://www.youtube.com/playlist?list=PLillGF-RfqbZTASqIqdvm1R5mLrQq79CU', 'PLillGF-RfqbZTASqIqdvm1R5mLrQq79CU'),
('Angular Complete Course 2024', 'Build enterprise-grade apps with Angular 17, RxJS, NgRx and Angular Material.', 'https://img.youtube.com/vi/3qBXWUpoPHo/maxresdefault.jpg', 'Maximilian Schwarzmüller', 'Frontend', 'Advanced', 'https://www.youtube.com/playlist?list=PL55RiY5tL51pT0DNJraU93FhMzhXxtDAo', 'PL55RiY5tL51pT0DNJraU93FhMzhXxtDAo'),
-- Backend
('Backend Engineering - Node.js', 'Build scalable and robust backends with Node.js, Express and REST APIs.', 'https://img.youtube.com/vi/Oe421EPjeBE/maxresdefault.jpg', 'Hussein Nasser', 'Backend', 'Advanced', 'https://www.youtube.com/playlist?list=PLQnljOFTzeIWH7D_rYf3S7uL9-F1x0G-7', 'PLQnljOFTzeIWH7D_rYf3S7uL9-F1x0G-7'),
('Node.js & Express REST API', 'Build a complete REST API with Node.js, Express, MongoDB and JWT auth.', 'https://img.youtube.com/vi/fgTGADljAeg/maxresdefault.jpg', 'Traversy Media', 'Backend', 'Intermediate', 'https://www.youtube.com/playlist?list=PLillGF-RfqbbiTGgA77tGO426V3hRF9iE', 'PLillGF-RfqbbiTGgA77tGO426V3hRF9iE'),
('Python Django Full Course', 'Build full-stack web apps with Django, Django REST Framework and PostgreSQL.', 'https://img.youtube.com/vi/PtQiiknWUcI/maxresdefault.jpg', 'Dennis Ivy', 'Backend', 'Intermediate', 'https://www.youtube.com/playlist?list=PL-51WBLyFTgMakJjyoMFGSVMFJBFMFJBF', 'PL-51WBLyFTgMakJjyoMFGSVMFJBFMFJBF'),
('Go Programming Language', 'Learn Go from scratch — goroutines, channels, REST APIs and microservices.', 'https://img.youtube.com/vi/yyUHQIec83I/maxresdefault.jpg', 'TechWorld with Nana', 'Backend', 'Intermediate', 'https://www.youtube.com/playlist?list=PLy7NvjMDP8m5azBytC_JMPyMFJBFMFJBF', 'PLy7NvjMDP8m5azBytC_JMPyMFJBFMFJBF'),
('FastAPI Python Backend', 'Build high-performance APIs with FastAPI, Pydantic, SQLAlchemy and async Python.', 'https://img.youtube.com/vi/7t2alSnE2-I/maxresdefault.jpg', 'Sebastián Ramírez', 'Backend', 'Intermediate', 'https://www.youtube.com/playlist?list=PLMCXHnjXnTnvFastAPI001', 'PLMCXHnjXnTnvFastAPI001'),
('GraphQL API Design', 'Design and build type-safe APIs with GraphQL, Apollo Server and subscriptions.', 'https://img.youtube.com/vi/ed8SzALpx1Q/maxresdefault.jpg', 'The Net Ninja', 'Backend', 'Intermediate', 'https://www.youtube.com/playlist?list=PL4cUxeGkcC9iK6Qhn-QLcXCXPQUX1OwGe', 'PL4cUxeGkcC9iK6Qhn-QLcXCXPQUX1OwGe'),
-- Architecture & System Design
('Advanced System Design', 'Master the architecture of large-scale distributed systems like Netflix and Uber.', 'https://img.youtube.com/vi/m8Icp_Cid5o/maxresdefault.jpg', 'ByteByteGo', 'Architecture', 'Advanced', 'https://www.youtube.com/playlist?list=PLMCXHnjXnTnvo6alSjVkgxV-VH6EPyvoX', 'PLMCXHnjXnTnvo6alSjVkgxV-VH6EPyvoX'),
('Microservices with Node.js', 'Build and deploy microservices using Node.js, Docker, RabbitMQ and Kubernetes.', 'https://img.youtube.com/vi/XUSHH0E-7zk/maxresdefault.jpg', 'Traversy Media', 'Architecture', 'Advanced', 'https://www.youtube.com/playlist?list=PLillGF-RfqbZTASqIqdvm1R5mLrQq79CU2', 'PLillGF-RfqbZTASqIqdvm1R5mLrQq79CU2'),
('Clean Code & SOLID Principles', 'Write maintainable, testable code using SOLID, DRY, KISS and design patterns.', 'https://img.youtube.com/vi/7EmboKQH8lM/maxresdefault.jpg', 'Fireship', 'Architecture', 'Intermediate', 'https://www.youtube.com/playlist?list=PLMCXHnjXnTnvCleanCode001', 'PLMCXHnjXnTnvCleanCode001'),
('Event-Driven Architecture', 'Design reactive systems with Kafka, RabbitMQ, event sourcing and CQRS.', 'https://img.youtube.com/vi/STKCRSUsyP0/maxresdefault.jpg', 'Hussein Nasser', 'Architecture', 'Advanced', 'https://www.youtube.com/playlist?list=PLQnljOFTzeIWEDA001', 'PLQnljOFTzeIWEDA001'),
-- DevOps & Cloud
('DevOps Roadmap 2025', 'Master Docker, Kubernetes, CI/CD pipelines, Terraform and cloud infrastructure.', 'https://img.youtube.com/vi/hQcFE0RD0cQ/maxresdefault.jpg', 'TechWorld with Nana', 'DevOps', 'Intermediate', 'https://www.youtube.com/playlist?list=PL4cUxeGkcC9jLYyp2Aoh6hcWuxFf6RlN2', 'PL4cUxeGkcC9jLYyp2Aoh6hcWuxFf6RlN2'),
('Docker & Kubernetes Complete Course', 'Containerize apps and orchestrate them at scale with Docker and Kubernetes.', 'https://img.youtube.com/vi/3c-iBn73dDE/maxresdefault.jpg', 'TechWorld with Nana', 'DevOps', 'Intermediate', 'https://www.youtube.com/playlist?list=PLy7NvjMDP8m5UweJFfzLLzLHUIyohu7A0', 'PLy7NvjMDP8m5UweJFfzLLzLHUIyohu7A0'),
('AWS Solutions Architect', 'Comprehensive AWS prep — EC2, S3, RDS, Lambda, VPC and certification practice.', 'https://img.youtube.com/vi/SOTamWNgDKc/maxresdefault.jpg', 'Stephane Maarek', 'DevOps', 'Advanced', 'https://www.youtube.com/playlist?list=PLt1SIbA8guusxiHz9bveV-UHs_biWFegU', 'PLt1SIbA8guusxiHz9bveV-UHs_biWFegU'),
('Terraform Infrastructure as Code', 'Provision and manage cloud infrastructure with Terraform on AWS, GCP and Azure.', 'https://img.youtube.com/vi/SLB_c_ayRMo/maxresdefault.jpg', 'FreeCodeCamp', 'DevOps', 'Advanced', 'https://www.youtube.com/playlist?list=PLWKjhJtqVAbmGw5fN5BQlwuug-8bDmabi', 'PLWKjhJtqVAbmGw5fN5BQlwuug-8bDmabi'),
('GitHub Actions CI/CD', 'Automate your entire software delivery pipeline with GitHub Actions workflows.', 'https://img.youtube.com/vi/R8_veQiYBjI/maxresdefault.jpg', 'TechWorld with Nana', 'DevOps', 'Intermediate', 'https://www.youtube.com/playlist?list=PLy7NvjMDP8m5CICD001', 'PLy7NvjMDP8m5CICD001'),
('Linux for DevOps Engineers', 'Master Linux command line, shell scripting, permissions and system administration.', 'https://img.youtube.com/vi/rrB13utjYV4/maxresdefault.jpg', 'FreeCodeCamp', 'DevOps', 'Beginner', 'https://www.youtube.com/playlist?list=PLWKjhJtqVAbmLinux001', 'PLWKjhJtqVAbmLinux001'),
-- Database
('PostgreSQL for Developers', 'Deep dive into PostgreSQL — indexing, query optimization, partitioning and scaling.', 'https://img.youtube.com/vi/qw--VYLpxG4/maxresdefault.jpg', 'Hussein Nasser', 'Database', 'Intermediate', 'https://www.youtube.com/playlist?list=PLQnljOFTzeIWPostgres001', 'PLQnljOFTzeIWPostgres001'),
('MongoDB Complete Course', 'Master MongoDB — CRUD, aggregation pipeline, indexing, Atlas and Mongoose.', 'https://img.youtube.com/vi/-56x56UppqQ/maxresdefault.jpg', 'The Net Ninja', 'Database', 'Beginner', 'https://www.youtube.com/playlist?list=PL4cUxeGkcC9h77dJ-QJlwGlZlTd4ecZOA', 'PL4cUxeGkcC9h77dJ-QJlwGlZlTd4ecZOA'),
('Redis Caching & Data Structures', 'Use Redis for caching, pub/sub, rate limiting, sessions and leaderboards.', 'https://img.youtube.com/vi/jgpVdJB2sKQ/maxresdefault.jpg', 'Hussein Nasser', 'Database', 'Intermediate', 'https://www.youtube.com/playlist?list=PLQnljOFTzeIWRedis001', 'PLQnljOFTzeIWRedis001'),
('SQL Mastery for Engineers', 'Write complex SQL queries, window functions, CTEs and optimize database performance.', 'https://img.youtube.com/vi/HXV3zeQKqGY/maxresdefault.jpg', 'FreeCodeCamp', 'Database', 'Intermediate', 'https://www.youtube.com/playlist?list=PLWKjhJtqVAbmSQL001', 'PLWKjhJtqVAbmSQL001'),
-- AI & Machine Learning
('Machine Learning with Python', 'Practical ML — regression, classification, clustering, neural networks with scikit-learn.', 'https://img.youtube.com/vi/7eh4d6sabA0/maxresdefault.jpg', 'Andrew Ng', 'AI & ML', 'Beginner', 'https://www.youtube.com/playlist?list=PLkDaE6sCZn6FNC6YRfRQc_FbeQrF8BwGI', 'PLkDaE6sCZn6FNC6YRfRQc_FbeQrF8BwGI'),
('Deep Learning Specialization', 'Neural networks, CNNs, RNNs, LSTMs and transformers from first principles.', 'https://img.youtube.com/vi/CS4cs9xVecg/maxresdefault.jpg', 'Andrew Ng', 'AI & ML', 'Advanced', 'https://www.youtube.com/playlist?list=PLkDaE6sCZn6Ec-XTbcX1uRg2_u4xOEky0', 'PLkDaE6sCZn6Ec-XTbcX1uRg2_u4xOEky0'),
('LangChain & LLM Engineering', 'Build production LLM apps with LangChain, RAG, vector databases and agents.', 'https://img.youtube.com/vi/lG7Uxts9SXs/maxresdefault.jpg', 'FreeCodeCamp', 'AI & ML', 'Advanced', 'https://www.youtube.com/playlist?list=PLWKjhJtqVAbmLLM001', 'PLWKjhJtqVAbmLLM001'),
('Generative AI for Developers', 'Prompt engineering, fine-tuning, embeddings and building GenAI applications.', 'https://img.youtube.com/vi/d4yCWBGFCEs/maxresdefault.jpg', 'Google DeepMind', 'AI & ML', 'Intermediate', 'https://www.youtube.com/playlist?list=PLMCXHnjXnTnvGenAI001', 'PLMCXHnjXnTnvGenAI001'),
('Computer Vision with OpenCV', 'Image processing, object detection, face recognition and deep learning with OpenCV.', 'https://img.youtube.com/vi/oXlwWbU8l2o/maxresdefault.jpg', 'FreeCodeCamp', 'AI & ML', 'Intermediate', 'https://www.youtube.com/playlist?list=PLWKjhJtqVAbmCV001', 'PLWKjhJtqVAbmCV001'),
('Natural Language Processing', 'Text classification, sentiment analysis, transformers and BERT with HuggingFace.', 'https://img.youtube.com/vi/8rXD5-xhemo/maxresdefault.jpg', 'Andrej Karpathy', 'AI & ML', 'Advanced', 'https://www.youtube.com/playlist?list=PLAqhIrjkxbuWI23v9cThsA9GvCAUhRvKZ', 'PLAqhIrjkxbuWI23v9cThsA9GvCAUhRvKZ'),
-- Data Science & Python
('Python for Data Science', 'NumPy, Pandas, Matplotlib, Seaborn and data analysis from scratch.', 'https://img.youtube.com/vi/LHBE6Q9XlzI/maxresdefault.jpg', 'Jose Portilla', 'Data Science', 'Beginner', 'https://www.youtube.com/playlist?list=PLWKjhJtqVAbmPython001', 'PLWKjhJtqVAbmPython001'),
('Data Analysis with Pandas', 'Master Pandas for data cleaning, transformation, groupby, merging and visualization.', 'https://img.youtube.com/vi/vmEHCJofslg/maxresdefault.jpg', 'Corey Schafer', 'Data Science', 'Intermediate', 'https://www.youtube.com/playlist?list=PL-osiE80TeTsWmV9i9c58mdDCSskIFdDS', 'PL-osiE80TeTsWmV9i9c58mdDCSskIFdDS'),
('Apache Spark & Big Data', 'Process massive datasets with Apache Spark, PySpark, Hadoop and data pipelines.', 'https://img.youtube.com/vi/GFC2gOL1p9k/maxresdefault.jpg', 'FreeCodeCamp', 'Data Science', 'Advanced', 'https://www.youtube.com/playlist?list=PLWKjhJtqVAbmSpark001', 'PLWKjhJtqVAbmSpark001'),
('Data Visualization with D3.js', 'Create interactive, animated data visualizations for the web using D3.js.', 'https://img.youtube.com/vi/_8V5o2UHG0E/maxresdefault.jpg', 'Curran Kelleher', 'Data Science', 'Intermediate', 'https://www.youtube.com/playlist?list=PLMCXHnjXnTnvD3001', 'PLMCXHnjXnTnvD3001'),
-- Cybersecurity
('Ethical Hacking Full Course', 'Penetration testing, network scanning, exploitation and bug bounty hunting.', 'https://img.youtube.com/vi/3Kq1MIfTWCE/maxresdefault.jpg', 'TCM Security', 'Cybersecurity', 'Intermediate', 'https://www.youtube.com/playlist?list=PLLKT__MCUeixqHJ1TRqrHsEd6_EdEvo47', 'PLLKT__MCUeixqHJ1TRqrHsEd6_EdEvo47'),
('Web Application Security & OWASP', 'OWASP Top 10, SQL injection, XSS, CSRF, authentication flaws and secure coding.', 'https://img.youtube.com/vi/WtHnT73NaaQ/maxresdefault.jpg', 'Troy Hunt', 'Cybersecurity', 'Intermediate', 'https://www.youtube.com/playlist?list=PLMCXHnjXnTnvOWASP001', 'PLMCXHnjXnTnvOWASP001'),
('Network Security Fundamentals', 'TCP/IP, firewalls, VPNs, IDS/IPS, Wireshark and network forensics.', 'https://img.youtube.com/vi/qiQR5rTSshw/maxresdefault.jpg', 'Professor Messer', 'Cybersecurity', 'Beginner', 'https://www.youtube.com/playlist?list=PLMCXHnjXnTnvNetwork001', 'PLMCXHnjXnTnvNetwork001'),
-- Mobile Development
('React Native Full Course', 'Build cross-platform iOS and Android apps with React Native and Expo.', 'https://img.youtube.com/vi/0-S5a0eXPoc/maxresdefault.jpg', 'Academind', 'Mobile', 'Intermediate', 'https://www.youtube.com/playlist?list=PL55RiY5tL51oyA8euSROLjzlW-epR6T9Z2', 'PL55RiY5tL51oyA8euSROLjzlW-epR6T9Z2'),
('Flutter & Dart Complete Course', 'Build beautiful native apps for iOS, Android and web with Flutter.', 'https://img.youtube.com/vi/VPvVD8t02U8/maxresdefault.jpg', 'The Net Ninja', 'Mobile', 'Intermediate', 'https://www.youtube.com/playlist?list=PL4cUxeGkcC9jLYyp2Aoh6hcWuxFf6RlN3', 'PL4cUxeGkcC9jLYyp2Aoh6hcWuxFf6RlN3'),
('Android Development with Kotlin', 'Build Android apps with Kotlin, Jetpack Compose, MVVM and Room database.', 'https://img.youtube.com/vi/EExSSotojVI/maxresdefault.jpg', 'Philipp Lackner', 'Mobile', 'Intermediate', 'https://www.youtube.com/playlist?list=PLQkwcJG4YTCTimTCpEL5FZgaWdIZQuB7m', 'PLQkwcJG4YTCTimTCpEL5FZgaWdIZQuB7m'),
-- DSA & Interview Prep
('Data Structures & Algorithms', 'Master arrays, trees, graphs, dynamic programming and sorting for interviews.', 'https://img.youtube.com/vi/8hly31xKli0/maxresdefault.jpg', 'Gayle Laakmann McDowell', 'DSA', 'Intermediate', 'https://www.youtube.com/playlist?list=PLMCXHnjXnTnvDSA001', 'PLMCXHnjXnTnvDSA001'),
('LeetCode Patterns & Techniques', 'Sliding window, two pointers, BFS/DFS, backtracking and greedy algorithms.', 'https://img.youtube.com/vi/A2bFN3MyNDA/maxresdefault.jpg', 'NeetCode', 'DSA', 'Intermediate', 'https://www.youtube.com/playlist?list=PLot-Xpze53ldVwtstag2TL4HQhAnC8ATf', 'PLot-Xpze53ldVwtstag2TL4HQhAnC8ATf'),
('System Design Interview Prep', 'Design scalable systems — URL shortener, Twitter, Netflix, WhatsApp and more.', 'https://img.youtube.com/vi/i7twT3x5yv8/maxresdefault.jpg', 'Alex Xu', 'DSA', 'Advanced', 'https://www.youtube.com/playlist?list=PLMCXHnjXnTnvSystemDesign001', 'PLMCXHnjXnTnvSystemDesign001'),
('Competitive Programming with C++', 'STL, segment trees, Fenwick trees, graph algorithms and contest strategies.', 'https://img.youtube.com/vi/vLnPwxZdW4Y/maxresdefault.jpg', 'Errichto', 'DSA', 'Advanced', 'https://www.youtube.com/playlist?list=PLl0KD3g-oDOHpWRyyGBUJ9jmul0lUOD80', 'PLl0KD3g-oDOHpWRyyGBUJ9jmul0lUOD80'),
-- Systems Programming
('Rust Programming Language', 'Ownership, borrowing, lifetimes, async Rust and building CLI tools and web servers.', 'https://img.youtube.com/vi/ygL_xcavzQ4/maxresdefault.jpg', 'Jon Gjengset', 'Systems', 'Advanced', 'https://www.youtube.com/playlist?list=PLqbS7AVVErFiWDOAVrPt7aYmnuuOLYvOa', 'PLqbS7AVVErFiWDOAVrPt7aYmnuuOLYvOa'),
('Operating Systems Fundamentals', 'Processes, threads, memory management, file systems and OS internals.', 'https://img.youtube.com/vi/26QPDBe-NB8/maxresdefault.jpg', 'MIT OpenCourseWare', 'Systems', 'Advanced', 'https://www.youtube.com/playlist?list=PLMCXHnjXnTnvOS001', 'PLMCXHnjXnTnvOS001'),
('Computer Networks from Scratch', 'TCP/IP stack, HTTP, DNS, TLS, WebSockets and how the internet actually works.', 'https://img.youtube.com/vi/qiQR5rTSshw/maxresdefault.jpg', 'Hussein Nasser', 'Systems', 'Intermediate', 'https://www.youtube.com/playlist?list=PLQnljOFTzeIWNetworks001', 'PLQnljOFTzeIWNetworks001'),
-- Blockchain & Web3
('Solidity & Smart Contracts', 'Build and deploy Ethereum smart contracts with Solidity, Hardhat and OpenZeppelin.', 'https://img.youtube.com/vi/gyMwXuJrbJQ/maxresdefault.jpg', 'Patrick Collins', 'Blockchain', 'Intermediate', 'https://www.youtube.com/playlist?list=PLMCXHnjXnTnvSolidity001', 'PLMCXHnjXnTnvSolidity001'),
('Web3 Full Stack Development', 'Build dApps with React, ethers.js, IPFS, MetaMask and The Graph.', 'https://img.youtube.com/vi/a0osIaAOFSE/maxresdefault.jpg', 'FreeCodeCamp', 'Blockchain', 'Advanced', 'https://www.youtube.com/playlist?list=PLWKjhJtqVAbmWeb3001', 'PLWKjhJtqVAbmWeb3001'),

-- Software Engineering Practices
('Git & GitHub Mastery', 'Branching strategies, rebasing, pull requests, GitHub Actions and open source workflow.', 'https://img.youtube.com/vi/RGOj5yH7evk/maxresdefault.jpg', 'FreeCodeCamp', 'Software Engineering', 'Beginner', 'https://www.youtube.com/playlist?list=PLWKjhJtqVAbmGit001', 'PLWKjhJtqVAbmGit001'),
('Test-Driven Development', 'Write better code with TDD, Jest, React Testing Library, Cypress and Playwright.', 'https://img.youtube.com/vi/Jv2uxzhPFl4/maxresdefault.jpg', 'Fireship', 'Software Engineering', 'Intermediate', 'https://www.youtube.com/playlist?list=PLMCXHnjXnTnvTDD001', 'PLMCXHnjXnTnvTDD001'),
('Software Design Patterns', 'Gang of Four patterns — Singleton, Factory, Observer, Strategy and more in TypeScript.', 'https://img.youtube.com/vi/tv-_1er1mWI/maxresdefault.jpg', 'Fireship', 'Software Engineering', 'Intermediate', 'https://www.youtube.com/playlist?list=PLMCXHnjXnTnvPatterns001', 'PLMCXHnjXnTnvPatterns001')
ON CONFLICT (youtube_playlist_id) DO NOTHING;

-- Add enrollment_count column to courses table
ALTER TABLE courses ADD COLUMN IF NOT EXISTS enrollment_count INTEGER DEFAULT 0;

-- Function to increment enrollment count
CREATE OR REPLACE FUNCTION increment_enrollment_count()
RETURNS TRIGGER AS $$
BEGIN
  UPDATE courses SET enrollment_count = enrollment_count + 1 WHERE id = NEW.course_id;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger: fires after each new enrollment
DROP TRIGGER IF EXISTS on_enrollment_insert ON enrollments;
CREATE TRIGGER on_enrollment_insert
  AFTER INSERT ON enrollments
  FOR EACH ROW EXECUTE FUNCTION increment_enrollment_count();

-- Backfill existing enrollment counts
UPDATE courses c
SET enrollment_count = (
  SELECT COUNT(*) FROM enrollments e WHERE e.course_id = c.id
);
CREATE INDEX IF NOT EXISTS idx_enrollments_user_id ON enrollments(user_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_course_id ON enrollments(course_id);
CREATE INDEX IF NOT EXISTS idx_progress_user_id ON progress(user_id);
CREATE INDEX IF NOT EXISTS idx_progress_course_id ON progress(course_id);
CREATE INDEX IF NOT EXISTS idx_courses_category ON courses(category);
CREATE INDEX IF NOT EXISTS idx_courses_level ON courses(level);
CREATE INDEX IF NOT EXISTS idx_courses_created_at ON courses(created_at DESC);

-- Additional seed courses (run this to populate the catalog)
INSERT INTO courses (title, description, thumbnail_url, instructor, category, level, youtube_url, youtube_playlist_id) VALUES
('JavaScript Full Course 2024', 'Complete JavaScript from basics to advanced — ES6+, async/await, DOM, APIs.', 'https://img.youtube.com/vi/PkZNo7MFNFg/maxresdefault.jpg', 'FreeCodeCamp', 'Web Development', 'Beginner', 'https://www.youtube.com/watch?v=PkZNo7MFNFg', 'PLWKjhJtqVAbkArDMazoARtNz1aMwjV8F6'),
('React JS Full Course', 'Build modern React apps with hooks, context, Redux and React Router.', 'https://img.youtube.com/vi/bMknfKXIFA8/maxresdefault.jpg', 'FreeCodeCamp', 'Frontend', 'Intermediate', 'https://www.youtube.com/watch?v=bMknfKXIFA8', 'PLWKjhJtqVAbnSe1qUNGi745qnLkoWahuL2'),
('Python Full Course for Beginners', 'Learn Python programming from scratch — variables, functions, OOP, file I/O.', 'https://img.youtube.com/vi/rfscVS0vtbw/maxresdefault.jpg', 'FreeCodeCamp', 'Data Science', 'Beginner', 'https://www.youtube.com/watch?v=rfscVS0vtbw', 'PLWKjhJtqVAbmPython002'),
('Node.js Crash Course', 'Build REST APIs with Node.js, Express, and MongoDB from scratch.', 'https://img.youtube.com/vi/fBNz5xF-Kx4/maxresdefault.jpg', 'Traversy Media', 'Backend', 'Intermediate', 'https://www.youtube.com/watch?v=fBNz5xF-Kx4', 'PLillGF-RfqbbiTGgA77tGO426V3hRF9iE2'),
('CSS Full Course', 'Master CSS3 — flexbox, grid, animations, responsive design and more.', 'https://img.youtube.com/vi/1Rs2ND1ryYc/maxresdefault.jpg', 'FreeCodeCamp', 'Web Development', 'Beginner', 'https://www.youtube.com/watch?v=1Rs2ND1ryYc', 'PLWKjhJtqVAbmCSS003'),
('TypeScript Tutorial', 'TypeScript from zero to hero — types, interfaces, generics, decorators.', 'https://img.youtube.com/vi/BwuLxPH8IDs/maxresdefault.jpg', 'Traversy Media', 'Web Development', 'Intermediate', 'https://www.youtube.com/watch?v=BwuLxPH8IDs', 'PLillGF-RfqbYeckUaD1z6nviTp31GLTH9'),
('Docker Tutorial for Beginners', 'Learn Docker from scratch — containers, images, volumes, Docker Compose.', 'https://img.youtube.com/vi/3c-iBn73dDE/maxresdefault.jpg', 'TechWorld with Nana', 'DevOps', 'Beginner', 'https://www.youtube.com/watch?v=3c-iBn73dDE', 'PLy7NvjMDP8m5UweJFfzLLzLHUIyohu7A1'),
('Kubernetes Tutorial for Beginners', 'Complete Kubernetes course — pods, deployments, services, ingress, Helm.', 'https://img.youtube.com/vi/X48VuDVv0do/maxresdefault.jpg', 'TechWorld with Nana', 'DevOps', 'Intermediate', 'https://www.youtube.com/watch?v=X48VuDVv0do', 'PLy7NvjMDP8m5UweJFfzLLzLHUIyohu7A2'),
('SQL Tutorial - Full Database Course', 'Learn SQL from scratch — SELECT, JOIN, subqueries, indexes, transactions.', 'https://img.youtube.com/vi/HXV3zeQKqGY/maxresdefault.jpg', 'FreeCodeCamp', 'Database', 'Beginner', 'https://www.youtube.com/watch?v=HXV3zeQKqGY', 'PLWKjhJtqVAbmSQL002'),
('Machine Learning Course', 'Andrew Ng ML course — regression, classification, neural networks, SVM.', 'https://img.youtube.com/vi/jGwO_UgTS7I/maxresdefault.jpg', 'Stanford Online', 'AI & ML', 'Intermediate', 'https://www.youtube.com/watch?v=jGwO_UgTS7I', 'PLkDaE6sCZn6FNC6YRfRQc_FbeQrF8BwGI2'),
('Git and GitHub for Beginners', 'Complete Git tutorial — commits, branches, merging, pull requests, GitHub.', 'https://img.youtube.com/vi/RGOj5yH7evk/maxresdefault.jpg', 'FreeCodeCamp', 'Software Engineering', 'Beginner', 'https://www.youtube.com/watch?v=RGOj5yH7evk', 'PLWKjhJtqVAbmGit002'),
('Java Full Course', 'Java programming from basics to OOP, collections, streams, and Spring Boot.', 'https://img.youtube.com/vi/eIrMbAQSU34/maxresdefault.jpg', 'Programming with Mosh', 'Backend', 'Beginner', 'https://www.youtube.com/watch?v=eIrMbAQSU34', 'PLTjRvDozrdlxjmB8rotating001'),
('AWS Cloud Practitioner', 'AWS fundamentals — EC2, S3, RDS, Lambda, IAM, VPC for beginners.', 'https://img.youtube.com/vi/SOTamWNgDKc/maxresdefault.jpg', 'FreeCodeCamp', 'DevOps', 'Beginner', 'https://www.youtube.com/watch?v=SOTamWNgDKc', 'PLWKjhJtqVAbmAWS001'),
('Data Structures Easy to Advanced', 'Arrays, linked lists, trees, graphs, heaps, hash tables with implementations.', 'https://img.youtube.com/vi/RBSGKlAvoiM/maxresdefault.jpg', 'FreeCodeCamp', 'DSA', 'Intermediate', 'https://www.youtube.com/watch?v=RBSGKlAvoiM', 'PLWKjhJtqVAbmDSA002'),
('C++ Full Course', 'Learn C++ from scratch — pointers, OOP, STL, templates, memory management.', 'https://img.youtube.com/vi/vLnPwxZdW4Y/maxresdefault.jpg', 'FreeCodeCamp', 'Systems', 'Beginner', 'https://www.youtube.com/watch?v=vLnPwxZdW4Y', 'PLWKjhJtqVAbmCpp001')
ON CONFLICT (youtube_playlist_id) DO NOTHING;

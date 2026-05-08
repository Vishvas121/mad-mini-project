'use client';

import { useState, useRef, useEffect } from 'react';
import { Send, Plus, Trash2, Sparkles, Code, User as UserIcon, Bot, Youtube, Search, ExternalLink, Play } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

type Tab = 'chat' | 'youtube' | 'google';

const suggestedPrompts = [
  'How do I optimize React component performance?',
  'Explain microservices architecture',
  'Best practices for database indexing',
  'How to handle errors in async/await?',
];

const YT_RESOURCES = [
  { title: 'CS50 Introduction to Computer Science', channel: 'CS50', duration: '25h', topic: 'CS Fundamentals', url: 'https://www.youtube.com/watch?v=8mAITcNt710', thumb: 'https://img.youtube.com/vi/8mAITcNt710/mqdefault.jpg' },
  { title: 'Harvard CS50 Web Programming', channel: 'CS50', duration: '20h', topic: 'Web Dev', url: 'https://www.youtube.com/watch?v=vzGllw18DkA', thumb: 'https://img.youtube.com/vi/vzGllw18DkA/mqdefault.jpg' },
  { title: 'Python Full Course', channel: 'freeCodeCamp', duration: '4h', topic: 'Python', url: 'https://www.youtube.com/watch?v=rfscVS0vtbw', thumb: 'https://img.youtube.com/vi/rfscVS0vtbw/mqdefault.jpg' },
  { title: 'Java Full Course', channel: 'Programming with Mosh', duration: '5h', topic: 'Backend', url: 'https://www.youtube.com/watch?v=eIrMbAQSU34', thumb: 'https://img.youtube.com/vi/eIrMbAQSU34/mqdefault.jpg' },
  { title: 'C++ Full Course', channel: 'freeCodeCamp', duration: '4h', topic: 'Systems', url: 'https://www.youtube.com/watch?v=vLnPwxZdW4Y', thumb: 'https://img.youtube.com/vi/vLnPwxZdW4Y/mqdefault.jpg' },
  { title: 'JavaScript Full Course', channel: 'freeCodeCamp', duration: '12h', topic: 'Web Dev', url: 'https://www.youtube.com/watch?v=PkZNo7MFNFg', thumb: 'https://img.youtube.com/vi/PkZNo7MFNFg/mqdefault.jpg' },
  { title: 'React JS Full Course', channel: 'freeCodeCamp', duration: '10h', topic: 'Frontend', url: 'https://www.youtube.com/watch?v=bMknfKXIFA8', thumb: 'https://img.youtube.com/vi/bMknfKXIFA8/mqdefault.jpg' },
  { title: 'Node.js Full Course', channel: 'freeCodeCamp', duration: '8h', topic: 'Backend', url: 'https://www.youtube.com/watch?v=Oe421EPjeBE', thumb: 'https://img.youtube.com/vi/Oe421EPjeBE/mqdefault.jpg' },
  { title: 'Data Structures', channel: 'Abdul Bari', duration: '8h', topic: 'DSA', url: 'https://www.youtube.com/watch?v=8hly31xKli0', thumb: 'https://img.youtube.com/vi/8hly31xKli0/mqdefault.jpg' },
  { title: 'Operating Systems', channel: 'Gate Smashers', duration: '10h', topic: 'CS Fundamentals', url: 'https://www.youtube.com/watch?v=26QPDBe-NB8', thumb: 'https://img.youtube.com/vi/26QPDBe-NB8/mqdefault.jpg' },
  { title: 'DBMS Full Course', channel: 'Gate Smashers', duration: '8h', topic: 'Database', url: 'https://www.youtube.com/watch?v=kBdlM6hNDAE', thumb: 'https://img.youtube.com/vi/kBdlM6hNDAE/mqdefault.jpg' },
  { title: 'Computer Networks', channel: 'Neso Academy', duration: '12h', topic: 'CS Fundamentals', url: 'https://www.youtube.com/watch?v=IPvYjXCsTg8', thumb: 'https://img.youtube.com/vi/IPvYjXCsTg8/mqdefault.jpg' },
  { title: 'Machine Learning', channel: 'Andrew Ng', duration: '20h', topic: 'AI/ML', url: 'https://www.youtube.com/watch?v=PPLop4L2eGk', thumb: 'https://img.youtube.com/vi/PPLop4L2eGk/mqdefault.jpg' },
  { title: 'Deep Learning', channel: 'freeCodeCamp', duration: '6h', topic: 'AI/ML', url: 'https://www.youtube.com/watch?v=aircAruvnKk', thumb: 'https://img.youtube.com/vi/aircAruvnKk/mqdefault.jpg' },
  { title: 'SQL Full Course', channel: 'freeCodeCamp', duration: '4h', topic: 'Database', url: 'https://www.youtube.com/watch?v=HXV3zeQKqGY', thumb: 'https://img.youtube.com/vi/HXV3zeQKqGY/mqdefault.jpg' },
  { title: 'Git & GitHub', channel: 'freeCodeCamp', duration: '1h', topic: 'Tools', url: 'https://www.youtube.com/watch?v=RGOj5yH7evk', thumb: 'https://img.youtube.com/vi/RGOj5yH7evk/mqdefault.jpg' },
  { title: 'Docker Full Course', channel: 'freeCodeCamp', duration: '3h', topic: 'DevOps', url: 'https://www.youtube.com/watch?v=fqMOX6JJhGo', thumb: 'https://img.youtube.com/vi/fqMOX6JJhGo/mqdefault.jpg' },
  { title: 'Kubernetes Full Course', channel: 'TechWorld with Nana', duration: '4h', topic: 'DevOps', url: 'https://www.youtube.com/watch?v=X48VuDVv0do', thumb: 'https://img.youtube.com/vi/X48VuDVv0do/mqdefault.jpg' },
  { title: 'System Design', channel: 'Gaurav Sen', duration: '2h', topic: 'System Design', url: 'https://www.youtube.com/watch?v=UzLMhqg3_Wc', thumb: 'https://img.youtube.com/vi/UzLMhqg3_Wc/mqdefault.jpg' },
  { title: 'Spring Boot Full Course', channel: 'Amigoscode', duration: '5h', topic: 'Backend', url: 'https://www.youtube.com/watch?v=vtPkZShrvXQ', thumb: 'https://img.youtube.com/vi/vtPkZShrvXQ/mqdefault.jpg' },
  { title: 'Angular Full Course', channel: 'freeCodeCamp', duration: '8h', topic: 'Frontend', url: 'https://www.youtube.com/watch?v=3dHNOWTI7H8', thumb: 'https://img.youtube.com/vi/3dHNOWTI7H8/mqdefault.jpg' },
  { title: 'TypeScript Full Course', channel: 'freeCodeCamp', duration: '3h', topic: 'Web Dev', url: 'https://www.youtube.com/watch?v=30LWjhZzg50', thumb: 'https://img.youtube.com/vi/30LWjhZzg50/mqdefault.jpg' },
  { title: 'Flutter Full Course', channel: 'freeCodeCamp', duration: '6h', topic: 'Mobile', url: 'https://www.youtube.com/watch?v=x0uinJvhNxI', thumb: 'https://img.youtube.com/vi/x0uinJvhNxI/mqdefault.jpg' },
  { title: 'Android Development', channel: 'freeCodeCamp', duration: '12h', topic: 'Mobile', url: 'https://www.youtube.com/watch?v=fis26HvvDII', thumb: 'https://img.youtube.com/vi/fis26HvvDII/mqdefault.jpg' },
  { title: 'Cyber Security Full Course', channel: 'freeCodeCamp', duration: '25h', topic: 'Security', url: 'https://www.youtube.com/watch?v=U_P23SqJaDc', thumb: 'https://img.youtube.com/vi/U_P23SqJaDc/mqdefault.jpg' },
  { title: 'Ethical Hacking Course', channel: 'TCM Security', duration: '15h', topic: 'Security', url: 'https://www.youtube.com/watch?v=3Kq1MIfTWCE', thumb: 'https://img.youtube.com/vi/3Kq1MIfTWCE/mqdefault.jpg' },
  { title: 'AWS Full Course', channel: 'freeCodeCamp', duration: '13h', topic: 'Cloud', url: 'https://www.youtube.com/watch?v=ulprqHHWlng', thumb: 'https://img.youtube.com/vi/ulprqHHWlng/mqdefault.jpg' },
  { title: 'Azure Full Course', channel: 'freeCodeCamp', duration: '11h', topic: 'Cloud', url: 'https://www.youtube.com/watch?v=NKEFWyqJ5XA', thumb: 'https://img.youtube.com/vi/NKEFWyqJ5XA/mqdefault.jpg' },
  { title: 'DevOps Full Course', channel: 'freeCodeCamp', duration: '6h', topic: 'DevOps', url: 'https://www.youtube.com/watch?v=j5Zsa_eOXeY', thumb: 'https://img.youtube.com/vi/j5Zsa_eOXeY/mqdefault.jpg' },
  { title: 'Data Science Full Course', channel: 'freeCodeCamp', duration: '10h', topic: 'Data Science', url: 'https://www.youtube.com/watch?v=ua-CiDNNj30', thumb: 'https://img.youtube.com/vi/ua-CiDNNj30/mqdefault.jpg' },
  { title: 'Pandas Tutorial', channel: 'Corey Schafer', duration: '4h', topic: 'Data Science', url: 'https://www.youtube.com/watch?v=vmEHCJofslg', thumb: 'https://img.youtube.com/vi/vmEHCJofslg/mqdefault.jpg' },
  { title: 'NumPy Tutorial', channel: 'freeCodeCamp', duration: '1h', topic: 'Data Science', url: 'https://www.youtube.com/watch?v=QUT1VHiLmmI', thumb: 'https://img.youtube.com/vi/QUT1VHiLmmI/mqdefault.jpg' },
  { title: 'Power BI Full Course', channel: 'freeCodeCamp', duration: '3h', topic: 'Data Science', url: 'https://www.youtube.com/watch?v=AGrl-H87pRU', thumb: 'https://img.youtube.com/vi/AGrl-H87pRU/mqdefault.jpg' },
  { title: 'Blockchain Full Course', channel: 'freeCodeCamp', duration: '5h', topic: 'Blockchain', url: 'https://www.youtube.com/watch?v=SSo_EIwHSd4', thumb: 'https://img.youtube.com/vi/SSo_EIwHSd4/mqdefault.jpg' },
  { title: 'Solidity Course', channel: 'Patrick Collins', duration: '16h', topic: 'Blockchain', url: 'https://www.youtube.com/watch?v=gyMwXuJrbJQ', thumb: 'https://img.youtube.com/vi/gyMwXuJrbJQ/mqdefault.jpg' },
  { title: 'Web3 Development', channel: 'freeCodeCamp', duration: '8h', topic: 'Blockchain', url: 'https://www.youtube.com/watch?v=M576WGiDBdQ', thumb: 'https://img.youtube.com/vi/M576WGiDBdQ/mqdefault.jpg' },
  { title: 'Linux Full Course', channel: 'freeCodeCamp', duration: '6h', topic: 'DevOps', url: 'https://www.youtube.com/watch?v=sWbUDq4S6Y8', thumb: 'https://img.youtube.com/vi/sWbUDq4S6Y8/mqdefault.jpg' },
  { title: 'Bash Scripting', channel: 'freeCodeCamp', duration: '3h', topic: 'DevOps', url: 'https://www.youtube.com/watch?v=SPwyp2NG-bE', thumb: 'https://img.youtube.com/vi/SPwyp2NG-bE/mqdefault.jpg' },
  { title: 'REST API Tutorial', channel: 'freeCodeCamp', duration: '1h', topic: 'Backend', url: 'https://www.youtube.com/watch?v=Q-BpqyOT3a8', thumb: 'https://img.youtube.com/vi/Q-BpqyOT3a8/mqdefault.jpg' },
  { title: 'GraphQL Full Course', channel: 'freeCodeCamp', duration: '4h', topic: 'Backend', url: 'https://www.youtube.com/watch?v=eIQh02xuVw4', thumb: 'https://img.youtube.com/vi/eIQh02xuVw4/mqdefault.jpg' },
  { title: 'Redis Tutorial', channel: 'freeCodeCamp', duration: '1h', topic: 'Database', url: 'https://www.youtube.com/watch?v=Hbt56gFj998', thumb: 'https://img.youtube.com/vi/Hbt56gFj998/mqdefault.jpg' },
  { title: 'MongoDB Full Course', channel: 'freeCodeCamp', duration: '3h', topic: 'Database', url: 'https://www.youtube.com/watch?v=ofme2o29ngU', thumb: 'https://img.youtube.com/vi/ofme2o29ngU/mqdefault.jpg' },
  { title: 'Express JS Tutorial', channel: 'Traversy Media', duration: '1h', topic: 'Backend', url: 'https://www.youtube.com/watch?v=L72fhGm1tfE', thumb: 'https://img.youtube.com/vi/L72fhGm1tfE/mqdefault.jpg' },
  { title: 'Next.js Full Course', channel: 'freeCodeCamp', duration: '6h', topic: 'Frontend', url: 'https://www.youtube.com/watch?v=wm5gMKuwSYk', thumb: 'https://img.youtube.com/vi/wm5gMKuwSYk/mqdefault.jpg' },
  { title: 'Tailwind CSS', channel: 'freeCodeCamp', duration: '4h', topic: 'Frontend', url: 'https://www.youtube.com/watch?v=dFgzHOX84xQ', thumb: 'https://img.youtube.com/vi/dFgzHOX84xQ/mqdefault.jpg' },
  { title: 'UI/UX Design Course', channel: 'freeCodeCamp', duration: '8h', topic: 'Design', url: 'https://www.youtube.com/watch?v=c9Wg6Cb_YlU', thumb: 'https://img.youtube.com/vi/c9Wg6Cb_YlU/mqdefault.jpg' },
  { title: 'Figma Tutorial', channel: 'freeCodeCamp', duration: '3h', topic: 'Design', url: 'https://www.youtube.com/watch?v=FTFaQWZBqQ8', thumb: 'https://img.youtube.com/vi/FTFaQWZBqQ8/mqdefault.jpg' },
  { title: 'Competitive Programming', channel: 'Errichto', duration: '2h', topic: 'DSA', url: 'https://www.youtube.com/watch?v=8hly31xKli0', thumb: 'https://img.youtube.com/vi/8hly31xKli0/mqdefault.jpg' },
  { title: 'Interview Prep DSA', channel: 'freeCodeCamp', duration: '8h', topic: 'DSA', url: 'https://www.youtube.com/watch?v=RBSGKlAvoiM', thumb: 'https://img.youtube.com/vi/RBSGKlAvoiM/mqdefault.jpg' },
  { title: 'Full Stack Development', channel: 'freeCodeCamp', duration: '12h', topic: 'Full Stack', url: 'https://www.youtube.com/watch?v=nu_pCVPKzTk', thumb: 'https://img.youtube.com/vi/nu_pCVPKzTk/mqdefault.jpg' },
  { title: 'Python for Data Science', channel: 'freeCodeCamp', duration: '12h', topic: 'Data Science', url: 'https://www.youtube.com/watch?v=LHBE6Q9XlzI', thumb: 'https://img.youtube.com/vi/LHBE6Q9XlzI/mqdefault.jpg' },
  { title: 'Advanced JavaScript', channel: 'freeCodeCamp', duration: '7h', topic: 'Web Dev', url: 'https://www.youtube.com/watch?v=SBmSRK3feww', thumb: 'https://img.youtube.com/vi/SBmSRK3feww/mqdefault.jpg' },
  { title: 'React Native Full Course', channel: 'Academind', duration: '8h', topic: 'Mobile', url: 'https://www.youtube.com/watch?v=0-S5a0eXPoc', thumb: 'https://img.youtube.com/vi/0-S5a0eXPoc/mqdefault.jpg' },
  { title: 'MERN Stack Full Course', channel: 'freeCodeCamp', duration: '8h', topic: 'Full Stack', url: 'https://www.youtube.com/watch?v=7CqJlxBYj-M', thumb: 'https://img.youtube.com/vi/7CqJlxBYj-M/mqdefault.jpg' },
  { title: 'Full Stack Next.js Project', channel: 'JavaScript Mastery', duration: '5h', topic: 'Full Stack', url: 'https://www.youtube.com/watch?v=Y6KDk5iyrYE', thumb: 'https://img.youtube.com/vi/Y6KDk5iyrYE/mqdefault.jpg' },
  { title: 'Redux Tutorial', channel: 'freeCodeCamp', duration: '3h', topic: 'Frontend', url: 'https://www.youtube.com/watch?v=93p3LxR9xfM', thumb: 'https://img.youtube.com/vi/93p3LxR9xfM/mqdefault.jpg' },
  { title: 'GraphQL API with Node.js', channel: 'The Net Ninja', duration: '2h', topic: 'Backend', url: 'https://www.youtube.com/watch?v=ed8SzALpx1Q', thumb: 'https://img.youtube.com/vi/ed8SzALpx1Q/mqdefault.jpg' },
  { title: 'Firebase Full Course', channel: 'freeCodeCamp', duration: '4h', topic: 'Backend', url: 'https://www.youtube.com/watch?v=9kRgVxULbag', thumb: 'https://img.youtube.com/vi/9kRgVxULbag/mqdefault.jpg' },
  { title: 'Supabase Tutorial', channel: 'freeCodeCamp', duration: '1h', topic: 'Backend', url: 'https://www.youtube.com/watch?v=dU7GwCOgvNY', thumb: 'https://img.youtube.com/vi/dU7GwCOgvNY/mqdefault.jpg' },
  { title: 'Prisma ORM Tutorial', channel: 'Traversy Media', duration: '1h', topic: 'Backend', url: 'https://www.youtube.com/watch?v=RebA5J-rlwg', thumb: 'https://img.youtube.com/vi/RebA5J-rlwg/mqdefault.jpg' },
  { title: 'PostgreSQL Full Course', channel: 'freeCodeCamp', duration: '4h', topic: 'Database', url: 'https://www.youtube.com/watch?v=qw--VYLpxG4', thumb: 'https://img.youtube.com/vi/qw--VYLpxG4/mqdefault.jpg' },
  { title: 'MySQL Full Course', channel: 'freeCodeCamp', duration: '3h', topic: 'Database', url: 'https://www.youtube.com/watch?v=7S_tz1z_5bA', thumb: 'https://img.youtube.com/vi/7S_tz1z_5bA/mqdefault.jpg' },
  { title: 'Advanced SQL', channel: 'freeCodeCamp', duration: '4h', topic: 'Database', url: 'https://www.youtube.com/watch?v=HXV3zeQKqGY', thumb: 'https://img.youtube.com/vi/HXV3zeQKqGY/mqdefault.jpg' },
  { title: 'Django Full Course', channel: 'freeCodeCamp', duration: '4h', topic: 'Backend', url: 'https://www.youtube.com/watch?v=F5mRW0jo-U4', thumb: 'https://img.youtube.com/vi/F5mRW0jo-U4/mqdefault.jpg' },
  { title: 'Flask Full Course', channel: 'freeCodeCamp', duration: '2h', topic: 'Backend', url: 'https://www.youtube.com/watch?v=Z1RJmh_OqeA', thumb: 'https://img.youtube.com/vi/Z1RJmh_OqeA/mqdefault.jpg' },
  { title: 'FastAPI Tutorial', channel: 'freeCodeCamp', duration: '1h', topic: 'Backend', url: 'https://www.youtube.com/watch?v=0sOvCWFmrtA', thumb: 'https://img.youtube.com/vi/0sOvCWFmrtA/mqdefault.jpg' },
  { title: 'Go Programming Full Course', channel: 'freeCodeCamp', duration: '7h', topic: 'Backend', url: 'https://www.youtube.com/watch?v=YS4e4q9oBaU', thumb: 'https://img.youtube.com/vi/YS4e4q9oBaU/mqdefault.jpg' },
  { title: 'Rust Programming', channel: 'freeCodeCamp', duration: '14h', topic: 'Systems', url: 'https://www.youtube.com/watch?v=zF34dRivLOw', thumb: 'https://img.youtube.com/vi/zF34dRivLOw/mqdefault.jpg' },
  { title: 'C Programming Full Course', channel: 'freeCodeCamp', duration: '4h', topic: 'Systems', url: 'https://www.youtube.com/watch?v=KJgsSFOSQv0', thumb: 'https://img.youtube.com/vi/KJgsSFOSQv0/mqdefault.jpg' },
  { title: 'Assembly Language Basics', channel: 'freeCodeCamp', duration: '3h', topic: 'Systems', url: 'https://www.youtube.com/watch?v=ViNnfoE56V8', thumb: 'https://img.youtube.com/vi/ViNnfoE56V8/mqdefault.jpg' },
  { title: 'Compiler Design', channel: 'Gate Smashers', duration: '8h', topic: 'CS Fundamentals', url: 'https://www.youtube.com/watch?v=9p0x0d6N4s8', thumb: 'https://img.youtube.com/vi/9p0x0d6N4s8/mqdefault.jpg' },
  { title: 'Software Engineering Course', channel: 'freeCodeCamp', duration: '5h', topic: 'CS Fundamentals', url: 'https://www.youtube.com/watch?v=QK9l2HnZ4bA', thumb: 'https://img.youtube.com/vi/QK9l2HnZ4bA/mqdefault.jpg' },
  { title: 'Design Patterns in Java', channel: 'Derek Banas', duration: '2h', topic: 'CS Fundamentals', url: 'https://www.youtube.com/watch?v=v9ejT8FO-7I', thumb: 'https://img.youtube.com/vi/v9ejT8FO-7I/mqdefault.jpg' },
  { title: 'Microservices Architecture', channel: 'freeCodeCamp', duration: '3h', topic: 'System Design', url: 'https://www.youtube.com/watch?v=rv4LlmLmVWk', thumb: 'https://img.youtube.com/vi/rv4LlmLmVWk/mqdefault.jpg' },
  { title: 'System Design Interview', channel: 'Gaurav Sen', duration: '2h', topic: 'System Design', url: 'https://www.youtube.com/watch?v=UzLMhqg3_Wc', thumb: 'https://img.youtube.com/vi/UzLMhqg3_Wc/mqdefault.jpg' },
  { title: 'Load Balancing Explained', channel: 'ByteByteGo', duration: '0.5h', topic: 'System Design', url: 'https://www.youtube.com/watch?v=K0Ta65OqQkY', thumb: 'https://img.youtube.com/vi/K0Ta65OqQkY/mqdefault.jpg' },
  { title: 'Caching Systems', channel: 'ByteByteGo', duration: '0.5h', topic: 'System Design', url: 'https://www.youtube.com/watch?v=U3RkDLtS7uY', thumb: 'https://img.youtube.com/vi/U3RkDLtS7uY/mqdefault.jpg' },
  { title: 'Message Queues (Kafka)', channel: 'ByteByteGo', duration: '0.5h', topic: 'System Design', url: 'https://www.youtube.com/watch?v=Ch5VhJzaoaI', thumb: 'https://img.youtube.com/vi/Ch5VhJzaoaI/mqdefault.jpg' },
  { title: 'Apache Kafka Full Course', channel: 'freeCodeCamp', duration: '4h', topic: 'DevOps', url: 'https://www.youtube.com/watch?v=GjU8Y4y2hZM', thumb: 'https://img.youtube.com/vi/GjU8Y4y2hZM/mqdefault.jpg' },
  { title: 'ElasticSearch Tutorial', channel: 'freeCodeCamp', duration: '2h', topic: 'Database', url: 'https://www.youtube.com/watch?v=E3MZx0g9z5Y', thumb: 'https://img.youtube.com/vi/E3MZx0g9z5Y/mqdefault.jpg' },
  { title: 'CI/CD Pipeline', channel: 'TechWorld with Nana', duration: '2h', topic: 'DevOps', url: 'https://www.youtube.com/watch?v=1tRLveSyNz8', thumb: 'https://img.youtube.com/vi/1tRLveSyNz8/mqdefault.jpg' },
  { title: 'Jenkins Tutorial', channel: 'freeCodeCamp', duration: '3h', topic: 'DevOps', url: 'https://www.youtube.com/watch?v=FX322RVNGj4', thumb: 'https://img.youtube.com/vi/FX322RVNGj4/mqdefault.jpg' },
  { title: 'GitHub Actions', channel: 'TechWorld with Nana', duration: '2h', topic: 'DevOps', url: 'https://www.youtube.com/watch?v=R8_veQiYBjI', thumb: 'https://img.youtube.com/vi/R8_veQiYBjI/mqdefault.jpg' },
  { title: 'Terraform Tutorial', channel: 'freeCodeCamp', duration: '2h', topic: 'DevOps', url: 'https://www.youtube.com/watch?v=SLB_c_ayRMo', thumb: 'https://img.youtube.com/vi/SLB_c_ayRMo/mqdefault.jpg' },
  { title: 'Ansible Tutorial', channel: 'TechWorld with Nana', duration: '2h', topic: 'DevOps', url: 'https://www.youtube.com/watch?v=wgQ3rHftxE0', thumb: 'https://img.youtube.com/vi/wgQ3rHftxE0/mqdefault.jpg' },
  { title: 'Cloud Computing Basics', channel: 'freeCodeCamp', duration: '2h', topic: 'Cloud', url: 'https://www.youtube.com/watch?v=2LaAJq1lB1Q', thumb: 'https://img.youtube.com/vi/2LaAJq1lB1Q/mqdefault.jpg' },
  { title: 'AWS Projects', channel: 'freeCodeCamp', duration: '5h', topic: 'Cloud', url: 'https://www.youtube.com/watch?v=Ia-UEYYR44s', thumb: 'https://img.youtube.com/vi/Ia-UEYYR44s/mqdefault.jpg' },
  { title: 'Azure DevOps', channel: 'freeCodeCamp', duration: '3h', topic: 'Cloud', url: 'https://www.youtube.com/watch?v=0yWAtQ6wYNM', thumb: 'https://img.youtube.com/vi/0yWAtQ6wYNM/mqdefault.jpg' },
  { title: 'Google Cloud Platform', channel: 'freeCodeCamp', duration: '6h', topic: 'Cloud', url: 'https://www.youtube.com/watch?v=EN4fEbcFZ_E', thumb: 'https://img.youtube.com/vi/EN4fEbcFZ_E/mqdefault.jpg' },
  { title: 'Computer Architecture', channel: 'freeCodeCamp', duration: '4h', topic: 'CS Fundamentals', url: 'https://www.youtube.com/watch?v=Z5JC9Ve1sfI', thumb: 'https://img.youtube.com/vi/Z5JC9Ve1sfI/mqdefault.jpg' },
  { title: 'Digital Logic Design', channel: 'Neso Academy', duration: '8h', topic: 'CS Fundamentals', url: 'https://www.youtube.com/watch?v=6p7H7Y1k9Xk', thumb: 'https://img.youtube.com/vi/6p7H7Y1k9Xk/mqdefault.jpg' },
  { title: 'Embedded Systems', channel: 'freeCodeCamp', duration: '5h', topic: 'Systems', url: 'https://www.youtube.com/watch?v=0L6kz0h9pV0', thumb: 'https://img.youtube.com/vi/0L6kz0h9pV0/mqdefault.jpg' },
  { title: 'IoT Full Course', channel: 'freeCodeCamp', duration: '4h', topic: 'Systems', url: 'https://www.youtube.com/watch?v=7xH7X4pXb9A', thumb: 'https://img.youtube.com/vi/7xH7X4pXb9A/mqdefault.jpg' },
  { title: 'AI Full Course', channel: 'freeCodeCamp', duration: '10h', topic: 'AI/ML', url: 'https://www.youtube.com/watch?v=JMUxmLyrhSk', thumb: 'https://img.youtube.com/vi/JMUxmLyrhSk/mqdefault.jpg' },
  { title: 'NLP Full Course', channel: 'freeCodeCamp', duration: '5h', topic: 'AI/ML', url: 'https://www.youtube.com/watch?v=CMrHM8a3hqw', thumb: 'https://img.youtube.com/vi/CMrHM8a3hqw/mqdefault.jpg' },
  { title: 'Computer Vision', channel: 'freeCodeCamp', duration: '4h', topic: 'AI/ML', url: 'https://www.youtube.com/watch?v=01sAkU_NvOY', thumb: 'https://img.youtube.com/vi/01sAkU_NvOY/mqdefault.jpg' },
  { title: 'Reinforcement Learning', channel: 'freeCodeCamp', duration: '3h', topic: 'AI/ML', url: 'https://www.youtube.com/watch?v=2pWv7GOvuf0', thumb: 'https://img.youtube.com/vi/2pWv7GOvuf0/mqdefault.jpg' },
  { title: 'Statistics for Data Science', channel: 'freeCodeCamp', duration: '8h', topic: 'Data Science', url: 'https://www.youtube.com/watch?v=xxpc-HPKN28', thumb: 'https://img.youtube.com/vi/xxpc-HPKN28/mqdefault.jpg' },
  { title: 'Linear Algebra', channel: 'freeCodeCamp', duration: '20h', topic: 'CS Fundamentals', url: 'https://www.youtube.com/watch?v=kjBOesZCoqc', thumb: 'https://img.youtube.com/vi/kjBOesZCoqc/mqdefault.jpg' },
  { title: 'Discrete Mathematics', channel: 'freeCodeCamp', duration: '10h', topic: 'CS Fundamentals', url: 'https://www.youtube.com/watch?v=xlUFkMKSB3Y', thumb: 'https://img.youtube.com/vi/xlUFkMKSB3Y/mqdefault.jpg' },
];

const GOOGLE_RESOURCES = [
  { title: 'React Documentation', desc: 'Official React docs â€” components, hooks, state management', url: 'https://react.dev', category: 'Frontend' },
  { title: 'MDN Web Docs', desc: 'Complete reference for HTML, CSS, JavaScript APIs', url: 'https://developer.mozilla.org', category: 'Web Dev' },
  { title: 'roadmap.sh', desc: 'Developer roadmaps for frontend, backend, DevOps, and more', url: 'https://roadmap.sh', category: 'Career' },
  { title: 'LeetCode', desc: 'Practice DSA problems for coding interviews', url: 'https://leetcode.com', category: 'DSA' },
  { title: 'GeeksforGeeks', desc: 'CS fundamentals, algorithms, system design articles', url: 'https://www.geeksforgeeks.org', category: 'CS Fundamentals' },
  { title: 'Stack Overflow', desc: 'Q&A for programming problems and debugging', url: 'https://stackoverflow.com', category: 'Community' },
  { title: 'GitHub', desc: 'Host code, contribute to open source, build portfolio', url: 'https://github.com', category: 'Tools' },
  { title: 'Dev.to', desc: 'Engineering articles, tutorials, and community posts', url: 'https://dev.to', category: 'Blog' },
  { title: 'Supabase Docs', desc: 'Open source Firebase alternative â€” PostgreSQL + Auth + Storage', url: 'https://supabase.com/docs', category: 'Backend' },
  { title: 'Vercel Docs', desc: 'Deploy Next.js apps, edge functions, and serverless APIs', url: 'https://vercel.com/docs', category: 'DevOps' },
  { title: 'System Design Primer', desc: 'GitHub repo â€” learn how to design large-scale systems', url: 'https://github.com/donnemartin/system-design-primer', category: 'System Design' },
  { title: 'NeetCode', desc: 'Structured DSA roadmap and video explanations for interviews', url: 'https://neetcode.io', category: 'DSA' },
];

const YT_TOPICS = ['All', 'AI/ML', 'Backend', 'Blockchain', 'CS Fundamentals', 'Cloud', 'DSA', 'Data Science', 'Database', 'Design', 'DevOps', 'Frontend', 'Full Stack', 'Mobile', 'Python', 'Security', 'System Design', 'Systems', 'Tools', 'Web Dev'];
const G_CATEGORIES = ['All', 'Frontend', 'Web Dev', 'Backend', 'DSA', 'Career', 'CS Fundamentals', 'System Design', 'Tools', 'Community', 'Blog', 'DevOps'];

export default function AIAssistantPage() {
  const [activeTab, setActiveTab] = useState<Tab>('chat');
  const [ytFilter, setYtFilter] = useState('All');
  const [ytSearch, setYtSearch] = useState('');
  const [gFilter, setGFilter] = useState('All');
  const [gSearch, setGSearch] = useState('');
  const [messages, setMessages] = useState<Message[]>([{
    id: '1', role: 'assistant',
    content: "Hey! I'm your AI-powered career assistant. I can help you with course recommendations, technical questions, interview prep, and career guidance. What would you like to learn today?",
    timestamp: new Date(),
  }]);
  const [inputValue, setInputValue] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => { messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages]);

  const resetChat = () => setMessages([{ id: '1', role: 'assistant', content: "Hey! I'm your AI career assistant. What would you like to learn today?", timestamp: new Date() }]);

  const filteredYt = YT_RESOURCES.filter(r =>
    (ytFilter === 'All' || r.topic === ytFilter) &&
    (!ytSearch || r.title.toLowerCase().includes(ytSearch.toLowerCase()) || r.channel.toLowerCase().includes(ytSearch.toLowerCase()))
  );

  const filteredG = GOOGLE_RESOURCES.filter(r =>
    (gFilter === 'All' || r.category === gFilter) &&
    (!gSearch || r.title.toLowerCase().includes(gSearch.toLowerCase()) || r.desc.toLowerCase().includes(gSearch.toLowerCase()))
  );

  const handleSendMessage = async (e?: React.FormEvent, prompt?: string) => {
    if (e) e.preventDefault();
    const messageContent = prompt || inputValue;
    if (!messageContent.trim()) return;
    const userMessage: Message = { id: Date.now().toString(), role: 'user', content: messageContent, timestamp: new Date() };
    setMessages(prev => [...prev, userMessage]);
    setInputValue('');
    setIsLoading(true);
    try {
      const response = await fetch('/api/chat', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ messages: [...messages, userMessage].map(m => ({ role: m.role, content: m.content })) }),
      });
      if (!response.ok) throw new Error('Failed');
      const reader = response.body?.getReader();
      const decoder = new TextDecoder();
      let assistantContent = '';
      const assistantMessage: Message = { id: (Date.now() + 1).toString(), role: 'assistant', content: '', timestamp: new Date() };
      setMessages(prev => [...prev, assistantMessage]);
      while (true) {
        const { done, value } = await reader!.read();
        if (done) break;
        assistantContent += decoder.decode(value, { stream: true });
        setMessages(prev => { const last = prev[prev.length - 1]; return last.role === 'assistant' ? [...prev.slice(0, -1), { ...last, content: assistantContent }] : prev; });
      }
    } catch {
      setMessages(prev => [...prev, { id: Date.now().toString(), role: 'assistant', content: 'Sorry, I encountered an error. Please try again.', timestamp: new Date() }]);
    } finally { setIsLoading(false); }
  };

  return (
    <div className="flex h-screen bg-background transition-colors duration-300">
      {/* Sidebar */}
      <div className="hidden md:flex w-64 border-r border-border bg-card flex-col">
        <div className="p-6 border-b border-border">
          <button onClick={() => { resetChat(); setActiveTab('chat'); }} className="button-primary w-full flex items-center justify-center gap-2">
            <Plus className="w-5 h-5" /> New Chat
          </button>
        </div>
        <div className="flex-1 p-4 space-y-1">
          {([
            ['chat', 'chat', 'AI Chat'],
            ['youtube', 'youtube', 'YouTube Resources'],
            ['google', 'google', 'Web Resources'],
          ] as [Tab, string, string][]).map(([key, iconKey, label]) => (
            <button key={key} onClick={() => setActiveTab(key)}
              className={`w-full text-left px-4 py-3 rounded-lg font-medium transition-colors text-sm flex items-center gap-2 ${activeTab === key ? 'bg-primary/10 text-primary' : 'text-muted-foreground hover:bg-muted'}`}>
              {iconKey === 'chat' ? <Bot className="w-4 h-4" /> : iconKey === 'youtube' ? <Youtube className="w-4 h-4" /> : <Search className="w-4 h-4" />}{label}
            </button>
          ))}
        </div>
        <div className="p-4 border-t border-border">
          <button onClick={resetChat} className="w-full flex items-center gap-2 px-4 py-2 text-muted-foreground hover:text-foreground hover:bg-muted rounded-lg transition-colors text-sm">
            <Trash2 className="w-4 h-4" /> Clear Chat
          </button>
        </div>
      </div>

      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Header */}
        <div className="border-b border-border bg-card p-4 flex items-center gap-3">
          <div className="w-9 h-9 rounded-lg bg-gradient-to-r from-primary to-secondary flex items-center justify-center text-white">
            {activeTab === 'youtube' ? <Youtube className="w-5 h-5" /> : activeTab === 'google' ? <Search className="w-5 h-5" /> : <Sparkles className="w-5 h-5" />}
          </div>
          <div>
            <h1 className="text-lg font-bold text-foreground">
              {activeTab === 'youtube' ? 'YouTube Engineering Resources' : activeTab === 'google' ? 'Web Engineering Resources' : 'AI Career Assistant'}
            </h1>
            <p className="text-xs text-muted-foreground">
              {activeTab === 'youtube' ? 'Curated YouTube courses â€” click to watch' : activeTab === 'google' ? 'Top engineering websites and docs' : 'Powered by Groq AI'}
            </p>
          </div>
          <div className="ml-auto flex gap-1 md:hidden">
            {(['chat', 'youtube', 'google'] as Tab[]).map(t => (
              <button key={t} onClick={() => setActiveTab(t)}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${activeTab === t ? 'bg-primary text-white' : 'bg-muted text-muted-foreground'}`}>
                {t === 'chat' ? <Bot className="w-4 h-4" /> : t === 'youtube' ? <Youtube className="w-4 h-4" /> : <Search className="w-4 h-4" />}
              </button>
            ))}
          </div>
        </div>

        {/* YouTube Resources */}
        {activeTab === 'youtube' && (
          <div className="flex-1 overflow-y-auto p-6 space-y-4">
            <div className="flex flex-col sm:flex-row gap-3">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <input value={ytSearch} onChange={e => setYtSearch(e.target.value)} placeholder="Search videos..."
                  className="w-full pl-9 pr-4 py-2.5 bg-input border border-border rounded-xl text-foreground placeholder:text-muted-foreground text-sm focus:outline-none focus:border-primary/50" />
              </div>
              <a href="https://www.youtube.com/results?search_query=software+engineering+tutorial" target="_blank" rel="noopener noreferrer"
                className="flex items-center gap-2 px-4 py-2.5 bg-red-500/20 text-red-400 border border-red-500/30 rounded-xl text-sm font-semibold hover:bg-red-500/30 transition-colors shrink-0">
                <Youtube className="w-4 h-4" /> Search on YouTube
              </a>
            </div>
            <div className="flex gap-2 flex-wrap">
              {YT_TOPICS.map(t => (
                <button key={t} onClick={() => setYtFilter(t)}
                  className={`px-3 py-1.5 rounded-full text-xs font-semibold transition-all ${ytFilter === t ? 'bg-red-500 text-white' : 'bg-white/5 text-slate-400 hover:bg-white/10 hover:text-white'}`}>{t}</button>
              ))}
            </div>
            <p className="text-xs text-slate-500 uppercase tracking-widest">
              Showing <span className="text-white font-semibold">{filteredYt.length}</span> videos
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {filteredYt.map(r => (
                <a key={`yt-${r.url}-${r.title}`} href={r.url} target="_blank" rel="noopener noreferrer"
                  className="group bg-white/5 border border-white/10 hover:border-red-500/30 rounded-2xl overflow-hidden transition-all hover:bg-white/[0.07]">
                  <div className="relative">
                    <img src={r.thumb} alt={r.title} className="w-full h-36 object-cover" />
                    <div className="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                      <div className="w-12 h-12 rounded-full bg-red-500 flex items-center justify-center">
                        <Play className="w-5 h-5 text-white fill-white" />
                      </div>
                    </div>
                    <span className="absolute bottom-2 right-2 bg-black/80 text-white text-xs px-2 py-0.5 rounded font-medium">{r.duration}</span>
                  </div>
                  <div className="p-4">
                    <span className="text-xs text-red-400 font-semibold">{r.topic}</span>
                    <h3 className="text-white font-semibold text-sm mt-1 line-clamp-2 leading-snug">{r.title}</h3>
                    <p className="text-slate-500 text-xs mt-1">{r.channel}</p>
                  </div>
                </a>
              ))}
            </div>
          </div>
        )}

        {/* Web Resources */}
        {activeTab === 'google' && (
          <div className="flex-1 overflow-y-auto p-6 space-y-4">
            <div className="flex flex-col sm:flex-row gap-3">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <input value={gSearch} onChange={e => setGSearch(e.target.value)} placeholder="Search resources..."
                  className="w-full pl-9 pr-4 py-2.5 bg-input border border-border rounded-xl text-foreground placeholder:text-muted-foreground text-sm focus:outline-none focus:border-primary/50" />
              </div>
              <a href="https://www.google.com/search?q=software+engineering+resources" target="_blank" rel="noopener noreferrer"
                className="flex items-center gap-2 px-4 py-2.5 bg-blue-500/20 text-blue-400 border border-blue-500/30 rounded-xl text-sm font-semibold hover:bg-blue-500/30 transition-colors shrink-0">
                <Search className="w-4 h-4" /> Search on Google
              </a>
            </div>
            <div className="flex gap-2 flex-wrap">
              {G_CATEGORIES.map(c => (
                <button key={c} onClick={() => setGFilter(c)}
                  className={`px-3 py-1.5 rounded-full text-xs font-semibold transition-all ${gFilter === c ? 'bg-blue-500 text-white' : 'bg-white/5 text-slate-400 hover:bg-white/10 hover:text-white'}`}>{c}</button>
              ))}
            </div>
            <p className="text-xs text-slate-500 uppercase tracking-widest">
              Showing <span className="text-white font-semibold">{filteredG.length}</span> resources
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {filteredG.map(r => (
                <a key={`yt-${r.url}-${r.title}`} href={r.url} target="_blank" rel="noopener noreferrer"
                  className="group flex flex-col gap-3 p-5 bg-white/5 border border-white/10 hover:border-blue-500/30 rounded-2xl transition-all hover:bg-white/[0.07]">
                  <div className="flex items-start justify-between gap-2">
                    <span className="px-2 py-0.5 bg-blue-500/20 text-blue-300 rounded-full text-xs font-semibold">{r.category}</span>
                    <ExternalLink className="w-4 h-4 text-slate-500 group-hover:text-blue-400 transition-colors shrink-0" />
                  </div>
                  <div>
                    <h3 className="text-white font-bold text-sm">{r.title}</h3>
                    <p className="text-slate-400 text-xs mt-1 leading-relaxed">{r.desc}</p>
                  </div>
                  <span className="text-blue-400 text-xs font-semibold mt-auto">{r.url.replace('https://', '')} â†’</span>
                </a>
              ))}
            </div>
          </div>
        )}

        {/* Chat Tab */}
        {activeTab === 'chat' && (
          <>
            <div className="flex-1 overflow-y-auto p-6">
              <div className="max-w-4xl mx-auto space-y-6">
                {messages.map(message => (
                  <div key={message.id} className={`flex gap-4 ${message.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                    {message.role === 'assistant' && (
                      <div className="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center flex-shrink-0">
                        <Bot className="w-5 h-5 text-primary" />
                      </div>
                    )}
                    <div className={`max-w-md lg:max-w-2xl rounded-2xl px-5 py-4 ${message.role === 'user' ? 'bg-primary text-primary-foreground shadow-lg shadow-primary/20' : 'card-elevated glass-lg shadow-xl'}`}>
                      <div className={`prose prose-sm dark:prose-invert max-w-none ${message.role === 'user' ? 'text-white' : 'text-foreground'}`}>
                        <ReactMarkdown remarkPlugins={[remarkGfm]}>{message.content}</ReactMarkdown>
                      </div>
                      <p className={`text-[10px] mt-3 opacity-40 font-medium ${message.role === 'user' ? 'text-right' : 'text-left'}`}>
                        {message.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </p>
                    </div>
                    {message.role === 'user' && (
                      <div className="w-8 h-8 rounded-full bg-secondary/20 flex items-center justify-center flex-shrink-0">
                        <UserIcon className="w-5 h-5 text-secondary" />
                      </div>
                    )}
                  </div>
                ))}
                {isLoading && (
                  <div className="flex justify-start">
                    <div className="glass-lg p-4 rounded-lg">
                      <div className="flex gap-2">
                        {[0, 0.1, 0.2].map((d, i) => <div key={i} className="w-2 h-2 rounded-full bg-slate-400 animate-bounce" style={{ animationDelay: `${d}s` }} />)}
                      </div>
                    </div>
                  </div>
                )}
                <div ref={messagesEndRef} />
              </div>
            </div>
            <div className="border-t border-border bg-card p-6">
              <div className="max-w-4xl mx-auto space-y-4">
                {messages.length === 1 && (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    {suggestedPrompts.map((prompt, i) => (
                      <button key={i} onClick={() => handleSendMessage(undefined, prompt)}
                        className="flex items-start gap-3 p-3 card-elevated rounded-lg text-left group transition-all hover:border-primary/30">
                        <Code className="w-5 h-5 text-primary flex-shrink-0 mt-0.5" />
                        <span className="text-sm text-muted-foreground group-hover:text-foreground">{prompt}</span>
                      </button>
                    ))}
                  </div>
                )}
                <form onSubmit={handleSendMessage} className="flex gap-3">
                  <input type="text" value={inputValue} onChange={e => setInputValue(e.target.value)}
                    placeholder="Ask me anything about your engineering career..."
                    className="flex-1 px-6 py-3 bg-input border border-border rounded-lg outline-none text-foreground placeholder:text-muted-foreground dark:bg-white/10 dark:border-white/20 transition-colors"
                    disabled={isLoading} />
                  <button type="submit" disabled={isLoading || !inputValue.trim()} className="button-primary disabled:opacity-50 flex items-center gap-2">
                    <Send className="w-5 h-5" />
                  </button>
                </form>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

